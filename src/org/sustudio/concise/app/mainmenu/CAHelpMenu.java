package org.sustudio.concise.app.mainmenu;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Menu;
import org.sustudio.concise.app.resources.CABundle;

public class CAHelpMenu extends CAMenuItem {

	public CAHelpMenu(Menu parent) {
		super(parent, CABundle.get("menu.help"));
	}

	@Override
	protected void createMenuItems() {
		
		addItem(
			CABundle.get("menu.help.visitConciseWeb"), 
			new WebUrlSelectionListener("http://concise.sustudio.org"));
		
		addItem(
			CABundle.get("menu.help.visitSUStudioWeb"), 
			new WebUrlSelectionListener("http://www.sustudio.org"));
		
	}

	
	public class WebUrlSelectionListener extends SelectionAdapter {
		
		private String url;
		
		public WebUrlSelectionListener(String url) {
			this.url = url;
		}
		
		public void widgetSelected(SelectionEvent event) {
			Program.launch(url);
		}
		
	}
}
