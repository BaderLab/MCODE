package org.cytoscape.mcode.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.mcode.internal.view.MCODEMainPanel;
import org.cytoscape.mcode.internal.view.MCODEResultsPanel;
import org.cytoscape.view.model.CyNetworkViewManager;

public abstract class AbstractMCODEAction extends AbstractCyAction {

	private static final long serialVersionUID = 844755168181859513L;

	protected final CySwingApplication swingApplication;
	protected final CyApplicationManager applicationManager;
	protected final CyNetworkViewManager netViewManager;

	public AbstractMCODEAction(final String name,
							   final CyApplicationManager applicationManager,
							   final CySwingApplication swingApplication,
							   final CyNetworkViewManager netViewManager) {
		super(name, applicationManager, ActionEnableSupport.ENABLE_FOR_NETWORK, netViewManager);
		this.applicationManager = applicationManager;
		this.swingApplication = swingApplication;
		this.netViewManager = netViewManager;
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
		return swingApplication.getCytoPanel(CytoPanelName.EAST);
	}

	/**
	 * @return The main panel of the app if it is opened, and null otherwise
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
	 * @return The result panels of the app if it is opened, or an empty collection otherwise
	 */
	protected Collection<MCODEResultsPanel> getResultPanels() {
		Collection<MCODEResultsPanel> panels = new ArrayList<MCODEResultsPanel>();
		CytoPanel cytoPanel = getResultsCytoPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			if (cytoPanel.getComponentAt(i) instanceof MCODEResultsPanel)
				panels.add((MCODEResultsPanel) cytoPanel.getComponentAt(i));
		}

		return panels;
	}

	protected MCODEResultsPanel getResultPanel(final int resultId) {
		for (MCODEResultsPanel panel : getResultPanels()) {
			if (panel.getResultId() == resultId) return panel;
		}

		return null;
	}

	/**
	 * @return true if the app is opened and false otherwise
	 */
	protected boolean isOpened() {
		return getMainPanel() != null;
	}
}
