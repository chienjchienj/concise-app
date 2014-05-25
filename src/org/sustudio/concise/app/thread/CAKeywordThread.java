package org.sustudio.concise.app.thread;

import java.sql.PreparedStatement;

import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.keyword.Keyword;
import org.sustudio.concise.core.keyword.KeywordIterator;

public class CAKeywordThread extends CAThread {

	public CAKeywordThread(CAQuery query) {
		super(Gear.KeywordLister, query);
	}
	
	public void running() {
		
		try {
			SQLiteDB.dropTableIfExists(CATable.KeywordLister);
			SQLiteDB.createTableIfNotExists(CATable.KeywordLister);
			PreparedStatement ps = SQLiteDB.prepareStatement(CATable.KeywordLister);
			
			dialog.setStatus("keywording...");
			int count = 0;
			Workspace workspace = Concise.getCurrentWorkspace();
			KeywordIterator iterator = new KeywordIterator(workspace, CAPrefs.SHOW_PART_OF_SPEECH);
			for (Keyword keyword : iterator) {
				if (isKilled()) {
					break;
				}
				
				// insert into database
				SQLiteDB.addKeyword(ps, keyword);
				count++;
				dialog.setStatus(count + " keywords created...");
				if (count % 1000 == 0) {
					SQLiteDB.executeBatch(ps);
				}
			}
			SQLiteDB.executeBatch(ps);
			ps.close();
			
			// update overall statistics
			Concise.getData().totalTermFreq = iterator.getCorpusSumTotalTermFreq();
			Concise.getData().totalRefTermFreq = iterator.getReferenceSumTotalTermFreq();
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(gear, e);
		}		
		
	}
	
}
