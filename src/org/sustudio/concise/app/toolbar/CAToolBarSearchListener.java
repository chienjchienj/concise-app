package org.sustudio.concise.app.toolbar;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolItem;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.concordance.Conc;

public class CAToolBarSearchListener extends SelectionAdapter {
	
	public void widgetSelected(SelectionEvent event) {
		if (event.widget instanceof ToolItem) {
			widgetDefaultSelected(event);
		}
	}
	
	public void widgetDefaultSelected(SelectionEvent event) {
		// validating query first
		CAQuery query = Concise.getActiveApp().toolBar.getQuery();
		if (validQuery(query)) {
			GearController controller = query.getGear().getController(Concise.getCurrentWorkspace());
			controller.doit(query);
		}
	}
	
	private boolean validQuery(CAQuery query) {
		// 定義不需要檢查的條件
		switch (query.getGear()) {
		case DocumentViewer:	// 應該不會送出才是
		case LemmaEditor:		// 應該不會送出才是
		case StopWorder:		// 應該不會送出才是
			throw new UnsupportedOperationException(query.getGear().label() + " 不應該檢查 CAQuery");
		
		case CorpusManager:
		case KeywordLister:
		case ReferenceCorpusManager:
		case WordClouder:
		case WordLister:
			return true;
		
		case WordCluster:
			if (query.ngram)
				return true;
		default:
			break;
		}
		if (query.searchStr.isEmpty()) return false;
		
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
