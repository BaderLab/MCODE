package ca.utoronto.tdccbr.mcode.internal.action;

import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;

import ca.utoronto.tdccbr.mcode.internal.view.NewAnalysisPanel;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEMainPanel;

public abstract class AbstractMCODEAction extends AbstractCyAction {

	private static final long serialVersionUID = 844755168181859513L;

	protected final CySwingApplication swingApplication;
	protected final CyApplicationManager applicationManager;
	protected final CyNetworkViewManager netViewManager;
	protected final CyServiceRegistrar serviceRegistrar;

	public AbstractMCODEAction(String name, String enableFor, CyServiceRegistrar serviceRegistrar) {
		super(name, serviceRegistrar.getService(CyApplicationManager.class), enableFor,
				serviceRegistrar.getService(CyNetworkViewManager.class));
		
		this.applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		this.swingApplication = serviceRegistrar.getService(CySwingApplication.class);
		this.netViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		this.serviceRegistrar = serviceRegistrar;
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
	protected NewAnalysisPanel getMainPanel() {
		CytoPanel cytoPanel = getControlCytoPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			if (cytoPanel.getComponentAt(i) instanceof NewAnalysisPanel)
				return (NewAnalysisPanel) cytoPanel.getComponentAt(i);
		}

		return null;
	}

	/**
	 * @return The result panels of the app if it is opened, or an empty collection otherwise
	 */
	protected Collection<MCODEMainPanel> getResultPanels() {
		Collection<MCODEMainPanel> panels = new ArrayList<>();
		CytoPanel cytoPanel = getResultsCytoPanel();
		int count = cytoPanel.getCytoPanelComponentCount();

		for (int i = 0; i < count; i++) {
			if (cytoPanel.getComponentAt(i) instanceof MCODEMainPanel)
				panels.add((MCODEMainPanel) cytoPanel.getComponentAt(i));
		}

		return panels;
	}

	protected MCODEMainPanel getResultPanel(final int resultId) {
		for (MCODEMainPanel panel : getResultPanels()) {
			if (panel.getSelectedResult() != null && panel.getSelectedResult().getId() == resultId)
				return panel;
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
