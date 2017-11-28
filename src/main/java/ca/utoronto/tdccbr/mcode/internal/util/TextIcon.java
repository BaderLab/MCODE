package ca.utoronto.tdccbr.mcode.internal.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.UIManager;

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

public class TextIcon implements Icon {
	
	private final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);
	
	private final String text;
	private final Font font;
	private final Color color;
	private final int width;
	private final int height;
	
	public TextIcon(String text, Font font, Color color, int width, int height) {
		this.text = text;
		this.font = font;
		this.color = color != null ? color : UIManager.getColor("CyColor.complement(-2)");
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        
        int xx = c.getWidth();
        int yy = c.getHeight();
        g2d.setPaint(TRANSPARENT_COLOR);
        g2d.fillRect(0, 0, xx, yy);
        
        Color fg = color;
        
        if (c instanceof AbstractButton) {
        	if (!c.isEnabled())
        		fg = UIManager.getColor("Label.disabledForeground");
        	else if (((AbstractButton) c).getModel().isPressed())
        		fg = fg.darker();
        }
        
        g2d.setPaint(fg);
        g2d.setFont(font);
        drawText(g2d, x, y);
        
        g2d.dispose();
	}
	
	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
	
	private void drawText(Graphics g, int x, int y) {
		FontMetrics fm = g.getFontMetrics(font);
		Rectangle2D rect = fm.getStringBounds(text, g);

		int textHeight = (int) rect.getHeight();
		int textWidth = (int) rect.getWidth();

		// Center text horizontally and vertically
		int xx = x + (getIconWidth() - textWidth) / 2;
		int yy = y + (getIconHeight() - textHeight) / 2 + fm.getAscent();

		g.drawString(text, xx, yy);
	}
}
