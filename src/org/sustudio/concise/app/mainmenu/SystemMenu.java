package org.sustudio.concise.app.mainmenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.dialog.AboutDialog;
import org.sustudio.concise.app.dialog.CAPreferencesDialog;

/**
 * 系統選單，僅Mac適用。 處理 About 和 Preferences。
 * 
 * @author Kuan-ming Su.
 *
 */
public class SystemMenu {

	private static AboutDialog about;
	private static CAPreferencesDialog preferences;
	
	/**
	 * System menu (Mac only) constructor.
	 */
	public static void getSystemMenu() {
		final Menu systemMenu = Display.getDefault().getSystemMenu();
		if (systemMenu != null) {
			
			final MenuItem iAbout = getSystemItem(systemMenu, SWT.ID_ABOUT);
			iAbout.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (about == null || about.isDisposed()) {
						about = new AboutDialog();
						about.open();
					}
					else {
						about.setActive();
					}
				}
			});
			
			final MenuItem iPreferences = getSystemItem(systemMenu, SWT.ID_PREFERENCES);
			iPreferences.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (Concise.getActiveApp() == null) {
						return;
					}
					
					if (preferences == null || preferences.isDisposed()) {
						preferences = new CAPreferencesDialog();
						preferences.open();
					}
					else {
						preferences.setActive();
					}
				}
			});
			
			systemMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuHidden(MenuEvent arg0) {
					iPreferences.setEnabled(true);
				}

				@Override
				public void menuShown(MenuEvent arg0) {
					iPreferences.setEnabled(Concise.getActiveApp() != null);
				}
				
			});
		}
	}
	
	private static MenuItem getSystemItem(Menu menu, int id) {
		for (MenuItem item : menu.getItems()) {
			if (item.getID() == id) return item;
		}
		return null;
	}
	
}
