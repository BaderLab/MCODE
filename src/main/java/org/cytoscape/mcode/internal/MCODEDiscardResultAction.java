package org.cytoscape.mcode.internal;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.mcode.internal.view.MCODEResultsPanel;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;

public class MCODEDiscardResultAction extends AbstractMCODEAction {

	private static final long serialVersionUID = 304724069977183435L;

	public static final String REQUEST_USER_CONFIRMATION_COMMAND = "requestUserConfirmation";

	private final int resultId;
	private final CyServiceRegistrar registrar;
	private final MCODEUtil mcodeUtil;

	public MCODEDiscardResultAction(final String name,
									final int resultId,
									final CyApplicationManager applicationManager,
									final CySwingApplication swingApplication,
									final CyNetworkViewManager netViewManager,
									final CyServiceRegistrar registrar,
									final MCODEUtil mcodeUtil) {
		super(name, applicationManager, swingApplication, netViewManager);
		this.resultId = resultId;
		this.registrar = registrar;
		this.mcodeUtil = mcodeUtil;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		MCODEResultsPanel panel = getResultPanel(resultId);

		if (panel != null) {
			int resultId = panel.getResultId();
			Integer confirmed = JOptionPane.YES_OPTION;
			boolean requestUserConfirmation = Boolean.valueOf(getValue(REQUEST_USER_CONFIRMATION_COMMAND).toString());

			if (requestUserConfirmation) {
				// Must make sure the user wants to close this results panel
				String message = "You are about to dispose of Result " + resultId + ".\nDo you wish to continue?";
				confirmed = JOptionPane.showOptionDialog(swingApplication.getJFrame(),
														 new Object[] { message },
														 "Confirm",
														 JOptionPane.YES_NO_OPTION,
														 JOptionPane.QUESTION_MESSAGE,
														 null,
														 null,
														 null);
			}

			if (confirmed == JOptionPane.YES_OPTION) {
				registrar.unregisterService(panel, CytoPanelComponent.class);
				mcodeUtil.removeNetworkResult(resultId);
			}
		}

		final CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);

		// If there are no more tabs in the cytopanel then we hide it
		if (cytoPanel.getCytoPanelComponentCount() == 0) {
			cytoPanel.setState(CytoPanelState.HIDE);
		}

		if (getResultPanels().size() == 0) {
			// Reset the results cache
			mcodeUtil.reset();
		}
	}
}
