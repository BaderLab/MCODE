package org.cytoscape.mcode.internal;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Set;

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
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;

/**
 * Closes the app panels.
 */
public class MCODECloseAction extends AbstractMCODEAction implements NetworkAboutToBeDestroyedListener {

	private static final long serialVersionUID = -8309835257402089360L;

	private final CyServiceRegistrar registrar;
	private final MCODEUtil mcodeUtil;

	public MCODECloseAction(final String name,
							final CyApplicationManager applicationManager,
							final CySwingApplication swingApplication,
							final CyNetworkViewManager netViewManager,
							final CyServiceRegistrar registrar,
							final MCODEUtil mcodeUtil) {
		super(name, applicationManager, swingApplication, netViewManager);
		this.registrar = registrar;
		this.mcodeUtil = mcodeUtil;
		setPreferredMenu("Apps.MCODE");
	}

	/**
	 * This method is called when the user wants to close MCODE.
	 * @param event Menu Item Selected.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		//First we must make sure that the app is opened
		if (isOpened()) {
			Collection<MCODEResultsPanel> resultPanels = getResultPanels();

			if (resultPanels.size() > 0) {
				String message = "You are about to close the MCODE app.\nDo you wish to continue?";
				int result = JOptionPane.showOptionDialog(swingApplication.getJFrame(),
														  new Object[] { message },
														  "Confirm",
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

	@Override
	public void handleEvent(final NetworkAboutToBeDestroyedEvent e) {
		if (isOpened()) {
			CyNetwork network = e.getNetwork();
			Set<Integer> resultIds = mcodeUtil.getNetworkResults(network.getSUID());

			for (int id : resultIds) {
				MCODEResultsPanel panel = getResultPanel(id);
				if (panel != null) panel.discard(false);
			}
		}
	}
}
