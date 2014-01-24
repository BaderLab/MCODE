package org.cytoscape.mcode.internal;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameterSet;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

public abstract class AbstractMCODETest {

	protected MCODEAlgorithm alg;
	protected MCODEUtil mcodeUtil;
	protected final NetworkViewTestSupport netViewTestSupport;

	public AbstractMCODETest() {
		netViewTestSupport = new NetworkViewTestSupport();
	}

	protected List<MCODECluster> findClusters(final CyNetwork net, final int resultId) {
		return findClusters(net, resultId, new MCODEParameterSet());
	}

	protected List<MCODECluster> findClusters(CyNetwork net, int resultId, MCODEParameterSet params) {
		mcodeUtil.getCurrentParameters().setParams(params, resultId, net.getSUID());
		alg = new MCODEAlgorithm(net.getSUID(), mcodeUtil);
		alg.scoreGraph(net, resultId);
		
		return alg.findClusters(net, resultId);
	}

	protected CyNetwork createCompleteGraph(int totalNodes) {
		CyNetwork net = netViewTestSupport.getNetwork();
		
		for (int i = 0; i < totalNodes; i++) {
			net.addNode();
		}
		
		List<CyNode> nodes = net.getNodeList();
		
		for (int i = 0; i < totalNodes; i++) {
			CyNode src = nodes.get(i);
			
			for (int j = 0; j < totalNodes; j++) {
				CyNode tgt = nodes.get(j);
				
				if (src != tgt && !net.containsEdge(src, tgt))
					net.addEdge(src, tgt, false);
			}
		}
		
		assertEquals((totalNodes*(totalNodes-1))/2, net.getEdgeCount());
		
		return net;
	}

}