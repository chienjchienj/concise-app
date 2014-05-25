package org.sustudio.concise.app.mainmenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.sustudio.concise.app.ConciseApp;

public class CAMainMenu {

	private CAMainMenu(final ConciseApp app) {
		Menu menu = Display.getDefault().getMenuBar();
		if (menu == null) {
			menu = new Menu(app, SWT.BAR);
			app.setMenu(menu);
		}
		
		for (MenuItem item : menu.getItems()) {
			item.dispose();
		}
		
		createMenuBar(menu);
	}
	
	private void createMenuBar(Menu menu) {
		if (menu != null) {
			new CAFileMenu(menu);
			new CAEditMenu(menu);
			new CAViewMenu(menu);
			new CACollocationMenu(menu);
			new CAKeywordMenu(menu);
			new CAWindowMenu(menu);
			new CAHelpMenu(menu);
		}
	}
	
	public static void createMainMenuFor(final ConciseApp app) {
		new CAMainMenu(app);
	}
		
}
