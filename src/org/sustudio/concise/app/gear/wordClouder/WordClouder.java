package org.sustudio.concise.app.gear.wordClouder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLUtils;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.helper.SaveOutputHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.thread.WordListerThread;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.wordlister.Word;

public class WordClouder extends GearController {

	private WordCloud wordCloud;
	private CloudLabelProvider labelProvider;
	
	public WordClouder() {
		super(CABox.GearBox, Gear.WordClouder);
		
		Thread thread = new Thread("Loading Cloud Data") { public void run() {
			try {
				if (SQLiteDB.tableExists(CATable.WordLister)) {
					setCloudData();
				}
			} catch (Exception e) {
				CAErrorMessageDialog.open(getGear(), e);
			}
		} };
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	protected Control createControl() {
		SashForm sash = new SashForm(this, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite cloudComp = new Composite(sash, SWT.NONE);
		cloudComp.setLayout(new GridLayout());
		cloudComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		wordCloud = new WordCloud(cloudComp, SWT.HORIZONTAL | SWT.VERTICAL | SWT.BORDER);
		wordCloud.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// scrolling empty TagCloud will crash (some listener bugs)...
		// so, we just disable it.
		// this will be re-enabled after calling resetCloud(List<WordStats>).
		//wordCloud.setEnabled(false);
		wordCloud.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseExit(MouseEvent e) {
				wordCloud.setToolTipText(null);
			}
			
			@Override
			public void mouseEnter(MouseEvent e) {
				CloudWord word = (CloudWord) e.data;
				wordCloud.setToolTipText(labelProvider.getToolTip(word.data));
			}
		});
		wordCloud.addGestureListener(new GestureListener() {
			@Override
			public void gesture(GestureEvent event) {
				if (event.detail == SWT.GESTURE_MAGNIFY) {
					if (event.magnification > 1) {
						wordCloud.zoomIn();
					}
					else if (event.magnification < 1) {
						wordCloud.zoomOut();
					}
				}
			}
			
		});
		
		
		labelProvider = new CloudLabelProvider();
		
		wordCloud.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				wordCloud.zoomFit();
			}
		});
		
		final ScrolledComposite sc = new ScrolledComposite(sash, SWT.V_SCROLL | SWT.H_SCROLL);
		final CloudOptionsComposite options = new CloudOptionsComposite(sc, SWT.NONE, this);
		sc.setContent(options);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				sc.setMinSize(options.computeSize(sc.getClientArea().width, SWT.DEFAULT));
			}
		});
		
		sash.setWeights(new int[] {70,30});

		return sash;
	}
	
	public Control getControl() {
		return wordCloud;
	}
	
	public Control[] getZoomableControls() {
		return new Control[] { wordCloud };
	}
	
	public void setCloudData() {
		final List<Word> data = new ArrayList<Word>();
		final int maxWords = 2000;
		
		try {
			
			String sql = SQLUtils.selectSyntax(CATable.WordLister, DBColumn.Freq);
			ResultSet rs = SQLiteDB.executeQuery(sql, maxWords);
			sql = null;
			
			while (rs.next()) {
				String word = rs.getString(DBColumn.Word.columnName());
				int docFreq = rs.getInt(DBColumn.DocFreq.columnName());
				long freq = rs.getLong(DBColumn.Freq.columnName());
				data.add(new Word(word, docFreq, freq));
			}
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		}
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				resetCloud(data);
				SaveOutputHelper.listenTo(getGear());
			}
		});
	}
	
	
	private void resetCloud(List<Word> data) {
		CASpinner spinner = new CASpinner(this);
		spinner.open();
		// TODO rewrite
		setInput(data);
		wordCloud.setEnabled(true);
		setStatusText("Showing top " + Formats.getNumberFormat(CAPrefs.CLOUDER_MAX_WORDS) + " words.");
		spinner.close();
	}
	
	@Override
	public void doit(CAQuery query) {
		ConciseThread thread = new WordListerThread(query);
		thread.start();
	}
	
	private List<Word> input;
	
	public void setInput(List<Word> data) {
		input = data;
		labelProvider.setMaxOccurrences( (data.get(0) ).totalTermFreq);
		int minIndex = Math.min(data.size() - 1, CAPrefs.CLOUDER_MAX_WORDS);
		labelProvider.setMinOccurrences( (data.get(minIndex) ).totalTermFreq);
		labelProvider.setMaxWords(CAPrefs.CLOUDER_MAX_WORDS);
		labelProvider.setInput(data);
		
		List<CloudWord> words = new ArrayList<CloudWord>();
		short i = 0;
		for (Word element : data) {
			CloudWord word = new CloudWord(labelProvider.getLabel(element));
			word.setColor(labelProvider.getColor(element));
			word.weight = labelProvider.getWeight(element);
			word.setFontData(labelProvider.getFontData(element));
			word.angle = labelProvider.getAngle(element);
			word.data = element;
			words.add(word);
			i++;
			word.id = i;
			if(i == CAPrefs.CLOUDER_MAX_WORDS) break;
		}
		wordCloud.setWords(words);
	}
	
	/**
	 * Resets the {@link WordCloud}. If <code>recalc</code> is
	 * <code>true</code>, the displayed elements will be updated
	 * with the values provided by used {@link ICloudLabelProvider}.
	 * Otherwise, the cloud will only be re-layouted, keeping fonts,
	 * colors and angles untouched.
	 * @param recalc
	 */
	public void reset(boolean recalc) {
		wordCloud.layoutCloud(recalc);
	}
	
	public List<Word> getInput() {
		return input;
	}
	
	public WordCloud getCloud() {
		return wordCloud;
	}
	
	public CloudLabelProvider getLabelProvider() {
		return labelProvider;
	}
	
	
	public class CloudLabelProvider {

		private double maxOccurrences;
		private double minOccurrences;
		
		private int maxWords;
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
				index = Math.round((float) index * (float) (colorList.size() - 1) / (float) maxWords);
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
				index = Math.round((float) index * (float) (fontList.size() - 1) / (float) maxWords);
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
		 * Max words that will display.
		 * @param maxWords		max words.
		 */
		public void setMaxWords(int maxWords) {
			this.maxWords = maxWords;
		}

		/**
		 * Provides a sorted input (entryList of a CFreqList)
		 * @param input		sorted input
		 */
		public void setInput(Object input) {
			this.input = input;
		}
		
	}
	
	
}
