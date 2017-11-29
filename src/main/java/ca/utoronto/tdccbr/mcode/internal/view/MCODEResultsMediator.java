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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JWindow;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.util.layout.SpringEmbeddedLayouter;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEResultsPanel.ExploreContentPanel;

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
			
			// Create Results panel
			MCODEResultsPanel resultsPanel = new MCODEResultsPanel(clusters, mcodeUtil, network, netView, resultId,
					discardResultAction, registrar);
			
			// Create all the images here for the clusters, since it can be a time consuming operation
			new Thread(() -> {
				final int cores = Runtime.getRuntime().availableProcessors();
				final ExecutorService exec = Executors.newFixedThreadPool(cores);
				final List<Callable<MCODECluster>> tasks = new ArrayList<>();
				int rank = 0;
				
				for (MCODECluster c : clusters) {
					c.setRank(++rank);
					
					if (c.isTooLargeToVisualize())
						continue;
					
					final Callable<MCODECluster> callable = () -> {
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
			
			// Add required listeners to results components
			List<ClusterPanel> clusterPanels = resultsPanel.getClusterBrowserPnl().getAllItems();
			
			for (ClusterPanel p : clusterPanels)
				p.getSizeSlider().addChangeListener(new SizeAction(resultsPanel, p));
			
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
	
	/**
	 * Handles the dynamic cluster size manipulation via the JSlider
	 */
	private class SizeAction implements ChangeListener {

		private final ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(3);
		private ScheduledFuture<?> futureLoader;
		
		private final MCODEResultsPanel resultsPanel;
		private final ClusterPanel clusterPanel;
		
		/**
		 * @param resultsPanel 
		 * @param p The selected cluster row
		 * @param nodeAttributesComboBox Reference to the attribute enumeration picker
		 */
		SizeAction(MCODEResultsPanel resultsPanel, ClusterPanel clusterPanel) {
			this.resultsPanel = resultsPanel;
			this.clusterPanel = clusterPanel;
		}

		@Override
		public void stateChanged(ChangeEvent evt) {
			JSlider source = (JSlider) evt.getSource();

			if (source.getValueIsAdjusting())
				return;
			
			// This method as been written so that the slider is responsive to the user's input at all times, despite
			// the computationally challenging drawing, layout out, and selection of large clusters. As such, we only
			// perform real work if the slider produces a different cluster, and furthermore we only perform the quick
			// processes here, while the heavy weights are handled by the drawer's thread.
			final double nodeScoreCutoff = (((double) source.getValue()) / 1000);
			final int index = clusterPanel.getIndex();
			// Store current cluster content for comparison
			final MCODECluster oldCluster = clusterPanel.getCluster();
			
			if (futureLoader != null && !futureLoader.isDone()) {
				futureLoader.cancel(true);
				
				if (!oldCluster.equals(resultsPanel.getCluster(index)))
					oldCluster.dispose();
			}
			
			final int resultId = resultsPanel.getResultId();
			final MCODEResult res = resultsMgr.getResult(resultId);
			final CyNetwork network = res.getNetwork();
			final MCODEAlgorithm alg = mcodeUtil.getNetworkAlgorithm(network.getSUID());
			
			final Runnable command = (() -> {
	            	final List<Long> oldNodes = oldCluster.getNodes();
	            	// Find the new cluster given the node score cutoff
				final MCODECluster newCluster = alg.exploreCluster(oldCluster, nodeScoreCutoff, network, resultId);
				// We only want to do the following work if the newly found cluster is actually different
				// So we get the new cluster content
				List<Long> newNodes = newCluster.getNodes();

				// And compare the old and new
				if (!newNodes.equals(oldNodes)) {
					// We want to set the loading icon
					oldCluster.setImage(null);
					
					// If the cluster has changed, then we conduct all non-rate-limiting steps...
					// Update the cluster array
					resultsPanel.replaceCluster(index, newCluster);
					// Update the cluster details
					clusterPanel.setCluster(newCluster);
					
					// Update the node attributes table
					ExploreContentPanel explorePanel = resultsPanel.getExploreContentPanel(index);
					
					if (explorePanel != null)
						explorePanel.updateEnumerationsTable(index);
					
					// Draw Graph and select the cluster in the view in a separate thread so that it can be
					// interrupted by the slider movement
					if (!newCluster.isDisposed()) {
						// There is a small difference between expanding and retracting the cluster size.
						// When expanding, new nodes need random position and thus must go through the layout.
						// When retracting, we simply use the layout that was generated and stored.
						// This speeds up the drawing process greatly.
						boolean layoutNecessary = newNodes.size() > oldNodes.size();
						oldCluster.dispose();
						
						// Graph drawing will only occur if the cluster is not too large,
						// otherwise a place holder will be drawn
						if (!newCluster.isTooLargeToVisualize())
							createClusterImage(newCluster, layoutNecessary);
					}
				}
	        });
	        
	        futureLoader = scheduler.schedule(command, 100, TimeUnit.MILLISECONDS);
		}
	}
}
