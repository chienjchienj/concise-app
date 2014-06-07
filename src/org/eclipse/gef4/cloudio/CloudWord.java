/*******************************************************************************
* Copyright (c) 2011 Stephan Schwiebert. All rights reserved. This program and
* the accompanying materials are made available under the terms of the Eclipse
* Public License v1.0 which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* <p/>
* Contributors: Stephan Schwiebert - initial API and implementation
*******************************************************************************/
package org.eclipse.gef4.cloudio;

import org.eclipse.gef4.cloudio.util.RectTree;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;

/**
 * Helper class which stores all data
 * required to render an element.
 * @author sschwieb
 * 
 * Renamed class to fit Concise App
 * @author Kuan-ming Su
 *
 */
public class CloudWord {
	
	public CloudWord(String string) {
		this.string = string;
	}

	public final String string;
	
	public double weight;

	public int x;

	public int y;

	private Color color;
	
	public RectTree tree;

	public float angle;

	private FontData[] fontData;

	public FontData[] getFontData() {
		return fontData;
	}

	public void setFontData(FontData[] fontData) {
		this.fontData = fontData.clone();
	}

	public short id;

	public int height;

	public int width;

	public Object data;

	public Point stringExtent;
	
	@Override
	public String toString() {
		return string;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}

}
