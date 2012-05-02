package org.cytoscape.mcode.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 ** User: Gary Bader
 ** Date: Jan 20, 2004
 ** Time: 6:18:03 PM
 ** Description: An implementation of the MCODE algorithm
 **/

/**
 * An implementation of the MCODE algorithm
 */
public class MCODEAlgorithm {

	private boolean cancelled;
	private TaskMonitor taskMonitor;
	
	private static final Logger logger = LoggerFactory.getLogger(MCODEAlgorithm.class);

	//data structure for storing information required for each node
	private static class NodeInfo {

		double density; //neighborhood density
		int numNodeNeighbors; //number of node neighbors
		Long[] nodeNeighbors; //stores node indices of all neighbors
		int coreLevel; //e.g. 2 = a 2-core
		double coreDensity; //density of the core neighborhood
		double score; //node score

		public NodeInfo() {
			this.density = 0.0;
			this.numNodeNeighbors = 0;
			this.coreLevel = 0;
			this.coreDensity = 0.0;
		}
	}

	//data structures useful to have around for more than one cluster finding iteration
	//key is the node SUID, value is a NodeInfo instance
	private Map<Long, NodeInfo> currentNodeInfoHashMap;
	//key is node score, value is nodeIndex
	private SortedMap<Double, List<Long>> currentNodeScoreSortedMap;
	//because every network can be scored and clustered several times with different parameters
	//these results have to be stored so that the same scores are used during exploration when
	//the user is switching between the various results
	//Since the network is not always rescored whenever a new result is generated (if the scoring parameters
	//haven't changed for example) the clustering method must save the current node scores under the new result
	//title for later reference
	//key is result id, value is nodeScoreSortedMap
	private Map<Integer, SortedMap<Double, List<Long>>> nodeScoreResultsMap = new HashMap<Integer, SortedMap<Double, List<Long>>>();
	//key is result id, value is nodeInfroHashMap
	private Map<Integer, Map<Long, NodeInfo>> nodeInfoResultsMap = new HashMap<Integer, Map<Long, NodeInfo>>();

	private MCODEParameterSet params; //the parameters used for this instance of the algorithm
	//stats
	private long lastScoreTime;
	private long lastFindTime;

	private final MCODEUtil mcodeUtil;

	/**
	 * The constructor.  Use this to get an instance of MCODE to run.
	 *
	 * @param networkID Allows the algorithm to get the parameters of the focused network
	 */
	public MCODEAlgorithm(final Long networkID, final MCODEUtil mcodeUtil) {
		this.mcodeUtil = mcodeUtil;
		this.params = mcodeUtil.getCurrentParameters().getParamsCopy(networkID);
	}

	public MCODEAlgorithm(final TaskMonitor taskMonitor, final Long networkID, final MCODEUtil mcodeUtil) {
		this(networkID, mcodeUtil);
		this.taskMonitor = taskMonitor;
	}

	public void setTaskMonitor(TaskMonitor taskMonitor, Long networkID) {
		this.params = mcodeUtil.getCurrentParameters().getParamsCopy(networkID);
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Get the time taken by the last score operation in this instance of the algorithm
	 *
	 * @return the duration of the scoring portion
	 */
	public long getLastScoreTime() {
		return lastScoreTime;
	}

	/**
	 * Get the time taken by the last find operation in this instance of the algorithm
	 *
	 * @return the duration of the finding process
	 */
	public long getLastFindTime() {
		return lastFindTime;
	}

	/**
	 * Get the parameter set used for this instance of MCODEAlgorithm
	 *
	 * @return The parameter set used
	 */
	public MCODEParameterSet getParams() {
		return params;
	}

	/**
	 * If set, will schedule the algorithm to be cancelled at the next convenient opportunity
	 *
	 * @param cancelled Set to true if the algorithm should be cancelled
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * Gets the calculated node score of a node from a given result.  Used in MCODEResultsPanel
	 * during the attribute setting method.
	 *
	 * @param nodeId Number which is used to identify the nodes in the score-sorted tree map
	 * @param resultId Id of the results for which we are retrieving a node score
	 * @return node score as a Double
	 */
	public double getNodeScore(Long nodeId, int resultId) {
		Map<Double, List<Long>> nodeScoreSortedMap = nodeScoreResultsMap.get(resultId);

		for (double nodeScore : nodeScoreSortedMap.keySet()) {
			List<Long> nodes = nodeScoreSortedMap.get(nodeScore);

			if (nodes.contains(nodeId)) {
				return nodeScore;
			}
		}

		return 0.0;
	}

	/**
	 * Gets the highest node score in a given result.  Used in the MCODEVisualStyleAction class to
	 * re-initialize the visual calculators.
	 *
	 * @param resultId Id of the result
	 * @return First key in the nodeScoreSortedMap corresponding to the highest score
	 */
	public double getMaxScore(int resultId) {
		SortedMap<Double, List<Long>> nodeScoreSortedMap = nodeScoreResultsMap.get(resultId);

		//Since the map is sorted, the first key is the highest value
		return nodeScoreSortedMap.firstKey();
	}

	/**
	 * Step 1: Score the graph and save scores as node attributes.  Scores are also
	 * saved internally in your instance of MCODEAlgorithm.
	 *
	 * @param inputNetwork The network that will be scored
	 * @param resultId Title of the result, used as an identifier in various hash maps
	 */
	public void scoreGraph(CyNetwork inputNetwork, int resultId) {
		params = getParams();
		String callerID = "MCODEAlgorithm.MCODEAlgorithm";

		if (inputNetwork == null) {
			logger.error("In " + callerID + ": inputNetwork was null.");
			return;
		}

		// Initialize
		long msTimeBefore = System.currentTimeMillis();
		Map<Long, NodeInfo> nodeInfoHashMap = new HashMap<Long, NodeInfo>(inputNetwork.getNodeCount());

		// Sort Doubles in descending order
		Comparator<Double> scoreComparator = new Comparator<Double>() {

			@Override
			public int compare(Double d1, Double d2) {
				return d2.compareTo(d1);
			}
		};
		
		// Will store Doubles (score) as the key, Lists of node indexes as values
		SortedMap<Double, List<Long>> nodeScoreSortedMap = new TreeMap<Double, List<Long>>(scoreComparator);

		// Iterate over all nodes and calculate MCODE score
		NodeInfo nodeInfo = null;
		List<Long> al = null;
		int i = 0;
		List<CyNode> nodes = inputNetwork.getNodeList();
		
		for (CyNode n : nodes) {
			nodeInfo = calcNodeInfo(inputNetwork, n.getSUID());
			nodeInfoHashMap.put(n.getSUID(), nodeInfo);
			double nodeScore = scoreNode(nodeInfo);
			
			// Save score for later use in TreeMap
			// Add a list of nodes to each score in case nodes have the same score
			if (nodeScoreSortedMap.containsKey(nodeScore)) {
				// Already have a node with this score, add it to the list
				al = nodeScoreSortedMap.get(nodeScore);
				al.add(n.getSUID());
			} else {
				al = new ArrayList<Long>();
				al.add(n.getSUID());
				nodeScoreSortedMap.put(nodeScore, al);
			}

			if (taskMonitor != null) {
				taskMonitor.setProgress( (++i / (double) nodes.size()) );
			}
			
			if (cancelled)
				break;
		}
		
		nodeScoreResultsMap.put(resultId, nodeScoreSortedMap);
		nodeInfoResultsMap.put(resultId, nodeInfoHashMap);

		currentNodeScoreSortedMap = nodeScoreSortedMap;
		currentNodeInfoHashMap = nodeInfoHashMap;

		long msTimeAfter = System.currentTimeMillis();
		lastScoreTime = msTimeAfter - msTimeBefore;
	}

	/**
	 * Step 2: Find all clusters given a scored graph.  If the input network has not been scored,
	 * this method will return null.  This method is called when the user selects network scope or
	 * single node scope.
	 *
	 * @param inputNetwork The scored network to find clusters in.
	 * @param resultSetName Title of the result
	 * @return An array containing an MCODECluster object for each cluster.
	 */
	public MCODECluster[] findClusters(CyNetwork inputNetwork, int resultId) {
		SortedMap<Double, List<Long>> nodeScoreSortedMap;
		Map<Long, NodeInfo> nodeInfoHashMap;

		// First we check if the network has been scored under this result title (i.e. scoring
		// was required due to a scoring parameter change).  If it hasn't then we want to use the
		// current scores that were generated the last time the network was scored and store them
		// under the title of this result set for later use
		if (!nodeScoreResultsMap.containsKey(resultId)) {
			nodeScoreSortedMap = currentNodeScoreSortedMap;
			nodeInfoHashMap = currentNodeInfoHashMap;

			nodeScoreResultsMap.put(resultId, nodeScoreSortedMap);
			nodeInfoResultsMap.put(resultId, nodeInfoHashMap);
		} else {
			nodeScoreSortedMap = nodeScoreResultsMap.get(resultId);
			nodeInfoHashMap = nodeInfoResultsMap.get(resultId);
		}

		params = getParams();
		MCODECluster currentCluster;
		String callerID = "MCODEAlgorithm.findClusters";

		if (inputNetwork == null) {
			logger.error("In " + callerID + ": inputNetwork was null.");
			return null;
		}

		if (nodeInfoHashMap == null || nodeScoreSortedMap == null) {
			logger.error("In " + callerID + ": nodeInfoHashMap or nodeScoreSortedMap was null.");
			return null;
		}

		// Initialization
		long msTimeBefore = System.currentTimeMillis();
		HashMap<Long, Boolean> nodeSeenHashMap = new HashMap<Long, Boolean>(); //key is node SUID
		Long currentNode = null;
		double findingProgress = 0;
		double findingTotal = 0;
		Collection<List<Long>> values = nodeScoreSortedMap.values(); //returns a Collection sorted by key order (descending)

		// In order to track the progress without significant lags (for times when many nodes have the same score
		// and no progress is reported) we count all the scored nodes and track those instead
		for (List<Long> value : values) {
			findingTotal += value.size();
		}

		// Stores the list of clusters as ArrayLists of node indices in the input Network
		List<MCODECluster> alClusters = new ArrayList<MCODECluster>();

		// Iterate over node ids sorted descending by their score
		for (List<Long> alNodesWithSameScore : values) {
			// Each score may be associated with multiple nodes, iterate over these lists
			for (int j = 0; j < alNodesWithSameScore.size(); j++) {
				currentNode = alNodesWithSameScore.get(j);

				if (!nodeSeenHashMap.containsKey(currentNode)) {
					currentCluster = new MCODECluster();
					currentCluster.setSeedNode(currentNode);//store the current node as the seed node
					// We store the current node seen hash map for later exploration purposes
					Map<Long, Boolean> nodeSeenHashMapSnapShot = new HashMap<Long, Boolean>(nodeSeenHashMap);

					// Here we use the original node score cutoff
					List<Long> alCluster = getClusterCore(currentNode,
														  nodeSeenHashMap,
														  params.getNodeScoreCutoff(),
														  params.getMaxDepthFromStart(),
														  nodeInfoHashMap);
					if (alCluster.size() > 0) {
						// Make sure seed node is part of cluster, if not already in there
						if (!alCluster.contains(currentNode)) {
							alCluster.add(currentNode);
						}

						// Create an input graph for the filter and haircut methods
						CySubNetwork clusterNet = createClusterNetwork(alCluster, inputNetwork);

						if (!filterCluster(clusterNet)) {
							if (params.isHaircut()) {
								haircutCluster(clusterNet, alCluster);
							}

							if (params.isFluff()) {
								fluffClusterBoundary(alCluster, nodeSeenHashMap, nodeInfoHashMap);
							}

							currentCluster.setALCluster(alCluster);
							clusterNet = createClusterNetwork(alCluster, inputNetwork);
							currentCluster.setNetwork(clusterNet);
							currentCluster.setClusterScore(scoreCluster(currentCluster));
							currentCluster.setNodeSeenHashMap(nodeSeenHashMapSnapShot);//store the list of all the nodes that have already been seen and incorporated in other clusters
							currentCluster.setResultId(resultId);
							//store detected cluster for later
							alClusters.add(currentCluster);
						}
					}
				}

				if (taskMonitor != null) {
					// We want to be sure that only progress changes are reported and not
					// miniscule decimal increments so that the taskMonitor isn't overwhelmed
					double newProgress = ++findingProgress / findingTotal;

					if ( Math.round(newProgress * 100) != Math.round((newProgress - 0.01) * 100) ) {
						taskMonitor.setProgress(newProgress);
					}
				}

				if (cancelled) {
					break;
				}
			}
		}

		// Once the clusters have been found we either return them or in the case of selection scope, we select only
		// the ones that contain the selected node(s) and return those
		List<MCODECluster> selectedALClusters = new ArrayList<MCODECluster>();

		if (MCODEParameterSet.SELECTION.equals(params.getScope())) {
			for (MCODECluster cluster : alClusters) {
				List<Long> alCluster = cluster.getALCluster();
				List<Long> alSelectedNodes = new ArrayList<Long>();

				for (int i = 0; i < params.getSelectedNodes().length; i++) {
					alSelectedNodes.add(params.getSelectedNodes()[i]);
				}

				// Method for returning all clusters that contain any of the selected nodes
				for (Long nodeIndex : alSelectedNodes) {
					if (alCluster.contains(nodeIndex)) {
						selectedALClusters.add(cluster);
						break;
					}
				}
			}

			alClusters = selectedALClusters;
		}

		// Finally convert the arraylist into a fixed array
		MCODECluster[] clusters = new MCODECluster[alClusters.size()];

		for (int c = 0; c < clusters.length; c++) {
			clusters[c] = (MCODECluster) alClusters.get(c);
		}

		long msTimeAfter = System.currentTimeMillis();
		lastFindTime = msTimeAfter - msTimeBefore;

		return clusters;
	}

	/**
	 * Finds the cluster based on user's input via size slider.
	 *
	 * @param cluster cluster being explored
	 * @param nodeScoreCutoff slider source value
	 * @param inputNetwork network
	 * @param resultId ID of the result set being explored
	 * @return explored cluster
	 */
	public MCODECluster exploreCluster(MCODECluster cluster,
									   double nodeScoreCutoff,
									   CyNetwork inputNetwork,
									   int resultId) {
		// This method is similar to the finding method with the exception of the filtering so that the decrease of the cluster size
		// can produce a single node, also the use of the node seen hash map is differentially applied...
		Map<Long, NodeInfo> nodeInfoHashMap = nodeInfoResultsMap.get(resultId);
		MCODEParameterSet params = mcodeUtil.getCurrentParameters().getResultParams(cluster.getResultId());
		final Map<Long, Boolean> nodeSeenHashMap;

		// If the size slider is below the set node score cutoff we use the node seen hash map so that clusters
		// with higher scoring seeds have priority, however when the slider moves higher than the node score cutoff
		// we allow the cluster to accrue nodes from all around without the priority restriction
		if (nodeScoreCutoff <= params.getNodeScoreCutoff()) {
			nodeSeenHashMap = new HashMap<Long, Boolean>(cluster.getNodeSeenHashMap());
		} else {
			nodeSeenHashMap = new HashMap<Long, Boolean>();
		}

		Long seedNode = cluster.getSeedNode();

		List<Long> alCluster = getClusterCore(seedNode, nodeSeenHashMap, nodeScoreCutoff, params
				.getMaxDepthFromStart(), nodeInfoHashMap);

		// Make sure seed node is part of cluster, if not already in there
		if (!alCluster.contains(seedNode)) {
			alCluster.add(seedNode);
		}

		// Create an input graph for the filter and haircut methods
		CySubNetwork clusterNetwork = createClusterNetwork(alCluster, inputNetwork);

		if (params.isHaircut()) {
			haircutCluster(clusterNetwork, alCluster);
		}

		if (params.isFluff()) {
			fluffClusterBoundary(alCluster, nodeSeenHashMap, nodeInfoHashMap);
		}

		cluster.setALCluster(alCluster);
		clusterNetwork = createClusterNetwork(alCluster, inputNetwork);
		cluster.setNetwork(clusterNetwork);
		cluster.setClusterScore(scoreCluster(cluster));
		
		return cluster;
	}

	private CySubNetwork createClusterNetwork(List<Long> alCluster, CyNetwork inputNetwork) {
		Set<CyNode> nodes = new HashSet<CyNode>();

		for (Long id : alCluster) {
			CyNode n = inputNetwork.getNode(id);
			nodes.add(n);
		}

		CySubNetwork outputNetwork = mcodeUtil.createSubNetwork(inputNetwork, nodes);

		return outputNetwork;
	}

	/**
	 * Score node using the formula from original MCODE paper.
	 * This formula selects for larger, denser cores.
	 * This is a utility function for the algorithm.
	 *
	 * @param nodeInfo The internal data structure to fill with node information
	 * @return The score of this node.
	 */
	private double scoreNode(NodeInfo nodeInfo) {
		if (nodeInfo.numNodeNeighbors > params.getDegreeCutoff()) {
			nodeInfo.score = nodeInfo.coreDensity * (double) nodeInfo.coreLevel;
		} else {
			nodeInfo.score = 0.0;
		}

		return nodeInfo.score;
	}

	/**
	 * Score a cluster.  Currently this ranks larger, denser clusters higher, although
	 * in the future other scoring functions could be created
	 *
	 * @param cluster
	 * @return The score of the cluster
	 */
	public double scoreCluster(MCODECluster cluster) {
		int numNodes = 0;
		double density = 0.0, score = 0.0;

		numNodes = cluster.getNetwork().getNodeCount();
		density = calcDensity(cluster.getNetwork(), true);
		score = density * numNodes;

		return score;
	}

	/**
	 * Calculates node information for each node according to the original MCODE publication.
	 * This information is used to score the nodes in the scoring stage.
	 * This is a utility function for the algorithm.
	 *
	 * @param inputNetwork The input network for reference
	 * @param nodeId    The SUID of the node in the input network to score
	 * @return A NodeInfo object containing node information required for the algorithm
	 */
	private NodeInfo calcNodeInfo(CyNetwork inputNetwork, Long nodeId) {
		final Long[] neighborhood;

		String callerID = "MCODEAlgorithm.calcNodeInfo";

		if (inputNetwork == null) {
			logger.error("In " + callerID + ": gpInputGraph was null.");
			return null;
		}

		// Get neighborhood of this node (including the node)
		CyNode rootNode = inputNetwork.getNode(nodeId);
		List<CyNode> neighbors = inputNetwork.getNeighborList(rootNode, CyEdge.Type.ANY);
		
		if (neighbors.size() < 2) {
			// If there are no neighbors or just one neighbor, nodeInfo calculation is trivial
			NodeInfo nodeInfo = new NodeInfo();

			if (neighbors.size() == 1) {
				nodeInfo.coreLevel = 1;
				nodeInfo.coreDensity = 1.0;
				nodeInfo.density = 1.0;
			}

			return nodeInfo;
		}

		Long[] neighborIndexes = new Long[neighbors.size()];
		int i = 0;

		for (CyNode n : neighbors) {
			neighborIndexes[i++] = n.getSUID();
		}

		// Add original node to extract complete neighborhood
		Arrays.sort(neighborIndexes);

		if (Arrays.binarySearch(neighborIndexes, nodeId) < 0) {
			neighborhood = new Long[neighborIndexes.length + 1];
			System.arraycopy(neighborIndexes, 0, neighborhood, 1, neighborIndexes.length);
			neighborhood[0] = nodeId;
			neighbors.add(rootNode);
		} else {
			neighborhood = neighborIndexes;
		}
		
		// extract neighborhood subgraph
		CySubNetwork neighborhoodNet = mcodeUtil.createSubNetwork(inputNetwork, neighbors);
		
		if (neighborhoodNet == null) {
			// this shouldn't happen
			logger.error("In " + callerID + ": gpNodeNeighborhood was null.");
			return null;
		}

		// Calculate the node information for each node
		NodeInfo nodeInfo = new NodeInfo();

		// Density
		if (neighborhoodNet != null) {
			nodeInfo.density = calcDensity(neighborhoodNet, params.isIncludeLoops());
		}
		
		nodeInfo.numNodeNeighbors = neighborhood.length;

		// Calculate the highest k-core
		Integer k = null;
		Object[] returnArray = getHighestKCore(neighborhoodNet);
		k = (Integer) returnArray[0];
		CySubNetwork kCore = (CySubNetwork) returnArray[1];
		nodeInfo.coreLevel = k.intValue();
		
		// Calculate the core density - amplifies the density of heavily interconnected regions and attenuates
		// that of less connected regions
		if (kCore != null) {
			nodeInfo.coreDensity = calcDensity(kCore, params.isIncludeLoops());
		}

		// Record neighbor array for later use in cluster detection step
		nodeInfo.nodeNeighbors = neighborhood;
		
		return nodeInfo;
	}

	/**
	 * Find the high-scoring central region of the cluster.
	 * This is a utility function for the algorithm.
	 *
	 * @param startNode       The node that is the seed of the cluster
	 * @param nodeSeenHashMap The list of nodes seen already
	 * @param nodeScoreCutoff Slider input used for cluster exploration
	 * @param maxDepthFromStart Limits the number of recursions
	 * @param nodeInfoHashMap Provides the node scores
	 * @return A list of node indexes representing the core of the cluster
	 */
	private List<Long> getClusterCore(Long startNode,
									  Map<Long, Boolean> nodeSeenHashMap,
									  double nodeScoreCutoff,
									  int maxDepthFromStart,
									  Map<Long, NodeInfo> nodeInfoHashMap) {
		List<Long> cluster = new ArrayList<Long>(); //stores node indexes
		getClusterCoreInternal(startNode,
							   nodeSeenHashMap,
							   ((NodeInfo) nodeInfoHashMap.get(startNode)).score,
							   1,
							   cluster,
							   nodeScoreCutoff,
							   maxDepthFromStart,
							   nodeInfoHashMap);

		return cluster;
	}

	/**
	 * An internal function that does the real work of getClusterCore, implemented to enable recursion.
	 *
	 * @param startNode         The node that is the seed of the cluster
	 * @param nodeSeenHashMap   The list of nodes seen already
	 * @param startNodeScore    The score of the seed node
	 * @param currentDepth      The depth away from the seed node that we are currently at
	 * @param cluster           The cluster to add to if we find a cluster node in this method
	 * @param nodeScoreCutoff   Helps determine if the nodes being added are within the given threshold
	 * @param maxDepthFromStart Limits the recursion
	 * @param nodeInfoHashMap   Provides score info
	 * @return true
	 */
	private boolean getClusterCoreInternal(Long startNode,
										   Map<Long, Boolean> nodeSeenHashMap,
										   double startNodeScore,
										   int currentDepth,
										   List<Long> cluster,
										   double nodeScoreCutoff,
										   int maxDepthFromStart,
										   Map<Long, NodeInfo> nodeInfoHashMap) {
		// base cases for recursion
		if (nodeSeenHashMap.containsKey(startNode)) {
			return true; //don't recheck a node
		}

		nodeSeenHashMap.put(startNode, true);

		if (currentDepth > maxDepthFromStart) {
			return true; //don't exceed given depth from start node
		}

		// Initialization
		Long currentNeighbor;
		int numNodeNeighbors = nodeInfoHashMap.get(startNode).numNodeNeighbors;
		int i = 0;

		for (i = 0; i < numNodeNeighbors; i++) {
			// go through all currentNode neighbors to check their core density for cluster inclusion
			currentNeighbor = nodeInfoHashMap.get(startNode).nodeNeighbors[i];

			if ((!nodeSeenHashMap.containsKey(currentNeighbor)) &&
				(nodeInfoHashMap.get(currentNeighbor).score >= (startNodeScore - startNodeScore * nodeScoreCutoff))) {

				// add current neighbor
				if (!cluster.contains(currentNeighbor)) {
					cluster.add(currentNeighbor);
				}

				// try to extend cluster at this node
				getClusterCoreInternal(currentNeighbor,
									   nodeSeenHashMap,
									   startNodeScore,
									   currentDepth + 1,
									   cluster,
									   nodeScoreCutoff,
									   maxDepthFromStart,
									   nodeInfoHashMap);
			}
		}

		return true;
	}

	/**
	 * Fluff up the cluster at the boundary by adding lower scoring, non cluster-core neighbors
	 * This implements the cluster fluff feature.
	 *
	 * @param cluster         The cluster to fluff
	 * @param nodeSeenHashMap The list of nodes seen already
	 * @param nodeInfoHashMap Provides neighbour info
	 * @return true
	 */
	private boolean fluffClusterBoundary(List<Long> cluster,
										 Map<Long, Boolean> nodeSeenHashMap,
										 Map<Long, NodeInfo> nodeInfoHashMap) {
		Long currentNode = null, nodeNeighbor = null;
		// Create a temp list of nodes to add to avoid concurrently modifying 'cluster'
		List<Long> nodesToAdd = new ArrayList<Long>();

		// Keep a separate internal nodeSeenHashMap because nodes seen during a fluffing
		// should not be marked as permanently seen,
		// they can be included in another cluster's fluffing step.
		Map<Long, Boolean> nodeSeenHashMapInternal = new HashMap<Long, Boolean>();

		// add all current neighbour's neighbours into cluster (if they have high enough clustering coefficients) and mark them all as seen
		for (int i = 0; i < cluster.size(); i++) {
			currentNode = cluster.get(i);

			for (int j = 0; j < nodeInfoHashMap.get(currentNode).numNodeNeighbors; j++) {
				nodeNeighbor = nodeInfoHashMap.get(currentNode).nodeNeighbors[j];

				if ((!nodeSeenHashMap.containsKey(nodeNeighbor)) &&
					(!nodeSeenHashMapInternal.containsKey(nodeNeighbor)) &&
					((((NodeInfo) nodeInfoHashMap.get(nodeNeighbor)).density) > params.getFluffNodeDensityCutoff())) {
					nodesToAdd.add(nodeNeighbor);
					nodeSeenHashMapInternal.put(nodeNeighbor, true);
				}
			}
		}

		// Add fluffed nodes to cluster
		if (nodesToAdd.size() > 0) {
			cluster.addAll(nodesToAdd.subList(0, nodesToAdd.size()));
		}

		return true;
	}

	/**
	 * Checks if the cluster needs to be filtered according to heuristics in this method
	 *
	 * @param clusterNetwork The cluster to check if it passes the filter
	 * @return true if cluster should be filtered, false otherwise
	 */
	private boolean filterCluster(CySubNetwork clusterNetwork) {
		if (clusterNetwork == null) {
			return true;
		}

		// filter if the cluster does not satisfy the user specified k-core
		CySubNetwork kCore = getKCore(clusterNetwork, params.getKCore());

		return kCore == null;
	}

	/**
	 * Gives the cluster a haircut (removed singly connected nodes by taking a 2-core)
	 *
	 * @param clusterNetwork The cluster network
	 * @param cluster        The cluster node ID list (in the original graph)
	 * @return true
	 */
	private boolean haircutCluster(CySubNetwork clusterNetwork, List<Long> cluster) {
		// get 2-core
		CySubNetwork kCore = getKCore(clusterNetwork, 2);

		if (kCore != null) {
			// clear the cluster and add all 2-core nodes back into it
			cluster.clear();
			// must add back the nodes in a way that preserves gpInputGraph node indices
			for (CyNode n : kCore.getNodeList()) {
				cluster.add(n.getSUID());
			}
		}
		
		return true;
	}

	/**
	 * Calculate the density of a network
	 * The density is defined as the number of edges/the number of possible edges
	 *
	 * @param network The input graph to calculate the density of
	 * @param includeLoops Include the possibility of loops when determining the number of
	 *                     possible edges.
	 * @return The density of the network
	 */
	public double calcDensity(CySubNetwork network, boolean includeLoops) {
		int possibleEdgeNum = 0, actualEdgeNum = 0, loopCount = 0;
		double density = 0;

		String callerID = "MCODEAlgorithm.calcDensity";

		if (network == null) {
			logger.error("In " + callerID + ": network was null.");
			return (-1.0);
		}

		int nodeCount = network.getNodeCount();
		int edgeCount = network.getEdgeCount();

		if (includeLoops) {
			//count loops
			List<CyNode> nodes = network.getNodeList();

			for (CyNode n : nodes) {
				List<CyEdge> loopEdges = network.getConnectingEdgeList(n, n, CyEdge.Type.ANY);

				if (loopEdges != null && loopEdges.size() > 0) {
					loopCount++;
				}
			}

			possibleEdgeNum = nodeCount * nodeCount;
			actualEdgeNum = edgeCount - loopCount;
		} else {
			possibleEdgeNum = nodeCount * nodeCount;
			actualEdgeNum = edgeCount;
		}

		density = (double) actualEdgeNum / (double) possibleEdgeNum;

		return density;
	}

	/**
	 * Find a k-core of a network. A k-core is a subgraph of minimum degree k
	 *
	 * @param inputNetwork The input network
	 * @param k            The k of the k-core to find e.g. 4 will find a 4-core
	 * @return Returns a subgraph with the core, if any was found at given k
	 */
	public CySubNetwork getKCore(CySubNetwork inputNetwork, int k) {
		String callerID = "MCODEAlgorithm.getKCore";

		if (inputNetwork == null) {
			logger.error("In " + callerID + ": inputNetwork was null.");
			return null;
		}

		// filter all nodes with degree less than k until convergence
		boolean firstLoop = true;
		CySubNetwork outputNetwork = null;

		while (true && !cancelled) {
			int numDeleted = 0;
			List<Long> alCoreNodeIndices = new ArrayList<Long>(inputNetwork.getNodeCount());
			List<CyNode> nodes = inputNetwork.getNodeList();

			for (CyNode n : nodes) {
				int degree = inputNetwork.getAdjacentEdgeList(n, CyEdge.Type.ANY).size();

				if (degree >= k) {
					alCoreNodeIndices.add(n.getSUID()); //contains all nodes with degree >= k
				} else {
					numDeleted++;
				}
			}

			if (numDeleted > 0 || firstLoop) {
				Set<CyNode> outputNodes = new HashSet<CyNode>();

				for (Long index : alCoreNodeIndices) {
					CyNode n = inputNetwork.getNode(index);
					outputNodes.add(n);
				}
				
				outputNetwork = mcodeUtil.createSubNetwork(inputNetwork.getRootNetwork(), outputNodes);
				
				if (outputNetwork.getNodeCount() == 0) {
					return null;
				}

				// Iterate again, but with a new k-core input graph
				inputNetwork = outputNetwork;

				if (firstLoop) {
					firstLoop = false;
				}
			} else {
				// stop the loop
				break;
			}
		}

		return outputNetwork;
	}

	/**
	 * Find the highest k-core in the input network.
	 *
	 * @param network The input network
	 * @return Returns the k-value and the core as an Object array.
	 *         The first object is the highest k value i.e. objectArray[0]
	 *         The second object is the highest k-core as a CyNetwork i.e. objectArray[1]
	 */
	public Object[] getHighestKCore(CySubNetwork network) {
		String callerID = "MCODEAlgorithm.getHighestKCore";

		if (network == null) {
			logger.error("In " + callerID + ": network was null.");
			return (null);
		}

		int i = 1;
		CySubNetwork curNet = null, prevNet = null;

		while ((curNet = getKCore(network, i)) != null) {
			network = curNet;
			prevNet = curNet;
			i++;
		}

		Integer k = i - 1;
		Object[] returnArray = new Object[2];
		returnArray[0] = k;
		returnArray[1] = prevNet; //in the last iteration, curNet is null (loop termination condition)

		return returnArray;
	}
}
