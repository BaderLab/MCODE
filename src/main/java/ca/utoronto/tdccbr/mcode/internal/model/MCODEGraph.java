package ca.utoronto.tdccbr.mcode.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CySubNetwork;

import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

public class MCODEGraph {

	private final CyNetwork parentNetwork;
	private final Map<Long, CyNode> nodeMap;
	private final Map<Long, CyEdge> edgeMap;
	private CySubNetwork subNetwork;
	private MCODEUtil mcodeUtil;
	private boolean disposed;
	
	private final Object lock = new Object();

	public MCODEGraph(CyNetwork parentNetwork, Collection<CyNode> nodes, Collection<CyEdge> edges,
			MCODEUtil mcodeUtil) {
		if (parentNetwork == null)
			throw new NullPointerException("parentNetwork is null!");
		if (nodes == null)
			throw new NullPointerException("nodes is null!");
		if (edges == null)
			throw new NullPointerException("edges is null!");

		this.mcodeUtil = mcodeUtil;
		this.parentNetwork = parentNetwork;
		this.nodeMap = new HashMap<>(nodes.size());
		this.edgeMap = new HashMap<>(edges.size());

		for (CyNode n : nodes)
			addNode(n);
		for (CyEdge e : edges)
			addEdge(e);
	}

	public boolean addNode(CyNode node) {
		if (disposed)
			throw new IllegalStateException("This cluster has been disposed.");
		
		nodeMap.put(node.getSUID(), node);
			
		return true;
	}

	public boolean addEdge(CyEdge edge) {
		if (disposed)
			throw new IllegalStateException("This cluster has been disposed.");
		
		if (containsNode(edge.getSource()) && containsNode(edge.getTarget())) {
			edgeMap.put(edge.getSUID(), edge);
			
			return true;
		}

		return false;
	}

	public int getNodeCount() {
		return nodeMap.size();
	}

	public int getEdgeCount() {
		return edgeMap.size();
	}

	public Collection<CyNode> getNodeList() {
		return nodeMap.values();
	}

	public Collection<CyEdge> getEdgeList() {
		return edgeMap.values();
	}

	public boolean containsNode(CyNode node) {
		return nodeMap.containsKey(node.getSUID());
	}

	public boolean containsEdge(CyEdge edge) {
		return edgeMap.containsKey(edge.getSUID());
	}

	public CyNode getNode(Long suid) {
		return nodeMap.get(suid);
	}

	public CyEdge getEdge(Long suid) {
		return edgeMap.get(suid);
	}

	public List<CyEdge> getAdjacentEdgeList(CyNode node, Type edgeType) {
		List<CyEdge> rootList = parentNetwork.getAdjacentEdgeList(node, edgeType);
		List<CyEdge> list = new ArrayList<>(rootList.size());

		for (CyEdge e : rootList) {
			if (containsEdge(e))
				list.add(e);
		}

		return list;
	}

	public List<CyEdge> getConnectingEdgeList(CyNode source, CyNode target, Type edgeType) {
		List<CyEdge> rootList = parentNetwork.getConnectingEdgeList(source, target, edgeType);
		List<CyEdge> list = new ArrayList<>(rootList.size());

		for (CyEdge e : rootList) {
			if (containsEdge(e))
				list.add(e);
		}

		return list;
	}

	public CyNetwork getParentNetwork() {
		return parentNetwork;
	}

	public CySubNetwork getSubNetwork() {
		synchronized (lock) {
			if (!disposed && subNetwork == null)
				subNetwork = mcodeUtil.createSubNetwork(parentNetwork, getNodeList(), getEdgeList(),
						SavePolicy.DO_NOT_SAVE);
	
			return subNetwork;
		}
	}

	public boolean isDisposed() {
		synchronized (lock) {
			return disposed;
		}
	}
	
	public void dispose() {
		synchronized (lock) {
			if (disposed)
				return;

			if (subNetwork != null) {
				mcodeUtil.destroy(subNetwork);
				subNetwork = null;
			}

			nodeMap.clear();
			edgeMap.clear();

			disposed = true;
		}
	}
}