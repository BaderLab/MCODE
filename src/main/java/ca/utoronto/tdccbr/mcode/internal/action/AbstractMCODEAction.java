package ca.utoronto.tdccbr.mcode.internal.action;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;

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
}
