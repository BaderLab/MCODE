package ca.utoronto.tdccbr.mcode.internal.view;

import static ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil.invokeOnEDT;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

import ca.utoronto.tdccbr.mcode.internal.action.MCODEDiscardResultAction;
import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResult;
import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;

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

public class MCODEResultsMediator {
	
	private final MCODEResultsManager resultsMgr;
	private final MCODEUtil mcodeUtil;
	private final CyServiceRegistrar registrar;
	
	public MCODEResultsMediator(MCODEResultsManager resultsMgr, MCODEUtil mcodeUtil, CyServiceRegistrar registrar) {
		this.resultsMgr = resultsMgr;
		this.mcodeUtil = mcodeUtil;
		this.registrar = registrar;
		
		resultsMgr.addPropertyChangeListener("networkResults", evt -> {
			Integer resultId = (Integer) evt.getNewValue();
			
			if (resultId != null)
				showResultsPanel(resultId);
		});
	}
	
	private void showResultsPanel(int resultId) {
		MCODEResult res = resultsMgr.getResult(resultId);
		List<MCODECluster> clusters = res != null ? res.getClusters() : null;
		
		if (clusters == null || clusters.isEmpty())
			return;
		
		CyNetwork network = res.getNetwork();
		CyNetworkView currView = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		
		MCODEDiscardResultAction discardResultAction = new MCODEDiscardResultAction("Discard Result", resultId,
				resultsMgr, mcodeUtil, registrar);

		invokeOnEDT(() -> {
			CyNetworkView netView = currView != null && currView.getModel().equals(network) ? currView : null;

			if (netView == null) {
				Collection<CyNetworkView> allViews = registrar.getService(CyNetworkViewManager.class)
						.getNetworkViews(network);

				if (allViews.isEmpty())
					netView = allViews.iterator().next();
			}
			
			MCODEResultsPanel resultsPanel = new MCODEResultsPanel(clusters, mcodeUtil, network, netView, resultId,
					discardResultAction, registrar);
			
			for (MCODECluster c : clusters) {
				c.addPropertyChangeListener("image", evt -> {
					invokeOnEDT(() -> {
						resultsPanel.getClusterBrowserPnl().update(new ImageIcon(c.getImage()), c.getRank() - 1);
					});
				});
			}
			
			registrar.registerService(resultsPanel, CytoPanelComponent.class, new Properties());
			
			// Focus the result panel
			CytoPanel cytoPanel = registrar.getService(CySwingApplication.class).getCytoPanel(CytoPanelName.EAST);
			int index = cytoPanel.indexOfComponent(resultsPanel);
			cytoPanel.setSelectedIndex(index);

			if (cytoPanel.getState() == CytoPanelState.HIDE)
				cytoPanel.setState(CytoPanelState.DOCK);
		});
	}
}
