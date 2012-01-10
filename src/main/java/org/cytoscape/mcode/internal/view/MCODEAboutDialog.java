package org.cytoscape.mcode.internal.view;

import java.awt.Insets;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.mcode.internal.util.MCODEResources;
import org.cytoscape.mcode.internal.util.MCODEUtil;
import org.cytoscape.mcode.internal.util.MCODEResources.ImageName;
import org.cytoscape.util.swing.OpenBrowser;

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
 * * Time: 5:47:31 PM
 * * Description: An about dialog box for MCODE
 */

/**
 * An about dialog box for MCODE
 */
public class MCODEAboutDialog extends JDialog {

	private static final long serialVersionUID = 635333288924094273L;

	private final OpenBrowser openBrowser;
	private final String version;
	private final String buildDate;

	/** Main panel for dialog box */
	private JEditorPane mainContainer;

	public MCODEAboutDialog(final CySwingApplication swingApplication,
							final OpenBrowser openBrowser,
							final MCODEUtil mcodeUtil) {
		super(swingApplication.getJFrame(), "About MCODE", false);
		this.openBrowser = openBrowser;
		
		version = mcodeUtil.getProperty("project.version");
		buildDate = mcodeUtil.getProperty("buildDate");

		setResizable(false);
		setContentPane(getMainContainer());
	}

	private JEditorPane getMainContainer() {
		if (mainContainer == null) {
			mainContainer = new JEditorPane();
			mainContainer.setMargin(new Insets(10, 10, 10, 10));
			mainContainer.setEditable(false);
			mainContainer.setEditorKit(new HTMLEditorKit());
			mainContainer.addHyperlinkListener(new HyperlinkAction());

			URL logoURL = MCODEResources.getUrl(ImageName.LOGO);
			String logoCode = "";

			if (logoURL != null) {
				logoCode = "<center><img src='" + logoURL + "'></center>";
			}

			String text = "<html><body>" +
						  logoCode +
						  "<P align=center><b>MCODE (Molecular Complex Detection) v" + version + " (" + buildDate + ")</b><BR>" +
						  "A Cytoscape App<BR><BR>" +

						  "Version " + version + " by <a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto<BR>" +
						  "Version 1.2 by Vuk Pavlovic (<a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto)<BR>" +
						  "Version 1.1 and 1.0 by Gary Bader (while in the <a href='http://cbio.mskcc.org/'>Sander Lab</a>,<BR>" +
						  "Memorial Sloan-Kettering Cancer Center)<BR><BR>" +

						  "If you use this app in your research, please cite:<BR>" +
						  "Bader GD, Hogue CW<BR>" +
						  "<a href='http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=12525261&dopt=Abstract'>An automated method for finding molecular complexes<BR>" +
						  "in large protein interaction networks.</a><BR>" +
						  "<i>BMC Bioinformatics</i>. 2003 Jan 13;4(1):2</P></body></html>";

			mainContainer.setText(text);
		}

		return mainContainer;
	}

	private class HyperlinkAction implements HyperlinkListener {

		public void hyperlinkUpdate(HyperlinkEvent event) {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				openBrowser.openURL(event.getURL().toString());
			}
		}
	}
}
