package org.sustudio.concise.app.gear.wordClouder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef4.cloudio.CloudWord;
import org.eclipse.gef4.cloudio.WordCloud;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLUtils;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.thread.WordListerThread;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.wordlister.Word;

/**
 * 用來顯示文字雲的 controller
 * 
 * @author Kuan-ming Su
 *
 */
public class WordClouder extends GearController {

	private WordCloud wordCloud;
	private CloudLabelProvider labelProvider;
	private List<Word> words = new ArrayList<Word>();
	
	public WordClouder() {
		super(CABox.GearBox, Gear.WordClouder);
	}

	@Override
	protected Control createControl() {
		SashForm sash = new SashForm(this, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite cloudComp = new Composite(sash, SWT.NONE);
		cloudComp.setLayout(new GridLayout());
		cloudComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		labelProvider = new CloudLabelProvider();
		
		wordCloud = new WordCloud(cloudComp, SWT.HORIZONTAL | SWT.VERTICAL | SWT.BORDER);
		wordCloud.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
	
	public void loadData() {
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		Thread t = new Thread() {
			public void run() {
				words.clear();
				final int maxWords = 2000;
				try {
					// WordClouder 和 WordLister 在資料庫中用同樣一個資料表
					SQLiteDB.createTableIfNotExists(CATable.WordLister);
					
					String sql = SQLUtils.selectSyntax(CATable.WordLister, DBColumn.Freq);
					ResultSet rs = SQLiteDB.executeQuery(sql, maxWords);
					while (rs.next()) {
						String word = rs.getString(DBColumn.Word.columnName());
						int docFreq = rs.getInt(DBColumn.DocFreq.columnName());
						long freq = rs.getLong(DBColumn.Freq.columnName());
						words.add(new Word(word, docFreq, freq));
					}
				} catch (Exception e) {
					workspace.logError(gear, e);
					Dialog.showException(e);
				}
				
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						reLayout();
						setStatusText("Showing top " + Formats.getNumberFormat(CAPrefs.CLOUDER_MAX_WORDS) + " words.");
						spinner.close();
					}
				});
			}
		};
		t.setDaemon(true);
		t.start();
		super.loadData();
	}
	
	protected void unloadData() {
		super.unloadData();
		words.clear();
	}
	
	@Override
	public void doit(CAQuery query) {
		ConciseThread thread = new WordListerThread(query);
		thread.start();
	}
	
	public void reLayout() {
		if (words.size() < 1) return;
		
		labelProvider.setMaxOccurrences( (words.get(0) ).totalTermFreq);
		int minIndex = Math.min(words.size() - 1, CAPrefs.CLOUDER_MAX_WORDS);
		labelProvider.setMinOccurrences( (words.get(minIndex) ).totalTermFreq);
		labelProvider.setInput(words);
		
		final List<CloudWord> cloudWords = new ArrayList<CloudWord>();
		short i = 0;
		for (Word element : this.words) {
			CloudWord word = new CloudWord(labelProvider.getLabel(element));
			word.setColor(labelProvider.getColor(element));
			word.weight = labelProvider.getWeight(element);
			word.setFontData(labelProvider.getFontData(element));
			word.angle = labelProvider.getAngle(element);
			word.data = element;
			cloudWords.add(word);
			i++;
			word.id = i;
			if(i == CAPrefs.CLOUDER_MAX_WORDS) break;
		}
		wordCloud.setWords(cloudWords);
	}
	
	/**
	 * 傳回 WordCloud 物件
	 * @return
	 */
	public WordCloud getCloud() {
		return wordCloud;
	}
	
	/**
	 * 傳回 CloudLabelProvider
	 * @return
	 */
	public CloudLabelProvider getLabelProvider() {
		return labelProvider;
	}
	
}
