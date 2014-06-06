package org.sustudio.concise.app.gear;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CADataUtils;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLiteDataType;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.helper.SaveOutputHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.utils.RevealInFinder;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.concordance.ConcLine;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class Concordancer 
	   extends GearController 
	   implements IGearSortable, IGearTableBased, IGearFilterable, IGearFileRevealable {
	
	class WordRange {
		
		public final String word;
		public final int start;
		public final int end;
		
		public WordRange(final String word, final int start, final int end) {
			this.word = word;
			this.start = start;
			this.end = end;
		}
	}

	private Table table;
	private List<String> searchWords = new ArrayList<String>();
	private final TextLayout textLayout = new TextLayout(getDisplay());
	private final TextStyle textStyle = new TextStyle();
	
	public Concordancer() {
		super(CABox.GearBox, Gear.Concordancer);
	}
		
	@Override
	protected Control createControl() {
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(60);
		tableColumn.setText("#");
		
		final TableColumn tblclmnL = new TableColumn(table, SWT.NONE);
		tblclmnL.setWidth(270);
		tblclmnL.setText("Left");
		tblclmnL.setData(_DB_COLUMN, DBColumn.Left_Span);
		tblclmnL.addSelectionListener(columnSortListener);
				
		final TableColumn tblclmnN = new TableColumn(table, SWT.NONE);
		tblclmnN.setWidth(60);
		tblclmnN.setText("Node");
		tblclmnN.setData(_DB_COLUMN, DBColumn.Node);
		tblclmnN.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnR = new TableColumn(table, SWT.NONE);
		tblclmnR.setWidth(270);
		tblclmnR.setText("Right");
		tblclmnR.setData(_DB_COLUMN, DBColumn.Right_Span);
		tblclmnR.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnTitle = new TableColumn(table, SWT.NONE);
		tblclmnTitle.setWidth(150);
		tblclmnTitle.setText("Title");
		tblclmnTitle.setData(_DB_COLUMN, DBColumn.Doc_Title);
		tblclmnTitle.addSelectionListener(columnSortListener);
		
		table.setSortColumn(tblclmnN);
		table.setSortDirection(SWT.DOWN);
		
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				String[] texts = getItemTexts(index);
				item.setText(0, texts[0]);
				item.setText(4, texts[4]);
				
				ConcLine concLine = Concise.getData().concLineList.get(index);
				item.setData(_DOC_ID, concLine.getDocId());
				item.setData(_WORD_ID, concLine.getWordId());
				
				// show program icon
				if (CAPrefs.SHOW_PROGRAM_ICON) {
					final ImageData imageData = Program.findProgram(FilenameUtils.getExtension(item.getText(4))).getImageData();
					item.setImage(4, new Image(getDisplay(), imageData));
				}
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				openFileInDocumentViewer();
			}
		});
		
		table.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent event) {
				int width = table.getClientArea().width;
				for (TableColumn col : table.getColumns()) {
					if (!tblclmnTitle.equals(col)) {
						width -= col.getWidth();
					}
				}
				tblclmnTitle.setWidth(width < 150 ? 150 : width);
			}
			
		});
		
		// draw different foreground color
		table.addListener(SWT.PaintItem, new Listener() {
			public void handleEvent(Event event) {
				if (Concise.getData().concLineList == null) {
					// table is loading content
					return;
				}
				
				textLayout.setFont(table.getFont());
				final TableItem item = (TableItem) event.item;
				int index = table.indexOf(item);
				final String[] texts = getItemTexts(index);
				switch (event.index) {
				case 1:	// left span
					makeTextLayout(texts[1]);
					textLayout.draw(
							event.gc, 
							event.x - 2 + item.getBounds(1).width - textLayout.getBounds().width, 
							event.y + 1);
					break;
				
				case 2:	// node
					textLayout.setText(texts[2]);
					textStyle.foreground = getColor(texts[2]);
					textLayout.setStyle(textStyle, 0, texts[2].length() - 1);
					textLayout.draw(
							event.gc, 
							event.x + (item.getBounds(2).width - textLayout.getBounds().width) / 2, 
							event.y + 1);
					break;
					
				case 3:	// right span
					makeTextLayout(texts[3]);
					textLayout.draw(
							event.gc, 
							event.x + 2, 
							event.y + 1);
					break;
				}
			}
		});
				
		return table;
	}
	
	private void makeTextLayout(final String text) {
		textLayout.setText(text);
		
		// text style
		List<WordRange> ranges = getSearchWordRangesFor(text);
		for (WordRange r : ranges) {
			textStyle.foreground = getColor(r.word);
			textLayout.setStyle(textStyle, r.start, r.end);
		}
	}
	
	private Color getColor(String searchWord) {
		int index = searchWords.indexOf(searchWord);
		int colorIndex = index % CAPrefs.HIGHLIGH_FG_COLOR_SCHEME.length;
		RGB rgb = CAPrefs.HIGHLIGH_FG_COLOR_SCHEME[colorIndex];
		return new Color(getDisplay(), rgb);
	}
	
	/**
	 * 找出要 highlight 的字眼位置
	 * @param text
	 * @return 要 highlight 的字眼位置
	 */
	private List<WordRange> getSearchWordRangesFor(String text) {
		List<WordRange> ranges = new ArrayList<WordRange>();
		for (String searchWord : searchWords) {
			int start = 0;
			for (int end = text.indexOf(" ", start);
				 end != -1;
				 start = end + 1, 
					end = text.indexOf(" ", start) == -1 ? 
							start < text.length() ? text.length() : -1 : 
							text.indexOf(" ", start))
			{
				if (end == -1 && start > 0 && start < text.length()) {
					end = text.length();
				}
				String word = text.substring(start, end);
				if (word.equals(searchWord)) {
					ranges.add(new WordRange(searchWord, start, end));
				}
			}
		}
		return ranges;
	}
	
	public void loadData() 
	{
		table.removeAll();
		table.setItemCount(0);
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		final DBColumn sortColumn = (DBColumn) table.getSortColumn().getData(_DB_COLUMN);
		final int sortDirection = table.getSortDirection();
		Thread thread = new Thread() { public void run() {
			
			int count = 0;
			try {
				// retrieve search words
				searchWords = CADataUtils.getConcSearchWords();
				
				// build table contents
				CADataUtils.resetConcLineList(
							sortColumn,
							sortDirection, 
							getFinder().whereSyntax());
				count = Concise.getData().concLineList.size();
				
			} catch (Exception e) {
				CAErrorMessageDialog.open(getGear(), e);
			} finally {
				
				final int itemCount = count;
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						table.setItemCount(itemCount);
						table.update();
						setStatusText(Formats.getNumberFormat(itemCount) + " lines found");
						
						spinner.close();
						SaveOutputHelper.listenTo(getGear());
					}
				});
			}
		
		} };
		thread.setDaemon(true);
		thread.start();
	}
	
	protected void unloadData() {
		if (Concise.getData().concLineList != null)
			Concise.getData().concLineList.clear();
		Concise.getData().concLineList = null;
		searchWords.clear();
		super.unloadData();
	}
	
	public void sort() {
		final CASpinner spinner = new CASpinner(this);
		spinner.setMessage("Sorting");
		spinner.open();
		try 
		{
			DBColumn sortColumn = (DBColumn) table.getSortColumn().getData(_DB_COLUMN);
			if (sortColumn.equals(DBColumn.Left_Span)) {
				// TODO module
				sortColumn = new DBColumn("L1", SQLiteDataType.VARCHAR);
			}
			CADataUtils.resetConcLineList(
					 sortColumn, 
					 table.getSortDirection(),
					 getFinder().whereSyntax());
		
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		} finally {
			table.clearAll();
			table.update();
		}
		spinner.close();
	}
	
	public String[] getItemTexts(int index) {
		ConcLine concLine = Concise.getData().concLineList.get(index);
		return new String[] { 
				String.valueOf(index+1), 
				concLine.getLeft(),
				concLine.getNode(),
				concLine.getRight(),
				concLine.getDocTitle() };	
	}

	@Override
	public boolean isRevealEnabled() {
		if (table.getSelectionCount() == 1) {
			int docID = (Integer) table.getSelection()[0].getData(_DOC_ID);
			try {
				
				final String filepath = workspace.getIndexReader().document(docID).get(ConciseField.FILENAME.field());
				return new File(filepath).exists();
				
			} catch (Exception e) {
				e.printStackTrace();
				Dialog.error(getShell(), "Exception", e.getMessage());
			}
		}
		return false;
	}

	@Override
	public void revealFileInFinder() {
		if (table.getSelectionCount() == 1) {
			int docID = (Integer) table.getSelection()[0].getData(_DOC_ID);
			try {
				final String filepath = workspace.getIndexReader().document(docID).get(ConciseField.FILENAME.field());
				if (new File(filepath).exists()) {
					RevealInFinder.show(filepath);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				Dialog.error(getShell(), "Exception", e.getMessage());
			}
		}
	}

	@Override
	public void openFileInDocumentViewer() {
		if (table.getSelectionCount() > 0) {
			TableItem item = table.getSelection()[0];
			Integer docID = (Integer) item.getData(_DOC_ID);
			Integer wordID = (Integer) item.getData(_WORD_ID);
			if (docID != null && wordID != null) {
				CAQuery query = Concise.getActiveApp().toolBar.getQuery();
				DocumentViewer fv = (DocumentViewer) Gear.DocumentViewer.open(workspace);
				fv.open(docID, wordID, query);
			}
		}
	}

	@Override
	public void showFinder() {
		getFinder().setHidden(false);
	}
}
