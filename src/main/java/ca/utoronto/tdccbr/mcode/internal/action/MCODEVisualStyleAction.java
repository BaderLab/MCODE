package ca.utoronto.tdccbr.mcode.internal.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.vizmap.VisualStyle;

import ca.utoronto.tdccbr.mcode.internal.model.MCODECluster;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
import ca.utoronto.tdccbr.mcode.internal.view.MCODEResultsPanel;

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
 * * User: Vuk Pavlovic
 * * Date: Jan 4, 2007
 * * Time: 11:30:35 AM
 * * Description: A controller for the MCODE attributes used for visualization
 */

/**
 * A controller for the MCODE attributes used for visualization. Only the onComponentSelected method is
 * used in this listener to determine when a result has been selected.
 */
public class MCODEVisualStyleAction extends AbstractMCODEAction implements CytoPanelComponentSelectedListener {

	private static final long serialVersionUID = -6884537645922099638L;

	private final MCODEUtil mcodeUtil;

	public MCODEVisualStyleAction(String title, CyServiceRegistrar registrar, MCODEUtil mcodeUtil) {
		super(title, ActionEnableSupport.ENABLE_FOR_NETWORK, registrar);
		this.mcodeUtil = mcodeUtil;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
	}

	/**
	 * Whenever an MCODE result tab is selected in the east CytoPanel, the MCODE attributes
	 * have to be rewritten to correspond to that particular result. At the same time the
	 * Visual Style has to redraw the network given the new attributes.
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleEvent(CytoPanelComponentSelectedEvent event) {
		// When the user selects a tab in the east cytopanel we want to see if it is a results panel
		// and if it is we want to re-draw the network with the MCODE visual style and reselect the
		// cluster that may be selected in the results panel
		Component component = event.getCytoPanel().getSelectedComponent();

		if (component instanceof MCODEResultsPanel) {
			MCODEResultsPanel resultsPanel = (MCODEResultsPanel) component;

			// To re-initialize the calculators we need the highest score of this particular result set
			double maxScore = resultsPanel.setNodeAttributesAndGetMaxScore();
			// Get the updated app's style
			VisualStyle appStyle = mcodeUtil.getAppStyle(maxScore);
			// Register the app's style but don't make it active by default
			mcodeUtil.registerVisualStyle(appStyle);
			
			// Get selected cluster of this results panel and select its nodes/edges again
			final MCODECluster cluster = resultsPanel.getSelectedCluster();
			
			if (cluster != null) {
				final List elements = new ArrayList<>();
				elements.addAll(cluster.getGraph().getEdgeList());
				elements.addAll(cluster.getGraph().getNodeList());
				
				mcodeUtil.setSelected(elements, resultsPanel.getNetwork());
			}
		}
	}
}
