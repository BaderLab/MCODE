package org.cytoscape.mcode.internal;

import java.awt.event.ActionEvent;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.mcode.internal.view.MCODEMainPanel;
import org.cytoscape.service.util.CyServiceRegistrar;

/**
 * Creates a new menu item under Apps menu section.
 */
public class MCODEOpenAction extends AbstractMCODEAction {

	private static final long serialVersionUID = 3521389398662580589L;

	private final CyServiceRegistrar registrar;
	private final MCODEAnalyzeAction analyzeAction;
	private final MCODEUtil mcodeUtil;

	public MCODEOpenAction(final String name,
						   final CyApplicationManager applicationManager,
						   final CySwingApplication swingApplication,
						   final CyServiceRegistrar registrar,
						   final MCODEAnalyzeAction analyzeAction,
						   final MCODEUtil mcodeUtil) {
		super(name, applicationManager, swingApplication);
		this.registrar = registrar;
		this.analyzeAction = analyzeAction;
		this.mcodeUtil = mcodeUtil;
		setPreferredMenu("Apps.MCODE");
	}

	/**
	 * This method is called when the user wants to start MCODE.
	 * @param event Menu Item Selected.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		// Display MCODEMainPanel in left cytopanel
		synchronized (this) {
			MCODEMainPanel mainPanel = null;

			// First we must make sure that the app is not already open
			if (!isOpened()) {
				mainPanel = new MCODEMainPanel(swingApplication, mcodeUtil);
				mainPanel.addAction(analyzeAction);

				registrar.registerService(mainPanel, CytoPanelComponent.class, new Properties());
				analyzeAction.updateEnableState();
			} else {
				mainPanel = getMainPanel();
			}

			if (mainPanel != null) {
				CytoPanel cytoPanel = getControlCytoPanel();
				int index = cytoPanel.indexOfComponent(mainPanel);
				cytoPanel.setSelectedIndex(index);
			}
		}
	}

	@Override
	public void updateEnableState() {
		setEnabled(!isOpened());
	}
}
