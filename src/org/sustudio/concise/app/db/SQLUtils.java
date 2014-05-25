package org.sustudio.concise.app.db;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.sustudio.concise.app.preferences.CAPrefs;

public class SQLUtils {

	public static String selectSyntax(CATable table, String whereClause, DBColumn column, int sortedDirection) {
		StringBuilder sql = new StringBuilder(SQLUtils.selectSyntax(table));
		if (whereClause != null) {
			sql.append(" WHERE " + whereClause + " ");
		}
		sql.append(" ORDER BY " + column.columnName());
		sql.append(sortedDirection == SWT.UP ? " ASC" : " DESC");
		
		// -1 = unlimited
		if (!table.equals(CATable.CorpusManager) &&
			!table.equals(CATable.ReferenceCorpusManager) &&
			CAPrefs.TOP_RECORDS != -1) 
		{
			sql.append(" LIMIT " + CAPrefs.TOP_RECORDS);
		}
		return sql.toString();
	}

	public static String selectSyntax(CATable table, DBColumn column, int sortedDirection) {
		return selectSyntax(table, null, column, sortedDirection);
	}

	/**
	 * Default order is descending
	 * @param table
	 * @param column
	 * @return
	 */
	public static String selectSyntax(CATable table, DBColumn column) {
		return selectSyntax(table, column, SWT.DOWN);
	}

	public static String selectSyntax(CATable table) {
		StringBuilder sql = new StringBuilder("SELECT * FROM " + table.name());
		//sql.append(StringUtils.join(table.columns(), ", "));
		//sql.append(" FROM " + table.name());
		return sql.toString();
	}

	public static String insertPreparedStatement(CATable table) {
		StringBuilder sql = new StringBuilder("INSERT INTO " + table.name() + " (");
		sql.append(StringUtils.join(table.columns(), ", ") + ") VALUES (");
		for (int i=0; i<table.columns().length; i++) {
			sql.append( (i > 0 ? ", ?" : "?"));
		}		
		sql.append(")");	
		return sql.toString();
	}

}
