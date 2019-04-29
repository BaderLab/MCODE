package ca.utoronto.tdccbr.mcode.internal.task;

import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.view.MainPanelMediator;

/**
 * Closes the result panels.
 */
public class MCODECloseAllResultsTask implements Task {

	@Tunable(description = "<html>You are about to close the MCODE app.<br />Do you want to continue?</html>", params="ForceSetDirectly=true")
	public boolean close = true;
	
	private final MainPanelMediator mediator;
	private final MCODEResultsManager resultsMgr;
	private final MCODEUtil mcodeUtil;
	
	public MCODECloseAllResultsTask(MainPanelMediator mediator, MCODEResultsManager resultsMgr, MCODEUtil mcodeUtil) {
		this.mediator = mediator;
		this.resultsMgr = resultsMgr;
		this.mcodeUtil = mcodeUtil;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Close MCODE";
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
		tm.setTitle(getTitle());
		tm.setProgress(-1);
		
		if (close) {
			tm.setStatusMessage("Discarding Results...");
			mediator.discardAllResults(false);
			resultsMgr.reset();
			mcodeUtil.reset();
		}
		
		tm.setProgress(1.0);
	}

	@Override
	public void cancel() {
		// Do nothing
	}
}
