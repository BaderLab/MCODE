package ca.utoronto.tdccbr.mcode.internal.task;

import java.util.Collection;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEResultsPanel;

/**
 * Closes the result panels.
 */
public class MCODECloseAllResultsTask implements Task {

	@Tunable(description = "<html>You are about to close the MCODE app.<br />Do you want to continue?</html>", params="ForceSetDirectly=true")
	public boolean close = true;
	
	private final CySwingApplication swingApplication;
	private final MCODEUtil mcodeUtil;
	
	public MCODECloseAllResultsTask(final CySwingApplication swingApplication,
						  			final MCODEUtil mcodeUtil) {
		this.swingApplication = swingApplication;
		this.mcodeUtil = mcodeUtil;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Close MCODE";
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		if (close) {
			final Collection<MCODEResultsPanel> resultPanels = mcodeUtil.getResultPanels();
			
			for (MCODEResultsPanel panel : resultPanels) {
				panel.discard(false);
			}

			CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.WEST);

			if (cytoPanel.getCytoPanelComponentCount() == 0)
				cytoPanel.setState(CytoPanelState.HIDE);
		}
	}

	@Override
	public void cancel() {
		// Do nothing
	}
}
