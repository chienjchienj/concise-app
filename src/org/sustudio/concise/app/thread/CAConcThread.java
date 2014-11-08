package org.sustudio.concise.app.thread;

import java.sql.PreparedStatement;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.search.ScoreDoc;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.SQLiteDataType;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.concordance.ConcLine;
import org.sustudio.concise.core.concordance.ConcLineIterator;

public class CAConcThread extends ConciseThread {

	public CAConcThread(CAQuery query) {
		super(Gear.Concordancer, query);
	}
	
	public void running() {
		
		try {
			dialog.setStatus("checking...");
			
			// reset dbColumns for position vectors
			CATable.Concordancer.setDBColumns(null);
			DBColumn[] dbCols = CATable.Concordancer.dbColumns();
			
			dialog.setStatus("concordancing...");
			Workspace workspace = Concise.getCurrentWorkspace();
			Conc conc = new Conc(workspace, query.searchStr, CAPrefs.SHOW_PART_OF_SPEECH);
			conc.setSpanSize(query.leftSpanSize, query.rightSpanSize);
			
			// add position vector columns
			for (int i=query.leftSpanSize; i>0; i--) {
				dbCols = ArrayUtils.add(dbCols, new DBColumn("L" + i, SQLiteDataType.VARCHAR));
			}
			for (int i=1; i<=query.rightSpanSize; i++) {
				dbCols = ArrayUtils.add(dbCols, new DBColumn("R" + i, SQLiteDataType.VARCHAR));
			}
			CATable.Concordancer.setDBColumns(dbCols);
			
			// Drop old table
			SQLiteDB.dropTableIfExists(CATable.Concordancer);
			// create new table and prepare to write data
			SQLiteDB.createTableIfNotExists(CATable.Concordancer);
			PreparedStatement ps = SQLiteDB.prepareStatement(CATable.Concordancer);
			int lineCount = 0;
			for (ScoreDoc scoreDoc :  conc.hitDocs()) 
			{
				if (isInterrupted()) {
					break;
				}
				
				for (ConcLine concLine : new ConcLineIterator(conc, scoreDoc)) 
				{
					if (isInterrupted()) {
						break;
					}
					
					// insert into database
					// TODO replace SQLiteDB.addConcLine(ps, concLine);
					ps.setInt	(1, concLine.getDocId());
					ps.setString(2, concLine.getDocTitle());
					ps.setString(3, concLine.getLeft());
					ps.setString(4, concLine.getNode());
					ps.setString(5, concLine.getRight());
					ps.setInt	(6, concLine.getWordId());
					
					// position vectors
					String[] l = concLine.getLeft().split(" ");
					int target = dbCols.length - query.rightSpanSize;
					for (int i= target - 1;
						 i >= dbCols.length - query.rightSpanSize - query.leftSpanSize;
						 i--) 
					{
						String s = "";
						int idx = Integer.valueOf(dbCols[i].columnName().substring(1));
						if (idx < l.length)
							s = l[l.length - idx];
						ps.setString(i+1, s);
					}
					// right position vector
					String[] r = concLine.getRight().split(" ");
					int base = dbCols.length - query.rightSpanSize;
					for (int i = base; i < dbCols.length; i++) {
						String word = "";
						if (i - base < r.length) {
							word = r[i-base];
						}
						ps.setString(i+1, word);
					}
					
					ps.addBatch();
					
					
					lineCount++;
					dialog.setStatus(lineCount + " concordance lines found...");
					
					if (lineCount % 1000 == 0) {
						SQLiteDB.executeBatch(ps);
					}
				}
				SQLiteDB.executeBatch(ps);
			}
			SQLiteDB.executeBatch(ps);
			ps.close();
			
		} catch (final Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
	}
	
}
