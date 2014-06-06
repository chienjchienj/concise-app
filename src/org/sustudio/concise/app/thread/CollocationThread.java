package org.sustudio.concise.app.thread;

import java.sql.PreparedStatement;

import org.apache.commons.lang3.ArrayUtils;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.db.SQLiteDataType;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.collocation.Collocate;
import org.sustudio.concise.core.collocation.CollocateIterator;
import org.sustudio.concise.core.collocation.SurfaceCollocateIterator;
import org.sustudio.concise.core.collocation.TextualCollocateIterator;
import org.sustudio.concise.core.collocation.TextualCollocateIterator.BOUNDARY;
import org.sustudio.concise.core.concordance.Conc;

public class CollocationThread extends CAThread {
	
	public CollocationThread(CAQuery query) {
		super(Gear.Collocator, query);
	}
	
	public void running() {
		
		try 
		{
			dialog.setStatus("configuring...");
			// reset dbColumns
			CATable.Collocator.setDBColumns(null);
			DBColumn[] dbCols = CATable.Collocator.dbColumns();
			
			dialog.setStatus("concordancing ...");
			// display original text for sentence iterator
			Workspace workspace = Concise.getCurrentWorkspace();
			Conc conc = new Conc(workspace, query.searchStr, CAPrefs.SHOW_PART_OF_SPEECH);
			conc.setSpanSize(query.leftSpanSize, query.rightSpanSize);
			
			CollocateIterator collocateIterator;
			switch (CAPrefs.COLLOCATION_MODE) {
			case Surface:
				collocateIterator = new SurfaceCollocateIterator(conc);
				// add collocate vector columns
				for (int i=query.leftSpanSize; i>0; i--) {
					dbCols = ArrayUtils.add(dbCols, new DBColumn("L" + i, SQLiteDataType.BIGINT));
				}
				dbCols = ArrayUtils.add(dbCols, DBColumn.NodeFreq);
				for (int i=1; i<=query.rightSpanSize; i++) {
					dbCols = ArrayUtils.add(dbCols, new DBColumn("R" + i, SQLiteDataType.BIGINT));
				}
				break;
			
			case SentenceTextual:
				collocateIterator = new TextualCollocateIterator(conc, BOUNDARY.SENTENCE);
				break;
			case ParagraphTextual:
				collocateIterator = new TextualCollocateIterator(conc, BOUNDARY.PARAGRAPH);
				break;
				
			
			default:
				collocateIterator = null;
				break;
			}
			CATable.Collocator.setDBColumns(dbCols);
			
			SQLiteDB.dropTableIfExists(CATable.Collocator);
			SQLiteDB.createTableIfNotExists(CATable.Collocator);
			PreparedStatement ps = SQLiteDB.prepareStatement(CATable.Collocator);
			
			int count = 0;
			for (Collocate coll : collocateIterator) {
				if (isInterrupted()) {
					break;
				}
				
				SQLiteDB.addCollocate(ps, coll, dbCols);
				count++;
				
				dialog.setStatus(count + " collocates ...");
				if (count % 1000 == 0) {
					SQLiteDB.executeBatch(ps);
				}
			}
			SQLiteDB.executeBatch(ps);
			ps.close();
			conc = null;
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}		
		
	}
	
}
