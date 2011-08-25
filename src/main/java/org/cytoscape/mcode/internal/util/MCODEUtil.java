package org.cytoscape.mcode.internal.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.mcode.internal.model.MCODEAlgorithm;
import org.cytoscape.mcode.internal.model.MCODECluster;
import org.cytoscape.mcode.internal.model.MCODECurrentParameters;
import org.cytoscape.mcode.internal.view.MCODELoader;
import org.cytoscape.mcode.internal.view.SpringEmbeddedLayouter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.RichVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;

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

/**
 * Utilities for MCODE
 */
public class MCODEUtil {

	private final RenderingEngineFactory<CyNetwork> renderingEngineFactory;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyRootNetworkFactory rootNetworkFactory;
	private final CyApplicationManager applicationMgr;
	private final CyNetworkViewManager networkViewMgr;
	private final CyNetworkManager networkMgr;
	private final VisualStyleFactory visualStyleFactory;
	private final VisualMappingManager visualMappingMgr;
	private final CySwingApplication swingApplication;
	private final CyEventHelper eventHelper;

	private boolean interrupted;
	private Image placeHolderImage;
	private VisualStyle style; // visual style used by the MCODE plugin
	private MCODECurrentParameters currentParameters;
	//Keeps track of networks (id is key) and their respective algorithms
	private Map<Long, MCODEAlgorithm> networkAlgorithms;

	public MCODEUtil(final RenderingEngineFactory<CyNetwork> renderingEngineFactory,
					 final CyNetworkViewFactory networkViewFactory,
					 final CyRootNetworkFactory rootNetworkFactory,
					 final CyApplicationManager applicationMgr,
					 final CyNetworkManager networkMgr,
					 final CyNetworkViewManager networkViewMgr,
					 final VisualStyleFactory visualStyleFactory,
					 final VisualMappingManager visualMappingMgr,
					 final CySwingApplication swingApplication,
					 final CyEventHelper eventHelper) {
		this.renderingEngineFactory = renderingEngineFactory;
		this.networkViewFactory = networkViewFactory;
		this.rootNetworkFactory = rootNetworkFactory;
		this.applicationMgr = applicationMgr;
		this.networkMgr = networkMgr;
		this.networkViewMgr = networkViewMgr;
		this.visualStyleFactory = visualStyleFactory;
		this.visualMappingMgr = visualMappingMgr;
		this.swingApplication = swingApplication;
		this.eventHelper = eventHelper;

		this.reset();
	}

	public void reset() {
		currentParameters = new MCODECurrentParameters();
		networkAlgorithms = new HashMap<Long, MCODEAlgorithm>();
	}

	public MCODECurrentParameters getCurrentParameters() {
		return currentParameters;
	}

	public boolean containsNetworkAlgorithm(long suid) {
		return networkAlgorithms.containsKey(suid);
	}
	
	public MCODEAlgorithm getNetworkAlgorithm(long suid) {
		return networkAlgorithms.get(suid);
	}
	
	public void addNetworkAlgorithm(long suid, MCODEAlgorithm alg) {
		networkAlgorithms.put(suid, alg);
	}

	/**
	 * Convert a network to an image.  This is used by the MCODEResultsPanel.
	 * 
	 * @param cluster Input network to convert to an image
	 * @param height  Height that the resulting image should be
	 * @param width   Width that the resulting image should be
	 * @param layouter Reference to the layout algorithm
	 * @param layoutNecessary Determinant of cluster size growth or shrinkage, the former requires layout
	 * @param loader Graphic loader displaying progress and process
	 * @return The resulting image
	 */
	public Image createClusterImage(final MCODECluster cluster,
									final int height,
									final int width,
									SpringEmbeddedLayouter layouter,
									boolean layoutNecessary,
									final MCODELoader loader) {
		// Progress reporters.
		// There are three basic tasks, the progress of each is calculated and then combined
		// using the respective weighting to get an overall progress global progress
		int weightSetupNodes = 20; // setting up the nodes and edges is deemed as 25% of the whole task
		int weightSetupEdges = 5;
		int weightLayout = 75; // layout it is 70%
		int goalTotal = weightSetupNodes + weightSetupEdges;

		if (layoutNecessary) {
			goalTotal += weightLayout;
		}

		// keeps track of progress as a percent of the totalGoal
		double progress = 0;

		final VisualStyle vs = getPluginStyle();
		final CyNetworkView clusterView = createNetworkView(cluster.getNetwork(), vs);

		clusterView.setVisualProperty(MinimalVisualLexicon.NETWORK_WIDTH, new Double(width));
		clusterView.setVisualProperty(MinimalVisualLexicon.NETWORK_HEIGHT, new Double(height));

		for (View<CyNode> nv : clusterView.getNodeViews()) {
			if (interrupted) {
				System.err.println("Interrupted: Node Setup");
				// before we short-circuit the method we reset the interruption so that the method can run without
				// problems the next time around
				if (layouter != null) layouter.resetDoLayout();
				resetLoading();

				return null;
			}

			final double x;
			final double y;

			// First we check if the MCODECluster already has a node view of this node (posing the more generic condition
			// first prevents the program from throwing a null pointer exception in the second condition)
			if (cluster.getView() != null && cluster.getView().getNodeView(nv.getModel()) != null) {
				//If it does, then we take the layout position that was already generated for it
				x = cluster.getView().getNodeView(nv.getModel())
						.getVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION);
				y = cluster.getView().getNodeView(nv.getModel())
						.getVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION);
			} else {
				// Otherwise, randomize node positions before layout so that they don't all layout in a line
				// (so they don't fall into a local minimum for the SpringEmbedder)
				// If the SpringEmbedder implementation changes, this code may need to be removed
				// size is small for many default drawn graphs, thus +100
				x = (clusterView.getVisualProperty(MinimalVisualLexicon.NETWORK_WIDTH) + 100) * Math.random();
				y = (clusterView.getVisualProperty(MinimalVisualLexicon.NETWORK_HEIGHT) + 100) * Math.random();

				if (!layoutNecessary) {
					goalTotal += weightLayout;
					progress /= (goalTotal / (goalTotal - weightLayout));
					layoutNecessary = true;
				}
			}

			nv.setVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION, x);
			nv.setVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION, y);

			if (loader != null) {
				progress += 100.0 * (1.0 / (double) clusterView.getNodeViews().size()) *
							((double) weightSetupNodes / (double) goalTotal);
				loader.setProgress((int) progress, "Setup: nodes");
			}
		}

		for (View<CyEdge> ev : clusterView.getEdgeViews()) {
			if (interrupted) {
				System.err.println("Interrupted: Edge Setup");
				if (layouter != null) layouter.resetDoLayout();
				resetLoading();

				return null;
			}

			if (loader != null) {
				progress += 100.0 * (1.0 / (double) clusterView.getEdgeViews().size()) *
							((double) weightSetupEdges / (double) goalTotal);
				loader.setProgress((int) progress, "Setup: edges");
			}
		}

		if (layoutNecessary) {
			if (layouter == null) {
				layouter = new SpringEmbeddedLayouter();
			}

			layouter.setGraphView(clusterView);

			// The doLayout method should return true if the process completes without interruption
			if (!layouter.doLayout(weightLayout, goalTotal, progress, loader)) {
				// Otherwise, if layout is not completed, set the interruption to false, and return null, not an image
				resetLoading();

				return null;
			}
		}

		final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) image.getGraphics();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					// TODO: remove this workaround when possible to get image off-screen
					//					displayNetworkView(clusterView);

					// Apply style again:
					vs.apply(clusterView);
					clusterView.fitContent();
					clusterView.updateView();

					//		RenderingEngine<CyNetwork> re = renderingEngineFactory.getInstance(panel, clusterView);
					RenderingEngine<CyNetwork> re = applicationMgr.getCurrentRenderingEngine();
					//		image = re.createImage(width, height);
					//				g.scale(1, 1);
					re.printCanvas(g);
					g.dispose();

					if (clusterView.getNodeViews().size() > 0) {
						cluster.setView(clusterView);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				} finally {
					// TODO: remove this workaround when possible to get image off-screen
					//					destroyNetworkViewAndModel(clusterView);
				}
			}
		});

		layouter.resetDoLayout();
		resetLoading();

		return image;
	}

	public CySubNetwork createSubNetwork(final CyNetwork net, Collection<CyNode> nodes) {
		final CyRootNetwork root = rootNetworkFactory.convert(net);
		final Set<CyEdge> edges = new HashSet<CyEdge>();

		for (CyNode n : nodes) {
			Set<CyEdge> adjacentEdges = new HashSet<CyEdge>(net.getAdjacentEdgeList(n, CyEdge.Type.ANY));

			// Get only the edges that connect nodes that belong to the subnetwork:
			for (CyEdge e : adjacentEdges) {
				if (nodes.contains(e.getSource()) && nodes.contains(e.getTarget())) {
					edges.add(e);
				}
			}
		}

		final CySubNetwork subNet = root.addSubNetwork(nodes, edges);

		return subNet;
	}

	public CyNetworkView createNetworkView(final CyNetwork net, VisualStyle vs) {
		final CyNetworkView view = networkViewFactory.getNetworkView(net, false);

		if (vs == null) vs = visualMappingMgr.getDefaultVisualStyle();
		visualMappingMgr.setVisualStyle(vs, view);
		vs.apply(view);

		return view;
	}

	public void displayNetworkView(final CyNetworkView view) {
		networkMgr.addNetwork(view.getModel());
		networkViewMgr.addNetworkView(view);

		view.fitContent();
		view.updateView();
	}

	public void destroyNetworkViewAndModel(CyNetworkView view) {
		if (view != null) {
			networkViewMgr.destroyNetworkView(view);
			networkMgr.destroyNetwork(view.getModel());
		}
	}

	public VisualStyle getPluginStyle() {
		if (style == null) {
			style = visualStyleFactory.getInstance("MCODE");

			style.setDefaultValue(MinimalVisualLexicon.NODE_SIZE, 40.0);
			style.setDefaultValue(MinimalVisualLexicon.NODE_WIDTH, 40.0);
			style.setDefaultValue(MinimalVisualLexicon.NODE_HEIGHT, 40.0);
			style.setDefaultValue(MinimalVisualLexicon.NODE_PAINT, Color.RED);
			style.setDefaultValue(MinimalVisualLexicon.NODE_FILL_COLOR, Color.RED);
			style.setDefaultValue(RichVisualLexicon.NODE_BORDER_PAINT, Color.BLACK);
			style.setDefaultValue(RichVisualLexicon.NODE_BORDER_WIDTH, 5.0);

			style.setDefaultValue(MinimalVisualLexicon.EDGE_PAINT, Color.BLUE);
			style.setDefaultValue(RichVisualLexicon.EDGE_UNSELECTED_PAINT, Color.BLUE);
			style.setDefaultValue(RichVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.BLUE);
			style.setDefaultValue(RichVisualLexicon.EDGE_SELECTED_PAINT, Color.BLUE);
			style.setDefaultValue(RichVisualLexicon.EDGE_STROKE_SELECTED_PAINT, Color.BLUE);

			// TODO            
			//            if (cluster.getSeedNode().intValue() == nv.getRootGraphIndex()) {
			//                nv.setShape(NodeView.RECTANGLE);
			//            } else {
			//                nv.setShape(NodeView.ELLIPSE);
			//            }
			//
			//            ev.setTargetEdgeEnd(EdgeView.BLACK_ARROW);
			//            ev.setTargetEdgeEndPaint(Color.CYAN);
			//            ev.setSourceEdgeEndPaint(Color.CYAN);

			// TODO:  change original network style?
			// // Node Shape:
			//		    private void createNodeShape(NodeAppearanceCalculator nac) {
			//	        DiscreteMapping discreteMapping = new DiscreteMapping(RECT, "MCODE_Node_Status", ObjectMapping.NODE_MAPPING);
			//	        //Node shapes are determined by three discrete classifications
			//	        discreteMapping.putMapValue("Clustered", ELLIPSE);
			//	        discreteMapping.putMapValue("Seed", RECT);
			//	        discreteMapping.putMapValue("Unclustered", DIAMOND);
			//
			//	        Calculator nodeShapeCalculator = new BasicCalculator("Seed and Cluster Status Calculator", discreteMapping, VisualPropertyType.NODE_SHAPE);
			//	        nac.setCalculator(nodeShapeCalculator);
			//
			// // NodeColor:
			//	        nac.getDefaultAppearance().set(VisualPropertyType.NODE_FILL_COLOR, Color.WHITE);
			//	        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
			//	        continuousMapping.setControllingAttributeName("MCODE_Score", null, false);
			//
			//	        Interpolator fInt = new LinearNumberToColorInterpolator();
			//	        continuousMapping.setInterpolator(fInt);
			//
			//	        //Node color is based on the score, the lower the score the darker the color
			//	        Color minColor = Color.BLACK;
			//	        Color maxColor = Color.RED;
			//
			//	        //Create two boundary conditions
			//	        //First we state that everything below or equalling 0 (min) will be white, and everything above that will
			//	        //start from black and fade into the next boundary color
			//	        BoundaryRangeValues bv0 = new BoundaryRangeValues(Color.WHITE, Color.WHITE, minColor);
			//	        //Now we state that anything anything below the max score will fade into red from the lower boundary color
			//	        //and everything equal or greater than the max (never occurs since this is the upper boundary) will be red
			//	        BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor, maxColor);
			//
			//	        //Set Data Points
			//	        double minValue = 0.0;
			//	        //the max value is set by MCODEVisualStyleAction based on the current result set's max score
			//	        continuousMapping.addPoint(minValue, bv0);
			//	        continuousMapping.addPoint(maxValue, bv2);
			//
			//	        Calculator nodeColorCalculator = new BasicCalculator("MCODE Score Color Calculator", continuousMapping, VisualPropertyType.NODE_FILL_COLOR);
			//	        nac.setCalculator(nodeColorCalculator);
			//
			//	    public void setMaxValue(double maxValue) {
			//	        this.maxValue = maxValue;
			//	    }
		}

		return style;
	}

	public VisualStyle getNetworkViewStyle(CyNetworkView view) {
		return view != null ? visualMappingMgr.getVisualStyle(view) : null;
	}

	@SuppressWarnings("unchecked")
	public void setSelected(final Collection<? extends CyTableEntry> elements, CyNetwork network, CyNetworkView view) {
		Collection<? extends CyTableEntry> allElements = new ArrayList<CyTableEntry>(network.getNodeList());
		allElements.addAll((Collection) network.getEdgeList());

		for (final CyTableEntry nodeOrEdge : allElements) {
			boolean select = elements.contains(nodeOrEdge);
			nodeOrEdge.getCyRow().set(CyNetwork.SELECTED, select);
		}

		eventHelper.flushPayloadEvents();

		if (view != null) {
			view.updateView();
//			eventHelper.flushPayloadEvents();
			swingApplication.getJFrame().repaint(); // TODO: remove this ugly hack!!!
		}
	}

	public void interruptLoading() {
		interrupted = true;
	}

	public void resetLoading() {
		interrupted = false;
	}

	/**
	 * Generates an image of a place holder saying "Too big to show".
	 *
	 * @param width width of the image
	 * @param height height of the image
	 * @return place holder
	 */
	public Image getPlaceHolderImage(int width, int height) {
		// We only want to generate a place holder image once so that memory is not eaten up
		if (placeHolderImage == null) {
			placeHolderImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

			Graphics2D g2 = (Graphics2D) placeHolderImage.getGraphics();

			int fontSize = 10;
			g2.setFont(new Font("Arial", Font.PLAIN, fontSize));
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Font f = g2.getFont();
			FontMetrics fm = g2.getFontMetrics(f);

			// Place Holder text
			String placeHolderText = "Too big to show";
			// We want to center the text vertically in the top 20 pixels
			height = 20;
			// White outline
			g2.setColor(Color.WHITE);
			g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2) - 1, (height / 2) +
																									(fontSize / 2) - 1);
			g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2) - 1, (height / 2) +
																									(fontSize / 2) + 1);
			g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2) + 1, (height / 2) +
																									(fontSize / 2) - 1);
			g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2) + 1, (height / 2) +
																									(fontSize / 2) + 1);
			//Red text
			g2.setColor(Color.RED);
			g2.drawString(placeHolderText, (width / 2) - (fm.stringWidth(placeHolderText) / 2), (height / 2) +
																								(fontSize / 2));
		}

		return placeHolderImage;
	}

	/**
	 * Converts a list of MCODE generated clusters to a list of networks that is sorted by the score of the cluster
	 *
	 * @param clusters   List of MCODE generated clusters
	 * @return A sorted array of cluster objects based on cluster score.
	 */
	public MCODECluster[] sortClusters(MCODECluster[] clusters) {
		Arrays.sort(clusters, new Comparator<MCODECluster>() {

			//sorting clusters by decreasing score
			public int compare(MCODECluster c1, MCODECluster c2) {
				double d1 = c1.getClusterScore();
				double d2 = c2.getClusterScore();
				if (d1 == d2) {
					return 0;
				} else if (d1 < d2) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		return clusters;
	}

	/**
	 * Utility method to get the names of all the nodes in a GraphPerspective
	 *
	 * @param network The input graph perspective to get the names from
	 * @return A concatenated set of all node names (separated by a comma)
	 */
	public String getNodeNameList(CyNetwork network) {
		StringBuffer sb = new StringBuffer();

		for (CyNode node : network.getNodeList()) {
			sb.append(node.getSUID());
			sb.append(", ");
		}

		if (sb.length() > 2) sb.delete(sb.length() - 2, sb.length());

		return sb.toString();
	}

	/**
	 * Save MCODE results to a file
	 *
	 * @param alg       The algorithm instance containing parameters, etc.
	 * @param clusters  The list of clusters
	 * @param network   The network source of the clusters
	 * @param fileName  The file name to write to
	 * @return True if the file was written, false otherwise
	 */
	public boolean exportMCODEResults(MCODEAlgorithm alg, MCODECluster[] clusters, CyNetwork network, String fileName) {
		if (alg == null || clusters == null || network == null || fileName == null) {
			return false;
		}

		String lineSep = System.getProperty("line.separator");

		try {
			File file = new File(fileName);
			FileWriter fout = new FileWriter(file);
			//write header
			fout.write("MCODE Plugin Results" + lineSep);
			fout.write("Date: " + DateFormat.getDateTimeInstance().format(new Date()) + lineSep + lineSep);
			fout.write("Parameters:" + lineSep + alg.getParams().toString() + lineSep);
			fout.write("Cluster	Score (Density*#Nodes)\tNodes\tEdges\tNode IDs" + lineSep);

			// get sub-networks for all clusters, score and rank them
			// convert the ArrayList to an array of GraphPerspectives and sort it by cluster score
			for (int i = 0; i < clusters.length; i++) {
				CyNetwork clusterNetwork = clusters[i].getNetwork();
				fout.write((i + 1) + "\t"); //rank
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(3);
				fout.write(nf.format(clusters[i].getClusterScore()) + "\t");
				// cluster size - format: (# prot, # intx)
				fout.write(clusterNetwork.getNodeCount() + "\t");
				fout.write(clusterNetwork.getEdgeCount() + "\t");
				// create a string of node names - this can be long
				fout.write(getNodeNameList(clusterNetwork) + lineSep);
			}

			fout.close();

			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
										  e.toString(),
										  "Error Writing to \"" + fileName + "\"",
										  JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
}
