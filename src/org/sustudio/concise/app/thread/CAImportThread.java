package org.sustudio.concise.app.thread;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.index.IndexNotFoundException;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CorpusManipulation;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.autocompleter.AutoCompleter;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.sustudio.concise.core.corpus.importer.ConciseFileUtils;
import org.sustudio.concise.core.corpus.importer.Importer;

public class CAImportThread extends ConciseThread {

	private CorpusManipulation manipulation;
	private File[] files;
	
	public CAImportThread(CorpusManipulation manipulation) {
		super(manipulation.manager(), new CAQuery(manipulation.manager()));
		this.manipulation = manipulation;
	}
	
	public void setFiles(File[] files) {
		this.files = files;
	}

	@Override
	public void running() {
		File indexDir = manipulation.indexDir();
		try {
			dialog.setStatus("checking file existence...");
			
			Workspace workspace = Concise.getCurrentWorkspace();
			if (gear == Gear.CorpusManager) {
				AutoCompleter.removeInstanceFor(workspace.getIndexReader());
			}
			
			// load existing files to check duplication
			// using MD5 to check 
			ArrayList<String> existingMD5s = new ArrayList<String>();
			try {
				for (ConciseDocument cd : new DocumentIterator(workspace, manipulation.indexReader())) {
					String md5 = ConciseFileUtils.getMD5(cd.documentFile);
					existingMD5s.add(md5);
				}
			} catch (IndexNotFoundException idxNotFoundException) {
				// empty index folder,
				// do nothing
			}
			
			dialog.setStatus("load settings...");
			Importer importer = new Importer(workspace, indexDir);
			for (File sourceFile : files) {
				if (isInterrupted()) {
					break;
				}
				
				String sourceMD5 = ConciseFileUtils.getMD5(sourceFile);
				if (!existingMD5s.contains(sourceMD5)) {
					dialog.setStatus(ArrayUtils.indexOf(files, sourceFile)+"/"+files.length+" "+sourceFile.getName());
					importer.indexFile(sourceFile, manipulation.isTokenized());
				}
			}
			importer.close();
			importer = null;
			
			existingMD5s.clear();
			existingMD5s = null;
			
			dialog.setStatus("re-open index directory...");
			if (gear == Gear.CorpusManager) {
				workspace.reopenIndexReader();
				if (workspace.getIndexReader() != null) {
					AutoCompleter.getInstanceFor(workspace.getIndexReader(), CAPrefs.SHOW_PART_OF_SPEECH);
				}
			}
			else if (gear == Gear.ReferenceCorpusManager) {
				workspace.reopenIndexReaderRef();
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
			for (ConciseDocument doc : new DocumentIterator(workspace, manipulation.indexReader())) 
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
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
		
	}

}
