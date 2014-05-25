package org.sustudio.concise.app.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.enums.SearchAction;
import org.sustudio.concise.app.gear.Gear;

public class CAToolBarGoToolItem extends ToolItem {

	public enum MODE { GO, SEARCH; }
	
	
	public SearchAction action;
	protected SearchAction prevAction;
	
	private final Text txtSearch;
	private DropdownSearchMenuSelectionListener goListener;
	private MODE currentMode;
	
	public CAToolBarGoToolItem(ToolBar parent, Text txtSearch) {
		super(parent, SWT.DROP_DOWN);
		this.txtSearch = txtSearch;
		
		setImage(SWTResourceManager.getImage(CAToolBar.class, "/org/sustudio/concise/app/icon/06-magnify-20x20.png"));
		setText("Go");
	}

	@Override
	protected void checkSubclass() {
		// force subclass toolitem
	}
	
	public void setMode(MODE mode) {
		if (goListener != null) {
			removeSelectionListener(goListener);
		}
		goListener = new DropdownSearchMenuSelectionListener(this);
		
		switch (mode) {
		case GO:
			goListener.addSearchAction(SearchAction.DEFAULT);
			break;
			
		case SEARCH:
			goListener.addSearchAction(SearchAction.WORD);
			goListener.addSearchAction(SearchAction.LIST);
			break;
		}
		currentMode = mode;
	}
	
	
	public void setAction(SearchAction action) {
		// check mode compatibility
		switch (currentMode) {
		case GO:
			if (action != SearchAction.DEFAULT)
				throw new UnsupportedOperationException("GO MODE has no such action.");
			break;
		case SEARCH:
			if (action == SearchAction.DEFAULT)
				throw new UnsupportedOperationException("SEARCH MODE has no such action.");
			break;
		}
		this.action = action;
		if (action == SearchAction.LIST) {
			txtSearch.setEnabled(false);
		}
		
		// set selection
		for (MenuItem item : goListener.menu.getItems()) {
			if (item.getText().equals(action.label())) {
				goListener.dropdown.setText(action.label());
				item.setSelection(true);
			}
			item.setSelection(false);
		}
	}
	
	
	private class DropdownSearchMenuSelectionListener extends CAToolBarSearchActionListener {
		private ToolItem dropdown;
		private Menu menu;
		
		public DropdownSearchMenuSelectionListener(ToolItem dropdown) {
			this.dropdown = dropdown;
			menu = new Menu(dropdown.getParent().getShell());
			
			dropdown.addSelectionListener(this);
		}
				
		public void addSearchAction(final SearchAction searchAction) {
			MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
			menuItem.setText(searchAction.label());
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					dropdown.setText(searchAction.label());
					action = searchAction;
					
					if (action == SearchAction.LIST) {
						txtSearch.setEnabled(false);
						Gear.SearchWorder.open(Concise.getCurrentWorkspace());
					}
					else {
						txtSearch.setEnabled(true);
					}
				}
			});
			if (menu.getItemCount() == 1 || searchAction == prevAction) {
				// first item
				dropdown.setText(searchAction.label());
				action = searchAction;
				menuItem.setSelection(true);
			}
		}
				
		public void widgetSelected(SelectionEvent event) {
			ToolItem item = (ToolItem) event.widget;
			if (!item.isEnabled()) return;
			if (event.detail == SWT.ARROW) {
				Rectangle rect = item.getBounds();
				Point pt = item.getParent().toDisplay(rect.x, rect.y);
				menu.setLocation(pt.x, pt.y + rect.height);
				menu.setVisible(true);
			}
			else {
				super.widgetDefaultSelected(event);
			}
		}
	}
	
}
