package ca.utoronto.tdccbr.mcode.internal.util;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.utoronto.tdccbr.mcode.internal.CyActivator;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEGraph;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameterManager;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;

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

// TODO refactor: remove circular dependencies
/**
 * Utilities for MCODE
 */
public class MCODEUtil {
	
	// 6-class RdYlBu
	public static final Color CLUSTER_NODE_COLOR = new Color(178, 24, 43);
	public static final Color CLUSTER_EDGE_COLOR = new Color(103, 169, 207);
	public static final Color CLUSTER_ARROW_COLOR = new Color(33, 102, 172);

	private final RenderingEngineFactory<CyNetwork> renderingEngineFactory;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyRootNetworkManager rootNetworkMgr;
	private final CyApplicationManager applicationMgr;
	private final CyNetworkViewManager networkViewMgr;
	private final CyNetworkManager networkMgr;
	private final VisualStyleFactory visualStyleFactory;
	private final VisualMappingManager visualMappingMgr;
	private final CySwingApplication swingApplication;
	private final VisualMappingFunctionFactory discreteMappingFactory;
	private final VisualMappingFunctionFactory continuousMappingFactory;
	private final FileUtil fileUtil;
	private final Properties props;

	private Image placeHolderImage;
	private VisualStyle clusterStyle;
	private VisualStyle appStyle;
	private MCODEParameterManager paramMgr;
	// Keeps track of networks (id is key) and their respective algorithms
	private Map<Long, MCODEAlgorithm> networkAlgorithms;
	
	private Set<CySubNetwork> createdSubNetworks;
	
	private final Object lock = new Object();
	
	private static final Logger logger = LoggerFactory.getLogger(MCODEUtil.class);
	
	public MCODEUtil(
			RenderingEngineFactory<CyNetwork> renderingEngineFactory,
			CyNetworkViewFactory networkViewFactory,
			CyRootNetworkManager rootNetworkMgr,
			CyApplicationManager applicationMgr,
			CyNetworkManager networkMgr,
			CyNetworkViewManager networkViewMgr,
			VisualStyleFactory visualStyleFactory,
			VisualMappingManager visualMappingMgr,
			CySwingApplication swingApplication,
			VisualMappingFunctionFactory discreteMappingFactory,
			VisualMappingFunctionFactory continuousMappingFactory,
			FileUtil fileUtil
	) {
		this.renderingEngineFactory = renderingEngineFactory;
		this.networkViewFactory = networkViewFactory;
		this.rootNetworkMgr = rootNetworkMgr;
		this.applicationMgr = applicationMgr;
		this.networkMgr = networkMgr;
		this.networkViewMgr = networkViewMgr;
		this.visualStyleFactory = visualStyleFactory;
		this.visualMappingMgr = visualMappingMgr;
		this.swingApplication = swingApplication;
		this.discreteMappingFactory = discreteMappingFactory;
		this.continuousMappingFactory = continuousMappingFactory;
		this.fileUtil = fileUtil;
		this.props = loadProperties("/mcode.properties");
		
		reset();
	}

	public String abbreviate(String s, int maxLength) {
		s = String.valueOf(s); // null check
		
		if (s.length() > maxLength)
			s = s.substring(0, maxLength) + "...";
		
		return s;
	}
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public void reset() {
		synchronized (lock) {
			paramMgr = new MCODEParameterManager();
			networkAlgorithms = new HashMap<>();
			createdSubNetworks = new HashSet<>();
		}
	}
	
	public void disposeUnusedNetworks(Collection<MCODEResult> allResults) {
		Set<MCODECluster> clusters = new HashSet<>();
		
		for (MCODEResult res : allResults)
			clusters.addAll(res.getClusters());
		
		// Create an index of cluster networks
		Map<CySubNetwork, Boolean> clusterNetworks = new HashMap<>();
		
		for (MCODECluster c : clusters)
			clusterNetworks.put(c.getNetwork(), Boolean.TRUE);
		
		synchronized (lock) {
			Iterator<CySubNetwork> iterator = createdSubNetworks.iterator();
			
			while (iterator.hasNext()) {
				CySubNetwork sn = iterator.next();
				
				// Only remove the subnetwork if it is not registered and does not belong to a cluster
				if (!clusterNetworks.containsKey(sn) && !networkMgr.networkExists(sn.getSUID())) {
					try {
						iterator.remove();
						destroy(sn);
					} catch (Exception e) {
						logger.error("Error disposing: " + sn, e);
					}
				}
			}
		}
	}

	public void destroy(CySubNetwork net) {
		if (net != null) {
			final CyRootNetwork rootNet = rootNetworkMgr.getRootNetwork(net);
			
			if (rootNet.containsNetwork(net)) {
				try {
					rootNet.removeSubNetwork(net);
					net.dispose();
				} catch (Exception e) {
					logger.error("Cannot destroy network: " + net, e);
				}
			}
		}
	}
	
	public MCODEParameterManager getParameterManager() {
		return paramMgr;
	}

	public boolean containsNetworkAlgorithm(Long suid) {
		synchronized (lock) {
			return networkAlgorithms.containsKey(suid);
		}
	}

	public MCODEAlgorithm getNetworkAlgorithm(Long suid) {
		synchronized (lock) {
			return networkAlgorithms.get(suid);
		}
	}

	public void addNetworkAlgorithm(Long suid, MCODEAlgorithm alg) {
		synchronized (lock) {
			networkAlgorithms.put(suid, alg);
		}
	}
	
	public void removeNetworkAlgorithm(Long suid) {
		synchronized (lock) {
			networkAlgorithms.remove(suid);
		}
	}

	public MCODEGraph createGraph(CyNetwork net, Collection<CyNode> nodes, boolean includeLoops) {
		final Set<CyEdge> edges = new HashSet<>();

		for (CyNode n : nodes) {
			final Set<CyEdge> adjacentEdges = new HashSet<>(net.getAdjacentEdgeList(n, CyEdge.Type.ANY));

			// Get only the edges that connect nodes that belong to the subnetwork:
			for (CyEdge e : adjacentEdges) {
				if (!includeLoops && e.getSource().getSUID() == e.getTarget().getSUID())
					continue;
				
				if (nodes.contains(e.getSource()) && nodes.contains(e.getTarget()))
					edges.add(e);
			}
		}

		final MCODEGraph graph = new MCODEGraph(net, nodes, edges, this); // TODO remove circular dependency MCODEUtil/MCODEGraph

		return graph;
	}
	
	public CySubNetwork createSubNetwork(CyNetwork net, Collection<CyNode> nodes, Collection<CyEdge> edges,
			SavePolicy policy) {
		final CyRootNetwork root = rootNetworkMgr.getRootNetwork(net);
		final CySubNetwork subNet = root.addSubNetwork(nodes, edges, policy);
		
		synchronized (lock) {
			// Save it for later disposal
			createdSubNetworks.add(subNet);
		}
		
		return subNet;
	}
	
	public CySubNetwork createSubNetwork(CyNetwork net, Collection<CyNode> nodes, boolean includeLoops,
			SavePolicy policy) {
		final Set<CyEdge> edges = new HashSet<>();

		for (CyNode n : nodes) {
			Set<CyEdge> adjacentEdges = new HashSet<>(net.getAdjacentEdgeList(n, CyEdge.Type.ANY));

			// Get only the edges that connect nodes that belong to the subnetwork:
			for (CyEdge e : adjacentEdges) {
				if (!includeLoops && e.getSource().getSUID() == e.getTarget().getSUID())
					continue;
				
				if (nodes.contains(e.getSource()) && nodes.contains(e.getTarget()))
					edges.add(e);
			}
		}
		
		return createSubNetwork(net, nodes, edges, policy);
	}

	public CyNetworkView createNetworkView(final CyNetwork net, VisualStyle vs) {
		final CyNetworkView view = networkViewFactory.createNetworkView(net);

		if (vs == null)
			vs = visualMappingMgr.getDefaultVisualStyle();
		
		visualMappingMgr.setVisualStyle(vs, view);
		vs.apply(view);
		
		return view;
	}
	
	public RenderingEngine<CyNetwork> createRenderingEngine(JPanel panel, CyNetworkView view) {
		return renderingEngineFactory.createRenderingEngine(panel, view);
	}

	public void displayNetworkView(final CyNetworkView view) {
		networkMgr.addNetwork(view.getModel());
		networkViewMgr.addNetworkView(view);
		applicationMgr.setCurrentNetworkView(view);

		view.fitContent();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public VisualStyle getClusterStyle() {
		if (clusterStyle == null) {
			clusterStyle = visualStyleFactory.createVisualStyle("MCODE Cluster");

			clusterStyle.setDefaultValue(NODE_SIZE, 40.0);
			clusterStyle.setDefaultValue(NODE_WIDTH, 40.0);
			clusterStyle.setDefaultValue(NODE_HEIGHT, 40.0);
			clusterStyle.setDefaultValue(NODE_PAINT, CLUSTER_NODE_COLOR);
			clusterStyle.setDefaultValue(NODE_FILL_COLOR, CLUSTER_NODE_COLOR);
			clusterStyle.setDefaultValue(NODE_BORDER_WIDTH, 0.0);

			clusterStyle.setDefaultValue(EDGE_WIDTH, 5.0);
			clusterStyle.setDefaultValue(EDGE_PAINT, CLUSTER_EDGE_COLOR);
			clusterStyle.setDefaultValue(EDGE_UNSELECTED_PAINT, CLUSTER_EDGE_COLOR);
			clusterStyle.setDefaultValue(EDGE_STROKE_UNSELECTED_PAINT, CLUSTER_EDGE_COLOR);
			clusterStyle.setDefaultValue(EDGE_SELECTED_PAINT, CLUSTER_EDGE_COLOR);
			clusterStyle.setDefaultValue(EDGE_STROKE_SELECTED_PAINT, CLUSTER_EDGE_COLOR);
			clusterStyle.setDefaultValue(EDGE_STROKE_SELECTED_PAINT, CLUSTER_EDGE_COLOR);

			NetworkViewRenderer viewRenderer = applicationMgr.getCurrentNetworkViewRenderer();
			
			if (viewRenderer == null)
				viewRenderer = applicationMgr.getDefaultNetworkViewRenderer();
			
			VisualLexicon lexicon = viewRenderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT)
					.getVisualLexicon();
			
			{
				VisualProperty vp = lexicon.lookup(CyEdge.class, "edgeTargetArrowShape");
	
				if (vp != null) {
					Object value = vp.parseSerializableString("ARROW");
					
					if (value != null)
						clusterStyle.setDefaultValue(vp, value);
				}
			}
			{
				VisualProperty vp = lexicon.lookup(CyEdge.class, "EDGE_SOURCE_ARROW_UNSELECTED_PAINT");
				
				if (vp != null)
					clusterStyle.setDefaultValue(vp, CLUSTER_ARROW_COLOR);
			}
			{
				VisualProperty vp = lexicon.lookup(CyEdge.class, "EDGE_TARGET_ARROW_UNSELECTED_PAINT");
				
				if (vp != null)
					clusterStyle.setDefaultValue(vp, CLUSTER_ARROW_COLOR);
			}
		}

		return clusterStyle;
	}

	public VisualStyle getAppStyle(double maxScore) {
		if (appStyle == null) {
			appStyle = visualStyleFactory.createVisualStyle("MCODE");

			// Node Shape:
			DiscreteMapping<String, NodeShape> nodeShapeDm = (DiscreteMapping<String, NodeShape>) discreteMappingFactory
					.createVisualMappingFunction("MCODE_Node_Status", String.class, NODE_SHAPE);

			nodeShapeDm.putMapValue("Clustered", NodeShapeVisualProperty.ELLIPSE);
			nodeShapeDm.putMapValue("Seed", NodeShapeVisualProperty.RECTANGLE);
			nodeShapeDm.putMapValue("Unclustered", NodeShapeVisualProperty.DIAMOND);

			// Set node width/height lock
			for (VisualPropertyDependency<?> dep : appStyle.getAllVisualPropertyDependencies()) {
				if (dep.getParentVisualProperty() == BasicVisualLexicon.NODE_SIZE &&
						dep.getVisualProperties().contains(BasicVisualLexicon.NODE_WIDTH) &&
						dep.getVisualProperties().contains(BasicVisualLexicon.NODE_HEIGHT))
					dep.setDependency(true);
			}
			
			appStyle.addVisualMappingFunction(nodeShapeDm);
		}

		// Node Color:
		appStyle.setDefaultValue(NODE_FILL_COLOR, Color.WHITE);

		// Important: Always recreate this mapping function with the new score.
		appStyle.removeVisualMappingFunction(NODE_FILL_COLOR);

		// The lower the score the darker the color
		ContinuousMapping<Double, Paint> nodeColorCm = (ContinuousMapping<Double, Paint>) continuousMappingFactory
				.createVisualMappingFunction("MCODE_Score", Double.class, NODE_FILL_COLOR);

		final Color MIN_COLOR = Color.BLACK;
		final Color MAX_COLOR = Color.RED;

		// First we state that everything below or equaling 0 (min) will be white, and everything above that will
		// start from black and fade into the next boundary color
		nodeColorCm.addPoint(0.0, new BoundaryRangeValues<>(Color.WHITE, Color.WHITE, MIN_COLOR));
		// Now we state that anything anything below the max score will fade into red from the lower boundary color
		// and everything equal or greater than the max (never occurs since this is the upper boundary) will be red
		// The max value is set by MCODEVisualStyleAction based on the current result set's max score
		nodeColorCm.addPoint(maxScore, new BoundaryRangeValues<>(MAX_COLOR, MAX_COLOR, MAX_COLOR));

		appStyle.addVisualMappingFunction(nodeColorCm);

		return appStyle;
	}

	public VisualStyle getNetworkViewStyle(CyNetworkView view) {
		return view != null ? visualMappingMgr.getVisualStyle(view) : null;
	}

	public void registerVisualStyle(VisualStyle style) {
		// Add it once only!
		if (!visualMappingMgr.getAllVisualStyles().contains(style)) {
			visualMappingMgr.addVisualStyle(style);
		}
	}

	public void setSelected(final Collection<? extends CyIdentifiable> elements, CyNetwork network) {
		final Collection<CyIdentifiable> allElements = new ArrayList<>(network.getNodeList());
		allElements.addAll(network.getEdgeList());

		for (final CyIdentifiable nodeOrEdge : allElements) {
			boolean select = elements.contains(nodeOrEdge);
			network.getRow(nodeOrEdge).set(CyNetwork.SELECTED, select);
		}
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
	 * Sorts a list of MCODE generated clusters by the score.
	 *
	 * @param clusters   List of MCODE generated clusters
	 */
	public void sortClusters(List<MCODECluster> clusters) {
		Collections.sort(clusters, (c1, c2) -> {
			//sorting clusters by decreasing score
			double d1 = c1.getScore();
			double d2 = c2.getScore();
			
			if (d1 == d2)     return 0;
			else if (d1 < d2) return 1;
			return -1;
		});
	}

	/**
	 * Utility method to get the names of all the nodes in a CyNetwork
	 *
	 * @param network The input graph network to get the names from
	 * @return A concatenated set of all node names (separated by a comma)
	 */
	public String getNodeNameList(CyNetwork network) {
		StringBuffer sb = new StringBuffer();

		for (CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			String id = "" + node.getSUID();

			if (row.isSet(CyNetwork.NAME)) {
				id = row.get(CyNetwork.NAME, String.class);
			}

			sb.append(id);
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
	public boolean exportMCODEResults(MCODEAlgorithm alg, List<MCODECluster> clusters, CyNetwork network) {
		if (alg == null || clusters == null || network == null)
			return false;

		final String lineSep = System.getProperty("line.separator");
		String fileName = null;
		FileWriter fout = null;

		try {
			// Call save method in MCODE get the file name
			Collection<FileChooserFilter> filters = new ArrayList<>();
			filters.add(new FileChooserFilter("Text format", "txt"));
			File file = fileUtil.getFile(swingApplication.getJFrame(),
										 "Export Graph as Interactions",
										 FileUtil.SAVE,
										 filters);

			if (file != null) {
				fileName = file.getAbsolutePath();
				fout = new FileWriter(file);

				// Write header
				fout.write("MCODE App Results" + lineSep);
				fout.write("Date: " + DateFormat.getDateTimeInstance().format(new Date()) + lineSep + lineSep);
				fout.write("Parameters:" + lineSep + alg.getParams().toString() + lineSep);
				fout.write("Cluster	Score (Density*#Nodes)\tNodes\tEdges\tNode IDs" + lineSep);

				// Get sub-networks for all clusters, score and rank them
				// convert the ArrayList to an array of CyNetworks and sort it by cluster score
				for (int i = 0; i < clusters.size(); i++) {
					final MCODECluster c = clusters.get(i);
					final CyNetwork clusterNetwork = c.getNetwork();
					fout.write((i + 1) + "\t"); //rank
					NumberFormat nf = NumberFormat.getInstance();
					nf.setMaximumFractionDigits(3);
					fout.write(nf.format(c.getScore()) + "\t");
					// cluster size - format: (# prot, # intx)
					fout.write(clusterNetwork.getNodeCount() + "\t");
					fout.write(clusterNetwork.getEdgeCount() + "\t");
					// create a string of node names - this can be long
					fout.write(getNodeNameList(clusterNetwork) + lineSep);
				}
				return true;
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
										  e.toString(),
										  "Error Writing to \"" + fileName + "\"",
										  JOptionPane.ERROR_MESSAGE);
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return false;
	}
	
	private static Properties loadProperties(String name) {
		Properties props = new Properties();

		try {
			InputStream in = CyActivator.class.getResourceAsStream(name);

			if (in != null) {
				props.load(in);
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return props;
	}
	
	public static String getName(final CyNetwork network) {
		String name = "";
		
		try {
			name = network.getRow(network).get(CyNetwork.NAME, String.class);
		} catch (Exception e) {
		}
		
		if (name == null || name.trim().isEmpty())
			name = "? (SUID: " + network.getSUID() + ")";
		
		return name;
	}
}
