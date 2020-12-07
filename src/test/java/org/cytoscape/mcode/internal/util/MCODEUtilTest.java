package org.cytoscape.mcode.internal.util;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.mcode.internal.AbstractMCODETest;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEParameters;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

/**
 * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
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
 * * Date: Jul 9, 2004
 * * Time: 11:57:55 AM
 * * Description  JUnit testing for MCODE
 */

/**
 * Test for the MCODE algorithm
 */
public class MCODEUtilTest extends AbstractMCODETest {

	CyNetwork networkSmall;
	@Mock CyRootNetworkManager rootNetMgr;
	@Mock CyApplicationManager appMgr;
	@Mock CyNetworkManager netMgr;
	@Mock CyNetworkViewManager netViewMgr;
	@Mock VisualMappingManager vmMgr;
	@Mock VisualStyleFactory styleFactory;
	@Mock VisualMappingFunctionFactory vmfFactory;
	@Mock CySwingApplication swingApp;
	@Mock FileUtil fileUtil;
	
	CyNetworkViewFactory netViewFactory;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		rootNetMgr = netViewTestSupport.getRootNetworkFactory();
		netViewFactory = netViewTestSupport.getNetworkViewFactory();
		
		mcodeUtil = new MCODEUtil(netViewFactory, rootNetMgr, appMgr, netMgr, netViewMgr,
				styleFactory, vmMgr, swingApp, vmfFactory, vmfFactory, fileUtil);
		resultsMgr = new MCODEResultsManager(mcodeUtil);
	}
	
	@Test
	public void testDisposeUnnusedNetworks() {
		final CyNetwork net = createCompleteGraph(16);
		final CyRootNetwork rn = netViewTestSupport.getRootNetworkFactory().getRootNetwork(net);
		int originalNetCount = rn.getSubNetworkList().size(); 
		final List<MCODECluster> clusters = findClusters(net);
		assertEquals(1, clusters.size());
		
		MCODEResult res = resultsMgr.createResult(net, new MCODEParameters(), clusters);
		mcodeUtil.disposeUnusedNetworks(Collections.singleton(res));
		
		assertEquals(originalNetCount + clusters.size(), rn.getSubNetworkList().size());
	}
}
