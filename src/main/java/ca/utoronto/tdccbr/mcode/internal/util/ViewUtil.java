package ca.utoronto.tdccbr.mcode.internal.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;

public class ViewUtil {

	public static ImageIcon createSpinnerIcon() {
		URL url = MCODEUtil.class.getClassLoader().getResource("img/lines-spinner-32.gif");
		
		return new ImageIcon(url);
	}
	
	public static void styleHeaderButton(AbstractButton btn, Font font) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		
		int h = new JComboBox<>().getPreferredSize().height;
		btn.setMinimumSize(new Dimension(h, h));
		btn.setPreferredSize(new Dimension(h, h));
	}
	
	public static Color setBrightness(Color color, float brightness) {
		if (brightness < 0f || brightness > 1f)
			throw new IllegalArgumentException("Invalid brightness value");

		int alpha = color.getAlpha();

		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		Color c = Color.getHSBColor(hsb[0], hsb[1], brightness);

		return setAlpha(c, alpha);
	}

	public static Color setAlpha(Color color, int alpha) {
		if (alpha < 0 || alpha > 255)
			throw new IllegalArgumentException("Invalid alpha value");

		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
	
	public static void recursiveDo(Component component, Consumer<JComponent> c) {
		if (component instanceof JComponent)
			c.accept((JComponent) component);
		
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents())
				recursiveDo(child, c);
		}
	}
	
	/**
	 * Utility method that invokes the code in Runnable.run on the AWT Event Dispatch Thread.
	 * @param runnable
	 */
	public static void invokeOnEDT(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}
	
	public static void invokeOnEDTAndWait(Runnable runnable) {
		invokeOnEDTAndWait(runnable, null);
	}
	
	public static void invokeOnEDTAndWait(Runnable runnable, Logger logger) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (Exception e) {
				if (logger != null)
					logger.error("Unexpected error", e);
				else
					e.printStackTrace();
			}
		}
	}
	
	private ViewUtil() {
		// Nothing to do here...
	}
}
