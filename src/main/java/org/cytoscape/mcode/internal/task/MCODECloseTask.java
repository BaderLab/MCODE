package org.cytoscape.mcode.internal.task;

import java.util.Collection;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.mcode.internal.view.MCODEMainPanel;
import org.cytoscape.mcode.internal.view.MCODEResultsPanel;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/**
 * Closes the app panels.
 */
public class MCODECloseTask implements Task {

	private final CySwingApplication swingApplication;
	private final CyServiceRegistrar registrar;
	private final MCODEUtil mcodeUtil;
	
	public MCODECloseTask(final CySwingApplication swingApplication,
						  final CyServiceRegistrar registrar,
						  final MCODEUtil mcodeUtil) {
		this.swingApplication = swingApplication;
		this.registrar = registrar;
		this.mcodeUtil = mcodeUtil;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		if (mcodeUtil.isOpened()) {
			int result = JOptionPane.YES_OPTION;
			Collection<MCODEResultsPanel> resultPanels = mcodeUtil.getResultPanels();

			if (resultPanels.size() > 0) {
				String message = "You are about to close the MCODE app.\nDo you wish to continue?";
				result = JOptionPane.showOptionDialog(swingApplication.getJFrame(),
													  new Object[] { message },
													  "Close MCODE",
													  JOptionPane.YES_NO_OPTION,
													  JOptionPane.QUESTION_MESSAGE,
													  null,
													  null,
													  null);
				if (result == JOptionPane.YES_OPTION) {
					for (MCODEResultsPanel panel : resultPanels) {
						panel.discard(false);
					}

					CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.WEST);

					if (cytoPanel.getCytoPanelComponentCount() == 0) {
						cytoPanel.setState(CytoPanelState.HIDE);
					}
				}
			}

			if (result == JOptionPane.YES_OPTION) {
				MCODEMainPanel mainPanel = mcodeUtil.getMainPanel();
	
				if (mainPanel != null) {
					registrar.unregisterService(mainPanel, CytoPanelComponent.class);
				}
	
				mcodeUtil.reset();
			}
		}
	}

	@Override
	public void cancel() {
		// Do nothing
	}
}
