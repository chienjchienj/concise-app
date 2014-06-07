package org.sustudio.concise.app.query;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.MultiTermQuery;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLUtils;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class CAQueryUtils {

	public static void logQuery(CAQuery query) throws SQLException, IOException {
		SQLiteDB.createTableIfNotExists(CATable.CAQuery);
		
		// remove old query (by gear)
		String removeSQL = "DELETE FROM " + CATable.CAQuery.name() +
				 		   " WHERE " + DBColumn.Gear.columnName() + " = '" + query.getGear().name() + "'";
		SQLiteDB.executeUpdate(removeSQL);
		
		// create new record
		String psSQL = SQLUtils.insertPreparedStatement(CATable.CAQuery);
		PreparedStatement ps = SQLiteDB.prepareStatement(psSQL);
		ps.setString	(1, query.getGear().name());
		ps.setString	(2, query.searchStr);
		ps.setInt		(3, query.leftSpanSize);
		ps.setInt		(4, query.rightSpanSize);
		ps.setBoolean	(5, query.ngram);
		ps.executeUpdate();
		ps.close();
		Concise.getCurrentWorkspace().getConnection().commit();
	}

	
	public static CAQuery getQuery(Gear gear) throws SQLException, IOException {
		CAQuery result = null;
		SQLiteDB.createTableIfNotExists(CATable.CAQuery);
		
		String sql = "SELECT * FROM " + CATable.CAQuery.name() +
					 " WHERE " + DBColumn.Gear.columnName() + " = '" + gear.name() + "'";
		ResultSet rs = SQLiteDB.executeQuery(sql);
		if (rs.next()) {	// there should always be only 1 record.
			result = new CAQuery(gear);
			result.searchStr 		= rs.getString(DBColumn.SearchString.columnName());
			result.leftSpanSize 	= rs.getInt(DBColumn.LeftSpanSize.columnName());
			result.rightSpanSize	= rs.getInt(DBColumn.RightSpanSize.columnName());
			result.ngram 			= rs.getBoolean(DBColumn.NGram.columnName());
		}
		rs.close();
		
		if (result == null) {
			// give it a default query
			result = new CAQuery(gear);
		}
		return result;
	}
	
	/**
	 * 檢查送出的 {@link CAQuery} 是否能被處理，看看輸入的搜尋字串有沒有問題
	 * @param query
	 * @return
	 */
	public static boolean isValidQuery(CAQuery query) {
		
		QueryParser parser = new QueryParser(
				Config.LUCENE_VERSION, 
				ConciseField.CONTENT.field(), 
				new WhitespaceAnalyzer(Config.LUCENE_VERSION));
		parser.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
		parser.setAllowLeadingWildcard(true);
		
		try {
			parser.parse(query.searchStr);
			
		} catch (ParseException e) {
			CAErrorMessageDialog.open(query.getGear(), e);			
			return false;
		}
		parser = null;
		return true;
	}
}
