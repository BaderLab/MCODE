package ca.utoronto.tdccbr.mcode.internal.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

public abstract class IconUtil {
	
	public static final String MCODE_ICON_LAYER_1 = "a";
	public static final String MCODE_ICON_LAYER_2 = "b";
	
	public static final String[] LAYERED_MCODE_ICON = new String[] { MCODE_ICON_LAYER_1, MCODE_ICON_LAYER_2 };
	public static final Color[] MCODE_ICON_COLORS = new Color[] { new Color(120, 0, 0), new Color(178, 24, 43) };
	
	private static Font iconFont;

	static {
		try {
			iconFont = Font.createFont(Font.TRUETYPE_FONT, IconUtil.class.getResourceAsStream("/fonts/mcode.ttf"));
		} catch (FontFormatException e) {
			throw new RuntimeException();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static Font getIconFont(float size) {
		return iconFont.deriveFont(size);
	}

	private IconUtil() {
		// ...
	}
}
