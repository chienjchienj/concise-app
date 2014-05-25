package org.sustudio.concise.app.db;


public enum CATable {

	/** CAQuery */
	CAQuery(new DBColumn[] {
					DBColumn.Gear,
					DBColumn.SearchString,
					DBColumn.SearchAction,
					DBColumn.LeftSpanSize,
					DBColumn.RightSpanSize,
					DBColumn.NGram }),
	
	/** Corpus Manager */
	CorpusManager(new DBColumn[] {
					DBColumn.DocID,
					DBColumn.Title,
					DBColumn.NumWords,
					DBColumn.NumParagraphs,
					DBColumn.Filepath,
					DBColumn.IsTokenized }),

	/** Reference Corpus Manager */
	ReferenceCorpusManager(new DBColumn[] {
					DBColumn.DocID,
					DBColumn.Title,
					DBColumn.NumWords,
					DBColumn.NumParagraphs,
					DBColumn.Filepath,
					DBColumn.IsTokenized }),
			
	/** Concordancer */
	Concordancer(new DBColumn[] { 
					DBColumn.DocID,
					DBColumn.Doc_Title,
					DBColumn.Left_Span,
					DBColumn.Node,
					DBColumn.Right_Span,
					DBColumn.WordID }),	
	
	/** Word Cluster */				
	WordCluster(new DBColumn[] {
					DBColumn.Freq,
					DBColumn.Cluster }), 
	
	/** Word Lister */
	WordLister(new DBColumn[] {
					DBColumn.Freq,
					DBColumn.Word,
					DBColumn.DocFreq,
					DBColumn.ChildrenWords}),
	
	/** Keyword Lister */
	KeywordLister(new DBColumn[] {
					DBColumn.Keyword, 
					DBColumn.Freq,
					DBColumn.Percent,
					DBColumn.FreqRef,
					DBColumn.PercentRef,
					DBColumn.LogLikelihood,
					DBColumn.LogLikelihood_P,
					DBColumn.ChiSquaredCorr,
					DBColumn.ChiSquaredCorr_P }),
	
	/** Collocator */
	Collocator(new DBColumn[] {
					DBColumn.Freq,
					DBColumn.Collocate,
					DBColumn.Freq_Left,
					DBColumn.Freq_Right,
					DBColumn.MI,
					DBColumn.MI3,
					DBColumn.Dice,
					DBColumn.OddsRatio,
					DBColumn.t_score,
					DBColumn.z_score,
					DBColumn.SimpleLL,
					DBColumn.LogLikelihood,
					DBColumn.ChiSquaredCorr,
					DBColumn.SignatureO,
					DBColumn.SignatureF1,
					DBColumn.SignatureF2,
					DBColumn.SignatureN	}),
	
	/** Collocational Networker */
	CollocationalNetworker(new DBColumn[] {
					DBColumn.BatchIndex,
					DBColumn.Collocate,
					DBColumn.SignatureO,
					DBColumn.SignatureF1,
					DBColumn.SignatureF2,
					DBColumn.SignatureN,
					DBColumn.NodeFreq }),
	
	/** Concordance Plotter */
	ConcordancePlotter(new DBColumn[] {
					DBColumn.DocID,
					DBColumn.PlotData,
					DBColumn.Hits,
					DBColumn.Words,
					DBColumn.Per1000,
					DBColumn.Filepath }),
	
	;
	
	private final DBColumn[] defaultDBColumns;
	private DBColumn[] dbColumns;
	
	CATable(final DBColumn[] dbColumns) {
		this.defaultDBColumns = dbColumns;
	}
	
	/**
	 * Returns {@link DBColumn} object by its column name
	 * @param column
	 * @return
	 */
	public DBColumn dbColumnOf(final String column) {
		for (DBColumn col : defaultDBColumns) {
			if (col.columnName().equals(column))
				return col;
		}
		return null;
	}
	
	public void setDBColumns(DBColumn[] dbColumns) {
		this.dbColumns = dbColumns;
	}
	
	public DBColumn[] dbColumns() {
		return dbColumns == null ? defaultDBColumns : dbColumns;
	}
	
	public String[] columns() {
		if (dbColumns() == null) return null;
		
		String[] cols = new String[dbColumns().length];
		for (int i=0; i<cols.length; i++) {
			cols[i] = dbColumns()[i].columnName();
		}
		return cols;
	}
	
	
	/**
	 * Returns create table sql schema
	 * @return
	 */
	public String getCreateTableIfNotExitsSQLSchema() {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS " + name() + " ( ");
		sql.append("  id Integer NOT NULL, ");
		for (DBColumn col : dbColumns()) {
			sql.append("  " + col.schema() + ", ");
		}
		sql.append("  PRIMARY KEY(id) ");
		sql.append(")");
		return sql.toString();
	}
	
	/**
	 * Returns drop table sql schema
	 * @return
	 */
	public String getDropTableSQLSchema() {
		final String sql = "DROP TABLE IF EXISTS " + name();
		return sql;
	}
}
