package org.sustudio.concise.app.mainmenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public abstract class CAMenuItem {

	protected final Menu menu;
	
	public CAMenuItem(Menu parent, String text) {
		MenuItem item = new MenuItem(parent, SWT.CASCADE);
		item.setText(text);
		
		menu = new Menu(item);
		item.setMenu(menu);
		
		createMenuItems();
	}
	
	public Menu getMenu() {
		return menu;
	}
	
	protected abstract void createMenuItems();
	
	
	protected MenuItem addRadioItem(String text, SelectionAdapter listener) {
		return addItem(menu, text, listener, SWT.NONE, SWT.RADIO);
	}
	
	protected MenuItem addRadioItem(String text, SelectionAdapter listener, int accelerator) {
		return addItem(menu, text, listener, accelerator, SWT.RADIO);
	}
	
	protected MenuItem addCheckItem(String text, SelectionAdapter listener) {
		return addItem(menu, text, listener, SWT.NONE, SWT.CHECK);
	}
	
	protected MenuItem addCheckItem(String text, SelectionAdapter listener, int accelerator) {
		return addItem(menu, text, listener, accelerator, SWT.CHECK);
	}
	
	protected MenuItem addItem(String text, SelectionAdapter listener) {
		return addItem(text, listener, SWT.NONE);
	}
	
	protected MenuItem addItem(String text, SelectionAdapter listener, int accelerator) {
		return addItem(menu, text, listener, accelerator);
	}
	
	protected MenuItem addItem(Menu menu, String text, SelectionAdapter listener, int accelerator) {
		return addItem(menu, text, listener, accelerator, SWT.NONE);
	}
	
	protected MenuItem addItem(Menu menu, String text, SelectionAdapter listener, int accelerator, int style) {
		MenuItem item = new MenuItem(menu, style);
		item.setText(text);
		item.setEnabled(listener != null);
		if (listener != null)
			item.addSelectionListener(listener);
		if (accelerator != SWT.NONE)
			item.setAccelerator(accelerator);
		return item;
	}
	
	protected MenuItem addSeparator(Menu menu) {
		return new MenuItem(menu, SWT.SEPARATOR);
	}
	
	protected MenuItem addSeparator() {
		return addSeparator(menu);
	}
	
	protected Menu addSubMenu(Menu menu, String text) {
		MenuItem item = new MenuItem(menu, SWT.CASCADE);
		item.setText(text);
		
		Menu subMenu = new Menu(item);
		item.setMenu(subMenu);
		
		return subMenu;
	}
}
