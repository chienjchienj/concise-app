package org.sustudio.concise.app.gear.wordClouder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.core.wordlister.Word;

public class CloudLabelProvider {

	private double maxOccurrences;
	private double minOccurrences;
	
	private Object input;
	
	private Map<Object, Color> colors = new HashMap<Object, Color>();
	private Map<Object, FontData[]> fonts = new HashMap<Object, FontData[]>();
	private Random random = new Random();
	protected List<Color> colorList;
	protected List<Font> fontList;
	protected List<Float> angles;
		
	public CloudLabelProvider() {
		colorList = new ArrayList<Color>();
		fontList = new ArrayList<Font>();
		angles = new ArrayList<Float>();
		angles.add(0F);
	}

	public String getLabel(Object element) {
		return ((Word)element).word;
	}
	
	public double getWeight(Object element) {
		double count  = Math.log(((Word)element).totalTermFreq - minOccurrences+1);
		count /= (Math.log(maxOccurrences));
		return count;
	}

	public Color getColor(Object element) {
		Color color = colors.get(element);
		if(color == null) {
			int index = ((List<?>) input).indexOf(element);
			index = Math.round((float) index * (float) (colorList.size() - 1) / (float) CAPrefs.CLOUDER_MAX_WORDS);
			color = colorList.get(index);
			//color = colorList.get(random.nextInt(colorList.size()));
			colors.put(element, color);
		}
		return color;
	}
	
	public FontData[] getFontData(Object element) {
		FontData[] data = fonts.get(element);
		if(data == null) {
			int index = ((List<?>) input).indexOf(element);
			index = Math.round((float) index * (float) (fontList.size() - 1) / (float) CAPrefs.CLOUDER_MAX_WORDS);
			data = fontList.get(index).getFontData();
			//data = fontList.get(random.nextInt(fontList.size())).getFontData();
			fonts.put(element, data);
		}
		return data;
	}
	
	public void setMaxOccurrences(long occurrences) {
		this.maxOccurrences = (double) occurrences;
	}

	public void setMinOccurrences(long occurrences) {
		this.minOccurrences = (double) occurrences;
	}
	
	public void dispose() {
		for (Color color : colorList) {
			color.dispose();
		}
		for (Font font : fontList) {
			font.dispose();
		}
	}
	
	public void setAngles(List<Float> angles) {
		this.angles = angles;
	}

	public float getAngle(Object element) {
		float angle = angles.get(random.nextInt(angles.size()));
		return angle;
	}
	
	public void setColors(RGB[] rgbs) {
		setColors(Arrays.asList(rgbs));
	}
	
	public void setColors(List<RGB> newColors) {
		if(newColors.isEmpty()) return;
		for (Color color : colorList) {
			color.dispose();
		}
		colorList.clear();
		colors.clear();
		for (RGB color : newColors) {
			Color c = new Color(Display.getDefault(), color);
			colorList.add(c);
		}
	}

	public void setFonts(List<FontData> newFonts) {
		if(newFonts.isEmpty()) return;
		for (Font font : fontList) {
			font.dispose();
		}
		fontList.clear();
		fonts.clear();
		for (FontData data : newFonts) {
			Font f = new Font(Display.getDefault(), data);
			fontList.add(f);
		}
	}

	public String getToolTip(Object element) {
		Word ws = (Word) element;
		return ws.word + " (" + Formats.getNumberFormat(ws.totalTermFreq) + ")";
	}
	
	/**
	 * Provides a sorted input (entryList of a CFreqList)
	 * @param input		sorted input
	 */
	public void setInput(Object input) {
		this.input = input;
	}
	
}
