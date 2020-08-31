package ca.utoronto.tdccbr.mcode.internal.action;

import static ca.utoronto.tdccbr.mcode.internal.util.ViewUtil.invokeOnEDT;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedEdgesListener;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedNodesListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEAlgorithm;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEAnalysisScope;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.task.MCODEAnalyzeTaskFactory;
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
 * * Date: May 5, 2004
 * * Time: 8:46:19 PM
 * * Description: simple score and find action for MCODE
 */

/**
 * Simple score and find action for MCODE. This should be the default for general users.
 */
@SuppressWarnings("serial")
public class AnalysisAction extends AbstractMCODEAction implements SetCurrentNetworkListener, AddedNodesListener,
		AddedEdgesListener, RemovedNodesListener, RemovedEdgesListener {

	public final static int FIRST_TIME = 0;
	public final static int RESCORE = 1;
	public final static int REFIND = 2;
	public final static int INTERRUPTION = 3;

	private int mode = FIRST_TIME;
	private Map<Long/*network_suid*/, Boolean> dirtyNetworks;
	
	private final MCODEResultsManager resultsMgr;
	private final MCODEUtil mcodeUtil;
	private final CyServiceRegistrar registrar;
	
	private static final Logger logger = LoggerFactory.getLogger(AnalysisAction.class);

	public AnalysisAction(
			String title,
			MCODEResultsManager resultsMgr,
			MCODEUtil mcodeUtil,
			CyServiceRegistrar registrar
	) {
		super(title, ActionEnableSupport.ENABLE_FOR_NETWORK, registrar);
		this.resultsMgr = resultsMgr;
		this.mcodeUtil = mcodeUtil;
		this.registrar = registrar;
		dirtyNetworks = new HashMap<>();
	}

	/**
	 * This method is called when the user clicks Analyze.
	 *
	 * @param evt Click of the analyzeButton on the NewAnalysisPanel.
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		// Get the selected network
		var network = applicationManager.getCurrentNetwork();

		// This should never happen, because the action should be disabled,
		// but let's keep this extra check anyway 
		if (network == null) {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(), "You must have a network to run this app.");
			return;
		}

		// MCODE needs a network of at least 1 node
		if (network.getNodeCount() < 1) {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(),
										  "The analysis cannot be performed on an empty network.",
										  "Analysis Interrupted",
										  JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		var taskObserver = new TaskObserver() {
			
			MCODEResult result = null;
			
			@Override
			public void taskFinished(ObservableTask task) {
				if (task.getResultClasses().contains(MCODEResult.class))
					result = task.getResults(MCODEResult.class);
			}
			
			@Override
			public void allFinished(FinishStatus finishStatus) {
				// Callback that should be executed after the analysis is done...
				setDirty(network, false);

				new Thread(() -> {
					mcodeUtil.disposeUnusedNetworks(resultsMgr.getAllResults());
				}).start();
				
				// Display clusters in a new modal dialog box
				if (finishStatus == FinishStatus.getSucceeded()) {
					if (result == null || result.getClusters().isEmpty())
						invokeOnEDT(() -> {
							JOptionPane.showMessageDialog(swingApplication.getJFrame(),
														  "No clusters were found.\n"
																  + "You can try changing the MCODE parameters or\n"
																  + "modifying your node selection if you are using\n"
																  + "a selection-specific scope.",
														  "No Results",
														  JOptionPane.WARNING_MESSAGE);
						});
				}
			}
		};
		
		execute(network, taskObserver);
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		updateEnableState();
	}

	@Override
	public void handleEvent(RemovedEdgesEvent e) {
		setDirty(e.getSource(), true);
	}

	@Override
	public void handleEvent(RemovedNodesEvent e) {
		setDirty(e.getSource(), true);
	}

	@Override
	public void handleEvent(AddedEdgesEvent e) {
		setDirty(e.getSource(), true);
	}

	@Override
	public void handleEvent(AddedNodesEvent e) {
		setDirty(e.getSource(), true);
	}
	
	private void execute(CyNetwork network, TaskObserver taskObserver) {
		int resultId = resultsMgr.getNextResultId();
		var currentParams = mcodeUtil.getParameterManager().getLiveParams().copy();
		var nodes = network.getNodeList();
		var selectedNodes = new ArrayList<Long>();

		for (CyNode n : nodes) {
			if (network.getRow(n).get(CyNetwork.SELECTED, Boolean.class))
				selectedNodes.add(n.getSUID());
		}

		var selectedNodesRGI = selectedNodes.toArray(new Long[selectedNodes.size()]);
		currentParams.setSelectedNodes(selectedNodesRGI);

		final MCODEAlgorithm alg;
		MCODEParameters savedParams = null;

		// Here we determine if we have already run mcode on this network before
		// if we have then we use the stored alg class and the last saved parameters
		// of that network (so as to determine if rescoring/refinding is required for
		// this network without interference by parameters of other networks)
		// otherwise we construct a new alg class
		if (mcodeUtil.containsNetworkAlgorithm(network.getSUID())) {
			alg = mcodeUtil.getNetworkAlgorithm(network.getSUID());
			// Get a copy of the last saved parameters for comparison with the current ones
			savedParams = mcodeUtil.getParameterManager().getNetworkParams(network.getSUID());
		} else {
			alg = new MCODEAlgorithm(null, mcodeUtil);
			mcodeUtil.addNetworkAlgorithm(network.getSUID(), alg);
			mode = FIRST_TIME;
		}

		var interruptedMessage = "";
		
		// These statements determine which portion of the algorithm needs to be conducted by
		// testing which parameters have been modified compared to the last saved parameters.
		// Here we ensure that only relevant parameters are looked at.  For example, fluff density
		// parameter is irrelevant if fluff is not used in the current parameters.  Also, none of
		// the clustering parameters are relevant if the optimization is used
		if (mode == FIRST_TIME || isDirty(network)
				|| (savedParams != null
						&& (currentParams.getIncludeLoops() != savedParams.getIncludeLoops()
						|| currentParams.getDegreeCutoff() != savedParams.getDegreeCutoff()))) {
			mode = RESCORE;
			logger.debug("Analysis: score network, find clusters");
			mcodeUtil.getParameterManager().setParams(currentParams, resultId, network);
		} else {
			var res = resultsMgr.getFreshResult(network.getSUID(), currentParams);
			
			if (res != null) {
				mode = INTERRUPTION;
				interruptedMessage =
						"The parameters you specified have not changed\n"
						+ "(you used the same parameters in analysis #" + res.getId() + ").";
			} else {
				mode = REFIND;
				logger.debug("Analysis: find clusters");
				mcodeUtil.getParameterManager().setParams(currentParams, resultId, network);
			}
		}
		
		// In case the user selected selection scope we must make sure that they selected at least 1 node
		if (currentParams.getScope() == MCODEAnalysisScope.SELECTION &&
			currentParams.getSelectedNodes().length < 1) {
			mode = INTERRUPTION;
			interruptedMessage = "You must first SELECT one or more NODES for this scope.";
		}

		if (mode == INTERRUPTION) {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(),
										  interruptedMessage,
										  "Analysis Interrupted",
										  JOptionPane.WARNING_MESSAGE);
		} else {
			// Run MCODE
			var tf = new MCODEAnalyzeTaskFactory(network, mode, resultId, alg, resultsMgr, mcodeUtil);
			registrar.getService(DialogTaskManager.class).execute(tf.createTaskIterator(), taskObserver);
		}
	}

	public void setDirty(CyNetwork net, boolean dirty) {
		if (mcodeUtil.containsNetworkAlgorithm(net.getSUID())) {
			if (dirty)
				dirtyNetworks.put(net.getSUID(), dirty);
			else
				dirtyNetworks.remove(net.getSUID());
		}
	}
	
	public int getMode() {
		return mode;
	}
	
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	/**
	 * @param net
	 * @return true if the network has been modified after the last analysis.
	 */
	private boolean isDirty(CyNetwork net) {
		return Boolean.TRUE.equals(dirtyNetworks.get(net.getSUID()));
	}
}
