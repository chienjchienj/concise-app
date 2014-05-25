package org.sustudio.concise.app.gear;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.index.IndexReader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLUtils;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.helper.SaveOutputHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.thread.CADeleteDocumentThread;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.utils.RevealInFinder;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;

public class CorpusManager 
	   extends GearController 
	   implements IGearTableBased, IGearSortable, IGearFilterable, IGearFileRevealable {
	
	private List<ConciseDocument> docList;
	private Table table;
	
	public CorpusManager() {
		this(Gear.CorpusManager);
	}
	
	public CorpusManager(Gear gear) {
		super(CABox.GearBox, gear);
		switch (gear) {
		case CorpusManager:				docList = Concise.getData().documentList;			break;
		case ReferenceCorpusManager:	docList = Concise.getData().referenceDocumentList;	break;
		default:
			return;
		}
	}

	@Override
	public Control createControl() {
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setMoveable(true);
		tableColumn.setWidth(60);
		tableColumn.setText("#");
		
		final TableColumn tblclmnTitle = new TableColumn(table, SWT.NONE);
		tblclmnTitle.setMoveable(true);
		tblclmnTitle.setWidth(380);
		tblclmnTitle.setText("Title");
		tblclmnTitle.setData(_DB_COLUMN, DBColumn.Title);
		tblclmnTitle.addSelectionListener(new ColumnSortListener());
		
		final TableColumn tblclmnWords = new TableColumn(table, SWT.RIGHT);
		tblclmnWords.setMoveable(true);
		tblclmnWords.setWidth(60);
		tblclmnWords.setText("Words");
		tblclmnWords.setData(_DB_COLUMN, DBColumn.NumWords);
		tblclmnWords.addSelectionListener(new ColumnSortListener());
		
		final TableColumn tblclmnParagraphs = new TableColumn(table, SWT.RIGHT);
		tblclmnParagraphs.setMoveable(true);
		tblclmnParagraphs.setWidth(60);
		tblclmnParagraphs.setText("Paragraphs");
		tblclmnParagraphs.setData(_DB_COLUMN, DBColumn.NumParagraphs);
		tblclmnParagraphs.addSelectionListener(new ColumnSortListener());
		
		final TableColumn tblclmnDirectory = new TableColumn(table, SWT.NONE);
		tblclmnDirectory.setMoveable(true);
		tblclmnDirectory.setWidth(250);
		tblclmnDirectory.setText("Directory");
		tblclmnDirectory.setData(_DB_COLUMN, DBColumn.Filepath);
		tblclmnDirectory.addSelectionListener(new ColumnSortListener());
		
		table.setSortColumn(tblclmnDirectory);
		table.setSortDirection(SWT.UP);
		
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				
				TableItem item = (TableItem) event.item;
				int index = event.index;
				
				item.setText(getItemTexts(index));
				item.setData(_DOC_ID, docList.get(index).docID);
				
				// show program icon
				if (CAPrefs.SHOW_PROGRAM_ICON) {
					final ImageData imageData = Program.findProgram(FilenameUtils.getExtension(item.getText(1))).getImageData();
					item.setImage(1, new Image(getDisplay(), imageData));
				}
				
				if (!new File(docList.get(index).filepath).exists()) {
					item.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
				}
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				openFileInDocumentViewer();
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (table.getSelectionCount() < 1) return;
				if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
					int docCount = 0;
					StringBuilder message = new StringBuilder();
					for (int index : table.getSelectionIndices()) {
						if (docCount > 0) message.append("\n");
						message.append(getItemTexts(index)[1]);
						docCount++;
						if (docCount > 5) {
							message.append("\nand " + (table.getSelectionCount() - 5) + " more...");
							break;
						}
					}
					
					if (Dialog.isConfirmed(getShell(), "Delete?", message.toString())) {
						
						final ArrayList<ConciseDocument> docs = new ArrayList<ConciseDocument>();
						for (int i : table.getSelectionIndices()) {
							docs.add(docList.get(i));
						}
						
						CADeleteDocumentThread deleteThread = new CADeleteDocumentThread(getGear());
						deleteThread.setDocuments(docs.toArray(new ConciseDocument[0]));
						deleteThread.start();
						docs.clear();
					}
					message.setLength(0);
				}
			}
		});
		
		table.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent arg0) {
				int width = table.getClientArea().width;
				for (TableColumn col : table.getColumns()) {
					if (!tblclmnDirectory.equals(col)) {
						width -= col.getWidth();
					}
				}
				tblclmnDirectory.setWidth(width < 250 ? 250 : width);
			}
			
		});
		
		return table;
	}
	
	protected void checkDocumentsSync() {
		if (!getFinder().isHidden())
			return;
		
		try {
			
			IndexReader reader = null;
			switch (getGear()) {
			case CorpusManager:	
				reader = workspace.getIndexReader(); break;
			case ReferenceCorpusManager:
				reader =  workspace.getIndexReaderRef(); break;
			default:
				break;
			}
			
			// re-counting document list
			if (reader != null &&  
				reader.maxDoc() != docList.size()) 
			{
				CASpinner spinner = new CASpinner(this);
				spinner.open();
				
				//
				// Now, dump all documents to database
				//
				CATable table = CATable.valueOf(gear.name());
				SQLiteDB.dropTableIfExists(table);
				SQLiteDB.createTableIfNotExists(table);
				PreparedStatement ps = SQLiteDB.prepareStatement(table);
				
				int count = 0;
				for (ConciseDocument doc : new DocumentIterator(reader)) 
				{
					ps.setInt	(1,  doc.docID);
					ps.setString(2,  doc.title);
					ps.setLong	(3,  doc.numWords);
					ps.setLong	(4,  doc.numParagraphs);
					ps.setString(5,  doc.filepath);
					ps.setBoolean(6, doc.isTokenized);
					ps.addBatch();
					
					if (count % 1000 == 0) {
						SQLiteDB.executeBatch(ps);
					}
					count++;
				}
				SQLiteDB.executeBatch(ps);
				
				spinner.close();
				loadData();
			}
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.error(getShell(), getGear().name() + " Exception", e.toString());
		}
	}
	
	public void loadData() {
		table.removeAll();
		table.setItemCount(0);
		
		CASpinner spinner = new CASpinner(this);
		spinner.open();
		DBColumn sortColumn = (DBColumn) table.getSortColumn().getData(_DB_COLUMN);
		int sortDirection = table.getSortDirection();
		try {
			if (docList != null) {
				docList.clear();
			}
			docList = new ArrayList<ConciseDocument>();
			
			CATable theTable = CATable.valueOf(gear.name()); 
			SQLiteDB.createTableIfNotExists(theTable);
			String sql = SQLUtils.selectSyntax(theTable, 
											   getFinder().whereSyntax(), 
											   sortColumn, 
											   sortDirection);
			ResultSet rs = SQLiteDB.executeQuery(sql);
			while (rs.next()) 
			{
				ConciseDocument doc = new ConciseDocument();
				doc.docID = 		rs.getInt(		DBColumn.DocID.columnName());
				doc.title = 		rs.getString(	DBColumn.Title.columnName());
				doc.numWords = 		rs.getLong(		DBColumn.NumWords.columnName());
				doc.numParagraphs = rs.getLong(		DBColumn.NumParagraphs.columnName());
				doc.filepath = 		rs.getString(	DBColumn.Filepath.columnName());
				docList.add(doc);
			}
			rs.close();
			
			// check synchronized
			checkDocumentsSync();
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		} finally {
			int count = docList.size();
			table.setItemCount(count);
			table.update();
			
			if (count == 0) {
				setStatusText("No document");
			}
			else {
				setStatusText(Formats.getNumberFormat(count) + (count > 1 ? " documents" : " document"));
			}
			
			spinner.close();
			SaveOutputHelper.listenTo(getGear());
		}
	}
	
	protected void unloadData() {
		if (Concise.getData().documentList != null) {
			Concise.getData().documentList.clear();
		}
		Concise.getData().documentList = null;
		super.unloadData();
	}
	
	public void sort() {
		loadData();
	}
		
	@Override
	public String[] getItemTexts(int index) {
		try 
		{
			ConciseDocument doc = docList.get(index);
			return new String[] {
					String.valueOf(index + 1),
					doc.title,
					Formats.getNumberFormat(doc.numWords),
					Formats.getNumberFormat(doc.numParagraphs),
					doc.filepath };
			
		} catch (Exception e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		}
		return null;
	}

	@Override
	public boolean isRevealEnabled() {
		if (table.getSelectionCount() == 1) {
			final ConciseDocument doc = docList.get(table.getSelectionIndex());
			if (new File(doc.filepath).exists()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void revealFileInFinder() {
		if (table.getSelectionCount() == 1) {
			final ConciseDocument doc = docList.get(table.getSelectionIndex());
			try {
				RevealInFinder.show(doc.filepath);
			} catch (IOException e) {
				workspace.logError(gear, e);
				Dialog.showException(e);
			}
		}
	}

	@Override
	public void openFileInDocumentViewer() {
		if (table.getSelectionCount() == 1) {
			TableItem item = table.getSelection()[0];
			Integer docID = (Integer) item.getData(_DOC_ID);
			if (docID != null) {
				try {
					IndexReader reader = null;
					switch (getGear()) {
					case CorpusManager:	
						reader = workspace.getIndexReader(); break;
					case ReferenceCorpusManager:
						reader =  workspace.getIndexReaderRef(); break;
					default:
						break;
					}
					if (reader != null) {
						DocumentViewer fv = (DocumentViewer) Gear.DocumentViewer.open(workspace);
						fv.open(docID, reader);
					}
				} catch (Exception e) {
					workspace.logError(gear, e);
					Dialog.showException(e);
				}
			}
		}
	}

	@Override
	public void showFinder() {
		getFinder().setHidden(false);
	}
}
