package org.sustudio.concise.app.db;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sustudio.concise.app.Concise;
import org.sustudio.concise.core.cluster.Cluster;
import org.sustudio.concise.core.collocation.Collocate;
import org.sustudio.concise.core.concordance.ConcLine;
import org.sustudio.concise.core.keyword.Keyword;
import org.sustudio.concise.core.wordlister.Word;

public class SQLiteDB {
	
	public static int createTableIfNotExists(CATable table) throws SQLException, IOException {
		String sql = table.getCreateTableIfNotExitsSQLSchema();
		return executeUpdate(sql);
	}
	
	public static int dropTableIfExists(CATable table) throws SQLException, IOException {
		final String sql = table.getDropTableSQLSchema();
		return executeUpdate(sql);
	}
	
	
	public static ResultSet executeQuery(String sql) throws SQLException, IOException {
		Statement s = Concise.getCurrentWorkspace().getConnection().createStatement();
		return s.executeQuery(sql);
	}
	
	
	public static ResultSet executeQuery(String sql, int limit) throws SQLException, IOException {
		sql += " LIMIT " + limit;
		Statement s = Concise.getCurrentWorkspace().getConnection().createStatement();
		return s.executeQuery(sql);
	}
	
	
	public static void closeResultSet(ResultSet rs) throws SQLException {
		if (rs != null && !rs.isClosed()) {
			Statement s = rs.getStatement();
			rs.close();
			s.close();
		}
	}
	
	public static boolean tableExists(CATable table) throws SQLException, IOException {
		String tableName = null;
		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table.name() + "'";
		Statement s = Concise.getCurrentWorkspace().getConnection().createStatement();
		ResultSet rs = s.executeQuery(sql);
		if (rs.next()) {
			tableName = rs.getString(1);
		}
		rs.close();
		s.close();
		return tableName != null;
	}
	
	
	public static int executeUpdate(String sql) throws SQLException, IOException {
		Statement s = Concise.getCurrentWorkspace().getConnection().createStatement();
		return s.executeUpdate(sql);
	}
	
	public static PreparedStatement prepareStatement(CATable table) throws SQLException, IOException {
		String psSQL = SQLUtils.insertPreparedStatement(table);
		return prepareStatement(psSQL);
	}
	
	public static PreparedStatement prepareStatement(String sql) throws SQLException, IOException {
		return Concise.getCurrentWorkspace().getConnection().prepareStatement(sql);
	}
	
	public static int executeBatch(PreparedStatement pstmt) throws SQLException, IOException {
		int[] result = pstmt.executeBatch();
		Concise.getCurrentWorkspace().getConnection().commit();	// manual commit
		return result.length;
	}
	
	
	//////////////////////////////////////////////////////////////////
	// Insert Data
	//////////////////////////////////////////////////////////////////
	
	public static void addCollocate(PreparedStatement ps, Collocate coll, DBColumn[] dbCols) throws SQLException {
		ps.setLong	(1,	 coll.getFreq());
		ps.setString(2,	 coll.getWord());
		ps.setLong	(3,	 coll.getLeftFreq());
		ps.setLong	(4,	 coll.getRightFreq());
		ps.setDouble(5,	 ensureDouble(coll.getMI()));
		ps.setDouble(6,	 ensureDouble(coll.getMI3()));
		ps.setDouble(7,	 ensureDouble(coll.getDice()));
		ps.setDouble(8,	 ensureDouble(coll.getOddsRatio()));
		ps.setDouble(9,	 ensureDouble(coll.getTscore()));
		ps.setDouble(10, ensureDouble(coll.getZscore()));
		ps.setDouble(11, ensureDouble(coll.getSimpleLL()));
		ps.setDouble(12, ensureDouble(coll.getLogLikelihood()));
		ps.setDouble(13, ensureDouble(coll.getChiSquaredCorr()));
		ps.setLong	(14, coll.getSignatureO());
		ps.setLong	(15, coll.getSignatureF1());
		ps.setLong	(16, coll.getSignatureF2());
		ps.setLong	(17, coll.getSignatureN());
						
		// add position vectors
		for (int i=17; i<dbCols.length; i++) {
			String col = dbCols[i].columnName();
			if (dbCols[i].equals(DBColumn.NodeFreq)) {
				ps.setLong(i+1, coll.getNodeFreq());
			}
			else {
				ps.setLong(i+1, coll.getCountAsPosition(col));
			}
		}
		ps.addBatch();
	}
	
	private static double ensureDouble(double value) {
		return new Double(value).equals(Double.NaN) ? 0d : value;
	}
	
	
	public static void addCluster(PreparedStatement ps, Cluster cluster) throws SQLException {
		ps.setLong	(1, cluster.freq);
		ps.setString(2, cluster.word);
		ps.addBatch();
	}
	
	
	public static void addConcLine(PreparedStatement ps, ConcLine concLine) throws SQLException {
		ps.setInt	(1, concLine.getDocId());
		ps.setString(2, concLine.getDocTitle());
		ps.setString(3, concLine.getLeft());
		ps.setString(4, concLine.getNode());
		ps.setString(5, concLine.getRight());
		ps.setInt	(6, concLine.getWordId());
		ps.addBatch();
	}
	
	public static void addKeyword(PreparedStatement ps, Keyword keyword) throws Exception {
		ps.setString(1, keyword.getWord());
		ps.setLong	(2, keyword.getFreq());
		ps.setDouble(3, keyword.getPercent());
		ps.setLong	(4, keyword.getRefFreq());
		ps.setDouble(5, keyword.getRefPercent());
		ps.setDouble(6, keyword.getLL());
		ps.setDouble(7, keyword.getLLPvalue());
		ps.setDouble(8, keyword.getChiSquare());
		ps.setDouble(9, keyword.getChiSquarePvalue());
		ps.addBatch();
	}
	
	public static void addWord(PreparedStatement ps, Word word) throws Exception {
		ps.setLong	(1, word.totalTermFreq);
		ps.setString(2, word.word);
		ps.setLong	(3, word.docFreq);
		ps.setBytes	(4, word.getChildrenByteArray());
		ps.addBatch();
	}
	
	public static void addCollocationalNetwork(PreparedStatement ps, Collocate coll, int batchIndex) throws Exception {
		ps.setInt	(1, batchIndex);
		ps.setString(2, coll.getWord());
		ps.setLong	(3, coll.getSignatureO());
		ps.setLong	(4, coll.getSignatureF1());
		ps.setLong	(5, coll.getSignatureF2());
		ps.setLong	(6, coll.getSignatureN());
		ps.setLong	(7, coll.getNodeFreq());
		ps.addBatch();
	}
}
