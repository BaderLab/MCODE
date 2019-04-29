package ca.utoronto.tdccbr.mcode.internal.task;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import ca.utoronto.tdccbr.mcode.internal.model.MCODEResultsManager;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEUtil;
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

/**
 * Closes the main MCODE panel.
 */
public class MCODECloseTask implements Task {

	private final MainPanelMediator mediator;
	private final CyServiceRegistrar registrar;
	
	public MCODECloseTask(
			MainPanelMediator mediator,
			MCODEResultsManager resultsMgr,
			MCODEUtil mcodeUtil,
			CyServiceRegistrar registrar
	) {
		this.mediator = mediator;
		this.registrar = registrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Close MCODE");

		if (mediator.isMainPanelOpen()) {
			tm.setStatusMessage("Closing MCODE Panel...");
			registrar.unregisterService(mediator.getMainPanel(), CytoPanelComponent.class);
		}
	}

	@Override
	public void cancel() {
		// Do nothing
	}
}
