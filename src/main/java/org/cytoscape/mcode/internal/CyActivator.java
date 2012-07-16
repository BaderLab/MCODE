package org.cytoscape.mcode.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void start(BundleContext bc) {

		CyApplicationManager appMgr = getService(bc, CyApplicationManager.class);
		CyNetworkViewManager netViewMgr = getService(bc, CyNetworkViewManager.class);
		CyNetworkManager netMgr = getService(bc, CyNetworkManager.class);
		TaskManager<?, ?> taskMgr = getService(bc, TaskManager.class);
		
		CyNetworkViewFactory netViewFactory = getService(bc, CyNetworkViewFactory.class);
		CyRootNetworkManager rootNetworkMgr = getService(bc, CyRootNetworkManager.class);
		
		CySwingApplication swingApp = getService(bc, CySwingApplication.class);
		RenderingEngineFactory<CyNetwork> dingRenderingEngineFactory = getService(bc, RenderingEngineFactory.class, "(id=ding)");
		CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		VisualStyleFactory visualStyleFactory = getService(bc, VisualStyleFactory.class);
		VisualMappingManager visualMappingMgr = getService(bc, VisualMappingManager.class);
		VisualMappingFunctionFactory discreteMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory continuousMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		
		FileUtil fileUtil = getService(bc, FileUtil.class);
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
		CyEventHelper eventHelper = getService(bc, CyEventHelper.class);
		
		MCODEUtil mcodeUtil = new MCODEUtil(dingRenderingEngineFactory, netViewFactory, rootNetworkMgr,
											appMgr, netMgr, netViewMgr, visualStyleFactory,
											visualMappingMgr, swingApp, eventHelper, discreteMappingFactory,
											continuousMappingFactory, fileUtil);
		
		MCODEAnalyzeAction analyzeAction = new MCODEAnalyzeAction("Analyze current network", appMgr, swingApp, netViewMgr, serviceRegistrar, taskMgr, mcodeUtil);
		MCODEOpenAction openAction = new MCODEOpenAction("Open MCODE", appMgr, swingApp, netViewMgr, serviceRegistrar, analyzeAction, mcodeUtil);
		MCODECloseAction closeAction = new MCODECloseAction("Close MCODE", appMgr, swingApp, netViewMgr, serviceRegistrar, mcodeUtil);
		MCODEHelpAction helpAction = new MCODEHelpAction("Help", appMgr, swingApp, netViewMgr, openBrowser);
		MCODEVisualStyleAction visualStyleAction = new MCODEVisualStyleAction("Apply MCODE style", appMgr, swingApp, netViewMgr, visualMappingMgr, mcodeUtil);
		MCODEAboutAction aboutAction = new MCODEAboutAction("About", appMgr, swingApp, netViewMgr, openBrowser, mcodeUtil);
		
		registerService(bc, openAction, CyAction.class, new Properties());
		registerService(bc, closeAction, CyAction.class, new Properties());
		registerService(bc, closeAction, NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc, helpAction, CyAction.class, new Properties());
		registerService(bc, aboutAction, CyAction.class, new Properties());
		registerService(bc, analyzeAction, SetCurrentNetworkListener.class, new Properties());
		registerService(bc, visualStyleAction, CyAction.class, new Properties());
		registerService(bc, visualStyleAction, CytoPanelComponentSelectedListener.class, new Properties());
	}
}
