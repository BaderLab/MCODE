package ca.utoronto.tdccbr.mcode.internal.view;

import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;

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
 * * Time: 5:47:31 PM
 * * Description: An about dialog box for MCODE
 */

/**
 * An about dialog box for MCODE
 */
@SuppressWarnings("serial")
public class AboutDialog extends JDialog {

	private static final String LOGO = "/img/logo.png";
	public static final String APP_URL = "http://www.baderlab.org/Software/MCODE";

	private final String version;
	private final String buildDate;

	/** Main panel for dialog box */
	private JEditorPane mainContainer;
	private JPanel buttonPanel;

	private final CyServiceRegistrar registrar;

	public AboutDialog(Window owner, MCODEUtil mcodeUtil, CyServiceRegistrar registrar) {
		super(owner, "About MCODE", ModalityType.APPLICATION_MODAL);
		this.registrar = registrar;
		
		version = mcodeUtil.getProperty("project.version");
		buildDate = mcodeUtil.getProperty("buildDate");

		getContentPane().setBackground(Color.WHITE);
		
		setResizable(false);
		getContentPane().add(getMainContainer(), BorderLayout.CENTER);
		getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
		pack();
	}

	private JEditorPane getMainContainer() {
		if (mainContainer == null) {
			mainContainer = new JEditorPane();
			mainContainer.setMargin(new Insets(10, 10, 10, 10));
			mainContainer.setEditable(false);
			mainContainer.setEditorKit(new HTMLEditorKit());
			mainContainer.addHyperlinkListener(new HyperlinkAction());

			URL logoURL = getClass().getResource(LOGO);

			mainContainer.setText(
					"<html><body style='font-family:Arial,Helvetica,sans-serif;'>"
					+ "<p align='center'><img src='" + logoURL.toString() + "'><BR><BR>"
					+ "(Molecular Complex Detection)<BR>"
					+ "<span style='font-size:small;'><b>version " + version + "</b>"
					+ " (" + buildDate + ")<BR><BR>"
					+ "A Cytoscape App</span>"
					+ "</p><BR>"
					
					+ "<hr size='4' noshade>"
					
					+ "<p align='center' style='font-size:small;'>MCODE is a Cytoscape app that finds clusters<BR>(highly interconnected regions) in a network.</p>"
					
					+ "<p align='left' style='font-family:Courier New,monospace;font-size:small'>"
					+ ". Version " + version + " by <a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto<BR>"
					+ ". Version 1.2 by Vuk Pavlovic (<a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto)<BR>"
					+ ". Version 1.1 and 1.0 by Gary Bader (while in the <a href='http://cbio.mskcc.org/'>Sander Lab</a>,<BR>"
					+ "&nbsp;&nbsp;Memorial Sloan-Kettering Cancer Center)"
					+ "</p>"
					
					+ "<p align='center' style='font-size:small;'>App Homepage:<BR><a href='" + APP_URL + "'>" + APP_URL + "</a></p><BR>"
					
					+ "<hr size='4' noshade>"
					
					+ "<p style='font-size:small'>If you use this app in your research, please cite:</p>"
					+ "<p style='font-family:Courier New,monospace;font-size:small'>"
					+ "Bader GD, Hogue CW<BR>"
					+ "<a href='http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=12525261&dopt=Abstract'>An automated method for finding molecular complexes<BR>"
					+ "in large protein interaction networks</a><BR>"
					+ "<i>BMC Bioinformatics. 2003 Jan 13;4(1):2</i><BR>"
					
					+ "</p><BR>"
					+ "</body></html>"
			);
			
			mainContainer.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_ENTER:
						case KeyEvent.VK_ESCAPE:
							dispose();
							break;
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
				}
			});
		}

		return mainContainer;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			JButton okBtn = new JButton(new AbstractAction("Close") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			makeSmall(okBtn);
			
			buttonPanel = LookAndFeelUtil.createOkCancelPanel(null, okBtn);
			buttonPanel.setOpaque(false);
			buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okBtn.getAction(), okBtn.getAction());
			getRootPane().setDefaultButton(okBtn);
		}
		
		return buttonPanel;
	}
	
	private class HyperlinkAction implements HyperlinkListener {
		
		@Override
		public void hyperlinkUpdate(HyperlinkEvent event) {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				registrar.getService(OpenBrowser.class).openURL(event.getURL().toString());
		}
	}
}
