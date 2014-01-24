package org.cytoscape.mcode.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CySubNetwork;

public class MCODEGraph {

	private final CyNetwork parentNetwork;
	private final Set<CyNode> nodes;
	private final Set<CyEdge> edges;
	private final Map<Long, CyNode> nodeMap;
	private final Map<Long, CyEdge> edgeMap;
	private CySubNetwork subNetwork;
	private MCODEUtil mcodeUtil;
	private boolean disposed;

	public MCODEGraph(final CyNetwork parentNetwork,
					  final Collection<CyNode> nodes,
					  final Collection<CyEdge> edges,
					  final MCODEUtil mcodeUtil) {
		if (parentNetwork == null)
			throw new NullPointerException("parentNetwork is null!");
		if (nodes == null)
			throw new NullPointerException("nodes is null!");
		if (edges == null)
			throw new NullPointerException("edges is null!");

		this.mcodeUtil = mcodeUtil;
		this.parentNetwork = parentNetwork;
		this.nodes = Collections.synchronizedSet(new HashSet<CyNode>(nodes.size()));
		this.edges = Collections.synchronizedSet(new HashSet<CyEdge>(edges.size()));
		this.nodeMap = Collections.synchronizedMap(new HashMap<Long, CyNode>(nodes.size()));
		this.edgeMap = Collections.synchronizedMap(new HashMap<Long, CyEdge>(edges.size()));

		for (CyNode n : nodes)
			addNode(n);
		for (CyEdge e : edges)
			addEdge(e);
	}

	public boolean addNode(CyNode node) {
		if (nodes.contains(node))
			return false;
		
		node = parentNetwork.getNode(node.getSUID());

		if (nodes.add(node)) {
			nodeMap.put(node.getSUID(), node);
			return true;
		}

		return false;
	}

	public boolean addEdge(CyEdge edge) {
		if (edges.contains(edge))
			return false;

		if (nodes.contains(edge.getSource()) && nodes.contains(edge.getTarget())) {
			edge = parentNetwork.getEdge(edge.getSUID());

			if (edges.add(edge)) {
				edgeMap.put(edge.getSUID(), edge);
				return true;
			}
		}

		return false;
	}

	public int getNodeCount() {
		return nodes.size();
	}

	public int getEdgeCount() {
		return edges.size();
	}

	public List<CyNode> getNodeList() {
		return new ArrayList<CyNode>(nodes);
	}

	public List<CyEdge> getEdgeList() {
		return new ArrayList<CyEdge>(edges);
	}

	public boolean containsNode(final CyNode node) {
		return nodes.contains(node);
	}

	public boolean containsEdge(final CyEdge edge) {
		return edges.contains(edge);
	}

	public CyNode getNode(final long index) {
		return nodeMap.get(index);
	}

	public CyEdge getEdge(final long index) {
		return edgeMap.get(index);
	}

	public List<CyEdge> getAdjacentEdgeList(final CyNode node, final Type edgeType) {
		List<CyEdge> rootList = parentNetwork.getAdjacentEdgeList(node, edgeType);
		List<CyEdge> list = new ArrayList<CyEdge>(rootList.size());

		for (CyEdge e : rootList) {
			if (containsEdge(e))
				list.add(e);
		}

		return list;
	}

	public List<CyEdge> getConnectingEdgeList(final CyNode source, final CyNode target, final Type edgeType) {
		List<CyEdge> rootList = parentNetwork.getConnectingEdgeList(source, target, edgeType);
		List<CyEdge> list = new ArrayList<CyEdge>(rootList.size());

		for (CyEdge e : rootList) {
			if (containsEdge(e))
				list.add(e);
		}

		return list;
	}

	public CyNetwork getParentNetwork() {
		return parentNetwork;
	}

	public synchronized CySubNetwork getSubNetwork() {
		if (!disposed && subNetwork == null)
			subNetwork = mcodeUtil.createSubNetwork(parentNetwork, nodes, SavePolicy.DO_NOT_SAVE);

		return subNetwork;
	}

	public synchronized boolean isDisposed() {
		return disposed;
	}
	
	public synchronized void dispose() {
		if (disposed) return;
		
		if (subNetwork != null) {
			mcodeUtil.destroy(subNetwork);
			subNetwork = null;
		}
		
		nodes.clear();
		edges.clear();
		nodeMap.clear();
		edgeMap.clear();
		
		disposed  = true;
	}
}