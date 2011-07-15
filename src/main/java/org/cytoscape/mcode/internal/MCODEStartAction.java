package org.cytoscape.mcode.internal;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.session.CyApplicationManager;

/**
 * Creates a new menu item under Plugins menu section.
 */
public class MCODEStartAction extends AbstractCyAction {

	private static final long serialVersionUID = 3521389398662580589L;

	public MCODEStartAction(final String menuTitle, final CyApplicationManager applicationManager) {
		super(menuTitle, applicationManager);
		setPreferredMenu("Plugins.MCODE");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO...
		JOptionPane.showMessageDialog(null, "TODO...");
	}
}
