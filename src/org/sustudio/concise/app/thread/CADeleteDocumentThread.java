package org.sustudio.concise.app.thread;

import java.io.IOException;
import java.sql.SQLException;

import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.Workspace;
import org.sustudio.concise.core.ConciseFile;
import org.sustudio.concise.core.autocompleter.AutoCompleter;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentWriter;

public class CADeleteDocumentThread extends ConciseThread {

	private boolean deleteAll = false;
	private ConciseDocument[] docs;
	
	public CADeleteDocumentThread(Gear gear) {
		super(gear, new CAQuery(gear));
	}
	
	public void setDocuments(ConciseDocument[] docs) {
		this.docs = docs;
	}
	
	public void setDeleteAll(boolean deleteAll) {
		this.deleteAll = deleteAll;
	}

	@Override
	public void running() {
		
		Workspace workspace = Concise.getCurrentWorkspace();
		try {
			ConciseFile indexDir = workspace.getIndexDirRef();
			if (gear == Gear.CorpusManager) {
				AutoCompleter.removeInstanceFor(workspace.getIndexReader());
				indexDir = workspace.getIndexDir();
			}
			
			dialog.setStatus("deleting...");
			DocumentWriter writer = new DocumentWriter(indexDir);
			if (deleteAll)
				writer.deleteAll();
			else
				writer.deleteDocuments(docs);
			writer.close();
			
			if (deleteAll) {
				dialog.setStatus("deleting database...");
				if (gear == Gear.CorpusManager) {
					SQLiteDB.dropTableIfExists(CATable.CorpusManager);
					SQLiteDB.dropTableIfExists(CATable.Concordancer);
					SQLiteDB.dropTableIfExists(CATable.Collocator);
					SQLiteDB.dropTableIfExists(CATable.KeywordLister);
					SQLiteDB.dropTableIfExists(CATable.WordCluster);
					SQLiteDB.dropTableIfExists(CATable.WordLister);
				}
				else if (gear == Gear.ReferenceCorpusManager) {
					SQLiteDB.dropTableIfExists(CATable.ReferenceCorpusManager);
					SQLiteDB.dropTableIfExists(CATable.KeywordLister);
				}
			}
			
			dialog.setStatus("re-open index directory...");
			if (gear == Gear.CorpusManager && workspace.getIndexReader() != null) {
				AutoCompleter.getInstanceFor(workspace.getIndexReader(), CAPrefs.SHOW_PART_OF_SPEECH);
			}
		} catch (IOException e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		} catch (SQLException e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		}
	}

}
