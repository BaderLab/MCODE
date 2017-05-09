package ca.utoronto.tdccbr.mcode.internal.rest;

import static ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction.FIRST_TIME;
import static ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction.INTERRUPTION;
import static ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction.REFIND;
import static ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction.RESCORE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ci.model.CIError;
import org.cytoscape.ci.model.CIResponse;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskObserver;

import ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAnalysisScope;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
import ca.utoronto.tdccbr.mcode.internal.task.MCODEAnalyzeTaskFactory;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * Allows a cyREST client to run MCODE.  
 */
@Api
@Path("/mcode/v1")
public class MCODEAnalysisResource {
	
	private final MCODEAnalyzeAction action;
	private final MCODEUtil mcodeUtil;
	private final CyServiceRegistrar serviceRegistrar;
	
	public MCODEAnalysisResource(MCODEAnalyzeAction action, MCODEUtil mcodeUtil, CyServiceRegistrar serviceRegistrar) {
		this.action = action;
		this.mcodeUtil = mcodeUtil;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Path("analyze")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CIResponse<MCODEResults> analyze(
    		@ApiParam(name = "params", value = "Input parameters", required = false)
    		MCODEParameters params
    ) {
		System.out.println("\n" + params);
		CIResponse<MCODEResults> response = new CIResponse<>();
		String errorMsg = null;
		int errorStatus = -1;
		
		if (params == null) // Use defaults
			params = new MCODEParameters();
		
		int mode = action.getMode();
		
		// Get requested network or the current one
		CyNetwork network = null;
		
		if (params.getNetworkSUID() != null) {
			network = serviceRegistrar.getService(CyNetworkManager.class).getNetwork(params.getNetworkSUID());
			
			if (network == null) {
				mode = INTERRUPTION;
				errorStatus = 1;
				errorMsg = "Cannot find network with SUID " + params.getNetworkSUID() + ".";
			}
		} else {
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
			
			if (network == null) {
				mode = INTERRUPTION;
				errorStatus = 2;
				errorMsg = "You must set the current network first.";
			}
		}
		
		int resultId = -1;
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
					errorStatus = 3;
					errorMsg = "You must select ONE OR MORE NODES for scope 'SELECTION'.";
				}
			} else {
				resultId = mcodeUtil.getNextResultId();
				
				if (mode == FIRST_TIME || action.isDirty(network)
					|| params.isIncludeLoops() != savedParams.isIncludeLoops()
					|| params.getDegreeCutoff() != savedParams.getDegreeCutoff()) {
					mode = RESCORE;
					mcodeUtil.getParameterManager().setParams(params, resultId, network.getSUID());
				} else {
					mode = REFIND;
					mcodeUtil.getParameterManager().setParams(params, resultId, network.getSUID());
				}
			}
		}
		
		action.setMode(mode);
		
		if (mode == INTERRUPTION) {
			CIError error = new CIError();
			error.status = errorStatus; // TODO
			error.message = errorMsg;
			
			response.errors = Collections.singletonList(error);
		} else {
			// Run MCODE
			RestTaskObserver observer = new RestTaskObserver(network, resultId);
			MCODEAnalyzeTaskFactory tf = new MCODEAnalyzeTaskFactory(network, mode, resultId, alg, mcodeUtil);
			serviceRegistrar.getService(SynchronousTaskManager.class).execute(tf.createTaskIterator(), observer);
			
			response.data = new MCODEResults(resultId, observer.getClusters());
		}
		
		return response;
	}
	
	private class RestTaskObserver implements TaskObserver {
		
		private final int resultId;
		private final CyNetwork network;
		private List<MCODECluster> clusters;
		public boolean finished;
		
		public RestTaskObserver(CyNetwork network, int resultId) {
			this.resultId = resultId;
			this.network = network;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void taskFinished(ObservableTask task) {
			clusters = task.getResults(List.class);
		}
		
		@Override
		public void allFinished(FinishStatus finishStatus) {
			// Callbak that should be executed after the analysis is done...
			action.setDirty(network, false);

			// Display clusters in a new modal dialog box
			if (finishStatus == FinishStatus.getSucceeded()) {
				if (clusters != null && !clusters.isEmpty()) {
					mcodeUtil.addResult(network.getSUID(), clusters);
					action.showResultsPanel(network, resultId, clusters);
				}
				
				finished = true;
			}

			mcodeUtil.disposeUnusedNetworks();
		}
		
		public boolean isFinished() {
			return finished;
		}
		
		public List<MCODECluster> getClusters() {
			return clusters;
		}
	}
}
