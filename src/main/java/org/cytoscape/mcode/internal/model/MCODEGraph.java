package org.cytoscape.mcode.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

public class MCODEGraph {

	private final CyRootNetwork rootNetwork;
	private final Set<CyNode> nodes;
	private final Set<CyEdge> edges;
	private final Map<Integer, CyNode> nodeMap;
	private final Map<Integer, CyEdge> edgeMap;
	
	private CySubNetwork subNetwork;
	
	public MCODEGraph(final CyRootNetwork rootNetwork, final Collection<CyNode> nodes, final Collection<CyEdge> edges) {
		if (rootNetwork == null)
			throw new NullPointerException("rootNetwork is null!");
		if (nodes == null)
			throw new NullPointerException("nodes is null!");
		if (edges == null)
			throw new NullPointerException("edges is null!");
		
		this.rootNetwork = rootNetwork;
		this.nodes = new HashSet<CyNode>(nodes.size());
		this.edges = new HashSet<CyEdge>(edges.size());
		this.nodeMap = new HashMap<Integer, CyNode>(nodes.size());
		this.edgeMap = new HashMap<Integer, CyEdge>(edges.size());
		
		for (CyNode n : nodes) addNode(n);
		for (CyEdge e : edges) addEdge(e);
	}

	public boolean addNode(CyNode node) {
		if (nodes.contains(node)) return false;
		node = rootNetwork.getNode(node.getIndex());
		
		if (nodes.add(node)) {
			nodeMap.put(node.getIndex(), node);
			return true;
		}
		
		return false;
	}

	public boolean addEdge(CyEdge edge) {
		if (edges.contains(edge)) return false;
		
		if (nodes.contains(edge.getSource()) && nodes.contains(edge.getTarget())) {
			edge = rootNetwork.getEdge(edge.getIndex());
			
			if (edges.add(edge)) {
				edgeMap.put(edge.getIndex(), edge);
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

	public boolean containsNode(CyNode node) {
		return nodes.contains(node);
	}

	public boolean containsEdge(CyEdge edge) {
		return edges.contains(edge);
	}

	public CyNode getNode(int index) {
		return nodeMap.get(index);
	}

	public CyEdge getEdge(int index) {
		return edgeMap.get(index);
	}

	public List<CyEdge> getAdjacentEdgeList(CyNode node, Type edgeType) {
		List<CyEdge> rootList = rootNetwork.getAdjacentEdgeList(node, edgeType);
		List<CyEdge> list = new ArrayList<CyEdge>(rootList.size());
		
		for (CyEdge e : rootList) {
			if (containsEdge(e))
				list.add(e);
		}
		
		return list;
	}

	public List<CyEdge> getConnectingEdgeList(CyNode source, CyNode target, Type edgeType) {
		List<CyEdge> rootList = rootNetwork.getConnectingEdgeList(source, target, edgeType);
		List<CyEdge> list = new ArrayList<CyEdge>(rootList.size());
		
		for (CyEdge e : rootList) {
			if (containsEdge(e))
				list.add(e);
		}
		
		return list;
	}

	public CyRootNetwork getRootNetwork() {
		return rootNetwork;
	}

	public CySubNetwork getSubNetwork() {
		if (subNetwork == null) {
			subNetwork = rootNetwork.addSubNetwork(nodes, edges);
		}
		
		return subNetwork;
	}
}
