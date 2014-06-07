package org.sustudio.concise.app.db;

public class DBColumn {
	
	private final String columnName;
	private final SQLiteDataType dataType;
		
	public DBColumn(String columnName, SQLiteDataType dataType) {
		this.columnName = columnName;
		this.dataType = dataType;
	}
	
	public SQLiteDataType getDataType() {
		return dataType;
	}
	
	public String columnName() {
		return columnName;
	}
	
	public String schema() {
		return columnName + " " + dataType.name() + " NOT NULL";
	}

	
	/////////////////////////////////////////////////////////
	// General Id
	/////////////////////////////////////////////////////////
	
	public static final DBColumn Id				= new DBColumn("id", 			SQLiteDataType.INTEGER);
	
	
	/////////////////////////////////////////////////////////
	// Concise Query (System table)
	/////////////////////////////////////////////////////////
	
	public static final DBColumn Gear			= new DBColumn("Gear",			SQLiteDataType.VARCHAR);
	public static final DBColumn SearchString	= new DBColumn("SearchString", 	SQLiteDataType.VARCHAR);
	//public static final DBColumn SearchAction 	= new DBColumn("SearchAction", 	SQLiteDataType.VARCHAR);
	public static final DBColumn LeftSpanSize	= new DBColumn("LeftSpanSize", 	SQLiteDataType.INTEGER);
	public static final DBColumn RightSpanSize	= new DBColumn("RightSpanSize", SQLiteDataType.INTEGER);
	public static final DBColumn NGram			= new DBColumn("NGram", 		SQLiteDataType.INTEGER);
	
	
	/////////////////////////////////////////////////////////
	// Corpus and Reference Corpus Manager
	/////////////////////////////////////////////////////////
	
	public static final DBColumn DocID 			= new DBColumn("DocId",			SQLiteDataType.INTEGER);
	public static final DBColumn Title			= new DBColumn("Title", 		SQLiteDataType.VARCHAR);
	public static final DBColumn NumWords		= new DBColumn("NumWords", 		SQLiteDataType.BIGINT);
	public static final DBColumn NumParagraphs 	= new DBColumn("NumParagraphs", SQLiteDataType.BIGINT);
	public static final DBColumn Filepath		= new DBColumn("Filepath", 		SQLiteDataType.VARCHAR);
	public static final DBColumn IsTokenized	= new DBColumn("IsTokenized",	SQLiteDataType.INTEGER);
	
	/////////////////////////////////////////////////////////
	// Concordancer
	/////////////////////////////////////////////////////////
	
	//public static final DBColumn DocID 			= new DBColumn("DocId",		SQLiteDataType.INTEGER);
	public static final DBColumn Doc_Title 		= new DBColumn("DocTitle", 		SQLiteDataType.VARCHAR);
	public static final DBColumn Left_Span 		= new DBColumn("LeftSpan", 		SQLiteDataType.VARCHAR);
	public static final DBColumn Node 			= new DBColumn("Node", 			SQLiteDataType.VARCHAR);
	public static final DBColumn Right_Span 	= new DBColumn("RightSpan",		SQLiteDataType.VARCHAR);
	public static final DBColumn WordID			= new DBColumn("WordID", 		SQLiteDataType.INTEGER);
	
	
	/////////////////////////////////////////////////////////
	// WordCluster
	/////////////////////////////////////////////////////////
	
	public static final DBColumn Freq 			= new DBColumn("Freq", 		SQLiteDataType.BIGINT);
	public static final DBColumn Cluster 		= new DBColumn("Cluster", 	SQLiteDataType.VARCHAR);
	
	
	/////////////////////////////////////////////////////////
	// Keyworder
	/////////////////////////////////////////////////////////
	
	public static final DBColumn Keyword 			= new DBColumn("Keyword", 				SQLiteDataType.VARCHAR);
	//public static final DBColumn Freq 			= new DBColumn("Freq", 					SQLiteDataType.BIGINT);
	public static final DBColumn FreqRef 			= new DBColumn("FreqRef", 				SQLiteDataType.BIGINT);
	public static final DBColumn Percent 			= new DBColumn("Percent", 				SQLiteDataType.DOUBLE);
	public static final DBColumn PercentRef 		= new DBColumn("PercentRef", 			SQLiteDataType.DOUBLE);
	public static final DBColumn LogLikelihood 		= new DBColumn("LogLikelihood", 		SQLiteDataType.DOUBLE);
	public static final DBColumn LogLikelihood_P 	= new DBColumn("LogLikelihoodPvalue",	SQLiteDataType.DOUBLE);
	public static final DBColumn ChiSquaredCorr		= new DBColumn("ChiSquaredCorr", 		SQLiteDataType.DOUBLE);
	public static final DBColumn ChiSquaredCorr_P 	= new DBColumn("ChiSquaredCorrPvalue", 	SQLiteDataType.DOUBLE);
	
	
	/////////////////////////////////////////////////////////
	// WordLister
	/////////////////////////////////////////////////////////
	
	//public static final DBColumn Freq = new DBColumn("Freq", SQLiteDataType.BIGINT);
	public static final DBColumn Word 			= new DBColumn("Word", SQLiteDataType.VARCHAR);
	public static final DBColumn DocFreq 		= new DBColumn("DocFreq", SQLiteDataType.BIGINT);
	public static final DBColumn ChildrenWords	= new DBColumn("ChildrenWords", SQLiteDataType.BLOB);
	
	
	/////////////////////////////////////////////////////////
	// Collocation
	/////////////////////////////////////////////////////////
	
	//public static final DBColumn Freq = new DBColumn("Freq", SQLiteDataType.BIGINT);
	public static final DBColumn Collocate 		= new DBColumn("Collocate", SQLiteDataType.VARCHAR);
	public static final DBColumn Freq_Left 		= new DBColumn("LFreq", 	SQLiteDataType.BIGINT);
	public static final DBColumn Freq_Right 	= new DBColumn("RFreq", 	SQLiteDataType.BIGINT);
	public static final DBColumn MI 			= new DBColumn("MI", 		SQLiteDataType.DOUBLE);
	public static final DBColumn MI3 			= new DBColumn("MI3", 		SQLiteDataType.DOUBLE);
	public static final DBColumn Dice 			= new DBColumn("Dice", 		SQLiteDataType.DOUBLE);
	public static final DBColumn OddsRatio 		= new DBColumn("OddsRatio", SQLiteDataType.DOUBLE);
	public static final DBColumn t_score 		= new DBColumn("Tscore", 	SQLiteDataType.DOUBLE);
	public static final DBColumn z_score 		= new DBColumn("Zscore", 	SQLiteDataType.DOUBLE);
	public static final DBColumn SimpleLL 		= new DBColumn("SimpleLL", 	SQLiteDataType.DOUBLE);
	//public static final DBColumn LogLikelihood = new DBColumn("LogLikelihood", SQLiteDataType.DOUBLE);
	//public static final DBColumn ChiSquaredCorr = new DBColumn("Coll_ChiSquaredCorr", SQLiteDataType.DOUBLE);
	public static final DBColumn SignatureO		= new DBColumn("SignatureO",	SQLiteDataType.BIGINT);
	public static final DBColumn SignatureF1	= new DBColumn("SignatureF1",	SQLiteDataType.BIGINT);
	public static final DBColumn SignatureF2	= new DBColumn("SignatureF2",	SQLiteDataType.BIGINT);
	public static final DBColumn SignatureN		= new DBColumn("SignatureN",	SQLiteDataType.BIGINT);
	public static final DBColumn NodeFreq		= new DBColumn("N",	SQLiteDataType.BIGINT);
	
	
	/////////////////////////////////////////////////////////
	// Collocational Networker
	/////////////////////////////////////////////////////////
	
	public static final DBColumn BatchIndex		= new DBColumn("BatchIndex",	SQLiteDataType.INTEGER);
	//public static final DBColumn SignatureO		= new DBColumn("SignatureO",	SQLiteDataType.BIGINT);
	//public static final DBColumn SignatureF1	= new DBColumn("SignatureF1",	SQLiteDataType.BIGINT);
	//public static final DBColumn SignatureF2	= new DBColumn("SignatureF2",	SQLiteDataType.BIGINT);
	//public static final DBColumn SignatureN		= new DBColumn("SignatureN",	SQLiteDataType.BIGINT);
	//public static final DBColumn NodeFreq		= new DBColumn("N",	SQLiteDataType.BIGINT);
	
	
	/////////////////////////////////////////////////////////
	// Concordance Plotter
	/////////////////////////////////////////////////////////
	
	//public static final DBColumn DocID 			= new DBColumn("DocId",			SQLiteDataType.INTEGER);
	public static final DBColumn PlotData		= new DBColumn("PlotData", SQLiteDataType.VARCHAR);
	public static final DBColumn Hits			= new DBColumn("Hits", SQLiteDataType.BIGINT);
	public static final DBColumn Words			= new DBColumn("Words", SQLiteDataType.BIGINT);
	public static final DBColumn Per1000		= new DBColumn("Per1000", SQLiteDataType.DOUBLE);
	
	
	
	
	/////////////////////////////////////////////////////////
	// Properties (Mainly Preferences)
	/////////////////////////////////////////////////////////
	
	public static final DBColumn Class = new DBColumn("Class", SQLiteDataType.VARCHAR);
	public static final DBColumn Object = new DBColumn("Object", SQLiteDataType.BLOB);
}
