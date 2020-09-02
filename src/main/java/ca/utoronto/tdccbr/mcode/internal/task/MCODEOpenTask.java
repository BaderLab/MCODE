package ca.utoronto.tdccbr.mcode.internal.task;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import ca.utoronto.tdccbr.mcode.internal.view.MainPanelMediator;

/**
 * Open the MCODE panel in the Control panel.
 */
public class MCODEOpenTask implements Task {

	private final MainPanelMediator mediator;
	
	public MCODEOpenTask(MainPanelMediator mediator) {
		this.mediator = mediator;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Open MCODE");
		tm.setStatusMessage("Opening MCODE Panel...");
		mediator.showMainPanel(true);
	}

	@Override
	public void cancel() {
		// Do nothing
	}
}
