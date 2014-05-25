package org.sustudio.concise.app.gear.wordClouder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.gef4.cloudio.IEditableCloudLabelProvider;
import org.eclipse.gef4.cloudio.TagCloud;
import org.eclipse.gef4.cloudio.TagCloudViewer;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.sustudio.concise.app.helper.ZoomHelper;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.wordlister.Word;

public class WordClouder extends GearController {

	private TagCloudViewer viewer;
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
		
		TagCloud wordCloud = new TagCloud(cloudComp, SWT.HORIZONTAL | SWT.VERTICAL | SWT.BORDER);
		wordCloud.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// scrolling empty TagCloud will crash (some listener bugs)...
		// so, we just disable it.
		// this will be re-enabled after calling resetCloud(List<WordStats>).
		wordCloud.setEnabled(false);
		
		labelProvider = new CloudLabelProvider();
		viewer = new TagCloudViewer(wordCloud);
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {	}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				List<?> list = (List<?>) newInput;
				if (list == null || list.size() == 0) return;
				labelProvider.setMaxOccurrences( ((Word) list.get(0) ).totalTermFreq);
				int minIndex = Math.min(list.size() - 1, ((TagCloudViewer) viewer).getMaxWords());
				labelProvider.setMinOccurrences( ((Word) list.get(minIndex) ).totalTermFreq);
				labelProvider.setMaxWords(((TagCloudViewer) viewer).getMaxWords());
				labelProvider.setInput(newInput);
			}
			
			public Object[] getElements(Object inputElement) {
				return ((List<?>)inputElement).toArray();
			}
			
		});
		viewer.getCloud().addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				viewer.getCloud().zoomFit();
			}
		});
		
		final ScrolledComposite sc = new ScrolledComposite(sash, SWT.V_SCROLL | SWT.H_SCROLL);
		final CloudOptionsComposite options = new CloudOptionsComposite(sc, SWT.NONE, viewer);
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
		return viewer.getCloud();
	}
	
	protected void setZoomableControls() {
		ZoomHelper.addControls(new Control[] { viewer.getCloud() });
	}
	
	public Control[] getZoomableControls() {
		return new Control[] { viewer.getCloud() };
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
		viewer.setInput(data);
		viewer.getCloud().setEnabled(true);
		setStatusText("Showing top " + Formats.getNumberFormat(viewer.getMaxWords()) + " words.");
		spinner.close();
	}
	
	
	public class CloudLabelProvider extends BaseLabelProvider implements IEditableCloudLabelProvider {

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

		@Override
		public String getLabel(Object element) {
			return ((Word)element).word;
		}
		
		@Override
		public double getWeight(Object element) {
			double count  = Math.log(((Word)element).totalTermFreq - minOccurrences+1);
			count /= (Math.log(maxOccurrences));
			return count;
		}

		@Override
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
		
		@Override
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

		@Override
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

		@Override
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
