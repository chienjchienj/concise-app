package org.sustudio.concise.app.thread;

import java.sql.PreparedStatement;

import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.wordClouder.WordClouder;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.wordlister.Word;
import org.sustudio.concise.core.wordlister.WordIterator;

public class WordListerThread extends ConciseThread {

	public WordListerThread(CAQuery query) {
		super(Gear.WordLister, query);
	}
	
	public void running() {
		
		dialog.setStatus("working...");
		
		try {
			// Drop old table
			SQLiteDB.dropTableIfExists(CATable.WordLister);
			SQLiteDB.createTableIfNotExists(CATable.WordLister);
			PreparedStatement ps = SQLiteDB.prepareStatement(CATable.WordLister);
			
			int uniqueWordCount = 0;
			long totalTermFreq = 0;
			
			// write to database
			Workspace workspace = Concise.getCurrentWorkspace();
			for (Word word : new WordIterator(workspace, CAPrefs.SHOW_PART_OF_SPEECH)) {
				if (isInterrupted()) {
					break;
				}
				
				SQLiteDB.addWord(ps, word);
				totalTermFreq += word.totalTermFreq;
				uniqueWordCount++;
				
				dialog.setStatus(uniqueWordCount + " unique words loaded... ");
				if (uniqueWordCount % 1000 == 0) {
					SQLiteDB.executeBatch(ps);
				}
			}
			SQLiteDB.executeBatch(ps);
			ps.close();
			
			// register to conciseresource
			Concise.getData().totalTermFreq = totalTermFreq;
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
	}
	
	protected void loadData() {
		if (gear == Gear.WordClouder) {
			((WordClouder) gear.getController(Concise.getCurrentWorkspace())).setCloudData();
		}
		else {
			super.loadData();
		}
	}
}
