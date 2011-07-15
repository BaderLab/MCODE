package org.cytoscape.mcode.internal.util;

import java.net.URL;

public class MCODEResources {

	public static enum Image {
		LOGO("/img/logo1.png"),
		LOGO_SMALL("/img/logo2.png"),
		LOGO_SIMPLE("/img/logo.png"),
		ARROW_EXPANDED("/img/arrow_expanded.gif"),
		ARROW_COLLAPSED("/img/arrow_collapsed.gif"),
		NOTE("/img/note.gif");

		private final String name;

		private Image(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public URL getUrl(Image img) {
		return this.getClass().getResource(img.toString());
	}

}
