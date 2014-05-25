package org.sustudio.concise.app.gear;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.sustudio.concise.app.db.DBColumn;

public class ColumnSortListener extends SelectionAdapter {
	
	public void widgetSelected(SelectionEvent event) {
		// double check
		if (event.widget instanceof TableColumn) {
			TableColumn column = (TableColumn) event.widget;
			DBColumn dbColumn = (DBColumn) column.getData(GearController._DB_COLUMN);
			if (dbColumn != null) {
				Table table = column.getParent();
				int dir = table.getSortDirection();
				
				if (column == table.getSortColumn())
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				else
					table.setSortColumn(column);
				table.setSortDirection(dir);
				
				if (table.getParent() instanceof IGearSortable) {
					((IGearSortable) table.getParent()).sort();
				}
			}
		}
		
		else if (event.widget instanceof TreeColumn) {
			TreeColumn column = (TreeColumn) event.widget;
			DBColumn dbColumn = (DBColumn) column.getData(GearController._DB_COLUMN);
			if (dbColumn != null) {
				Tree tree = column.getParent();
				int dir = tree.getSortDirection();
				
				if (column == tree.getSortColumn())
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				else
					tree.setSortColumn(column);
				tree.setSortDirection(dir);
				
				if (tree.getParent() instanceof IGearSortable) {
					((IGearSortable) tree.getParent()).sort();
				}
			}
		}
	}
	
}
