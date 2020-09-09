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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
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
	
	public static final String STYLE_TITLE = "MCODE";
	public static final String CLUSTER_IMG_STYLE_TITLE = "MCODE (Cluster Image)";
	
	// Columns
	public static final String NAMESPACE = "MCODE";
	
	/** Use it with {@link MCODEUtil#columnName(MCODEResult, String)} */
	public static final String SCORE_ATTR = "Score";
	/** Use it with {@link MCODEUtil#columnName(MCODEResult, String)} */
	public static final String NODE_STATUS_ATTR = "Node Status";
	/** Use it with {@link MCODEUtil#columnName(MCODEResult, String)} */
	public static final String CLUSTERS_ATTR = "Clusters";
	
	// 6-class RdYlBu for Cluster Style
	public static final Color CLUSTER_NODE_COLOR = new Color(178, 24, 43);
	public static final Color CLUSTER_EDGE_COLOR = new Color(103, 169, 207);
	public static final Color CLUSTER_ARROW_COLOR = new Color(33, 102, 172);
	
	// MCODE Style
	private static final Color NODE_DEF_COLOR = Color.WHITE;
	private static final Color NODE_MIN_COLOR = Color.BLACK;
	private static final Color NODE_MAX_COLOR = Color.RED;

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
	
	private final MCODEParameterManager paramMgr = new MCODEParameterManager();
	// Keeps track of networks (id is key) and their respective algorithms
	private final Map<Long, MCODEAlgorithm> networkAlgorithms = new HashMap<>();
	private final Set<CySubNetwork> createdSubNetworks = new HashSet<>();
	
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
			paramMgr.reset();
			networkAlgorithms.clear();
			createdSubNetworks.clear();
		}
	}
	
	public void disposeUnusedNetworks(Collection<MCODEResult> allResults) {
		var clusters = new HashSet<MCODECluster>();
		
		for (var res : allResults)
			clusters.addAll(res.getClusters());
		
		// Create an index of cluster networks
		var clusterNetworks = new HashMap<CySubNetwork, Boolean>();
		
		for (var c : clusters)
			clusterNetworks.put(c.getNetwork(), Boolean.TRUE);
		
		synchronized (lock) {
			var iterator = createdSubNetworks.iterator();
			
			while (iterator.hasNext()) {
				var sn = iterator.next();
				
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
			var rootNet = rootNetworkMgr.getRootNetwork(net);
			
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
		var edges = new HashSet<CyEdge>();

		for (CyNode n : nodes) {
			var adjacentEdges = new HashSet<>(net.getAdjacentEdgeList(n, CyEdge.Type.ANY));

			// Get only the edges that connect nodes that belong to the subnetwork:
			for (var e : adjacentEdges) {
				if (!includeLoops && e.getSource().getSUID() == e.getTarget().getSUID())
					continue;
				
				if (nodes.contains(e.getSource()) && nodes.contains(e.getTarget()))
					edges.add(e);
			}
		}

		var graph = new MCODEGraph(net, nodes, edges, this); // TODO remove circular dependency MCODEUtil/MCODEGraph

		return graph;
	}
	
	public CySubNetwork createSubNetwork(CyNetwork net, Collection<CyNode> nodes, Collection<CyEdge> edges,
			SavePolicy policy) {
		var rootNet = rootNetworkMgr.getRootNetwork(net);
		var subNet = rootNet.addSubNetwork(nodes, edges, policy);
		
		synchronized (lock) {
			// Save it for later disposal
			createdSubNetworks.add(subNet);
		}
		
		return subNet;
	}
	
	/**
	 * Create a new subnetwork from the passed cluster network.
	 */
	public CySubNetwork createSubNetwork(CySubNetwork clusterNet, MCODEResult res, SavePolicy policy) {
		var nodes = clusterNet.getNodeList();
		var edges = new HashSet<CyEdge>();

		for (var n : nodes) {
			var adjacentEdges = new HashSet<>(clusterNet.getAdjacentEdgeList(n, CyEdge.Type.ANY));

			// Get only the edges that connect nodes that belong to the subnetwork:
			for (var e : adjacentEdges) {
				if (!res.getParameters().getIncludeLoops() && e.getSource().getSUID() == e.getTarget().getSUID())
					continue;
				
				if (nodes.contains(e.getSource()) && nodes.contains(e.getTarget()))
					edges.add(e);
			}
		}
		
		var newNet = createSubNetwork(clusterNet, nodes, edges, policy);
		
		// Create MCODE columns and copy values from parent network
		copyMCODEColumns(newNet, res);
		
		return newNet;
	}

	public CyNetworkView createNetworkView(CyNetwork net, VisualStyle vs) {
		var view = networkViewFactory.createNetworkView(net);

		if (vs != null) {
			visualMappingMgr.setVisualStyle(vs, view);
			vs.apply(view);
		}
		
		return view;
	}
	
	public RenderingEngine<CyNetwork> createRenderingEngine(JPanel panel, CyNetworkView view) {
		return renderingEngineFactory.createRenderingEngine(panel, view);
	}

	public void displayNetworkView(CyNetworkView view) {
		networkMgr.addNetwork(view.getModel());
		networkViewMgr.addNetworkView(view);
		applicationMgr.setCurrentNetworkView(view);

		view.fitContent();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public VisualStyle createClusterImageStyle(MCODEResult res) {
		var style = visualStyleFactory.createVisualStyle(CLUSTER_IMG_STYLE_TITLE + " (" + res.getId() + ")");

		// Defaults
		style.setDefaultValue(NODE_SIZE, 40.0);
		style.setDefaultValue(NODE_WIDTH, 40.0);
		style.setDefaultValue(NODE_HEIGHT, 40.0);
		style.setDefaultValue(NODE_PAINT, CLUSTER_NODE_COLOR);
		style.setDefaultValue(NODE_FILL_COLOR, CLUSTER_NODE_COLOR);
		style.setDefaultValue(NODE_BORDER_WIDTH, 0.0);

		style.setDefaultValue(EDGE_WIDTH, 5.0);
		style.setDefaultValue(EDGE_PAINT, CLUSTER_EDGE_COLOR);
		style.setDefaultValue(EDGE_UNSELECTED_PAINT, CLUSTER_EDGE_COLOR);
		style.setDefaultValue(EDGE_STROKE_UNSELECTED_PAINT, CLUSTER_EDGE_COLOR);
		style.setDefaultValue(EDGE_SELECTED_PAINT, CLUSTER_EDGE_COLOR);
		style.setDefaultValue(EDGE_STROKE_SELECTED_PAINT, CLUSTER_EDGE_COLOR);
		style.setDefaultValue(EDGE_STROKE_SELECTED_PAINT, CLUSTER_EDGE_COLOR);
		
		var viewRenderer = applicationMgr.getCurrentNetworkViewRenderer();
		
		if (viewRenderer == null)
			viewRenderer = applicationMgr.getDefaultNetworkViewRenderer();
		
		var lexicon = viewRenderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT)
				.getVisualLexicon();
		
		{
			VisualProperty vp = lexicon.lookup(CyEdge.class, "edgeTargetArrowShape");

			if (vp != null) {
				var value = vp.parseSerializableString("ARROW");
				
				if (value != null)
					style.setDefaultValue(vp, value);
			}
		}
		{
			VisualProperty vp = lexicon.lookup(CyEdge.class, "EDGE_SOURCE_ARROW_UNSELECTED_PAINT");
			
			if (vp != null)
				style.setDefaultValue(vp, CLUSTER_ARROW_COLOR);
		}
		{
			VisualProperty vp = lexicon.lookup(CyEdge.class, "EDGE_TARGET_ARROW_UNSELECTED_PAINT");
			
			if (vp != null)
				style.setDefaultValue(vp, CLUSTER_ARROW_COLOR);
		}
		
		// Node Shape Mapping
		var nodeShapeDm = (DiscreteMapping<String, NodeShape>) discreteMappingFactory
				.createVisualMappingFunction(columnName(NODE_STATUS_ATTR, res), String.class, NODE_SHAPE);
		
		nodeShapeDm.putMapValue("Clustered", NodeShapeVisualProperty.ELLIPSE);
		nodeShapeDm.putMapValue("Seed", NodeShapeVisualProperty.RECTANGLE);
		
		style.addVisualMappingFunction(nodeShapeDm);

		return style;
	}

	public VisualStyle createMCODEStyle(MCODEResult res) {
		var title = STYLE_TITLE + " (" + res.getId() + ")";
		var appStyle = getStyle(title);
		
		if (appStyle == null) {
			appStyle = visualStyleFactory.createVisualStyle(title);
			registerVisualStyle(appStyle);
			
			// Default values
			appStyle.setDefaultValue(NODE_FILL_COLOR, NODE_DEF_COLOR);
			
			// Set node width/height lock
			for (var dep : appStyle.getAllVisualPropertyDependencies()) {
				if (dep.getParentVisualProperty() == BasicVisualLexicon.NODE_SIZE &&
						dep.getVisualProperties().contains(BasicVisualLexicon.NODE_WIDTH) &&
						dep.getVisualProperties().contains(BasicVisualLexicon.NODE_HEIGHT))
					dep.setDependency(true);
			}
		}
		
		// IMPORTANT: Always recreate the mapping functions for the correct result columns!!!
		
		// -- Node Shape:
		appStyle.removeVisualMappingFunction(NODE_SHAPE);
		
		var nodeShapeDm = (DiscreteMapping<String, NodeShape>) discreteMappingFactory
				.createVisualMappingFunction(columnName(NODE_STATUS_ATTR, res), String.class, NODE_SHAPE);

		nodeShapeDm.putMapValue("Clustered", NodeShapeVisualProperty.ELLIPSE);
		nodeShapeDm.putMapValue("Seed", NodeShapeVisualProperty.RECTANGLE);
		nodeShapeDm.putMapValue("Unclustered", NodeShapeVisualProperty.DIAMOND);

		appStyle.addVisualMappingFunction(nodeShapeDm);

		// -- Node Color:
		appStyle.removeVisualMappingFunction(NODE_FILL_COLOR);

		// The lower the score the darker the color
		var alg = getNetworkAlgorithm(res.getNetwork().getSUID());
		double maxScore = alg.getMaxScore(res.getId());
		
		var nodeColorCm = (ContinuousMapping<Double, Paint>) continuousMappingFactory
				.createVisualMappingFunction(columnName(SCORE_ATTR, res), Double.class, NODE_FILL_COLOR);

		// First we state that everything below or equaling 0 (min) will be white, and everything above that will
		// start from black and fade into the next boundary color
		nodeColorCm.addPoint(0.0, new BoundaryRangeValues<>(NODE_DEF_COLOR, NODE_DEF_COLOR, NODE_MIN_COLOR));
		// Now we state that anything anything below the max score will fade into red from the lower boundary color
		// and everything equal or greater than the max (never occurs since this is the upper boundary) will be red
		// The max value is set by MCODEVisualStyleAction based on the current result set's max score
		nodeColorCm.addPoint(maxScore, new BoundaryRangeValues<>(NODE_MAX_COLOR, NODE_MAX_COLOR, NODE_MAX_COLOR));

		appStyle.addVisualMappingFunction(nodeColorCm);

		return appStyle;
	}

	public VisualStyle getNetworkViewStyle(CyNetworkView view) {
		return view != null ? visualMappingMgr.getVisualStyle(view) : null;
	}
	
	/**
	 * Returns the first style with the passed title or null if none is found.
	 */
	public VisualStyle getStyle(String title) {
		var allStyles = visualMappingMgr.getAllVisualStyles();
		
		for (var vs : allStyles) {
			if (title.equals(vs.getTitle()))
				return vs;
		}
		
		return null;
	}

	public void registerVisualStyle(VisualStyle style) {
		// Add it once only!
		if (!visualMappingMgr.getAllVisualStyles().contains(style))
			visualMappingMgr.addVisualStyle(style);
	}

	public void setSelected(final Collection<? extends CyIdentifiable> elements, CyNetwork network) {
		var allElements = new ArrayList<CyIdentifiable>(network.getNodeList());
		allElements.addAll(network.getEdgeList());

		for (var nodeOrEdge : allElements) {
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

			var g2 = (Graphics2D) placeHolderImage.getGraphics();

			int fontSize = 10;
			g2.setFont(new Font("Arial", Font.PLAIN, fontSize));
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			var f = g2.getFont();
			var fm = g2.getFontMetrics(f);

			// Place Holder text
			var placeHolderText = "Too big to show";
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
		var sb = new StringBuffer();

		for (var node : network.getNodeList()) {
			var row = network.getRow(node);
			var id = "" + node.getSUID();

			if (row.isSet(CyNetwork.NAME))
				id = row.get(CyNetwork.NAME, String.class);

			sb.append(id);
			sb.append(", ");
		}

		if (sb.length() > 2)
			sb.delete(sb.length() - 2, sb.length());

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

		var lineSep = System.getProperty("line.separator");
		String fileName = null;
		FileWriter fout = null;

		try {
			// Call save method in MCODE get the file name
			var filters = new ArrayList<FileChooserFilter>();
			filters.add(new FileChooserFilter("Text format", "txt"));
			var file = fileUtil.getFile(swingApplication.getJFrame(),
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
					var c = clusters.get(i);
					var clusterNetwork = c.getNetwork();
					fout.write((i + 1) + "\t"); //rank
					var nf = NumberFormat.getInstance();
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
	
	public void createMCODEColumns(MCODEResult res) {
		createMCODEColumns(res.getNetwork(), res);
	}
	
	/**
	 * @param clusterNet
	 * @param res if null, the column names won't have the result id as suffix
	 */
	public void createMCODEColumns(CyNetwork net, MCODEResult res) {
		// Create MCODE columns as local ones:
		var table = net.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		createColumn(table, columnName(SCORE_ATTR, res), Double.class, false);
		createColumn(table, columnName(NODE_STATUS_ATTR, res), String.class, false);
		createColumn(table, columnName(CLUSTERS_ATTR, res), String.class, true);
	}
	
	public void copyMCODEColumns(CyNetwork clusterNet, MCODEResult res) {
		// Create MCODE columns as local ones (these ones don't have the result number as suffix)
		createMCODEColumns(clusterNet, res);
		
		// Copy the values from the parent network
		var parentNet = res.getNetwork();
		var parentNodeTbl = parentNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		var clusterNodeTbl = clusterNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		for (var node : clusterNet.getNodeList()) {
			var parentRow = parentNodeTbl.getRow(node.getSUID());
			var clusterRow = clusterNodeTbl.getRow(node.getSUID());
			
			try {
				var score = parentRow.get(columnName(SCORE_ATTR, res), Double.class);
				
				if (score != null)
					clusterRow.set(columnName(SCORE_ATTR, res), score);
			} catch (Exception e) {
				logger.error("MCODE cannot copy value from column '" + columnName(SCORE_ATTR, res) + "'", e);
			}
			
			try {
				var status = parentRow.get(columnName(NODE_STATUS_ATTR, res), String.class);
				
				if (status != null)
					clusterRow.set(columnName(NODE_STATUS_ATTR, res), status);
			} catch (Exception e) {
				logger.error("MCODE cannot copy value from column '" + columnName(NODE_STATUS_ATTR, res) + "'", e);
			}
			
			try {
				var clusters = parentRow.getList(columnName(CLUSTERS_ATTR, res), String.class);
				
				if (clusters != null)
					clusterRow.set(columnName(CLUSTERS_ATTR, res), clusters);
			} catch (Exception e) {
				logger.error("MCODE cannot copy value from column '" + columnName(CLUSTERS_ATTR, res) + "'", e);
			}
		}
		
		createColumn(parentNodeTbl, columnName(SCORE_ATTR, res), Double.class, false);
		createColumn(parentNodeTbl, columnName(NODE_STATUS_ATTR, res), String.class, false);
		createColumn(parentNodeTbl, columnName(CLUSTERS_ATTR, res), String.class, true);
	}

	public void createColumn(CyTable table, String name, Class<?> type, boolean isList) {
		// Create MCODE columns as LOCAL ones
		try {
			if (table.getColumn(name) == null) {
				if (isList)
					table.createListColumn(name, type, false);
				else
					table.createColumn(name, type, false);
			}
		} catch (IllegalArgumentException e) {
			logger.error("MCODE cannot create column '" + name + "'", e);
		}
	}
	
	public void removeMCODEColumns(MCODEResult res) {
		var net = res.getNetwork();
		
		// Create MCODE columns as local ones:
		var localNodeTbl = net.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		removeColumn(localNodeTbl, columnName(SCORE_ATTR, res));
		removeColumn(localNodeTbl, columnName(NODE_STATUS_ATTR, res));
		removeColumn(localNodeTbl, columnName(CLUSTERS_ATTR, res));
	}
	
	private void removeColumn(CyTable table, String name) {
		try {
			table.deleteColumn(name);
		} catch (IllegalArgumentException e) {
			logger.error("MCODE cannot remove column '" + name + "'", e);
		}
	}

	public static String columnName(String name) {
		return columnName(name, null);
	}
	
	public static String columnName(String name, MCODEResult res) {
		var prefix = NAMESPACE + "::";
		var suffix = res != null ? " (" + res.getId() + ")" : "";
		
		return prefix + name + suffix;
	}
	
	private static Properties loadProperties(String name) {
		var props = new Properties();

		try {
			var in = CyActivator.class.getResourceAsStream(name);

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
		var name = "";
		
		try {
			name = network.getRow(network).get(CyNetwork.NAME, String.class);
		} catch (Exception e) {
		}
		
		if (name == null || name.trim().isEmpty())
			name = "? (SUID: " + network.getSUID() + ")";
		
		return name;
	}
}
