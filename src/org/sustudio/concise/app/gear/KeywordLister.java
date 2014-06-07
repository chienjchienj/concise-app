package org.sustudio.concise.app.gear;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sustudio.concise.app.db.CADataUtils;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.thread.CAKeywordThread;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.keyword.Keyword;

public class KeywordLister 
	   extends GearController 
	   implements IGearSortable, IGearTableBased, IGearFilterable {

	private Table table;
	
	private DBColumn sortDBColumn = CAPrefs.KEYNESS.dbColumn();
	private int keywordsCount = 0;
	private int positiveKeywordsCount = 0;
		
	public KeywordLister() {
		super(CABox.GearBox, Gear.KeywordLister);
	}
	
	@Override
	public Control createControl() {
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setMoveable(true);
		tableColumn.setWidth(100);
		tableColumn.setText("#");
		
		final TableColumn tblclmnFreq = new TableColumn(table, SWT.RIGHT);
		tblclmnFreq.setMoveable(true);
		tblclmnFreq.setWidth(100);
		tblclmnFreq.setText("Freq");
		tblclmnFreq.setData(_DB_COLUMN, DBColumn.Freq);
		tblclmnFreq.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnPct = new TableColumn(table, SWT.RIGHT);
		tblclmnPct.setMoveable(true);
		tblclmnPct.setWidth(100);
		tblclmnPct.setText("%");
		tblclmnPct.setData(_DB_COLUMN, DBColumn.Percent);
		tblclmnPct.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnRefFreq = new TableColumn(table, SWT.RIGHT);
		tblclmnRefFreq.setMoveable(true);
		tblclmnRefFreq.setWidth(100);
		tblclmnRefFreq.setText("R.Freq");
		tblclmnRefFreq.setData(_DB_COLUMN, DBColumn.Percent);
		tblclmnRefFreq.addSelectionListener(columnSortListener);
				
		final TableColumn tblclmnRefPct = new TableColumn(table, SWT.RIGHT);
		tblclmnRefPct.setMoveable(true);
		tblclmnRefPct.setWidth(100);
		tblclmnRefPct.setText("R.%");
		tblclmnRefPct.setData(_DB_COLUMN, DBColumn.Percent);
		tblclmnRefPct.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnKeyword = new TableColumn(table, SWT.NONE);
		tblclmnKeyword.setMoveable(true);
		tblclmnKeyword.setWidth(100);
		tblclmnKeyword.setText("Keyword");
		tblclmnKeyword.setData(_DB_COLUMN, DBColumn.Keyword);
		tblclmnKeyword.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnKeyness = new TableColumn(table, SWT.RIGHT);
		tblclmnKeyness.setMoveable(true);
		tblclmnKeyness.setWidth(100);
		tblclmnKeyness.setText("Keyness");
		tblclmnKeyness.setData(_DB_COLUMN, CAPrefs.KEYNESS.dbColumn());
		tblclmnKeyness.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnPvalue = new TableColumn(table, SWT.RIGHT);
		tblclmnPvalue.setMoveable(true);
		tblclmnPvalue.setWidth(100);
		tblclmnPvalue.setText("P value");
		tblclmnPvalue.setData(_DB_COLUMN, CAPrefs.KEYNESS.pValueDBColumn());
		tblclmnPvalue.addSelectionListener(columnSortListener);
				
		table.setSortColumn(tblclmnKeyness);
		table.setSortDirection(SWT.DOWN);
		
		table.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				final TableItem item = (TableItem) event.item;
				final int index = event.index;
				String[] texts = getItemTexts(index);
				if (texts != null) {
					item.setText(texts);
					
					Keyword key = workspace.DATA.keywordList.get(index);
					double pct = key.getPercent();
					double pctRef = key.getRefPercent();
					if (pct < pctRef) {
						item.setForeground(new Color(getDisplay(), CAPrefs.HIGHLIGH_FG_COLOR_SCHEME[0]));
					}
				}
			}
		});
		
		return table;
	}	
	
	public void loadData() {
		sort();
	}
	
	protected void unloadData() {
		if (workspace.DATA.keywordList != null)
			workspace.DATA.keywordList.clear();
		workspace.DATA.keywordList = null;
	}

	public void updateMeasurementColumn() {
		table.getColumn(6).setData(_DB_COLUMN, CAPrefs.KEYNESS.dbColumn());
		table.getColumn(7).setData(_DB_COLUMN, CAPrefs.KEYNESS.pValueDBColumn());
		table.clearAll();
		table.update();
	}
	
	public String[] getItemTexts(int index) {
		try 
		{
			Keyword keyword = workspace.DATA.keywordList.get(index);
			
			long freq 		= keyword.getFreq();
			long freqRef 	= keyword.getRefFreq();
			double pct		= keyword.getPercent();
			double pctRef	= keyword.getRefPercent();
			
			double keyness 	= CAPrefs.KEYNESS.valueOf(keyword);
			double p		= CAPrefs.KEYNESS.pValueOf(keyword);
			
			return new String[] {
					String.valueOf(index + 1),
					Formats.getNumberFormat(freq),
					Formats.getPercentFormat(pct),
					Formats.getNumberFormat(freqRef),
					Formats.getPercentFormat(pctRef),
					keyword.getWord(),
					Formats.getDecimalFormat(keyness),
					Formats.getDecimalFormat(p) };
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		}
		return null;
	}
	
	public void sort() {
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		final DBColumn sortColumn = (DBColumn) table.getSortColumn().getData(_DB_COLUMN);
		final int sortDirection = table.getSortDirection();
		Thread thread = new Thread() {
			public void run() {
				try 
				{
					keywordsCount = CADataUtils.resetKeywordList(
							sortColumn,
							sortDirection,
							getFinder().whereSyntax());
					positiveKeywordsCount = workspace.DATA.keywordList.size();
					
				} catch (Exception e) {
					CAErrorMessageDialog.open(getGear(), e);
				} finally {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							resetKeywordList();
							spinner.close();
						}
					});
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}
	
	private void resetKeywordList() {
		setStatusText((CAPrefs.SHOW_NEGATIVE_KEYWORDS ? "" : Formats.getNumberFormat(positiveKeywordsCount) + " of ") + Formats.getNumberFormat(keywordsCount) + " keywords.");
		table.clearAll();
		table.setItemCount(CAPrefs.SHOW_NEGATIVE_KEYWORDS ? keywordsCount : positiveKeywordsCount);
		table.update();
	}

	public DBColumn getSortDBColumn() {
		return sortDBColumn;
	}

	@Override
	public void showFinder() {
		getFinder().setHidden(false);
	}

	@Override
	public void doit(CAQuery query) {
		ConciseThread thread = new CAKeywordThread(query);
		thread.start();
	}
	
}
