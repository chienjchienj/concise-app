package org.sustudio.concise.app.utils;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class LabelFont {

	private static Font font;
	
	public static Font getFont() {
		if (font == null) {
			FontData fd = Display.getDefault().getSystemFont().getFontData()[0];
			fd.setHeight(fd.getHeight() - 2);
			font = new Font(Display.getDefault(), fd);
		}
		return font;
	}
	
	public static int getFontSize() {
		return getFont().getFontData()[0].getHeight();
	}
	
}
