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
			final Border aquaBorder = UIManager.getBorder("InsetBorder.aquaVariant");
			border = aquaBorder != null ? aquaBorder : BorderFactory.createTitledBorder("SAMPLE").getBorder();
		} else {
			final Border aquaBorder = UIManager.getBorder("TitledBorder.aquaVariant");
			final TitledBorder tb = aquaBorder != null ?
					BorderFactory.createTitledBorder(aquaBorder, title) : BorderFactory.createTitledBorder(title);
			border = tb;
		}
		
		return border;
	}
}
