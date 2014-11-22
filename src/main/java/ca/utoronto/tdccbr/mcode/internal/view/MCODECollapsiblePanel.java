package ca.utoronto.tdccbr.mcode.internal.view;

import static ca.utoronto.tdccbr.mcode.internal.util.UIUtil.getLookAndFeelBorder;
import static ca.utoronto.tdccbr.mcode.internal.util.UIUtil.isNimbusLAF;
import static ca.utoronto.tdccbr.mcode.internal.util.UIUtil.isWinLAF;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.Border;

import ca.utoronto.tdccbr.mcode.internal.util.MCODEResources;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEResources.ImageName;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Original code written by: Gary Bader
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
 * * Description: The Main Panel allowing user to choose scope and other parameters
 */

/**
 * The user-triggered collapsible panel containing the component (trigger) in the titled border
 */
public class MCODECollapsiblePanel extends JPanel {
	
	private static final long serialVersionUID = 2010434345567315524L;
	
	private final static int COLLAPSED = 0, EXPANDED = 1; // image States

	private Border border; 

	// Title displayed in the titled border
	protected AbstractButton titleComponent; 

	// Expand/Collapse button
	private final ImageIcon[] iconArrow;
	private JButton arrowBtn;

	// Content Pane
	private JPanel contentPane;

	// Container State
	private boolean collapsed; // stores current state of the collapsable panel
	
	/**
	 * Constructor for an option button controlled collapsible panel. This is
	 * useful when a group of options each have unique sub contents. The radio
	 * buttons should be created, grouped, and then used to construct their own
	 * collapsible panels. This way choosing a different option in the same
	 * option group will collapse all unselected options. Expanded panels draw a
	 * border around the contents and through the radio button in the fashion of
	 * a titled border.
	 * 
	 * @param component Radio button that expands and collapses the panel based on if it is selected or not
	 */
	public MCODECollapsiblePanel(JRadioButton component) {
		this(component, !component.isSelected());
        component.addItemListener(new MCODECollapsiblePanel.ExpandAndCollapseAction());
        
        setCollapsed(collapsed);
	}

	/**
	 * Constructor for a label/button controlled collapsible panel. Displays a
	 * clickable title that resembles a native titled border except for an arrow
	 * on the right side indicating an expandable panel. The actual border only
	 * appears when the panel is expanded.
	 * 
	 * @param title Title of the collapsible panel in string format, used to
	 *              create a button with text and an arrow icon
	 */
	public MCODECollapsiblePanel(String title) {
		this(null, true);
    	getArrowBtn().setText(title);
    	
    	setCollapsed(collapsed);
	}
	
	private MCODECollapsiblePanel(final AbstractButton titleComponent, final boolean collapsed) {
		border = getLookAndFeelBorder();
    	iconArrow = createExpandAndCollapseIcon();
    	
    	this.titleComponent = titleComponent != null ? titleComponent : getArrowBtn();
    	this.collapsed = collapsed;
    	
    	if (isWinLAF())
    		setBorder(border);
    	else
    		getContentPane().setBorder(border);
    	
		setLayout(new BorderLayout());
		
		add(this.titleComponent, BorderLayout.NORTH);
		add(getContentPane(), BorderLayout.CENTER);
    }

	private JButton getArrowBtn() {
		if (arrowBtn == null) {
			arrowBtn = new JButton("", iconArrow[COLLAPSED]);
			
			if (isWinLAF()) {
				arrowBtn.setMargin(new Insets(2, 2, 2, 2));
			} else {
				arrowBtn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
				arrowBtn.setMargin(new Insets(0, 0, 3, 0));
				arrowBtn.setContentAreaFilled(false);
			}
			
			arrowBtn.setFocusable(false);
			arrowBtn.setHorizontalAlignment(JButton.LEFT);
			arrowBtn.setHorizontalTextPosition(JButton.RIGHT);
			arrowBtn.setVerticalAlignment(JButton.CENTER);
			arrowBtn.setVerticalTextPosition(JButton.CENTER);

			// We want to use the same font as those in the titled border font
			Font font = BorderFactory.createTitledBorder(border, "Sample").getTitleFont();
			if (font == null) font = UIManager.getFont("Label.font");
			Color color = BorderFactory.createTitledBorder(border, "Sample").getTitleColor();
			if (color == null) color = UIManager.getColor("Label.foreground");
			
			if (font != null) arrowBtn.setFont(font);
			if (isNimbusLAF()) arrowBtn.setFont(arrowBtn.getFont().deriveFont(Font.BOLD));
			if (color != null) arrowBtn.setForeground(color);

			arrowBtn.addActionListener(new MCODECollapsiblePanel.ExpandAndCollapseAction());
		}
		
		return arrowBtn;
	}
	
	/**
	 * Sets the title of of the border title component.
	 * 
	 * @param text The string title.
	 */
	public void setTitleComponentText(String text) {
		if (titleComponent instanceof JButton)
			titleComponent.setText(text);
	}

	/**
	 * This class requires that all content be placed within a designated panel,
	 * this method returns that panel.
	 * 
	 * @return panel The content panel.
	 */
	public JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		}
		
		return contentPane;
	}

	/**
	 * Collapses or expands the panel. This is done by adding or removing the
	 * content pane, alternating between a frame and empty border, and changing
	 * the title arrow. Also, the current state is stored in the collapsed
	 * boolean.
	 * 
	 * @param collapse When set to true, the panel is collapsed, else it is expanded
	 */
	public void setCollapsed(boolean collapse) {
		if (collapse) {
			// collapse the contentPane, remove content and set border to empty border
			getContentPane().setVisible(false);
			getArrowBtn().setIcon(iconArrow[COLLAPSED]);
		} else {
			// expand the contentPane, add content and set border to titled border
			getContentPane().setVisible(true);
			getArrowBtn().setIcon(iconArrow[EXPANDED]);
		}
		
		collapsed = collapse;
		updateUI();
	}

	/**
	 * Returns the current state of the panel, collapsed (true) or expanded (false).
	 * 
	 * @return collapsed Returns true if the panel is collapsed and false if it is expanded
	 */
	public boolean isCollapsed() {
		return collapsed;
	}

	/**
	 * Returns an ImageIcon array with arrow images used for the different
	 * states of the panel.
	 * 
	 * @return iconArrow An ImageIcon array holding the collapse and expanded
	 *         versions of the right hand side arrow
	 */
	private ImageIcon[] createExpandAndCollapseIcon() {
		final ImageIcon[] iconArrow = new ImageIcon[2];
		URL iconURL;
		iconURL = MCODEResources.getUrl(ImageName.ARROW_COLLAPSED);

		if (iconURL != null)
			iconArrow[COLLAPSED] = new ImageIcon(iconURL);
		
		iconURL = MCODEResources.getUrl(ImageName.ARROW_EXPANDED);

		if (iconURL != null)
			iconArrow[EXPANDED] = new ImageIcon(iconURL);
		
		return iconArrow;
	}

	/**
	 * Handles expanding and collapsing of extra content on the user's click of
	 * the titledBorder component.
	 */
	private final class ExpandAndCollapseAction extends AbstractAction implements ActionListener, ItemListener {
		
		private static final long serialVersionUID = 2010434345567315525L;

		@Override
		public void actionPerformed(ActionEvent e) {
			setCollapsed(!isCollapsed());
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			setCollapsed(!isCollapsed());
		}
	}

	/**
	 * Sets the tooltip text of this MCODECollapsiblePanel.
	 * 
	 * @param text The string to set as the tooltip.
	 */
	@Override
	public void setToolTipText(final String text) {
		super.setToolTipText(text);
		titleComponent.setToolTipText(text);
	}
}
