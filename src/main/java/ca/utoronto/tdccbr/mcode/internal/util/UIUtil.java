package ca.utoronto.tdccbr.mcode.internal.util;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public final class UIUtil {
	
	private UIUtil() {}
	
	public static Border createTitledBorder(final String title) {
		final Border border;
		
		if (title == null || title.isEmpty()) {
			final Border aquaBorder = isAquaLAF() ? UIManager.getBorder("InsetBorder.aquaVariant") : null;
			border = aquaBorder != null ? aquaBorder : BorderFactory.createTitledBorder("SAMPLE").getBorder();
		} else {
			final Border aquaBorder = isAquaLAF() ? UIManager.getBorder("TitledBorder.aquaVariant") : null;
			final TitledBorder tb = aquaBorder != null ?
					BorderFactory.createTitledBorder(aquaBorder, title) : BorderFactory.createTitledBorder(title);
			border = tb;
		}
		
		return border;
	}
	
	public static Border getLookAndFeelBorder() {
		// Try to create Aqua recessed borders on Mac OS
		Border border = isAquaLAF() ? UIManager.getBorder("InsetBorder.aquaVariant") : null;
		
		if (border == null) {
			if (isWinLAF())
				border = new TitledBorder("");
			else
				border = BorderFactory.createTitledBorder("SAMPLE").getBorder();
		}
			
		if (border == null)
			border = BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"));
		
		return border;
	}
	
	public static boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac OS X");
	}
	
	public static boolean isAquaLAF() {
		return UIManager.getLookAndFeel() != null &&
				"com.apple.laf.AquaLookAndFeel".equals(UIManager.getLookAndFeel().getClass().getName());
	}
	
	public static boolean isNimbusLAF() {
		return UIManager.getLookAndFeel() != null &&
				"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel".equals(UIManager.getLookAndFeel().getClass().getName());
	}
	
	public static boolean isWinLAF() {
		return UIManager.getLookAndFeel() != null &&
				"com.sun.java.swing.plaf.windows.WindowsLookAndFeel".equals(UIManager.getLookAndFeel().getClass().getName());
	}
}
