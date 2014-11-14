package ca.utoronto.tdccbr.mcode.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
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
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import ca.utoronto.tdccbr.mcode.internal.util.MCODEResources;
import ca.utoronto.tdccbr.mcode.internal.util.MCODEResources.ImageName;

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
 * * Description: The Main Panel allowing user to choose scope and other parameters
 */

/**
 * The user-triggered collapsible panel containing the component (trigger) in the titled border
 */
public class MCODECollapsiblePanel extends JPanel {
	
	private static final long serialVersionUID = 2010434345567315524L;
	// Border
	// includes upper left component and line type
	private CollapsableTitledBorder border; 

	private final Border collapsedBorderLine; 
	private final Border expandedBorderLine; 

	// Title displayed in the titled border
	// protected scope for unit testing
	AbstractButton titleComponent; 

	// Expand/Collapse button
	private final static int COLLAPSED = 0, EXPANDED = 1; // image States
	private final ImageIcon[] iconArrow;
	private JButton arrowBtn;

	// Content Pane
	private JPanel panel;

	// Container State
	private boolean collapsed; // stores current state of the collapsable panel
	
	private boolean aquaBorder;

	/**
	 * Constructor for an option button controlled collapsible panel. This is
	 * useful when a group of options each have unique sub contents. The radio
	 * buttons should be created, grouped, and then used to construct their own
	 * collapsible panels. This way choosing a different option in the same
	 * option group will collapse all unselected options. Expanded panels draw a
	 * border around the contents and through the radio button in the fashion of
	 * a titled border.
	 * 
	 * @param component
	 *            Radio button that expands and collapses the panel based on if
	 *            it is selected or not
	 */
	public MCODECollapsiblePanel(JRadioButton component) {
		this(component, !component.isSelected());
        component.addItemListener(new MCODECollapsiblePanel.ExpandAndCollapseAction());
        
        setCollapsed(collapsed);
        placeTitleComponent();
	}

	/**
	 * Constructor for a label/button controlled collapsible panel. Displays a
	 * clickable title that resembles a native titled border except for an arrow
	 * on the right side indicating an expandable panel. The actual border only
	 * appears when the panel is expanded.
	 * 
	 * @param title
	 *            Title of the collapsible panel in string format, used to
	 *            create a button with text and an arrow icon
	 */
	public MCODECollapsiblePanel(String title) {
		this(null, true);
    	getArrowBtn().setText(title);
    	
    	setCollapsed(collapsed);
        placeTitleComponent();
	}
	
	private MCODECollapsiblePanel(final AbstractButton titleComponent, final boolean collapsed) {
		collapsedBorderLine = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		expandedBorderLine = UIManager.getBorder("TitledBorder.aquaVariant");
    	aquaBorder = expandedBorderLine != null;
    	
    	iconArrow = createExpandAndCollapseIcon();
    	
    	if (titleComponent != null)
			titleComponent.setOpaque(false);
    	
    	this.titleComponent = titleComponent != null ? titleComponent : getArrowBtn();
    	this.collapsed = collapsed;
    	
    	setOpaque(false);
		setLayout(new BorderLayout());
		
		add(this.titleComponent, BorderLayout.CENTER);
		add(getPanel(), BorderLayout.CENTER);
    }

	private JButton getArrowBtn() {
		if (arrowBtn == null) {
			arrowBtn = new JButton("", iconArrow[COLLAPSED]);
			arrowBtn.setBorder(BorderFactory.createEmptyBorder(0, 2, (aquaBorder ? 20 : 5), 2));
			arrowBtn.setHorizontalAlignment(JButton.LEFT);
			arrowBtn.setVerticalTextPosition(JButton.CENTER);
			arrowBtn.setHorizontalTextPosition(JButton.RIGHT);
			arrowBtn.setIconTextGap(4);
			arrowBtn.setMargin(new Insets(0, 0, 3, 0));

			// We want to use the same font as those in the titled border font
			Font font = BorderFactory.createTitledBorder("Sample").getTitleFont();
			if (font == null) font = UIManager.getFont("Label.font");
			Color color = BorderFactory.createTitledBorder("Sample").getTitleColor();
			if (color == null) color = UIManager.getColor("Label.foreground");
			
			if (font != null) arrowBtn.setFont(font);
			if (color != null) arrowBtn.setForeground(color);
			arrowBtn.setFocusable(false);
			arrowBtn.setContentAreaFilled(false);

			arrowBtn.addActionListener(new MCODECollapsiblePanel.ExpandAndCollapseAction());
		}
		
		return arrowBtn;
	}
	
	@SuppressWarnings("serial")
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel() {
				@Override
				public Component add(Component comp) {
					maybeSetTransparent(comp);
					return super.add(comp);
				}
				@Override
				public Component add(Component comp, int index) {
					maybeSetTransparent(comp);
					return super.add(comp, index);
				}
				@Override
				public void add(Component comp, Object constraints) {
					maybeSetTransparent(comp);
					super.add(comp, constraints);
				}
				@Override
				public void add(Component comp, Object constraints, int index) {
					maybeSetTransparent(comp);
					super.add(comp, constraints, index);
				}
				@Override
				public Component add(String name, Component comp) {
					maybeSetTransparent(comp);
					return super.add(name, comp);
				}
			};
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	        panel.setOpaque(false);
		}
		
		return panel;
	}

	/**
	 * Sets the bounds of the border title component so that it is properly
	 * positioned.
	 */
	private void placeTitleComponent() {
		Insets insets = this.getInsets();
		Rectangle containerRectangle = this.getBounds();
		Rectangle componentRectangle = border.getComponentRect(containerRectangle, insets);
		titleComponent.setBounds(componentRectangle);
	}

	/**
	 * Sets the title of of the border title component.
	 * 
	 * @param text
	 *            The string title.
	 */
	public void setTitleComponentText(String text) {
		if (titleComponent instanceof JButton) {
			titleComponent.setText(text);
		}
		placeTitleComponent();
	}

	/**
	 * This class requires that all content be placed within a designated panel,
	 * this method returns that panel.
	 * 
	 * @return panel The content panel.
	 */
	public JPanel getContentPane() {
		return getPanel();
	}

	/**
	 * Collapses or expands the panel. This is done by adding or removing the
	 * content pane, alternating between a frame and empty border, and changing
	 * the title arrow. Also, the current state is stored in the collapsed
	 * boolean.
	 * 
	 * @param collapse
	 *            When set to true, the panel is collapsed, else it is expanded
	 */
	public void setCollapsed(boolean collapse) {
		if (collapse) {
			// collapse the panel, remove content and set border to empty border
			remove(getPanel());
			getArrowBtn().setIcon(iconArrow[COLLAPSED]);
			border = new CollapsableTitledBorder(collapsedBorderLine, titleComponent);
		} else {
			// expand the panel, add content and set border to titled border
			add(getPanel(), BorderLayout.NORTH);
			getArrowBtn().setIcon(iconArrow[EXPANDED]);
			border = new CollapsableTitledBorder(expandedBorderLine, titleComponent);
		}
		setBorder(border);
		collapsed = collapse;
		updateUI();
	}

	/**
	 * Returns the current state of the panel, collapsed (true) or expanded
	 * (false).
	 * 
	 * @return collapsed Returns true if the panel is collapsed and false if it
	 *         is expanded
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
		ImageIcon[] iconArrow = new ImageIcon[2];
		URL iconURL;
		iconURL = MCODEResources.getUrl(ImageName.ARROW_COLLAPSED);

		if (iconURL != null) {
			iconArrow[COLLAPSED] = new ImageIcon(iconURL);
		}
		
		iconURL = MCODEResources.getUrl(ImageName.ARROW_EXPANDED);

		if (iconURL != null) {
			iconArrow[EXPANDED] = new ImageIcon(iconURL);
		}
		return iconArrow;
	}

	/**
	 * Handles expanding and collapsing of extra content on the user's click of
	 * the titledBorder component.
	 */
	private final class ExpandAndCollapseAction extends AbstractAction implements ActionListener, ItemListener {
		private static final long serialVersionUID = 2010434345567315525L;

		public void actionPerformed(ActionEvent e) {
			setCollapsed(!isCollapsed());
		}

		public void itemStateChanged(ItemEvent e) {
			setCollapsed(!isCollapsed());
		}
	}

	/**
	 * Special titled border that includes a component in the title area
	 */
	private final class CollapsableTitledBorder extends TitledBorder {
		
		private static final long serialVersionUID = 2010434345567315526L;
		JComponent component;

		public CollapsableTitledBorder(Border border, JComponent component) {
			this(border, component, LEFT, TOP);
		}

		public CollapsableTitledBorder(Border border, JComponent component, int titleJustification, int titlePosition) {
			// TitledBorder needs border, title, justification, position, font,
			// and color
			super(border, null, titleJustification, titlePosition, null, null);
			this.component = component;
			
			if (this.component != null)
				this.component.setOpaque(false);
			
			if (border == null)
				this.border = super.getBorder();
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Rectangle borderR = new Rectangle(x + EDGE_SPACING, y + EDGE_SPACING, width - (EDGE_SPACING * 2), height
					- (EDGE_SPACING * 2));
			Insets borderInsets;
			if (border != null) {
				borderInsets = border.getBorderInsets(c);
			} else {
				borderInsets = new Insets(0, 0, 0, 0);
			}

			Rectangle rect = new Rectangle(x, y, width, height);
			Insets insets = getBorderInsets(c);
			Rectangle compR = getComponentRect(rect, insets);
			int diff;
			switch (titlePosition) {
			case ABOVE_TOP:
				diff = compR.height + TEXT_SPACING;
				borderR.y += diff;
				borderR.height -= diff;
				break;
			case TOP:
			case DEFAULT_POSITION:
				diff = insets.top / 2 - borderInsets.top - EDGE_SPACING;
				borderR.y += diff;
				borderR.height -= diff;
				break;
			case BELOW_TOP:
			case ABOVE_BOTTOM:
				break;
			case BOTTOM:
				diff = insets.bottom / 2 - borderInsets.bottom - EDGE_SPACING;
				borderR.height -= diff;
				break;
			case BELOW_BOTTOM:
				diff = compR.height + TEXT_SPACING;
				borderR.height -= diff;
				break;
			}
			border.paintBorder(c, g, borderR.x, borderR.y, borderR.width, borderR.height);
			
			if (!aquaBorder) {
				Color col = g.getColor();
				g.setColor(c.getBackground());
				g.fillRect(compR.x, compR.y, compR.width, compR.height);
				g.setColor(col);
			}
		}

		@Override
		public Insets getBorderInsets(Component c, Insets insets) {
			Insets borderInsets;
			if (border != null) {
				borderInsets = border.getBorderInsets(c);
			} else {
				borderInsets = new Insets(0, 0, 0, 0);
			}
			insets.top = EDGE_SPACING + TEXT_SPACING + borderInsets.top;
			insets.right = EDGE_SPACING + TEXT_SPACING + borderInsets.right;
			insets.bottom = EDGE_SPACING + TEXT_SPACING + borderInsets.bottom;
			insets.left = EDGE_SPACING + TEXT_SPACING + borderInsets.left;

			if (c == null || component == null) {
				return insets;
			}

			int compHeight = component.getPreferredSize().height;

			switch (titlePosition) {
			case ABOVE_TOP:
				insets.top += compHeight + TEXT_SPACING;
				break;
			case TOP:
			case DEFAULT_POSITION:
				insets.top += Math.max(compHeight, borderInsets.top) - borderInsets.top;
				break;
			case BELOW_TOP:
				insets.top += compHeight + TEXT_SPACING;
				break;
			case ABOVE_BOTTOM:
				insets.bottom += compHeight + TEXT_SPACING;
				break;
			case BOTTOM:
				insets.bottom += Math.max(compHeight, borderInsets.bottom) - borderInsets.bottom;
				break;
			case BELOW_BOTTOM:
				insets.bottom += compHeight + TEXT_SPACING;
				break;
			}
			return insets;
		}

		public Rectangle getComponentRect(Rectangle rect, Insets borderInsets) {
			Dimension compD = component.getPreferredSize();
			Rectangle compR = new Rectangle(0, 0, compD.width, compD.height);
			switch (titlePosition) {
			case ABOVE_TOP:
				compR.y = EDGE_SPACING;
				break;
			case TOP:
			case DEFAULT_POSITION:
				if (titleComponent instanceof JButton) {
					compR.y = EDGE_SPACING + (borderInsets.top - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
				} else if (titleComponent instanceof JRadioButton) {
					compR.y = (borderInsets.top - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
				}
				break;
			case BELOW_TOP:
				compR.y = borderInsets.top - compD.height - TEXT_SPACING;
				break;
			case ABOVE_BOTTOM:
				compR.y = rect.height - borderInsets.bottom + TEXT_SPACING;
				break;
			case BOTTOM:
				compR.y = rect.height - borderInsets.bottom + TEXT_SPACING
						+ (borderInsets.bottom - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
				break;
			case BELOW_BOTTOM:
				compR.y = rect.height - compD.height - EDGE_SPACING;
				break;
			}
			switch (titleJustification) {
			case LEFT:
			case DEFAULT_JUSTIFICATION:
				// compR.x = TEXT_INSET_H + borderInsets.left;
				compR.x = TEXT_INSET_H + borderInsets.left - EDGE_SPACING;
				break;
			case RIGHT:
				compR.x = rect.width - borderInsets.right - TEXT_INSET_H - compR.width;
				break;
			case CENTER:
				compR.x = (rect.width - compR.width) / 2;
				break;
			}
			return compR;
		}
	}

	/**
	 * Sets the tooltip text of this MCODECollapsiblePanel.
	 * 
	 * @param text
	 *            The string to set as the tooltip.
	 */
	@Override
	public void setToolTipText(final String text) {
		super.setToolTipText(text);
		titleComponent.setToolTipText(text);
	}
	
	private void maybeSetTransparent(final Component comp) {
		if (aquaBorder && comp instanceof JPanel)
			((JPanel)comp).setOpaque(false);
	}
}
