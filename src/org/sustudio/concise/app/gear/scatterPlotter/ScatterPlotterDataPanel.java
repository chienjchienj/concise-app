package org.sustudio.concise.app.gear.scatterPlotter;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javafx.scene.chart.XYChart.Data;
import javafx.util.converter.NumberStringConverter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.scatterPlotter.ScatterPlotter.HoverNode;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.app.query.DefaultConcQuery;
import org.sustudio.concise.core.statistics.WordPlotData;
import org.sustudio.concise.core.wordlister.Word;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class ScatterPlotterDataPanel extends Composite {

	private final ScatterPlotter plotter;
	private final Table dataTable;
	private List<WordPlotData> wordData;
	
	public ScatterPlotterDataPanel(Composite parent, final ScatterPlotter plotter) {
		super(parent, SWT.EMBEDDED);
		this.plotter = plotter;
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);
		
		ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final ToolItem tltmDelete = new ToolItem(toolBar, SWT.NONE);
		tltmDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				deleteWords();
			}
		});
		tltmDelete.setImage(SWTResourceManager.getImage(getClass(), "/org/sustudio/concise/app/icon/201-remove.png"));
		tltmDelete.setText("Delete");
		tltmDelete.setEnabled(false);
		
		final ToolItem tltmConcord = new ToolItem(toolBar, SWT.NONE);
		tltmConcord.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Gear.Concordancer.open(Concise.getCurrentWorkspace())
					.doit(new DefaultConcQuery(wordData.get(dataTable.getSelectionIndex()).getWord().word));
			}
		});
		tltmConcord.setImage(SWTResourceManager.getImage(getClass(), "/org/sustudio/concise/app/icon/97-puzzle.png"));
		tltmConcord.setText("Concord.");
		tltmConcord.setEnabled(false);
		
		new ToolItem(toolBar, SWT.SEPARATOR_FILL);
		
		final ToolItem tltmDetail = new ToolItem(toolBar, SWT.NONE);
		tltmDetail.setText("Detail");
		tltmDetail.setImage(SWTResourceManager.getImage(getClass(), "/org/sustudio/concise/app/icon/19-gear.png"));
		tltmDetail.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// TODO 開啓 correspondence ananlysis 的詳細資料
			}
		});
		
		dataTable = new Table(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);
		dataTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		dataTable.setHeaderVisible(true);
		dataTable.setLinesVisible(true);
		CopyPasteHelper.listenTo(dataTable);
		
		SelectionAdapter columnSortListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				TableColumn column = (TableColumn) event.widget;
				DBColumn dbColumn = (DBColumn) column.getData(GearController._DB_COLUMN);
				if (dbColumn != null) {
					Table table = column.getParent();
					int dir = table.getSortDirection();
					
					if (column == table.getSortColumn())
						dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
					else
						table.setSortColumn(column);
					table.setSortDirection(dir);
					
					plotter.sort();
				}
			}
		};
		
		final TableColumn tblclmWord = new TableColumn(dataTable, SWT.NONE);
		tblclmWord.setData(GearController._DB_COLUMN, DBColumn.Word);
		tblclmWord.addSelectionListener(columnSortListener);
		tblclmWord.setText("Word");
		tblclmWord.setWidth(100);
		final TableColumn tblclmFreq = new TableColumn(dataTable, SWT.RIGHT);
		tblclmFreq.setData(GearController._DB_COLUMN, DBColumn.Freq);
		tblclmFreq.addSelectionListener(columnSortListener);
		tblclmFreq.setText("Frequency");
		tblclmFreq.setWidth(120);
		
		dataTable.setSortDirection(SWT.DOWN);
		dataTable.setSortColumn(tblclmFreq);
		
		dataTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				if (wordData != null) {
					Word word = wordData.get(index).getWord();
					item.setText(0, word.getWord());
					item.setText(1, new NumberStringConverter("#,###,###,###").toString(word.totalTermFreq));
				}
			}
		});
		dataTable.setItemCount(0);
		
		dataTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				for (Data<Number, Number> data : plotter.getChart().getData().get(0).getData()) {
					HoverNode node = (HoverNode) data.getNode();
					node.hideLabel();
				}
				for (int i : dataTable.getSelectionIndices()) {
					HoverNode node = (HoverNode) plotter.getChart().getData().get(0).getData().get(i).getNode();
					node.showLabel();
				}
				
				tltmDelete.setEnabled(dataTable.getSelectionCount() > 0);
				tltmConcord.setEnabled(dataTable.getSelectionCount() == 1);
			}
		});
		
		dataTable.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.DEL:
				case SWT.BS:
					deleteWords();
					dataTable.setSelection(-1);
					break;
					
				case SWT.ESC:
					dataTable.setSelection(-1);
					for (Data<Number, Number> data : plotter.getChart().getData().get(0).getData()) {
						HoverNode node = (HoverNode) data.getNode();
						node.hideLabel();
					}
					tltmDelete.setEnabled(false);
					tltmConcord.setEnabled(false);
					break;
				}
			}
		});
	}
	
	private void deleteWords() {
		try {
			if (wordData == null) return;
			StringBuilder sb = new StringBuilder("Delete ");
			for (int i = 0; i < dataTable.getSelectionCount() && i < 5; i++) {
				sb.append("[" + wordData.get(i).getWord().getWord() + "] ");
			}
			if (dataTable.getSelectionCount() > 5) {
				sb.append(" and " + (dataTable.getSelectionCount() - 5) + " more...");
			}
			sb.append("?");
			
			if (Dialog.isConfirmed(getShell(), "Delete Words?", sb.toString())) {
				String sql = "DELETE FROM " + CATable.ScatterPlotter.name() + " WHERE " + DBColumn.Word.columnName() + " = ?";
				PreparedStatement ps = SQLiteDB.prepareStatement(sql);
				for (int i : dataTable.getSelectionIndices()) {
					String word = wordData.get(i).getWord().word;
					ps.setString(1, word);
					ps.addBatch();
				}
				SQLiteDB.executeBatch(ps);
				plotter.loadData();
			}
			else {
				return;
			}
			sb.setLength(0);
			
		} catch (SQLException | IOException e) {
			Concise.getCurrentWorkspace().logError(Gear.ScatterPlotter, e);
			Dialog.showException(e);
		}
	}
	
	public Control getZoomableControl() {
		return dataTable;
	}
	
	public void setInput(List<WordPlotData> wordData) {
		dataTable.removeAll();
		this.wordData = wordData;
		dataTable.setItemCount(wordData.size());
	}
	
	public void clearTable() {
		dataTable.setItemCount(0);
		dataTable.removeAll();
	}
	
	public DBColumn getSortColumn() {
		return (DBColumn) dataTable.getSortColumn().getData(GearController._DB_COLUMN);
	}
	
	public int getSortDirection() {
		return dataTable.getSortDirection();
	}

	public void checkSubclass() {
		
	}
}
