package org.sustudio.concise.app.toolbar;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.enums.SearchAction;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.concordance.Conc;

public class CAToolBarSearchActionListener extends SelectionAdapter {
	
	public void widgetDefaultSelected(SelectionEvent event) {
		// validating query first
		CAQuery query = Concise.getActiveApp().toolBar.getQuery();
		if (validQuery(query)) {
			CASearchAction.doIt(query);
		}
	}
	
	private boolean validQuery(CAQuery query) {
		if (query.searchAction == SearchAction.DEFAULT) return true;
		else if (query.searchStr.isEmpty()) return false;
		
		try {
			// use Conc class to test validation
			new Conc(Concise.getCurrentWorkspace(), query.searchStr, CAPrefs.SHOW_PART_OF_SPEECH);
		
		} catch (ParseException e) {
			//Dialog.error(Concise.getActiveApp(), "Invalid Search Word.", e.getMessage());
			CAErrorMessageDialog.open(query.getGear(), e);
			return false;
			
		} catch (IOException e) {
			Concise.getCurrentWorkspace().logError(query.getGear(), e);
			Dialog.showException(e);
		}
		return true;
	}
}
