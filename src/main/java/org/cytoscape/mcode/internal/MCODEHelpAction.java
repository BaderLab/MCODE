package org.cytoscape.mcode.internal;

import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.OpenBrowser;

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
 * * User: vukpavlovic
 * * Date: Jan 10, 2007
 * * Time: 5:55:40 PM
 * * Description: Opens a browser with the MCODE wiki page.
 */

/**
 * Opens a browser with the MCODE wiki page.
 */

public class MCODEHelpAction extends AbstractMCODEAction {

	private static final long serialVersionUID = -8129187221346920847L;

	private final OpenBrowser openBrowser;

	public MCODEHelpAction(final String name,
						   final CyApplicationManager applicationManager,
						   final CySwingApplication swingApplication,
						   final OpenBrowser openBrowser) {
		super(name, applicationManager, swingApplication);
		this.openBrowser = openBrowser;
		setPreferredMenu("Plugins.MCODE");
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		openBrowser.openURL("http://www.baderlab.org/Software/MCODE/");
	}
}
