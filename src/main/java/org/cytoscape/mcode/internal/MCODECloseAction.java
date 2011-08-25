package org.cytoscape.mcode.internal;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.mcode.internal.view.MCODEMainPanel;
import org.cytoscape.mcode.internal.view.MCODEResultsPanel;
import org.cytoscape.service.util.CyServiceRegistrar;

/**
 * Closes the plugin panels.
 */
public class MCODECloseAction extends AbstractMCODEAction {

	private static final long serialVersionUID = -8309835257402089360L;

	private final CyServiceRegistrar registrar;
	private final MCODEUtil mcodeUtil;

	public MCODECloseAction(final String name,
							final CyApplicationManager applicationManager,
							final CySwingApplication swingApplication,
							final CyServiceRegistrar registrar,
							final MCODEUtil mcodeUtil) {
		super(name, applicationManager, swingApplication);
		this.registrar = registrar;
		this.mcodeUtil = mcodeUtil;
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
			Collection<MCODEResultsPanel> resultPanels = getResultPanels();

			if (resultPanels.size() > 0) {
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
					for (MCODEResultsPanel p : resultPanels) {
						int resultId = p.getResultId();
						mcodeUtil.getCurrentParameters().removeResultParams(resultId);
						registrar.unregisterService(p, CytoPanelComponent.class);
					}

					if (cytoPanel.getCytoPanelComponentCount() == 0) {
						cytoPanel.setState(CytoPanelState.HIDE);
					}
				}
			}

			MCODEMainPanel mainPanel = getMainPanel();

			if (mainPanel != null) {
				registrar.unregisterService(mainPanel, CytoPanelComponent.class);
			}

			mcodeUtil.reset();
		}
	}

	@Override
	public void updateEnableState() {
		setEnabled(isOpened());
	}
}
