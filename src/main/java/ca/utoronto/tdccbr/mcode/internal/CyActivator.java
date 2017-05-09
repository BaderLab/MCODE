package ca.utoronto.tdccbr.mcode.internal;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.awt.Component;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
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
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import ca.utoronto.tdccbr.mcode.internal.rest.MCODEAnalysisResource;
import ca.utoronto.tdccbr.mcode.internal.task.MCODECloseTaskFactory;
import ca.utoronto.tdccbr.mcode.internal.task.MCODEOpenTaskFactory;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEMainPanel;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEResultsPanel;

public class CyActivator extends AbstractCyActivator {

	private CyServiceRegistrar registrar;
	private MCODEUtil mcodeUtil;
	
	public CyActivator() {
		super();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void start(BundleContext bc) {
		registrar = getService(bc, CyServiceRegistrar.class);
		
		CyApplicationManager appMgr = getService(bc, CyApplicationManager.class);
		CyNetworkViewManager netViewMgr = getService(bc, CyNetworkViewManager.class);
		CyNetworkManager netMgr = getService(bc, CyNetworkManager.class);
		
		CyNetworkViewFactory netViewFactory = getService(bc, CyNetworkViewFactory.class);
		CyRootNetworkManager rootNetworkMgr = getService(bc, CyRootNetworkManager.class);
		
		CySwingApplication swingApp = getService(bc, CySwingApplication.class);
		RenderingEngineFactory<CyNetwork> dingRenderingEngineFactory = getService(bc, RenderingEngineFactory.class, "(id=ding)");
		
		VisualStyleFactory visualStyleFactory = getService(bc, VisualStyleFactory.class);
		VisualMappingManager visualMappingMgr = getService(bc, VisualMappingManager.class);
		VisualMappingFunctionFactory discreteMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory continuousMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		
		FileUtil fileUtil = getService(bc, FileUtil.class);
		OpenBrowser openBrowser = getService(bc, OpenBrowser.class);
		
		mcodeUtil = new MCODEUtil(dingRenderingEngineFactory, netViewFactory, rootNetworkMgr,
								  appMgr, netMgr, netViewMgr, visualStyleFactory,
								  visualMappingMgr, swingApp, discreteMappingFactory,
								  continuousMappingFactory, fileUtil);
		
		closeMCODEPanels();
		
		MCODEAnalyzeAction analyzeAction = new MCODEAnalyzeAction("Analyze Current Network", registrar, mcodeUtil);
		MCODEHelpAction helpAction = new MCODEHelpAction("Help", openBrowser, registrar);
		MCODEVisualStyleAction visualStyleAction = new MCODEVisualStyleAction("Apply MCODE style", registrar, mcodeUtil);
		MCODEAboutAction aboutAction = new MCODEAboutAction("About", registrar, mcodeUtil);
		
		registerService(bc, helpAction, CyAction.class, new Properties());
		registerService(bc, aboutAction, CyAction.class, new Properties());
		registerAllServices(bc, analyzeAction, new Properties());
		registerService(bc, visualStyleAction, CyAction.class, new Properties());
		registerService(bc, visualStyleAction, CytoPanelComponentSelectedListener.class, new Properties());
		
		MCODEOpenTaskFactory openTaskFactory = new MCODEOpenTaskFactory(swingApp, registrar, mcodeUtil, analyzeAction);
		Properties openTaskFactoryProps = new Properties();
		openTaskFactoryProps.setProperty(PREFERRED_MENU, "Apps.MCODE");
		openTaskFactoryProps.setProperty(TITLE, "Open MCODE");
		openTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		
		registerService(bc, openTaskFactory, TaskFactory.class, openTaskFactoryProps);
		
		MCODECloseTaskFactory closeTaskFactory = new MCODECloseTaskFactory(swingApp, registrar, mcodeUtil);
		Properties closeTaskFactoryProps = new Properties();
		closeTaskFactoryProps.setProperty(PREFERRED_MENU, "Apps.MCODE");
		closeTaskFactoryProps.setProperty(TITLE, "Close MCODE");
		closeTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
		
		registerService(bc, closeTaskFactory, TaskFactory.class, closeTaskFactoryProps);
		registerService(bc, closeTaskFactory, NetworkAboutToBeDestroyedListener.class, new Properties());
		
		MCODEAnalysisResource analysisResource = new MCODEAnalysisResource(analyzeAction, mcodeUtil, registrar);
		registerService(bc, analysisResource, MCODEAnalysisResource.class);
	}
	
	@Override
	public void shutDown() {
		closeMCODEPanels();
		super.shutDown();
	}
	
	private void closeMCODEPanels() {
		// First, unregister result panels...
		final CytoPanel resPanel = mcodeUtil.getResultsCytoPanel();
		
		if (resPanel != null) {
			int count = resPanel.getCytoPanelComponentCount();
			
			try {
				for (int i = 0; i < count; i++) {
					final Component comp = resPanel.getComponentAt(i);
					
					// Compare the class names to also get panels that may have been left by old versions of MCODE
					if (comp.getClass().getName().equals(MCODEResultsPanel.class.getName()))
						registrar.unregisterAllServices(comp);
				}
			} catch (Exception e) {
			}
		}
		
		// Now, unregister main panels...
		final CytoPanel ctrlPanel = mcodeUtil.getControlCytoPanel();
		
		if (ctrlPanel != null) {
			int count = ctrlPanel.getCytoPanelComponentCount();
	
			for (int i = 0; i < count; i++) {
				try {
					final Component comp = ctrlPanel.getComponentAt(i);
					
					// Compare the class names to also get panels that may have been left by old versions of MCODE
					if (comp.getClass().getName().equals(MCODEMainPanel.class.getName()))
						registrar.unregisterAllServices(comp);
				} catch (Exception e) {
				}
			}
		}
	}
}
