package org.sustudio.concise.app.mainmenu;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.enums.KeynessMeasurement;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.KeywordLister;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.resources.CABundle;

public class CAKeywordMenu extends CAMenuItem {

	public CAKeywordMenu(Menu parent) {
		super(parent, CABundle.get("menu.keyword"));
	}

	@Override
	protected void createMenuItems() {
		
		addItem(CABundle.get("menu.keyword.keynessStat"), null);
		MenuItem item = addRadioItem("  " + KeynessMeasurement.LogLikelihood.label(), 
				new KeynessMeasurementListener(KeynessMeasurement.LogLikelihood));
		item.setSelection(CAPrefs.KEYNESS == KeynessMeasurement.LogLikelihood);
		
		item = addRadioItem("  " + KeynessMeasurement.ChiSquared.label(), 
				new KeynessMeasurementListener(KeynessMeasurement.ChiSquared));
		item.setSelection(CAPrefs.KEYNESS == KeynessMeasurement.ChiSquared);
		
		addSeparator();
		
		item = addCheckItem(CABundle.get("menu.keyword.negativeKeywords"), new NegativeKeywordListener());
		item.setSelection(CAPrefs.SHOW_NEGATIVE_KEYWORDS);
	}
	
	
	private class KeynessMeasurementListener extends SelectionAdapter {
		private KeynessMeasurement measure;
		public KeynessMeasurementListener(KeynessMeasurement measure) {
			this.measure = measure;
		}
		public void widgetSelected(SelectionEvent event) {
			CAPrefs.KEYNESS = measure;
			KeywordLister k = (KeywordLister) Gear.KeywordLister.getController(Concise.getCurrentWorkspace());
			k.updateMeasurementColumn();
		}
	}
	
	private class NegativeKeywordListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			MenuItem item = (MenuItem) event.widget;
			CAPrefs.SHOW_NEGATIVE_KEYWORDS = !CAPrefs.SHOW_NEGATIVE_KEYWORDS;
			item.setSelection(CAPrefs.SHOW_NEGATIVE_KEYWORDS);
			KeywordLister k = (KeywordLister) Gear.KeywordLister.getController(Concise.getCurrentWorkspace());
			k.sort();
		}
	}
}
