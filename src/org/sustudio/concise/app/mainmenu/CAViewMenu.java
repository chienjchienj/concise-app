package org.sustudio.concise.app.mainmenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.helper.ZoomHelper;
import org.sustudio.concise.app.resources.CABundle;

public class CAViewMenu extends CAMenuItem {

	public CAViewMenu(Menu parent) {
		super(parent, CABundle.get("menu.view"));
	}
	
	@Override
	protected void createMenuItems() {
		
		addGear(Gear.CorpusManager, SWT.MOD1 | '/');
		addGear(Gear.ReferenceCorpusManager);
		
		addSeparator();
		
		addGear(Gear.Concordancer);
		addGear(Gear.Collocator);
		addGear(Gear.WordCluster);
		addGear(Gear.WordLister);
		addGear(Gear.KeywordLister);
		
		addSeparator();
		
		addGear(Gear.ConcordancePlotter);
		addGear(Gear.WordTrender);
		addGear(Gear.WordClouder);
		addGear(Gear.CollocationalNetworker);
		addGear(Gear.ScatterPlotter);
				
		addSeparator();
				
		Menu toolMenu = addSubMenu(menu, CABundle.get("menu.view.tools"));
		addGear(toolMenu, Gear.DocumentViewer);
		addGear(toolMenu, Gear.LemmaEditor);
		// TODO remove
		//addGear(toolMenu, Gear.SearchWorder);
		addGear(toolMenu, Gear.StopWorder);
		
		addSeparator();
		
		addItem(CABundle.get("menu.zoomFit"), ZoomHelper.zoomDefaultListener, SWT.MOD1 | '0');
		addItem(CABundle.get("menu.zoomIn"), ZoomHelper.zoomInListener, SWT.MOD1 | '+');
		addItem(CABundle.get("menu.zoomOut"), ZoomHelper.zoomOutListener, SWT.MOD1 | '-');
		
		addSeparator();
		
		addItem(CABundle.get("menu.view.enterFullScreen"), new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						Shell shell = Display.getDefault().getActiveShell();
						if (shell == null) {
							shell = Display.getDefault().getShells()[0];
						}
						shell.setFullScreen(!shell.getFullScreen());
					} }, SWT.MOD1 | SWT.MOD4 | 'F');
	}

	private void addGear(Gear gear) {
		addGear(menu, gear, -1);
	}
	
	private void addGear(Gear gear, int accelerator) {
		addGear(menu, gear, accelerator);
	}
	
	private void addGear(Menu menu, final Gear gear) {
		addGear(menu, gear, -1);
	}
	
	private void addGear(Menu menu, final Gear gear, int accelerator) {
		SelectionAdapter listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				gear.open(Concise.getCurrentWorkspace());
			}
		};
		MenuItem item = addItem(menu, gear.label(), listener, accelerator);
		item.setImage(gear.image());
	}
	
}
