package org.sustudio.concise.app.db;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.concordancePlotter.ConcPlotData;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.core.cluster.Cluster;
import org.sustudio.concise.core.collocation.Collocate;
import org.sustudio.concise.core.concordance.ConcLine;
import org.sustudio.concise.core.keyword.Keyword;
import org.sustudio.concise.core.wordlister.Word;

public class CADataUtils {
	
	public static int resetWordList(DBColumn sortedColumn, int sortedDirection, String whereClause) throws SQLException, ClassNotFoundException, IOException {
		int rsCount = 0;
		long corpusTotalTermFreq = 0;
		SQLiteDB.createTableIfNotExists(CATable.WordLister);
		
		if (Concise.getData().wordlist != null) {
			Concise.getData().wordlist.clear();
		}
		Concise.getData().wordlist = new ArrayList<Word>();
		String sql = SQLUtils.selectSyntax(CATable.WordLister, whereClause, sortedColumn, sortedDirection);
		ResultSet rs = SQLiteDB.executeQuery(sql);
		while (rs.next()) {
			rsCount++;
			String word 		= rs.getString(	DBColumn.Word.columnName());
			long docFreq 		= rs.getLong(	DBColumn.DocFreq.columnName());
			long totalTermFreq 	= rs.getLong(	DBColumn.Freq.columnName());
			byte[] bytes		= rs.getBytes(	DBColumn.ChildrenWords.columnName());
			
			Word ccWord = new Word(word, docFreq, totalTermFreq);
			ccWord.setChildrenByteArray(bytes);
			Concise.getData().wordlist.add(ccWord);
			corpusTotalTermFreq += totalTermFreq;
		}
		rs.close();
		Concise.getData().totalTermFreq = corpusTotalTermFreq;
		return rsCount;
	}
	
	
	/**
	 * Retrieve search words from concordance result
	 * @return
	 * @throws SQLException
	 * @throws UnsupportedEncodingException 
	 */
	public static List<String> getConcSearchWords() throws SQLException, IOException {
		List<String> searchWords = new ArrayList<String>();
		SQLiteDB.createTableIfNotExists(CATable.Concordancer);
		String sql = "SELECT " + DBColumn.Node.columnName() + " FROM " + CATable.Concordancer.name() + " GROUP BY " + DBColumn.Node.columnName();
		ResultSet rs = SQLiteDB.executeQuery(sql);
		while (rs.next()) {
			searchWords.add(rs.getString(DBColumn.Node.columnName()));
		}
		return searchWords;
	}
	
	public static int resetConcLineList(DBColumn sortedColumn, int sortedDirection, String whereClause) throws SQLException, IOException {
		int rsCount = 0;
		SQLiteDB.createTableIfNotExists(CATable.Concordancer);
		
		if (Concise.getData().concLineList != null) {
			Concise.getData().concLineList.clear();
		}
		Concise.getData().concLineList = new ArrayList<ConcLine>();
		String sql = SQLUtils.selectSyntax(CATable.Concordancer, whereClause, sortedColumn, sortedDirection);
		ResultSet rs = SQLiteDB.executeQuery(sql);
		while (rs.next()) {
			rsCount++;
			// rebuild object
			ConcLine concLine = new ConcLine();
			concLine.setDocId(		rs.getInt(	 DBColumn.DocID.columnName()));
			concLine.setDocTitle(	rs.getString(DBColumn.Doc_Title.columnName()));
			concLine.setLeft(		rs.getString(DBColumn.Left_Span.columnName()));
			concLine.setNode(		rs.getString(DBColumn.Node.columnName()));
			concLine.setRight(		rs.getString(DBColumn.Right_Span.columnName()));
			concLine.setWordId(		rs.getInt(	 DBColumn.WordID.columnName()));
			Concise.getData().concLineList.add(concLine);
		}
		rs.close();
		return rsCount;
	}
	
	public static int resetClusterList(DBColumn sortedColumn, int sortedDirection, String whereClause) throws SQLException, IOException {
		int rsCount = 0;
		SQLiteDB.createTableIfNotExists(CATable.WordCluster);
		
		if (Concise.getData().clusterList != null) {
			Concise.getData().clusterList.clear();
		}
		Concise.getData().clusterList = new ArrayList<Cluster>();
		String sql = SQLUtils.selectSyntax(CATable.WordCluster, whereClause, sortedColumn, sortedDirection);
		ResultSet rs = SQLiteDB.executeQuery(sql);
		while (rs.next()) {
			rsCount++;
			// rebuild object
			String cluster = rs.getString(DBColumn.Cluster.columnName());
			long freq = rs.getLong(DBColumn.Freq.columnName());
			Concise.getData().clusterList.add(new Cluster(cluster, freq));
		}
		rs.close();
		return rsCount;
	}
	
	public static int resetKeywordList(DBColumn sortColumn, int sortDirection, String whereClause) throws SQLException, IOException {
		int rsCount = 0;
		SQLiteDB.createTableIfNotExists(CATable.KeywordLister);
		
		if (Concise.getData().keywordList != null) {
			Concise.getData().keywordList.clear();
		}
		Concise.getData().keywordList = new ArrayList<Keyword>();
		String sql = SQLUtils.selectSyntax(CATable.KeywordLister, whereClause, sortColumn, sortDirection);
		ResultSet rs = SQLiteDB.executeQuery(sql);
		while (rs.next()) {
			rsCount++;
			Keyword keyword = new Keyword();
			keyword.p1 				= rs.getDouble	(DBColumn.Percent.columnName());
			keyword.p2 				= rs.getDouble	(DBColumn.PercentRef.columnName());
			if (!CAPrefs.SHOW_NEGATIVE_KEYWORDS && keyword.p1 < keyword.p2) {
				continue;
			}
			keyword.word 		= rs.getString	(DBColumn.Keyword.columnName());
			keyword.f1 				= rs.getLong	(DBColumn.Freq.columnName());
			keyword.f2 				= rs.getLong	(DBColumn.FreqRef.columnName());
			keyword.ll 				= rs.getDouble	(DBColumn.LogLikelihood.columnName());
			keyword.YatesChiSquare 	= rs.getDouble	(DBColumn.ChiSquaredCorr.columnName());
			
			Concise.getData().keywordList.add(keyword);
		}
		rs.close();
		return rsCount;
	}
	
	public static int resetCollocateList(DBColumn sortColumn, int sortDirection, String whereClause) throws Exception {
		int rsCount = 0;
		SQLiteDB.createTableIfNotExists(CATable.Collocator);
		if (Concise.getData().collocateList != null) {
			Concise.getData().collocateList.clear();
		}
		
		//
		// check database columns
		//
		ResultSet rs = SQLiteDB.executeQuery("SELECT * FROM " + CATable.Collocator.name() + " LIMIT 1");
		ResultSetMetaData metadata = rs.getMetaData();
		
		if (metadata.getColumnCount() - 1 != CATable.Collocator.dbColumns().length) {
			// reset dbColumns
			//	   Collocator's dbColumns will expand with its position vector
			CATable.Collocator.setDBColumns(null);
			DBColumn[] dbCols = CATable.Collocator.dbColumns();
			for (int i = 1; i <= metadata.getColumnCount(); i++) {
				// metadata index is 1-based 
				String columnName = metadata.getColumnName(i);
				if (columnName.equals("N") ||
					columnName.matches("^[LR]+\\d+")) 
				{
					dbCols = ArrayUtils.add(dbCols, new DBColumn(columnName, SQLiteDataType.BIGINT));
				}
			}
			CATable.Collocator.setDBColumns(dbCols);
		}
		rs.close();
		
		//
		// Now, read from database
		//
		Concise.getData().collocateList = new ArrayList<Collocate>();
		String sql = SQLUtils.selectSyntax(CATable.Collocator, whereClause, sortColumn, sortDirection);
		rs = SQLiteDB.executeQuery(sql);
		while (rs.next()) 
		{
			rsCount++;
			// rebuild object
			Collocate coll = new Collocate(
										rs.getString(DBColumn.Collocate.columnName()),
										rs.getLong	(DBColumn.SignatureO.columnName()),
										rs.getLong	(DBColumn.SignatureF1.columnName()),
										rs.getLong	(DBColumn.SignatureF2.columnName()),
										rs.getLong	(DBColumn.SignatureN.columnName()));
			
			//
			// append context info to surface collocation
			//
			coll.lFreq 			= rs.getLong(	DBColumn.Freq_Left.columnName());
			coll.rFreq 			= rs.getLong(	DBColumn.Freq_Right.columnName());
			
			// read position vectors
			DBColumn[] dbCols = CATable.Collocator.dbColumns();
			for (int i=0; i<dbCols.length; i++) {
				String col = dbCols[i].columnName();
				if (col.matches("^[LR]\\d+") || col.equals("N")) {
					long count = rs.getLong(col);
					if (col.equals("N")) {
						coll.setNodeFreq(count);
					}
					else {
						coll.setPositionVector(col, count);
					}
				}
			}
			
			Concise.getData().collocateList.add(coll);
		}
		rs.close();
		return rsCount;
	}
	
	
	public static int resetPlotList(DBColumn sortColumn, int sortDirection, String whereClause) throws Exception 
	{
		int rsCount = 0;
		SQLiteDB.createTableIfNotExists(CATable.ConcordancePlotter);
		
		if (Concise.getData().plotDataList != null) {
			Concise.getData().plotDataList.clear();
		}
		Concise.getData().plotDataList = new ArrayList<ConcPlotData>();
		String sql = SQLUtils.selectSyntax(CATable.ConcordancePlotter, whereClause, sortColumn, sortDirection);
		ResultSet rs = SQLiteDB.executeQuery(sql);
		while (rs.next()) {
			rsCount++;
			ConcPlotData plotData = new ConcPlotData();
			plotData.docID		= rs.getInt(DBColumn.DocID.columnName());
			plotData.setPositions(rs.getString(DBColumn.PlotData.columnName()));
			plotData.hits 		= rs.getLong(DBColumn.Hits.columnName());
			plotData.words 		= rs.getLong(DBColumn.Words.columnName());
			plotData.per1000 	= rs.getDouble(DBColumn.Per1000.columnName());
			plotData.filepath 	= rs.getString(DBColumn.Filepath.columnName());
			Concise.getData().plotDataList.add(plotData);
		}
		rs.close();
		return rsCount;
	}
	
}
