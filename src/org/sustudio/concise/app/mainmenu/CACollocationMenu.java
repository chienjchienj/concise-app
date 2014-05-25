package org.sustudio.concise.app.mainmenu;

import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CollocationMode;
import org.sustudio.concise.app.enums.EffectSizeMeasurement;
import org.sustudio.concise.app.enums.SignificanceMeasurement;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.Collocator;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.preferences.CAPrefsUtils;
import org.sustudio.concise.app.resources.CABundle;

public class CACollocationMenu extends CAMenuItem {

	public CACollocationMenu(Menu parent) {
		super(parent, CABundle.get("menu.collocation"));
	}

	@Override
	protected void createMenuItems() {
		
		addRadioItem(CABundle.get("menu.collocation.effectSizeMeasures"), null);
		addEffectSizeMeasurement(EffectSizeMeasurement.MI);
		addEffectSizeMeasurement(EffectSizeMeasurement.MI3);
				
		addEffectSizeMeasurement(EffectSizeMeasurement.Dice);
		addEffectSizeMeasurement(EffectSizeMeasurement.OddsRatio);
		
		
		addSeparator();
		
		
		addRadioItem(CABundle.get("menu.collocation.significanceMeasures"), null);
		addSignificanceMeasurement(SignificanceMeasurement.Tscore);
		addSignificanceMeasurement(SignificanceMeasurement.Zscore);
		addSignificanceMeasurement(SignificanceMeasurement.SimpleLL);
				
		addSignificanceMeasurement(SignificanceMeasurement.ChiSquaredCorr);
		addSignificanceMeasurement(SignificanceMeasurement.LogLikelihood);
		
		
		addSeparator();
		
		final MenuItem surfItem = addRadioItem(CollocationMode.Surface.label(), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				MenuItem item = (MenuItem) event.widget;
				if (item.getSelection()) {
					CAPrefs.COLLOCATION_MODE = CollocationMode.Surface;
					try {
						CAPrefsUtils.writePrefs();
					} catch (Exception e) {
						Concise.getCurrentWorkspace().logError(null, e);
						Dialog.showException(e);
					}
					
					Gear gear = Gear.getActiveGear();
					Concise.getActiveApp().toolBar.setToolBarLayout(gear);
				}
			}
		});
		final MenuItem sentItem = addRadioItem(CollocationMode.SentenceTextual.label(), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				MenuItem item = (MenuItem) event.widget;
				if (item.getSelection()) {
					CAPrefs.COLLOCATION_MODE = CollocationMode.SentenceTextual;
					try {
						CAPrefsUtils.writePrefs();
					} catch (Exception e) {
						Concise.getCurrentWorkspace().logError(null, e);
						Dialog.showException(e);
					}
					
					Gear gear = Gear.getActiveGear();
					Concise.getActiveApp().toolBar.setToolBarLayout(gear);
				}
			}
		});
		final MenuItem paraItem = addRadioItem(CollocationMode.ParagraphTextual.label(), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				MenuItem item = (MenuItem) event.widget;
				if (item.getSelection()) {
					CAPrefs.COLLOCATION_MODE = CollocationMode.ParagraphTextual;
					try {
						CAPrefsUtils.writePrefs();
					} catch (Exception e) {
						Concise.getCurrentWorkspace().logError(null, e);
						Dialog.showException(e);
					}
					
					Gear gear = Gear.getActiveGear();
					Concise.getActiveApp().toolBar.setToolBarLayout(gear);
				}
			}
		});
		
		getMenu().addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent event) {
				surfItem.setSelection(false);
				sentItem.setSelection(false);
				paraItem.setSelection(false);
				
				switch (CAPrefs.COLLOCATION_MODE) {
				default:
				case Surface:	
					surfItem.setSelection(true);	break;
				case SentenceTextual:	
					sentItem.setSelection(true);	break;
				case ParagraphTextual:
					paraItem.setSelection(true);	break;
				}
			}			
		});
	}

	
	protected MenuItem addEffectSizeMeasurement(EffectSizeMeasurement measure) {
		MenuItem item = addRadioItem("  " + measure.label(), 
							new CollocateMeasurementListener(measure));
		item.setSelection(CAPrefs.EFFECT_SIZE == measure);
		return item;
	}
	
	protected MenuItem addSignificanceMeasurement(SignificanceMeasurement measure) {
		MenuItem item = addRadioItem("  " + measure.label(), 
							new CollocateMeasurementListener(measure));
		item.setSelection(CAPrefs.SIGNIFICANCE == measure);
		return item;
	}
		
	private class CollocateMeasurementListener extends SelectionAdapter {
		private Object measure;
		public CollocateMeasurementListener(Object measure) {
			this.measure = measure;
		}
		public void widgetSelected(SelectionEvent event) {
			Collocator c = (Collocator) Gear.Collocator.getController(Concise.getCurrentWorkspace());
			if (measure instanceof SignificanceMeasurement) {
				CAPrefs.SIGNIFICANCE = (SignificanceMeasurement) measure;
				c.updateMeasurementColumn((SignificanceMeasurement) measure);
			}
			else if (measure instanceof EffectSizeMeasurement) {
				CAPrefs.EFFECT_SIZE = (EffectSizeMeasurement) measure;
				c.updateMeasurementColumn((EffectSizeMeasurement) measure);
			}
		}
	}
}
