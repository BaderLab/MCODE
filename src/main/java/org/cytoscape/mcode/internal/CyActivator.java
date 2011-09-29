package org.cytoscape.mcode.internal;

import org.cytoscape.work.TaskManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.util.swing.OpenBrowser;

import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.mcode.internal.MCODEVisualStyleAction;
import org.cytoscape.mcode.internal.MCODEAboutAction;
import org.cytoscape.mcode.internal.MCODEOpenAction;
import org.cytoscape.mcode.internal.MCODEAnalyzeAction;
import org.cytoscape.mcode.internal.MCODECloseAction;
import org.cytoscape.mcode.internal.MCODEHelpAction;

import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.model.events.NetworkDestroyedListener;

import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	@SuppressWarnings("unchecked")
	public void start(BundleContext bc) {

		CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		CyNetworkViewManager networkViewMgr = getService(bc, CyNetworkViewManager.class);
		CyNetworkManager networkMgr = getService(bc, CyNetworkManager.class);
		TaskManager taskManager = getService(bc, TaskManager.class);
		
		CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
		CyRootNetworkFactory rootNetworkFactory = getService(bc, CyRootNetworkFactory.class);
		
		CySwingApplication swingApplication = getService(bc, CySwingApplication.class);
		RenderingEngineFactory<CyNetwork> dingRenderingEngineFactory = getService(bc, RenderingEngineFactory.class, "(id=ding)");
		CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		VisualStyleFactory visualStyleFactory = getService(bc, VisualStyleFactory.class);
		VisualMappingManager visualMappingMgr = getService(bc, VisualMappingManager.class);
		VisualMappingFunctionFactory discreteMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory continuousMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		
		FileUtil fileUtil = getService(bc, FileUtil.class);
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
		CyEventHelper eventHelper = getService(bc, CyEventHelper.class);
		
		MCODEUtil mcodeUtil = new MCODEUtil(dingRenderingEngineFactory, networkViewFactory, rootNetworkFactory,
											applicationManager, networkMgr, networkViewMgr, visualStyleFactory,
											visualMappingMgr, swingApplication, eventHelper, discreteMappingFactory,
											continuousMappingFactory, fileUtil);
		
		MCODEAnalyzeAction analyzeAction = new MCODEAnalyzeAction("Analyze current network", applicationManager, swingApplication, serviceRegistrar, taskManager, mcodeUtil);
		MCODEOpenAction openAction = new MCODEOpenAction("Open MCODE", applicationManager, swingApplication, serviceRegistrar, analyzeAction, mcodeUtil);
		MCODECloseAction closeAction = new MCODECloseAction("Close MCODE", applicationManager, swingApplication, serviceRegistrar, mcodeUtil);
		MCODEHelpAction helpAction = new MCODEHelpAction("Help", applicationManager, swingApplication, openBrowser);
		MCODEAboutAction aboutAction = new MCODEAboutAction("About", applicationManager, swingApplication, openBrowser, "${project.version}", "${buildDate}");
		MCODEVisualStyleAction visualStyleAction = new MCODEVisualStyleAction("Apply MCODE style", applicationManager, swingApplication, visualMappingMgr, mcodeUtil);
		
		registerService(bc, openAction, CyAction.class, new Properties());
		registerService(bc, closeAction, CyAction.class, new Properties());
		registerService(bc, closeAction, NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc, helpAction, CyAction.class, new Properties());
		registerService(bc, aboutAction, CyAction.class, new Properties());
		registerService(bc, analyzeAction, NetworkAddedListener.class, new Properties());
		registerService(bc, analyzeAction, NetworkDestroyedListener.class, new Properties());
		registerService(bc, visualStyleAction, CyAction.class, new Properties());
		registerService(bc, visualStyleAction, CytoPanelComponentSelectedListener.class, new Properties());
	}
}
