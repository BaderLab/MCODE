package ca.utoronto.tdccbr.mcode.internal.task;

import static ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction.FIRST_TIME;
import static ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction.INTERRUPTION;
import static ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction.REFIND;
import static ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction.RESCORE;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAnalysisScope;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
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
 ** User: Gary Bader
 ** Date: Jan 26, 2004
 ** Time: 2:44:30 PM
 ** Description: Stores an MCODE parameter set
 **/

public class MCODEAnalyzeCommandTask extends AbstractTask {

	@ContainsTunables
	public MCODEParameters params = new MCODEParameters();
	
	private int resultId = -1;
	
	private final MCODEAnalyzeAction action;
	private final MCODEResultsManager resultsMgr;
	private final MCODEUtil mcodeUtil;
	private final CyServiceRegistrar registrar;
	
	public MCODEAnalyzeCommandTask(
			MCODEAnalyzeAction action,
			MCODEResultsManager resultsMgr,
			MCODEUtil mcodeUtil,
			CyServiceRegistrar registrar
	) {
		this.action = action;
		this.resultsMgr = resultsMgr;
		this.mcodeUtil = mcodeUtil;
		this.registrar = registrar;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		int mode = action.getMode();
		
		// Get requested network or the current one
		CyNetwork network = null;
		
		if (params.getNetwork() != null) {
			network = registrar.getService(CyNetworkManager.class).getNetwork(params.getNetwork().getSUID());
			
			if (network == null) {
				mode = INTERRUPTION;
				throw new IllegalArgumentException("Cannot find network with SUID " + params.getNetwork() + ".");
			}
		} else {
			network = registrar.getService(CyApplicationManager.class).getCurrentNetwork();
			
			if (network == null) {
				mode = INTERRUPTION;
				throw new RuntimeException("You must set the current network first.");
			}
		}
		
		MCODEAlgorithm alg = null;
		
		if (network != null) {
			final MCODEParameters savedParams;
			
			if (mcodeUtil.containsNetworkAlgorithm(network.getSUID())) {
				alg = mcodeUtil.getNetworkAlgorithm(network.getSUID());
				savedParams = mcodeUtil.getParameterManager().getParamsCopy(network.getSUID());
			} else {
				alg = new MCODEAlgorithm(null, mcodeUtil);
				mcodeUtil.addNetworkAlgorithm(network.getSUID(), alg);
				savedParams = mcodeUtil.getParameterManager().getParamsCopy(null);
			}
		
			// In case the user selected selection scope we must make sure that they selected at least 1 node
			if (params.getScope() == MCODEAnalysisScope.SELECTION) {
				List<CyNode> nodes = network.getNodeList();
				List<Long> selectedNodes = new ArrayList<>();
		
				for (CyNode n : nodes) {
					if (network.getRow(n).get(CyNetwork.SELECTED, Boolean.class))
						selectedNodes.add(n.getSUID());
				}
		
				if (!selectedNodes.isEmpty()) {
					params.setSelectedNodes(selectedNodes.toArray(new Long[selectedNodes.size()]));
				} else {
					mode = INTERRUPTION;
					throw new RuntimeException("You must select ONE OR MORE NODES for scope 'SELECTION'.");
				}
			} else {
				resultId = resultsMgr.getNextResultId();
				params.setNetwork(network);
				
				if (mode == FIRST_TIME || action.isDirty(network)
					|| params.getIncludeLoops() != savedParams.getIncludeLoops()
					|| params.getDegreeCutoff() != savedParams.getDegreeCutoff()) {
					mode = RESCORE;
					mcodeUtil.getParameterManager().setParams(params, resultId, network);
				} else {
					mode = REFIND;
					mcodeUtil.getParameterManager().setParams(params, resultId, network);
				}
			}
		}
		
		action.setMode(mode);
		
		if (mode != INTERRUPTION) {
			// Run MCODE
			MCODEAnalyzeTask analyzeTask = new MCODEAnalyzeTask(network, mode, resultId, alg, resultsMgr, mcodeUtil);
			ShowClustersTask showClustersTask = new ShowClustersTask(analyzeTask);
			
			insertTasksAfterCurrentTask(showClustersTask);
			insertTasksAfterCurrentTask(analyzeTask);
		}
	}
	
	private class ShowClustersTask extends AbstractTask {

		private MCODEAnalyzeTask analysisTask;
		
		public ShowClustersTask(MCODEAnalyzeTask analysisTask) {
			this.analysisTask = analysisTask;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void run(TaskMonitor tm) throws Exception {
			action.setDirty(params.getNetwork(), false);
			
			List<MCODECluster> clusters = (List<MCODECluster>) analysisTask.getResults(List.class);
			
			if (clusters != null && !clusters.isEmpty())
				resultsMgr.addResult(params.getNetwork().getSUID(), clusters);
				
			mcodeUtil.disposeUnusedNetworks(clusters);
		}
	}
}
