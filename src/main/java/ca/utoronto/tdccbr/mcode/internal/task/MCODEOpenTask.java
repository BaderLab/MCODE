package ca.utoronto.tdccbr.mcode.internal.task;

import java.util.Properties;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import ca.utoronto.tdccbr.mcode.internal.action.AnalysisAction;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEMainPanel;
import ca.utoronto.tdccbr.mcode.internal.view.MainPanelMediator;

/**
 * Open the MCODE panel in the Control panel.
 */
public class MCODEOpenTask implements Task {

	private final CyServiceRegistrar registrar;
	private final MainPanelMediator mediator;
	private final AnalysisAction analysisAction;
	
	public MCODEOpenTask(MainPanelMediator mediator, AnalysisAction analysisAction, CyServiceRegistrar registrar) {
		this.mediator = mediator;
		this.registrar = registrar;
		this.analysisAction = analysisAction;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Open MCODE");
		
		synchronized (this) {
			MCODEMainPanel mainPanel = mediator.getMainPanel();
			
			// First we must make sure that the app is not already open
			if (!mediator.isMainPanelOpen()) {
				tm.setStatusMessage("Opening MCODE Panel...");
				registrar.registerService(mainPanel, CytoPanelComponent.class, new Properties());
				analysisAction.updateEnableState();
			}

			mediator.selectMainPanel();
			
			if (mainPanel.getResultsCount() == 0)
				mediator.showNewAnalysisDialog();
		}
	}

	@Override
	public void cancel() {
		// Do nothing
	}
}
