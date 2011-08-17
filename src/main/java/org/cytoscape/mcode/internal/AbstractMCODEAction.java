package org.cytoscape.mcode.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.mcode.internal.view.MCODEMainPanel;
import org.cytoscape.mcode.internal.view.MCODEResultsPanel;

public abstract class AbstractMCODEAction extends AbstractCyAction {

	private static final long serialVersionUID = 844755168181859513L;

	protected final CySwingApplication swingApplication;

	public AbstractMCODEAction(String name, CyApplicationManager applicationManager, CySwingApplication swingApplication) {
		super(name, applicationManager);
		this.swingApplication = swingApplication;
	}

	/**
	 * @return Cytoscape's control panel
	 */
	protected CytoPanel getControlCytoPanel() {
		return swingApplication.getCytoPanel(CytoPanelName.WEST);
	}

	/**
	 * @return Cytoscape's results panel
	 */
	protected CytoPanel getResultsCytoPanel() {
		return swingApplication.getCytoPanel(CytoPanelName.WEST);
	}

	/**
	 * @return The main panel of the plugin if it is opened, and null otherwise
	 */
	protected MCODEMainPanel getMainPanel() {
		CytoPanel cytoPanel = getControlCytoPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			if (cytoPanel.getComponentAt(i) instanceof MCODEMainPanel)
				return (MCODEMainPanel) cytoPanel.getComponentAt(i);
		}

		return null;
	}

	/**
	 * @return The results panel of the plugin if it is opened, and null otherwise
	 */
	protected MCODEResultsPanel getResultsPanel() {
		CytoPanel cytoPanel = getResultsCytoPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			if (cytoPanel.getComponentAt(i) instanceof MCODEResultsPanel)
				return (MCODEResultsPanel) cytoPanel.getComponentAt(i);
		}

		return null;
	}

	/**
	 * @return true if the plugin is opened and false otherwise
	 */
	protected boolean isOpened() {
		return getMainPanel() != null;
	}
}
