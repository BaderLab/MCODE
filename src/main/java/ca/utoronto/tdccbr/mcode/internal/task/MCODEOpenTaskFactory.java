package ca.utoronto.tdccbr.mcode.internal.task;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import ca.utoronto.tdccbr.mcode.internal.view.MainPanelMediator;

public class MCODEOpenTaskFactory implements TaskFactory {

	private final MainPanelMediator mediator;

	public MCODEOpenTaskFactory(MainPanelMediator mediator) {
		this.mediator = mediator;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MCODEOpenTask(mediator));
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
