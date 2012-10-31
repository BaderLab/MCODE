package org.cytoscape.mcode.internal.task;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.mcode.internal.MCODEAnalyzeAction;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class MCODEOpenTaskFactory implements TaskFactory {

	private final CySwingApplication swingApplication;
	private final CyServiceRegistrar registrar;
	private final MCODEUtil mcodeUtil;
	private final MCODEAnalyzeAction analyzeAction;
	
	public MCODEOpenTaskFactory(final CySwingApplication swingApplication,
			 					final CyServiceRegistrar registrar,
			 					final MCODEUtil mcodeUtil,
			 					final MCODEAnalyzeAction analyzeAction) {
		this.swingApplication = swingApplication;
		this.registrar = registrar;
		this.mcodeUtil = mcodeUtil;
		this.analyzeAction = analyzeAction;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MCODEOpenTask(swingApplication, registrar, mcodeUtil, analyzeAction));
	}

	@Override
	public boolean isReady() {
		return !mcodeUtil.isOpened();
	}
}
