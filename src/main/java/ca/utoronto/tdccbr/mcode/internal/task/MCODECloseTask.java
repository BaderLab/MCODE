package ca.utoronto.tdccbr.mcode.internal.task;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEMainPanel;

/**
 * Closes the main MCODE panel.
 */
public class MCODECloseTask implements Task {

	private final MCODECloseAllResultsTask closeAllResultsTask;
	private final MCODEResultsManager resultsMgr;
	private final MCODEUtil mcodeUtil;
	private final CyServiceRegistrar registrar;
	
	public MCODECloseTask(
			MCODECloseAllResultsTask closeAllResultsTask,
			MCODEResultsManager resultsMgr,
			MCODEUtil mcodeUtil,
			CyServiceRegistrar registrar
	) {
		this.closeAllResultsTask = closeAllResultsTask;
		this.resultsMgr = resultsMgr;
		this.mcodeUtil = mcodeUtil;
		this.registrar = registrar;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		if (closeAllResultsTask == null || closeAllResultsTask.close) {
			MCODEMainPanel mainPanel = mcodeUtil.getMainPanel();

			if (mainPanel != null)
				registrar.unregisterService(mainPanel, CytoPanelComponent.class);

			resultsMgr.reset();
			mcodeUtil.reset();
		}
	}

	@Override
	public void cancel() {
		// Do nothing
	}
}
