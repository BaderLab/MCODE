package org.cytoscape.mcode.internal;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

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

public abstract class AbstractMCODETest {

	protected MCODEAlgorithm alg;
	protected MCODEResultsManager resultsMgr;
	protected MCODEUtil mcodeUtil;
	protected final NetworkViewTestSupport netViewTestSupport;

	public AbstractMCODETest() {
		netViewTestSupport = new NetworkViewTestSupport();
	}

	protected List<MCODECluster> findClusters(final CyNetwork net) {
		return findClusters(net, new MCODEParameters());
	}

	protected List<MCODECluster> findClusters(CyNetwork net, MCODEParameters params) {
		final int resultId = resultsMgr.getNextResultId();
		
		mcodeUtil.getParameterManager().setParams(params, resultId, net);
		alg = new MCODEAlgorithm(net.getSUID(), mcodeUtil);
		alg.scoreGraph(net, resultId);
		List<MCODECluster> clusters = alg.findClusters(net, resultId);
		
		if (!clusters.isEmpty()) {
			MCODEResult res = resultsMgr.createResult(net, clusters);
			resultsMgr.addResult(res);
		}
		
		return clusters;
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