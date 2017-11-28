package ca.utoronto.tdccbr.mcode.internal.view;

import static ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil.invokeOnEDT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JPanel;
import javax.swing.JWindow;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;

import ca.utoronto.tdccbr.mcode.internal.action.MCODEDiscardResultAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.util.layout.SpringEmbeddedLayouter;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 * * User: Gary Bader
 * * Date: Jun 25, 2004
 * * Time: 7:00:13 PM
 * * Description: Utilities for MCODE
 */

public class MCODEResultsMediator {
	
	private final MCODEResultsManager resultsMgr;
	private final MCODEUtil mcodeUtil;
	private final CyServiceRegistrar registrar;
	
	public MCODEResultsMediator(MCODEResultsManager resultsMgr, MCODEUtil mcodeUtil, CyServiceRegistrar registrar) {
		this.resultsMgr = resultsMgr;
		this.mcodeUtil = mcodeUtil;
		this.registrar = registrar;
		
		resultsMgr.addPropertyChangeListener("networkResults", evt -> {
			Integer resultId = (Integer) evt.getNewValue();
			
			if (resultId != null)
				showResultsPanel(resultId);
		});
	}
	
	private void showResultsPanel(int resultId) {
		MCODEResult res = resultsMgr.getResult(resultId);
		List<MCODECluster> clusters = res != null ? res.getClusters() : null;
		
		if (clusters == null || clusters.isEmpty())
			return;
		
		CyNetwork network = res.getNetwork();
		CyNetworkView currView = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		
		MCODEDiscardResultAction discardResultAction = new MCODEDiscardResultAction("Discard Result", resultId,
				resultsMgr, mcodeUtil, registrar);

		invokeOnEDT(() -> {
			CyNetworkView netView = currView != null && currView.getModel().equals(network) ? currView : null;

			if (netView == null) {
				Collection<CyNetworkView> allViews = registrar.getService(CyNetworkViewManager.class)
						.getNetworkViews(network);

				if (allViews.isEmpty())
					netView = allViews.iterator().next();
			}
			
			MCODEResultsPanel resultsPanel = new MCODEResultsPanel(clusters, mcodeUtil, network, netView, resultId,
					discardResultAction, registrar);
			
			// Also create all the images here for the clusters, since it can be a time consuming operation
			new Thread(() -> {
				final int cores = Runtime.getRuntime().availableProcessors();
				final ExecutorService exec = Executors.newFixedThreadPool(cores);
				final List<Callable<MCODECluster>> tasks = new ArrayList<>();
				int rank = 0;
				
				for (MCODECluster c : clusters) {
					final int row = rank;
					c.setRank(++rank);
					
					final Callable<MCODECluster> callable = () -> {System.out.println(">>> " + row);
						createClusterImage(c, true);
						
						return c;
					};
					tasks.add(callable);
				}
				
				try {
					final List<Future<MCODECluster>> futureList = exec.invokeAll(tasks);
					
					for (Future<MCODECluster> future : futureList)
						future.get();
				} catch (Exception e) {
					throw new RuntimeException("Cannot create cluster image(s).", e);
				} finally {
		            exec.shutdown();
		        }
			}).start();
			
			registrar.registerService(resultsPanel, CytoPanelComponent.class, new Properties());
			
			// Focus the result panel
			CytoPanel cytoPanel = registrar.getService(CySwingApplication.class).getCytoPanel(CytoPanelName.EAST);
			int index = cytoPanel.indexOfComponent(resultsPanel);
			cytoPanel.setSelectedIndex(index);

			if (cytoPanel.getState() == CytoPanelState.HIDE)
				cytoPanel.setState(CytoPanelState.DOCK);
			
		});
	}
	
	/**
	 * Convert a network to an image and set it to the passed cluster.
	 * 
	 * @param cluster Input network to convert to an image
	 * @param layoutNecessary Determinant of cluster size growth or shrinkage, the former requires layout
	 */
	private void createClusterImage(MCODECluster cluster, boolean layoutNecessary) {
		final CyNetwork net = cluster.getNetwork();
		final VisualStyle vs = mcodeUtil.getClusterStyle();
		final CyNetworkView clusterView = mcodeUtil.createNetworkView(net, vs);

		int width = ClusterPanel.GRAPH_IMG_SIZE;
		int height = ClusterPanel.GRAPH_IMG_SIZE;
		
		clusterView.setVisualProperty(NETWORK_WIDTH, new Double(width));
		clusterView.setVisualProperty(NETWORK_HEIGHT, new Double(height));

		for (View<CyNode> nv : clusterView.getNodeViews()) {
			// Node position
			final double x;
			final double y;

			// First we check if the MCODECluster already has a node view of this node (posing the more generic condition
			// first prevents the program from throwing a null pointer exception in the second condition)
			if (cluster.getView() != null && cluster.getView().getNodeView(nv.getModel()) != null) {
				// If it does, then we take the layout position that was already generated for it
				x = cluster.getView().getNodeView(nv.getModel()).getVisualProperty(NODE_X_LOCATION);
				y = cluster.getView().getNodeView(nv.getModel()).getVisualProperty(NODE_Y_LOCATION);
			} else {
				// Otherwise, randomize node positions before layout so that they don't all layout in a line
				// (so they don't fall into a local minimum for the SpringEmbedder)
				// If the SpringEmbedder implementation changes, this code may need to be removed
				// size is small for many default drawn graphs, thus +100
				x = (clusterView.getVisualProperty(NETWORK_WIDTH) + 100) * Math.random();
				y = (clusterView.getVisualProperty(NETWORK_HEIGHT) + 100) * Math.random();
				layoutNecessary = true;
			}

			nv.setVisualProperty(NODE_X_LOCATION, x);
			nv.setVisualProperty(NODE_Y_LOCATION, y);

			// Node shape
			if (cluster.getSeedNode() == nv.getModel().getSUID())
				nv.setLockedValue(NODE_SHAPE, NodeShapeVisualProperty.RECTANGLE);
			else
				nv.setLockedValue(NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		}

		if (layoutNecessary) {
			SpringEmbeddedLayouter layouter = new SpringEmbeddedLayouter(clusterView);
			layouter.doLayout();
		}
		
		final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) image.getGraphics();
		final Dimension size = new Dimension(width, height);

		invokeOnEDT(() -> {
			try {
				JPanel panel = new JPanel();
				panel.setPreferredSize(size);
				panel.setSize(size);
				panel.setMinimumSize(size);
				panel.setMaximumSize(size);
				panel.setBackground((Color) vs.getDefaultValue(NETWORK_BACKGROUND_PAINT));

				JWindow window = new JWindow();
				window.getContentPane().add(panel, BorderLayout.CENTER);

				RenderingEngine<CyNetwork> re = mcodeUtil.createRenderingEngine(panel, clusterView);

				vs.apply(clusterView);
				clusterView.fitContent();
				clusterView.updateView();
				window.pack();
				window.repaint();

				re.createImage(width, height);
				re.printCanvas(g);
				g.dispose();
				
				if (!clusterView.getNodeViews().isEmpty())
					cluster.setView(clusterView);
				
				cluster.setImage(image);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}
}
