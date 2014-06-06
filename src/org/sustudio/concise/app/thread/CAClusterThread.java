package org.sustudio.concise.app.thread;

import java.sql.PreparedStatement;

import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.cluster.Cluster;
import org.sustudio.concise.core.cluster.ClusterIterator;
import org.sustudio.concise.core.cluster.ConcClusterIterator;
import org.sustudio.concise.core.cluster.NgramClusterIterator;
import org.sustudio.concise.core.concordance.Conc;

public class CAClusterThread extends ConciseThread {

	public CAClusterThread(CAQuery query) {
		super(Gear.WordCluster, query);		
	}
	
	public void running() {
		
		try {
			// load existing files...
			
			dialog.setStatus("configuring...");
			SQLiteDB.dropTableIfExists(CATable.WordCluster);
			SQLiteDB.createTableIfNotExists(CATable.WordCluster);
			PreparedStatement ps = SQLiteDB.prepareStatement(CATable.WordCluster);
			
			Workspace workspace = Concise.getCurrentWorkspace();
			dialog.setStatus("clustering...");
			
			ClusterIterator iterator;
			if (query.ngram) {
				iterator = new NgramClusterIterator(workspace, query.leftSpanSize, CAPrefs.SHOW_PART_OF_SPEECH);
			}
			else {
				Conc conc = new Conc(workspace, query.searchStr, CAPrefs.SHOW_PART_OF_SPEECH);
				conc.setSpanSize(query.leftSpanSize, query.rightSpanSize);
				iterator = new ConcClusterIterator(conc);
			}
			
			int count = 0;
			// insert into database
			for (Cluster cluster : iterator) {
				if (isInterrupted()) break;
				
				SQLiteDB.addCluster(ps, cluster);
				count++;
				dialog.setStatus("writing " + count + " clusters...");
				if (count % 10000 == 0) {
					SQLiteDB.executeBatch(ps);
				}
			}
			SQLiteDB.executeBatch(ps);
			ps.close();
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
	}
}
