package org.cytoscape.mcode.internal.task;

import java.util.Set;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.mcode.internal.view.MCODEResultsPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class MCODECloseTaskFactory implements TaskFactory, NetworkAboutToBeDestroyedListener {
	
	private final CySwingApplication swingApplication;
	private final CyServiceRegistrar registrar;
	private final MCODEUtil mcodeUtil;
	
	public MCODECloseTaskFactory(final CySwingApplication swingApplication,
								 final CyServiceRegistrar registrar,
								 final MCODEUtil mcodeUtil) {
		this.swingApplication = swingApplication;
		this.registrar = registrar;
		this.mcodeUtil = mcodeUtil;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MCODECloseTask(swingApplication, registrar, mcodeUtil));
	}

	@Override
	public boolean isReady() {
		return mcodeUtil.isOpened();
	}
	
	@Override
	public void handleEvent(final NetworkAboutToBeDestroyedEvent e) {
		if (mcodeUtil.isOpened()) {
			CyNetwork network = e.getNetwork();
			Set<Integer> resultIds = mcodeUtil.getNetworkResults(network.getSUID());

			for (int id : resultIds) {
				MCODEResultsPanel panel = mcodeUtil.getResultPanel(id);
				if (panel != null) panel.discard(false);
			}
		}
	}
}
