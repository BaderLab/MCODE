package org.cytoscape.mcode.internal.task;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.mcode.internal.MCODEAnalyzeAction;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.mcode.internal.view.MCODEMainPanel;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/**
 * Open the MCODE panel in the Control panel.
 */
public class MCODEOpenTask implements Task {

	private final CySwingApplication swingApplication;
	private final CyServiceRegistrar registrar;
	private final MCODEUtil mcodeUtil;
	private final MCODEAnalyzeAction analyzeAction;
	
	public MCODEOpenTask(final CySwingApplication swingApplication,
						 final CyServiceRegistrar registrar,
						 final MCODEUtil mcodeUtil,
						 final MCODEAnalyzeAction analyzeAction) {
		this.swingApplication = swingApplication;
		this.registrar = registrar;
		this.mcodeUtil = mcodeUtil;
		this.analyzeAction = analyzeAction;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Display MCODEMainPanel in left cytopanel
		synchronized (this) {
			MCODEMainPanel mainPanel = null;

			// First we must make sure that the app is not already open
			if (!mcodeUtil.isOpened()) {
				mainPanel = new MCODEMainPanel(swingApplication, mcodeUtil);
				mainPanel.addAction(analyzeAction);

				registrar.registerService(mainPanel, CytoPanelComponent.class, new Properties());
				analyzeAction.updateEnableState();
			} else {
				mainPanel = mcodeUtil.getMainPanel();
			}

			if (mainPanel != null) {
				CytoPanel cytoPanel = mcodeUtil.getControlCytoPanel();
				int index = cytoPanel.indexOfComponent(mainPanel);
				cytoPanel.setSelectedIndex(index);
			}
		}
	}

	@Override
	public void cancel() {
		// Do nothing
	}
}
