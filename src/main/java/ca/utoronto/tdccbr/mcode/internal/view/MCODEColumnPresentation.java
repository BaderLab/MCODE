package ca.utoronto.tdccbr.mcode.internal.view;

import static ca.utoronto.tdccbr.mcode.internal.util.IconUtil.LAYERED_MCODE_ICON;
import static ca.utoronto.tdccbr.mcode.internal.util.IconUtil.MCODE_ICON_COLORS;
import static ca.utoronto.tdccbr.mcode.internal.util.IconUtil.getIconFont;

import javax.swing.Icon;

import org.cytoscape.application.swing.CyColumnPresentation;
import org.cytoscape.util.swing.TextIcon;

public class MCODEColumnPresentation implements CyColumnPresentation {

	private final Icon icon; 
	
	public MCODEColumnPresentation() {
		icon = new TextIcon(LAYERED_MCODE_ICON, getIconFont(14.0f), MCODE_ICON_COLORS, 16, 16);
	}
	
	@Override
	public Icon getNamespaceIcon() {
		return icon;
	}

	@Override
	public String getNamespaceDescription() {
		return null;
	}
}
