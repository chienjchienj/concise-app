package org.sustudio.concise.app.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.Gear;

public class CAToolBarGearToolItem extends ToolItem {

	public CAToolBarGearToolItem(ToolBar parent) {
		super(parent, SWT.DROP_DOWN);
		setImage(SWTResourceManager.getImage(CAToolBar.class, "/org/sustudio/concise/app/icon/20-gear2-20x20.png"));
		setText("Gears");
		setToolTipText("Gears");
		addSelectionListener(new DropdownGearSelectionListener());
		
	}

	
	@Override
	protected void checkSubclass() {
		// force subclass toolitem
	}
	
	private class DropdownGearSelectionListener extends SelectionAdapter {

		private Menu menu;
		
		public void addItem(final Gear gear) {
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(gear.label());
			menuItem.setImage(gear.image());
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					gear.open(Concise.getCurrentWorkspace());
				}
			});
		}
		
		public void addSeparator() {
			new MenuItem(menu, SWT.SEPARATOR);
		}
		
		public void widgetSelected(SelectionEvent event) {
			ToolItem item = (ToolItem) event.widget;
			Shell shell = item.getParent().getShell();
			menu = new Menu(shell);
			addItem(Gear.Concordancer);
			addItem(Gear.Collocator);
			addItem(Gear.WordLister);
			addItem(Gear.WordCluster);
			addItem(Gear.KeywordLister);
			addSeparator();
			addItem(Gear.CorpusManager);
			addItem(Gear.ReferenceCorpusManager);
			addSeparator();
			addItem(Gear.ConcordancePlotter);
			addItem(Gear.WordTrender);
			addItem(Gear.WordClouder);
			addItem(Gear.CollocationalNetworker);
			addItem(Gear.ScatterPlotter);
			addSeparator();
			addItem(Gear.DocumentViewer);
			addItem(Gear.LemmaEditor);
			//addItem(Gear.SearchWorder);
			addItem(Gear.StopWorder);
			addItem(Gear.DictionaryEditor);
			
			Rectangle rect = item.getBounds();
			Point pt = item.getParent().toDisplay(rect.x, rect.y);
			menu.setLocation(pt.x, pt.y + rect.height);
			menu.setVisible(true);
		}
		
	}
}
