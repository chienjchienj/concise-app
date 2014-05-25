package org.sustudio.concise.app.utils;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorImage {

	public static Image createImage(RGB rgb, Point size) {
		return createImage(rgb, size.x, size.y);
	}
	
	public static Image createImage(RGB rgb, int x, int y) {
		Color color = new Color(Display.getCurrent(), rgb);
		Image image;
		image = new Image(Display.getCurrent(), x, y);
		GC gc = new GC(image);
		gc.setBackground(color);
		gc.fillRoundRectangle(0, 0, x, y, 3, 3);
		color.dispose();
		gc.dispose();
		return image;
	}
	
	public static Image createImage(Color color, Point size) {
		return createImage(color.getRGB(), size.x, size.y);
	}
	
	public static Image createImage(Color color, int x, int y) {
		return createImage(color.getRGB(), x, y);
	}
		
}
