package ca.utoronto.tdccbr.mcode.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.awt.Component;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
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

import ca.utoronto.tdccbr.mcode.internal.action.AboutAction;
import ca.utoronto.tdccbr.mcode.internal.action.AnalysisAction;
import ca.utoronto.tdccbr.mcode.internal.action.MCODEHelpAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.task.CreateClusterNetworkViewTaskFactory;
import ca.utoronto.tdccbr.mcode.internal.task.MCODEAnalyzeCommandTaskFactory;
import ca.utoronto.tdccbr.mcode.internal.task.MCODECloseTaskFactory;
import ca.utoronto.tdccbr.mcode.internal.task.MCODEOpenTaskFactory;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEMainPanel;
import ca.utoronto.tdccbr.mcode.internal.view.MainPanelMediator;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 * * User: Gary Bader
 * * Date: Jun 25, 2004
 * * Time: 7:00:13 PM
 * * Description: Utilities for MCODE
 */

public class CyActivator extends AbstractCyActivator {

	private CyServiceRegistrar registrar;
	private MCODEUtil mcodeUtil;
	private MainPanelMediator mainPanelMediator;
	
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
		
		MCODEResultsManager resultsMgr = new MCODEResultsManager(mcodeUtil);

		closeMCODEPanels();
		
		AnalysisAction analysisAction = new AnalysisAction("Analyze Current Network", resultsMgr, mcodeUtil, registrar);
		MCODEHelpAction helpAction = new MCODEHelpAction("Help", openBrowser, registrar);
		AboutAction aboutAction = new AboutAction("About", registrar, mcodeUtil);
		
		registerService(bc, helpAction, CyAction.class);
		registerService(bc, aboutAction, CyAction.class);
		registerAllServices(bc, analysisAction);
		
		// View Mediators
		mainPanelMediator = new MainPanelMediator(analysisAction, resultsMgr, mcodeUtil, registrar);
		registerService(bc, mainPanelMediator, NetworkAboutToBeDestroyedListener.class);
		registerService(bc, mainPanelMediator, SetCurrentNetworkListener.class);
		
		// Tasks
		{
			MCODEOpenTaskFactory factory = new MCODEOpenTaskFactory(mainPanelMediator, analysisAction, registrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Apps.MCODE");
			props.setProperty(TITLE, "Open MCODE");
			props.setProperty(MENU_GRAVITY, "1.0");
			
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			MCODECloseTaskFactory factory = new MCODECloseTaskFactory(mainPanelMediator, resultsMgr, mcodeUtil, registrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Apps.MCODE");
			props.setProperty(TITLE, "Close MCODE");
			props.setProperty(MENU_GRAVITY, "2.0");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			
			registerService(bc, factory, TaskFactory.class, props);
		}
		
		// Commands
		{
			MCODEAnalyzeCommandTaskFactory factory = new MCODEAnalyzeCommandTaskFactory(analysisAction, resultsMgr, mcodeUtil, registrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "cluster");
			props.setProperty(COMMAND_NAMESPACE, "mcode");
			props.setProperty(COMMAND_DESCRIPTION, "Finds clusters in a network.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Analyzes the specified network in order to find clusters.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON,
					"{ \"id\": 1, "
					+ "\"clusters\": [ "
					+ "{ \"rank\": 1, \"name\": \"Cluster 1\", \"score\": 3.335, \"seedNode\": 178, \"nodes\": [ 184, 177, 178, 175 ] }, "
					+ "{ \"rank\": 2, \"name\": \"Cluster 2\", \"score\": 2.41, \"seedNode\": 185, \"nodes\": [ 192, 201, 185, 189, 270 ] } "
					+ "] "
					+ "}"
			);
			
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			CreateClusterNetworkViewTaskFactory factory = new CreateClusterNetworkViewTaskFactory(resultsMgr, mcodeUtil, registrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "view");
			props.setProperty(COMMAND_NAMESPACE, "mcode");
			props.setProperty(COMMAND_DESCRIPTION, "Creates a view from a cluster.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Creates a network and view from the specified MCODE cluster and returns the new view's SUID.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ \"view\": 104 }");
			
			registerService(bc, factory, TaskFactory.class, props);
		}
	}
	
	@Override
	public void shutDown() {
		mainPanelMediator.disposeNewAnalysisDialog();
		closeMCODEPanels();
		super.shutDown();
	}
	
	private void closeMCODEPanels() {
		// First, unregister result panels from old versions of MCODE
		CytoPanel resPanel = registrar.getService(CySwingApplication.class).getCytoPanel(CytoPanelName.EAST);
		
		if (resPanel != null) {
			int count = resPanel.getCytoPanelComponentCount();
			
			try {
				for (int i = 0; i < count; i++) {
					final Component comp = resPanel.getComponentAt(i);
					
					// Compare the class names to also get panels that may have been left by old versions of MCODE
					if (comp.getClass().getName().equals("ca.utoronto.tdccbr.mcode.internal.view.MCODEResultsPanel"))
						registrar.unregisterAllServices(comp);
				}
			} catch (Exception e) {
			}
		}
		
		// Then unregister the main panel...
		CytoPanel ctrlPanel = registrar.getService(CySwingApplication.class).getCytoPanel(CytoPanelName.WEST);
		
		if (ctrlPanel != null) {
			int count = ctrlPanel.getCytoPanelComponentCount();
	
			for (int i = 0; i < count; i++) {
				try {
					Component comp = ctrlPanel.getComponentAt(i);
					String name = comp.getClass().getName();
					
					// Compare the class names to also get panels that may have been left by old versions of MCODE
					if (name.equals(MCODEMainPanel.class.getName()))
						registrar.unregisterAllServices(comp);
				} catch (Exception e) {
				}
			}
		}
	}
}
