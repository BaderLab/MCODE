package ca.utoronto.tdccbr.mcode.internal.task;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import ca.utoronto.tdccbr.mcode.internal.action.AnalysisAction;
import ca.utoronto.tdccbr.mcode.internal.view.MainPanelMediator;

public class MCODEOpenTaskFactory implements TaskFactory {

	private final MainPanelMediator mediator;
	private final AnalysisAction analysisAction;
	private final CyServiceRegistrar registrar;
	
	public MCODEOpenTaskFactory(
			MainPanelMediator mediator,
			AnalysisAction analysisAction,
			CyServiceRegistrar registrar
	) {
		this.mediator = mediator;
		this.analysisAction = analysisAction;
		this.registrar = registrar;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MCODEOpenTask(mediator, analysisAction, registrar));
	}

	@Override
	public boolean isReady() {
		return !mediator.isMainPanelOpen();
	}
}
