package org.sustudio.concise.app.thread;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.Workspace;
import org.sustudio.concise.core.ConciseFile;
import org.sustudio.concise.core.Workspace.INDEX;
import org.sustudio.concise.core.autocompleter.AutoCompleter;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.sustudio.concise.core.corpus.DocumentWriter;
import org.sustudio.concise.core.corpus.importer.ConciseField;
import org.sustudio.concise.core.corpus.importer.Importer;


/**
 * 
 * 將文件重新分詞的 thread
 * 
 * @author Kuan-ming Su
 *
 */
public class CAReTokenizeThread extends ConciseThread {

	public CAReTokenizeThread() {
		super(Gear.CorpusManager, new CAQuery(Gear.CorpusManager));
	}
	
	
	@Override
	public void running() {
		
		/**
		 * 1.) Read corpus file list
		 * 2.) remove corpus index folder
		 * 3.) re-tokenize corpus file
		 * 
		 * 4.) Read reference corpus file list
		 * 5.) remove reference corpus index folder
		 * 6.) re-tokenize reference corpus file
		 */
		
		try {
			
			Workspace workspace = Concise.getCurrentWorkspace();
			AutoCompleter.removeInstanceFor(workspace.getIndexReader(INDEX.DOCUMENT));
			
			// Drop database
			SQLiteDB.dropTableIfExists(CATable.CorpusManager);
			SQLiteDB.dropTableIfExists(CATable.Concordancer);
			SQLiteDB.dropTableIfExists(CATable.Collocator);
			SQLiteDB.dropTableIfExists(CATable.KeywordLister);
			SQLiteDB.dropTableIfExists(CATable.WordCluster);
			SQLiteDB.dropTableIfExists(CATable.WordLister);
			SQLiteDB.dropTableIfExists(CATable.ReferenceCorpusManager);
			
			// read custom dictionary
			CAPrefs.userDictionaries = workspace.getDictionaryFiles();
			
			handleIndex(Workspace.INDEX.DOCUMENT);
			handleIndex(Workspace.INDEX.REFERENCE);
			
			AutoCompleter.getInstanceFor(workspace.getIndexReader(INDEX.DOCUMENT), CAPrefs.SHOW_PART_OF_SPEECH);
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(gear, e);
		}
		
	}
	
	@SuppressWarnings("resource")
	protected void handleIndex(Workspace.INDEX indexType) throws Exception {
		
		/*
		 * 1. read file list
		 * 2. remove existing index
		 * 3. re-import files
		 */
		
		Workspace workspace = Concise.getCurrentWorkspace();
		IndexReader reader = workspace.getIndexReader(indexType);
		ConciseFile originalFolder = workspace.getOriginalDocFolder(indexType);
		if (reader == null) return;
		
		// 1.) Read corpus file list
		HashMap<File, Boolean> files = new HashMap<File, Boolean>();
		for (int i=0; i<reader.maxDoc(); i++) {
			Document document = reader.document(i);
			if (document == null) continue;
			
			boolean isTokenized = document.getField(ConciseField.IS_TOKENIZED.field()).numericValue().intValue() == 1;
			File file = new File( originalFolder, document.get(ConciseField.FILENAME.field()) );
			files.put(file, isTokenized);
		}
		
		// 2.) remove existing index
		DocumentWriter writer = new DocumentWriter(workspace, indexType);
		writer.deleteAll();
		writer.close();
		
		// 3.) re-import files
		dialog.setStatus("load settings...");
		Importer importer = new Importer(workspace, indexType);
		importer.setCopyFiles(false);
		for (Map.Entry<File, Boolean> file : files.entrySet()) {
			if (isInterrupted()) return;
			
			// check availability
			if (file.getKey().exists()) {
				dialog.setStatus(file.getKey().getName());
				importer.indexFile(file.getKey(), file.getValue());
			}
		}
		importer.close();
		files.clear();
		
		Gear gear = indexType.equals(INDEX.DOCUMENT) ? Gear.CorpusManager : Gear.ReferenceCorpusManager;
		reader = workspace.reopenIndexReader(indexType);
		if (reader == null) {
			return;
		}
		
		
		dialog.setStatus("loading data...");
		//
		// Now, dump all documents to database
		//
		CATable table = CATable.valueOf(gear.name());
		SQLiteDB.dropTableIfExists(table);
		SQLiteDB.createTableIfNotExists(table);
		PreparedStatement ps = SQLiteDB.prepareStatement(table);
		
		int count = 0;
		for (ConciseDocument doc : new DocumentIterator(workspace, indexType)) 
		{
			ps.setInt	(1,  doc.docID);
			ps.setString(2,  doc.title);
			ps.setLong	(3,  doc.numWords);
			ps.setLong	(4,  doc.numParagraphs);
			ps.setString(5,  doc.filename);
			ps.setBoolean(6, doc.isTokenized);
			ps.addBatch();
			
			if (count % 1000 == 0) {
				SQLiteDB.executeBatch(ps);
			}
			count++;
		}
		SQLiteDB.executeBatch(ps);
	}
	
}
