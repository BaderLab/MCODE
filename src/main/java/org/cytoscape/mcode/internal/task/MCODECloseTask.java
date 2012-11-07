package org.cytoscape.mcode.internal.task;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.mcode.internal.view.MCODEMainPanel;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/**
 * Closes the main MCODE panel.
 */
public class MCODECloseTask implements Task {

	private final MCODECloseAllResultsTask closeAllResultsTask;
	private final CyServiceRegistrar registrar;
	private final MCODEUtil mcodeUtil;
	
	public MCODECloseTask(final MCODECloseAllResultsTask closeAllResultsTask,
						  final CyServiceRegistrar registrar,
			  			  final MCODEUtil mcodeUtil) {
		this.closeAllResultsTask = closeAllResultsTask;
		this.registrar = registrar;
		this.mcodeUtil = mcodeUtil;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		if (closeAllResultsTask == null || closeAllResultsTask.close) {
			MCODEMainPanel mainPanel = mcodeUtil.getMainPanel();

			if (mainPanel != null) {
				registrar.unregisterService(mainPanel, CytoPanelComponent.class);
			}

			mcodeUtil.reset();
		}
	}

	@Override
	public void cancel() {
		// Do nothing
	}
}
