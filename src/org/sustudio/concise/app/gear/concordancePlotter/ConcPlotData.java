package org.sustudio.concise.app.gear.concordancePlotter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.preferences.CAPrefs;

public class ConcPlotData {

	public int docID;
	public long[] positions;
	public long hits;
	public long words;
	public double per1000;
	public String filepath;
	
	public ArrayList<String> searchWords = new ArrayList<String>();
	private HashMap<Long, String> positionWordMapping = new HashMap<Long, String>();
	
	public void setPositions(String positionString) {
		if (positionString != null) {
			positions = new long[0];
			StringTokenizer st = new StringTokenizer(positionString, "\t");
			while (st.hasMoreTokens()) {
				String data = st.nextToken();
				
				// 位置資料的格式 word:position (生活:868)
				String word = data.substring(0, data.lastIndexOf(':'));
				long p = Long.valueOf(data.substring(data.lastIndexOf(':') + 1));
				positions = ArrayUtils.add(positions, p);
				positionWordMapping.put(p, word);
				if (!searchWords.contains(word)) {
					searchWords.add(word);
				}
			}
		}
	}
	
	/**
	 * 透過位置傳回相對應的字
	 * @param position
	 * @return
	 */
	public String getWordByPosition(long position) {
		return positionWordMapping.get(Long.valueOf(position));
	}
	
	public Image getPlotImage(int plotWidth, int plotHeight) {
		plotWidth = plotWidth == SWT.DEFAULT ? 400 : plotWidth;
		plotHeight = plotHeight == SWT.DEFAULT ? 18 : plotHeight;
		
		Image image = new Image(Display.getCurrent(), plotWidth, plotHeight);
		GC gc = new GC(image);
		gc = drawPlot(gc);
		gc.dispose();
		return image;
	}
	
	public GC drawPlot(final GC gc) {
		return drawPlot(gc, false);
	}
	
	public GC drawPlot(final GC gc, final boolean paintBackground) {
		final Rectangle rect = gc.getClipping();
		final Color originalBackground = gc.getBackground();
		//final Color originalForeground = gc.getForeground();
		
		if (paintBackground) {
			gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			gc.fillRectangle(rect);
		}
		//int lineWidth = Math.round( (float) rect.width / (float) words);
		//if (lineWidth < 1) lineWidth = 1;
		
		for (String searchWord : searchWords) {
			// reduce data
			HashMap<Integer, Integer> data = new HashMap<Integer, Integer>();
			for (long p : positions) {
				if (positionWordMapping.get(Long.valueOf(p)).equals(searchWord)) {
					int posX = Math.round( rect.width * p / words );
					Integer count = data.get(posX);
					if (count == null)
						data.put(posX, 1);
					else
						data.put(posX, count + 1);
				}
			}
			
			gc.setBackground(getWordColor(searchWord));
			
			// draw lines
			for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
				int posX = entry.getKey();
				int alpha = 255;
				if (entry.getValue() == 1) alpha = 95;
				else if (entry.getValue() == 2) alpha = 191;
				gc.setAlpha(alpha);
				gc.fillOval(rect.x+posX-(rect.height-2)/2, rect.y, rect.height-2, rect.height-2);
			}
			data.clear();
			data = null;
		}
		
		gc.setBackground(originalBackground);
		//gc.setForeground(originalForeground);
		
		return gc;
	}
	
	
	private Color getWordColor(String word) {
		int colorIndex = searchWords.indexOf(word) % CAPrefs.HIGHLIGH_BG_COLOR_SCHEME.length;
		RGB rgb = CAPrefs.HIGHLIGH_BG_COLOR_SCHEME[colorIndex];
		return new Color(Display.getCurrent(), rgb);
	}
	
	
}
