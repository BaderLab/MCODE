package org.cytoscape.mcode.internal;

import java.awt.event.ActionEvent;
import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.mcode.internal.view.MCODEMainPanel;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyApplicationManager;

/**
 * Creates a new menu item under Plugins menu section.
 */
public class MCODEOpenAction extends AbstractMCODEAction {

	private static final long serialVersionUID = 3521389398662580589L;

	private final CyServiceRegistrar registrar;
	private final MCODEAnalyzeAction analyzeAction;

	public MCODEOpenAction(final String name,
						   final CyApplicationManager applicationManager,
						   final CySwingApplication swingApplication,
						   final CyServiceRegistrar registrar,
						   final MCODEAnalyzeAction analyzeAction) {
		super(name, applicationManager, swingApplication);
		this.registrar = registrar;
		this.analyzeAction = analyzeAction;
		setPreferredMenu("Plugins.MCODE");
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

			// First we must make sure that the plugin is not already open
			if (!isOpened()) {
				mainPanel = new MCODEMainPanel(swingApplication);
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
