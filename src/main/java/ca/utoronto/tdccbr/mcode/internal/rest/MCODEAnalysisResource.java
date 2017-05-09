package ca.utoronto.tdccbr.mcode.internal.rest;

import static ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil.invokeOnEDTAndWait;

import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

import ca.utoronto.tdccbr.mcode.internal.MCODEAnalyzeAction;
import ca.utoronto.tdccbr.mcode.internal.MCODEDiscardResultAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEResultsPanel;
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
    public MCODEResults analyze(
    		@ApiParam(name = "params", value = "Input parameters", required = false)
    		MCODEParameters params
    ) {
		System.out.println("\n===> MCODEAnalysisResource.getClusters:\n" + params);
		
		if (params == null) // Use defaults
			params = new MCODEParameters();
		
		// Get requested network or the current one
		final CyNetwork network;
		
		if (params.getNetworkSUID() != null)
			network = serviceRegistrar.getService(CyNetworkManager.class).getNetwork(params.getNetworkSUID());
		else
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
		
		final int resultId = mcodeUtil.getNextResultId();
		
//		List<CyNode> nodes = network.getNodeList();
//		List<Long> selectedNodes = new ArrayList<>();
//
//		for (CyNode n : nodes) {
//			if (network.getRow(n).get(CyNetwork.SELECTED, Boolean.class)) {
//				selectedNodes.add(n.getSUID());
//			}
//		}
//
//		Long[] selectedNodesRGI = selectedNodes.toArray(new Long[selectedNodes.size()]);
//
//		MCODEParameters currentParamsCopy = getMainPanel().getCurrentParamsCopy();
//		currentParamsCopy.setSelectedNodes(selectedNodesRGI);
//		
//		final MCODEAlgorithm alg;
//		final MCODEParameters savedParamsCopy;
//		int analyze = FIRST_TIME;
//		
//		if (mcodeUtil.containsNetworkAlgorithm(network.getSUID())) {
//			alg = mcodeUtil.getNetworkAlgorithm(network.getSUID());
//			savedParamsCopy = mcodeUtil.getParameterManager().getParamsCopy(network.getSUID());
//		} else {
//			alg = new MCODEAlgorithm(null, mcodeUtil);
//			mcodeUtil.addNetworkAlgorithm(network.getSUID(), alg);
//			savedParamsCopy = mcodeUtil.getParameterManager().getParamsCopy(null);
//		}
//		
//		// These statements determine which portion of the algorithm needs to be conducted by
//		// testing which parameters have been modified compared to the last saved parameters.
//		// Here we ensure that only relevant parameters are looked at.  For example, fluff density
//		// parameter is irrelevant if fluff is not used in the current parameters.  Also, none of
//		// the clustering parameters are relevant if the optimization is used
//		if (analyze == FIRST_TIME || isDirty(network)
//				|| currentParamsCopy.isIncludeLoops() != savedParamsCopy.isIncludeLoops()
//				|| currentParamsCopy.getDegreeCutoff() != savedParamsCopy.getDegreeCutoff()) {
//			analyze = RESCORE;
//			mcodeUtil.getParameterManager().setParams(currentParamsCopy, resultId, network.getSUID());
//		} else if (parametersChanged(savedParamsCopy, currentParamsCopy)) {
//			analyze = REFIND;
//			mcodeUtil.getParameterManager().setParams(currentParamsCopy, resultId, network.getSUID());
//		} else {
//			analyze = INTERRUPTION;
//			mcodeUtil.getParameterManager().setParams(currentParamsCopy, resultId, network.getSUID());
//		}
//		
//		// Run MCODE
//		MCODEAnalyzeTaskFactory taskFactory = new MCODEAnalyzeTaskFactory(network, analyze, resultId, alg, mcodeUtil);
//		serviceRegistrar.getService(DialogTaskManager.class).execute(taskFactory.createTaskIterator());
		
		RestTaskObserver taskObserver = new RestTaskObserver(network, resultId);
		action.execute(network, resultId, params, taskObserver);
		
		while (!taskObserver.isFinished()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return new MCODEResults(resultId, taskObserver.getClusters());
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

					invokeOnEDTAndWait(() -> {
						MCODEDiscardResultAction discardResultAction = 
								new MCODEDiscardResultAction("Discard Result", resultId, mcodeUtil, serviceRegistrar);

						// TODO
						final CyNetworkView networkView = serviceRegistrar.getService(CyApplicationManager.class)
								.getCurrentNetworkView();
						
						MCODEResultsPanel resultsPanel = new MCODEResultsPanel(clusters, mcodeUtil, network,
								networkView, resultId, discardResultAction);
						
						serviceRegistrar.registerService(resultsPanel, CytoPanelComponent.class, new Properties());
						
						// Focus the result panel
						CytoPanel cytoPanel = serviceRegistrar.getService(CySwingApplication.class)
								.getCytoPanel(CytoPanelName.EAST);
						int index = cytoPanel.indexOfComponent(resultsPanel);
						cytoPanel.setSelectedIndex(index);

						if (cytoPanel.getState() == CytoPanelState.HIDE)
							cytoPanel.setState(CytoPanelState.DOCK);
					});
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
