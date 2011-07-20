package org.cytoscape.mcode.internal;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.mcode.internal.model.MCODEAlgorithm;
import org.cytoscape.mcode.internal.model.MCODECurrentParameters;
import org.cytoscape.mcode.internal.model.MCODEParameterSet;
import org.cytoscape.mcode.internal.task.MCODEAnalyzeTask;
import org.cytoscape.mcode.internal.view.MCODEResultsPanel;
import org.cytoscape.mcode.internal.view.MCODEVisualStyle;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;

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
public class MCODEAnalyzeAction extends AbstractMCODEAction implements NetworkViewAddedListener,
		NetworkViewDestroyedListener {

	private static final long serialVersionUID = 87924889404093104L;

	public final static int FIRST_TIME = 0;
	public final static int RESCORE = 1;
	public final static int REFIND = 2;
	public final static int INTERRUPTION = 3;

	private final CyServiceRegistrar registrar;

	//Keeps track of networks (id is key) and their respective algorithms
	private Map<Long, MCODEAlgorithm> networkManager;

	private MCODEResultsPanel resultPanel;

	int analyze = FIRST_TIME;
	int resultId = 1;

	MCODEVisualStyle MCODEVS;

	public MCODEAnalyzeAction(final String title,
							  final CyApplicationManager applicationManager,
							  final CySwingApplication swingApplication,
							  final CyServiceRegistrar registrar) {
		super(title, applicationManager, swingApplication);
		this.registrar = registrar;
		enableFor = "networkAndView";
		networkManager = new HashMap<Long, MCODEAlgorithm>();
	}

	/**
	 * This method is called when the user clicks Analyze.
	 *
	 * @param event Click of the analyzeButton on the MCODEMainPanel.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		String callerID = "MCODEAnalyzeAction.actionPerformed";
		String interruptedMessage = "";

		// Get the selected network
		final View<CyNetwork> netView = applicationManager.getCurrentNetworkView();

		// This should never happen, because the action should be disabled,
		// but let's keep this extra check anyway 
		if (netView == null) {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(),
										  "You must have a network view to run this plugin.");
			return;
		}

		CyNetwork network = netView.getModel();

		// MCODE needs a network of at least 1 node
		if (network.getNodeCount() < 1) {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(),
										  "The analysis cannot be performed on an empty network.",
										  "Analysis Interrupted",
										  JOptionPane.WARNING_MESSAGE);
			return;
		}

		List<CyNode> nodes = network.getNodeList();
		List<Integer> selectedNodes = new ArrayList<Integer>();

		for (CyNode n : nodes) {
			if (n.getCyRow().get(CyNetwork.SELECTED, Boolean.class)) {
				selectedNodes.add(n.getIndex());
			}
		}

		Integer[] selectedNodesRGI = selectedNodes.toArray(new Integer[selectedNodes.size()]);

		MCODEParameterSet currentParamsCopy = getMainPanel().getCurrentParamsCopy();
		currentParamsCopy.setSelectedNodes(selectedNodesRGI);

		final MCODEAlgorithm alg;
		final MCODEParameterSet savedParamsCopy;

		// Here we determine if we have already run mcode on this network before
		// if we have then we use the stored alg class and the last saved parameters
		// of that network (so as to determine if rescoring/refinding is required for
		// this network without interference by parameters of other networks)
		// otherwise we construct a new alg class
		if (networkManager.containsKey(network.getSUID())) {
			alg = networkManager.get(network.getSUID());
			// Get a copy of the last saved parameters for comparison with the current ones
			savedParamsCopy = MCODECurrentParameters.getInstance().getParamsCopy(network.getSUID());
		} else {
			alg = new MCODEAlgorithm(null);
			savedParamsCopy = MCODECurrentParameters.getInstance().getParamsCopy(null);
			networkManager.put(network.getSUID(), alg);
			analyze = FIRST_TIME;
		}

		// These statements determine which portion of the algorithm needs to be conducted by
		// testing which parameters have been modified compared to the last saved parameters.
		// Here we ensure that only relavant parameters are looked at.  For example, fluff density
		// parameter is irrelevant if fluff is not used in the current parameters.  Also, none of
		// the clustering parameters are relevant if the optimization is used
		if (currentParamsCopy.isIncludeLoops() != savedParamsCopy.isIncludeLoops() ||
			currentParamsCopy.getDegreeCutoff() != savedParamsCopy.getDegreeCutoff() || analyze == FIRST_TIME) {
			analyze = RESCORE;
			System.err.println("Analysis: score network, find clusters");
			MCODECurrentParameters.getInstance().setParams(currentParamsCopy, resultId, network.getSUID());
		} else if (!currentParamsCopy.getScope().equals(savedParamsCopy.getScope()) ||
				   (!currentParamsCopy.getScope().equals(MCODEParameterSet.NETWORK) && currentParamsCopy
						   .getSelectedNodes() != savedParamsCopy.getSelectedNodes()) ||
				   currentParamsCopy.isOptimize() != savedParamsCopy.isOptimize() ||
				   (!currentParamsCopy.isOptimize() && (currentParamsCopy.getKCore() != savedParamsCopy.getKCore() ||
														currentParamsCopy.getMaxDepthFromStart() != savedParamsCopy
																.getMaxDepthFromStart() ||
														currentParamsCopy.isHaircut() != savedParamsCopy.isHaircut() ||
														currentParamsCopy.getNodeScoreCutoff() != savedParamsCopy
																.getNodeScoreCutoff() ||
														currentParamsCopy.isFluff() != savedParamsCopy.isFluff() || (currentParamsCopy
						   .isFluff() && currentParamsCopy.getFluffNodeDensityCutoff() != savedParamsCopy
						   .getFluffNodeDensityCutoff())))) {
			analyze = REFIND;
			System.err.println("Analysis: find clusters");
			MCODECurrentParameters.getInstance().setParams(currentParamsCopy, resultId, network.getSUID());
		} else {
			analyze = INTERRUPTION;
			interruptedMessage = "The parameters you specified\nhave not changed.";
			MCODECurrentParameters.getInstance().setParams(currentParamsCopy, resultId, network.getSUID());
		}

		// Finally we save the current parameters
		//MCODECurrentParameters.getInstance().setParams(currentParamsCopy, resultId, network.getIdentifier());

		// In case the user selected selection scope we must make sure that they selected at least 1 node
		if (currentParamsCopy.getScope().equals(MCODEParameterSet.SELECTION) &&
			currentParamsCopy.getSelectedNodes().length < 1) {
			analyze = INTERRUPTION;
			interruptedMessage = "You must select ONE OR MORE NODES\nfor this scope.";
		}

		if (analyze == INTERRUPTION) {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(),
										  interruptedMessage,
										  "Analysis Interrupted",
										  JOptionPane.WARNING_MESSAGE);
		} else {
			// Run MCODE
			MCODEAnalyzeTask analyzeTask = new MCODEAnalyzeTask(network, analyze, resultId, alg);
			// TODO:
			//			//Configure JTask
			//			JTaskConfig config = new JTaskConfig();
			//
			//			//Show Cancel/Close Buttons
			//			config.displayCancelButton(true);
			//			config.displayStatus(true);
			//
			//			//Execute Task via TaskManager
			//			//This automatically pops-open a JTask Dialog Box
			//			TaskManager.executeTask(analyzeTask, config);

			// Display clusters in a new modal dialog box
			if (analyzeTask.isCompletedSuccessfully()) {
				if (analyzeTask.getClusters().length > 0) {
					resultId++;
					resultPanel = new MCODEResultsPanel(analyzeTask.getClusters(), analyzeTask.getAlg(), network,
														analyzeTask.getImageList(), resultId);

					registrar.registerService(resultPanel, CytoPanelComponent.class, new Properties());
				} else {
					JOptionPane.showMessageDialog(swingApplication.getJFrame(),
												  "No clusters were found.\n"
														  + "You can try changing the MCODE parameters or\n"
														  + "modifying your node selection if you are using\n"
														  + "a selection-specific scope.",
												  "No Results",
												  JOptionPane.WARNING_MESSAGE);
				}
			}
		}

		//		//this if statemet ensures that the east cytopanel is not loaded if there are no results in it
		//		if (resultFound || (analyze == INTERRUPTION && cytoPanel.indexOfComponent(resultPanel) >= 0)) {
		//			//focus the result panel
		//			int index = cytoPanel.indexOfComponent(resultPanel);
		//			cytoPanel.setSelectedIndex(index);
		//			cytoPanel.setState(CytoPanelState.DOCK);
		//
		//			// Add the MCODE visual style but don't make it active by default.
		//			VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
		//			VisualStyle currentStyle = vmm.getVisualStyle();
		//			vmm.setVisualStyle(MCODEVS);
		//			vmm.setVisualStyle(currentStyle);
		//			vmm.applyAppearances();
		//		}
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		updateEnableState();
	}

	@Override
	public void handleEvent(NetworkViewDestroyedEvent e) {
		updateEnableState();
	}
}
