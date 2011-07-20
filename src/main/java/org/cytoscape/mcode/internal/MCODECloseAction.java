package org.cytoscape.mcode.internal;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.mcode.internal.model.MCODECurrentParameters;
import org.cytoscape.mcode.internal.view.MCODEMainPanel;
import org.cytoscape.mcode.internal.view.MCODEResultsPanel;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyApplicationManager;

/**
 * Closes the plugin panels.
 */
public class MCODECloseAction extends AbstractMCODEAction {

	private static final long serialVersionUID = -8309835257402089360L;

	private final CyServiceRegistrar registrar;

	public MCODECloseAction(final String name,
							final CyApplicationManager applicationManager,
							final CySwingApplication swingApplication,
							final CyServiceRegistrar registrar) {
		super(name, applicationManager, swingApplication);
		this.registrar = registrar;
		setPreferredMenu("Plugins.MCODE");
	}

	/**
	 * This method is called when the user wants to close MCODE.
	 * @param event Menu Item Selected.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.WEST);

		//First we must make sure that the plugin is opened
		if (isOpened()) {
			MCODEResultsPanel resultsPanel = getResultsPanel();

			if (resultsPanel != null) {
				String message = "You are about to close the MCODE plugin.\nDo you wish to continue?";
				int result = JOptionPane.showOptionDialog(swingApplication.getJFrame(),
														  new Object[] { message },
														  "Confirm",
														  JOptionPane.YES_NO_OPTION,
														  JOptionPane.QUESTION_MESSAGE,
														  null,
														  null,
														  null);
				if (result == JOptionPane.YES_OPTION) {
					int resultId = resultsPanel.getResultId();
					MCODECurrentParameters.removeResultParams(resultId);

					registrar.unregisterService(resultsPanel, CytoPanelComponent.class);

					if (cytoPanel.getCytoPanelComponentCount() == 0) {
						cytoPanel.setState(CytoPanelState.HIDE);
					}
				}
			}

			MCODEMainPanel mainPanel = getMainPanel();

			if (mainPanel != null) {
				registrar.unregisterService(mainPanel, CytoPanelComponent.class);
			}
		}
	}

	@Override
	public void updateEnableState() {
		setEnabled(isOpened());
	}
}
