package org.cytoscape.mcode.internal.model;

import static org.junit.Assert.*;

import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.model.CyNetwork;
import org.junit.Before;
import org.junit.Test;

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
public class MCODEAlgorithmTest {

	MCODEAlgorithm alg;
	CyNetwork networkSmall;
	MCODEParameterSet params;
	MCODEUtil mcodeutil;

	@Before
	public void setUp() throws Exception {
		// TODO
//		mcodeutil = new MCODEUtil(renderingEngineFactory, networkViewFactory, rootNetworkFactory, applicationManager,
//								  networkManager, networkViewManager, vmMgr, eventHelper);
//		alg = new MCODEAlgorithm(null);
//		params = new MCODEParameterSet();
//		networkSmall = Cytoscape.createNetworkFromFile("testData" + File.separator + "smallTest.sif");
	}

	/**
	 * Run MCODE on a small test network with some default parameters
	 */
	@Test
	public void testMCODEAlgorithmSmall() {
		// TODO
//		params.setAllAlgorithmParams(MCODEParameterSet.NETWORK, null, false, 2, 2, false, 100, 0.2, false, true, 0.1);
//		alg.scoreGraph(networkSmall, 1);
//		MCODECluster[] clusters = alg.findClusters(networkSmall, 1);
//
//		assertEquals(clusters.length, 1);
//		double score = alg.scoreCluster(clusters[0]);
//		assertEquals(score, (double) 1.5, 0);
	}
}
