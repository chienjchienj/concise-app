package org.sustudio.concise.app.mainmenu;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.widgets.SystemMonitor;

public class CAWindowMenu extends CAMenuItem {

	private MenuItem miniItem;
	private MenuItem zoomItem;
	private MenuItem frontItem;
	
	public CAWindowMenu(Menu parent) {
		super(parent, CABundle.get("menu.window"));
	}

	@Override
	protected void createMenuItems() {
		
		miniItem = addItem(CABundle.get("menu.window.minimize"), new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							getShell().setMinimized(true);
						} }, SWT.MOD1 | 'M');
		zoomItem = addItem(CABundle.get("menu.window.zoom"), new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							getShell().setMaximized(!getShell().getMaximized());
						} });
				
		addSeparator();
		
		addItem(CABundle.get("menu.window.selectPrevGear"), new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							
							int index = Concise.getActiveApp().gearBoxView.getSelectionIndex() - 1;
							if (index < 0) {
								index = Concise.getActiveApp().gearBoxView.getItemCount() - 1;
							}
							Concise.getActiveApp().gearBoxView.setSelection(index);
							
						} }, SWT.MOD1 | '[');
		addItem(CABundle.get("menu.window.selectNextGear"), new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							
							int index = Concise.getActiveApp().gearBoxView.getSelectionIndex() + 1;
							if (index > Concise.getActiveApp().gearBoxView.getItemCount() - 1) {
								index = 0;
							}
							Concise.getActiveApp().gearBoxView.setSelection(index);
							
						} }, SWT.MOD1 | ']');
				
		addSeparator();
		
		addItem("System Monitor", new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							SystemMonitor.open();
						}
		});
		
		addSeparator();
		
		frontItem = addItem(CABundle.get("menu.window.bringAllToFront"), new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							Shell shell = getShell();
							for (Shell s : Display.getDefault().getShells())
								s.setActive();
							shell.setActive();
						} });
	}

	private Shell getShell() {
		Shell shell = Display.getDefault().getActiveShell();
		if (shell == null && Display.getDefault().getShells().length > 0)
			shell = Display.getDefault().getShells()[0];
		
		if (shell != null && 
			shell.getListeners(SWT.Deiconify) != null && 
			!ArrayUtils.contains(shell.getListeners(SWT.Deiconify), shellListener)) {
			
			shell.addShellListener(shellListener);
		}
		return shell;
	}
	
	private ShellAdapter shellListener = new ShellAdapter() {
		public void shellDeiconified(ShellEvent event) {
			miniItem.setEnabled(true);
			zoomItem.setEnabled(true);
			frontItem.setEnabled(true);
		}
		public void shellIconified(ShellEvent event) {
			miniItem.setEnabled(false);
			zoomItem.setEnabled(false);
			frontItem.setEnabled(false);
		}
	};
}
