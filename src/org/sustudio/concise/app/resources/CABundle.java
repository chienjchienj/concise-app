package org.sustudio.concise.app.resources;

import java.util.ResourceBundle;

import org.sustudio.concise.app.preferences.CAPrefs;

public class CABundle {

	private static ResourceBundle _bundle;
	
	private static ResourceBundle getInstance() {
		if (_bundle == null || 
			!CAPrefs.LOCALE.equals(_bundle.getLocale())) 
		{
			_bundle = ResourceBundle.getBundle("org.sustudio.concise.app.resources.bundle", CAPrefs.LOCALE);
		}
		return _bundle;
	}
	
	public static String get(final String key) {
		return getInstance().getString(key);
	}
	
}
