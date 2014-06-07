package org.sustudio.concise.app.gear;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CADataUtils;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.enums.EffectSizeMeasurement;
import org.sustudio.concise.app.enums.SignificanceMeasurement;
import org.sustudio.concise.app.helper.SaveOutputHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.DefaultConcQuery;
import org.sustudio.concise.app.thread.CollocationThread;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.collocation.Collocate;

public class Collocator 
	   extends GearController 
	   implements IGearTableBased, IGearSortable, IGearFilterable, IGearConcordable {
	
	private Table table;
	private HashSet<TableColumn> basicColumns;
	
	private Color highlightColor = new Color(getDisplay(), CAPrefs.HIGHLIGH_FG_COLOR_SCHEME.length == 0 ? new RGB(255, 0, 0) : CAPrefs.HIGHLIGH_FG_COLOR_SCHEME[0]);
	
	public Collocator() {
		super(CABox.GearBox, Gear.Collocator);
	}

	private TableColumn addTableColumn(String text, int style, int width, DBColumn sortDBColumn)
	{
		TableColumn tblclm;
		if (sortDBColumn == DBColumn.Freq_Left) {
			tblclm = new TableColumn(table, style, 3);
		}
		else if (sortDBColumn == DBColumn.Freq_Right) {
			tblclm = new TableColumn(table, style, 4);
		}
		else {
			tblclm = new TableColumn(table, style);
		}
		tblclm.setWidth(width);
		tblclm.setText(text);
		tblclm.setMoveable(true);
		if (sortDBColumn != null) {
			tblclm.setData(_DB_COLUMN, sortDBColumn);
			tblclm.addSelectionListener(columnSortListener);
		}
		return tblclm;
	}
	
	@Override
	protected Control createControl() {
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		// register columns
		basicColumns = new HashSet<TableColumn>();
		basicColumns.add(addTableColumn("#", SWT.NONE, 60, null));
		TableColumn freqColumn = addTableColumn("Freq.", SWT.RIGHT, 60, DBColumn.Freq);
		basicColumns.add(freqColumn); 
		basicColumns.add(addTableColumn("Word", SWT.NONE, 100, DBColumn.Collocate));
		
		if (CAPrefs.COLLOCATION_MODE.isSurface()) {
			basicColumns.add(addTableColumn("L.Freq.", SWT.RIGHT, 60, DBColumn.Freq_Left));
			basicColumns.add(addTableColumn("R.Freq.", SWT.RIGHT, 60, DBColumn.Freq_Right));
		}
		
		basicColumns.add(addTableColumn(CAPrefs.EFFECT_SIZE.label(), SWT.RIGHT, 80, CAPrefs.EFFECT_SIZE.dbColumn()));
		basicColumns.add(addTableColumn(CAPrefs.SIGNIFICANCE.label(), SWT.RIGHT, 80, CAPrefs.SIGNIFICANCE.dbColumn()));
		
		table.setSortColumn(table.getColumn(1));
		table.setSortDirection(SWT.DOWN);
		
		// add SetData listener
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				
				String[] texts = getItemTexts(index);
				if (texts != null) {
					item.setText(texts);
					
					// get max number
					long max = 0;
					for (int i=basicColumns.size(); i<table.getColumnCount(); i++) {
						long v = Long.parseLong(texts[i].replace(",", ""));
						max = v > max ? v : max;
					}
					// highlight
					for (int i=basicColumns.size(); i<table.getColumnCount(); i++) {
						long v = Long.parseLong(texts[i].replace(",", ""));
						if (v == max) {
							item.setForeground(i, highlightColor);
						}
					}
				}
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				showConcord();
			}
		});
		
		return table;
	}
	
	public void redraw() {
		highlightColor = new Color(getDisplay(), CAPrefs.HIGHLIGH_FG_COLOR_SCHEME[0]);
		super.redraw();
	}
	
	public void loadData() {
		table.setItemCount(0);
		table.removeAll();
		CASpinner spinner = new CASpinner(this);
		spinner.open();
		DBColumn sortColumn = (DBColumn) table.getSortColumn().getData(_DB_COLUMN);
		int sortDirection = table.getSortDirection();
		int count = 0;
		try {
			CADataUtils.resetCollocateList(
							sortColumn,
							sortDirection,
							getFinder().whereSyntax());
			count = Concise.getData().collocateList.size();
							
		} catch (Exception e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		} finally {
			// check position vector columns
			if (count > 0) 
			{
				Collocate coll = Concise.getData().collocateList.get(0);
				resetPositionColumns(coll.getLeftSpanSize(), coll.getRightSpanSize());
			}
			
			table.setItemCount(count);
			resetTable();
			setStatusText(Formats.getNumberFormat(count) + " collocates found.");
			
			spinner.close();
			SaveOutputHelper.listenTo(getGear());
		}
	}
	
	protected void unloadData() {
		if (Concise.getData().collocateList != null)
			Concise.getData().collocateList.clear();
		Concise.getData().collocateList = null;
		super.unloadData();
	}
	
	public String[] getItemTexts(int index) {
		Collocate coll = Concise.getData().collocateList.get(index);
		if (CAPrefs.COLLOCATION_MODE.isTextual()) {
			return new String[] {
					String.valueOf(index + 1),
					Formats.getNumberFormat(coll.getFreq()),
					coll.getWord(),
					Formats.getDecimalFormat(CAPrefs.EFFECT_SIZE.valueOf(coll)),
					Formats.getDecimalFormat(CAPrefs.SIGNIFICANCE.valueOf(coll))
			};
		}
		
		String[] itemText = new String[] {
				String.valueOf(index + 1),
				Formats.getNumberFormat(coll.getFreq()),
				coll.getWord(),
				Formats.getNumberFormat(coll.getLeftFreq()),
				Formats.getNumberFormat(coll.getRightFreq()),
				Formats.getDecimalFormat(CAPrefs.EFFECT_SIZE.valueOf(coll)),
				Formats.getDecimalFormat(CAPrefs.SIGNIFICANCE.valueOf(coll))
		};
		
		// display position vectors
		for (int i=basicColumns.size(); i<table.getColumnCount(); i++) {
			String col = table.getColumn(i).getText();
			long value = 0;
			if (col.equals("*")) {
				value = coll.getNodeFreq();
			}
			else {
				value = coll.getPositionVector().get(col).longValue();
			}
			itemText = ArrayUtils.add(itemText, Formats.getNumberFormat(value));
		}
		return itemText;
	}
	
	
	public void resetPositionColumns(int leftSpanSize, int rightSpanSize) {
		for (TableColumn tblclm : table.getColumns()) {
			if (!basicColumns.contains(tblclm))
				tblclm.dispose();
		}
		
		if (CAPrefs.COLLOCATION_MODE.isTextual())
		{
			// dispose L.Freq. and R.Freq. column
			for (Iterator<TableColumn> iter = basicColumns.iterator(); iter.hasNext();) {
				TableColumn tblclm = iter.next();
				DBColumn dataColumn = (DBColumn) tblclm.getData(_DB_COLUMN);
				if (dataColumn != null && 
					(dataColumn.equals(DBColumn.Freq_Left) || 
					dataColumn.equals(DBColumn.Freq_Right))) 
				{
					if (table.getSortColumn().equals(tblclm)) {
						table.setSortColumn(table.getColumn(1));
					}
					iter.remove();
					tblclm.dispose();
				}
			}
		}
				
		else if (CAPrefs.COLLOCATION_MODE.isSurface()) 
		{
			// check if L.Freq. and R.Freq. column exist
			boolean leftFreqColumnExists = false;
			boolean rightFreqColumnExists = false;
			for (TableColumn tblclm : basicColumns) {
				DBColumn dataColumn = (DBColumn) tblclm.getData(_DB_COLUMN);
				if (dataColumn != null && dataColumn.equals(DBColumn.Freq_Left))
					leftFreqColumnExists = true;
				if (dataColumn != null && dataColumn.equals(DBColumn.Freq_Right))
					rightFreqColumnExists = true;
			}
			if (!leftFreqColumnExists)
				basicColumns.add(addTableColumn("L.Freq.", SWT.RIGHT, 60, DBColumn.Freq_Left));
			if (!rightFreqColumnExists)
				basicColumns.add(addTableColumn("R.Freq.", SWT.RIGHT, 60, DBColumn.Freq_Right));
			
			// rebuild columns
			for (int i=0; i<leftSpanSize; i++) {
				createTableColumn("L"+String.valueOf(leftSpanSize - i));
			}
			createTableColumn("*");
			for (int i=1; i<=rightSpanSize; i++) {
				createTableColumn("R"+String.valueOf(i));
			}
		}
	}
	
	public void updateMeasurementColumn(EffectSizeMeasurement measurement) {
		TableColumn tblclmn = table.getColumn( table.getColumnCount() > 6 ? 5 : 3);
		tblclmn.setText(measurement.label());
		tblclmn.setData(_DB_COLUMN, measurement.dbColumn());
		resetTable();
	}
	
	public void updateMeasurementColumn(SignificanceMeasurement measurement) {
		TableColumn tblclmn = table.getColumn( table.getColumnCount() > 6 ? 6 : 4);
		tblclmn.setText(measurement.label());
		tblclmn.setData(_DB_COLUMN, measurement.dbColumn());
		resetTable();
	}
	
	protected void resetTable() {
		table.clearAll();
		table.update();
	}
	
	private TableColumn createTableColumn(String field) {
		TableColumn tblclmn = new TableColumn(table, SWT.RIGHT);
		tblclmn.setWidth(30);
		tblclmn.setMoveable(true);
		tblclmn.setText(field);
		
		if (field.equals("*")) field = "N";
		tblclmn.setData(_DB_COLUMN, CATable.Collocator.dbColumnOf(field));
		tblclmn.addSelectionListener(columnSortListener);
		return tblclmn;
	}

	
	@Override
	public void sort() {
		try 
		{
			CADataUtils.resetCollocateList(
					(DBColumn) table.getSortColumn().getData(_DB_COLUMN),
					table.getSortDirection(),
					getFinder().whereSyntax());
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		} finally {
			resetTable();
		}
	}

	@Override
	public boolean isConcordEnabled() {
		return table.getSelectionCount() == 1;
	}

	@Override
	public void showConcord() {
		if (table.getSelectionCount() == 1) {
			String word = Concise.getData().collocateList.get(table.getSelectionIndex()).getWord();
			if (word.contains(" ")) {
				word = "\"" + word.trim() + "\"";	// add quotation mark to make phrase search
			}
			Gear.Concordancer.open(Concise.getCurrentWorkspace())
				.doit(new DefaultConcQuery(word));
		}
	}

	@Override
	public void showFinder() {
		getFinder().setHidden(false);
	}

	@Override
	public void doit(CAQuery query) {
		ConciseThread thread = new CollocationThread(query);
		thread.start();
	}
		
}
