package org.sustudio.concise.app.helper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.IGearCollocatable;
import org.sustudio.concise.app.gear.IGearConcordable;
import org.sustudio.concise.app.gear.IGearFileRevealable;
import org.sustudio.concise.app.resources.CABundle;

public class PopupMenuHelper {
	
	public static void addPopupMenuFor(final GearController gearView) {
		final Control control = gearView.getControl();
		
		Menu menu = new Menu(control);
		control.setMenu(menu);
		
		addConcordItemFor(gearView);
		addCollocItemFor(gearView);
		addRevealInFinderItemFor(gearView);
		
		// add copy and select all
		
		final MenuItem copyItem = new MenuItem(menu, SWT.NONE);
		copyItem.addSelectionListener(CopyPasteHelper.getCopySelectionAdapter());
				
		final MenuItem selectAllItem = new MenuItem(menu, SWT.NONE);
		selectAllItem.addSelectionListener(CopyPasteHelper.getSelectAllSelectionAdapter());
		
		// add zoom 
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		MenuItem zoomFitItem = new MenuItem(menu, SWT.NONE);
		zoomFitItem.addSelectionListener(ZoomHelper.zoomDefaultListener);
		zoomFitItem.setText(CABundle.get("menu.zoomFit"));
		
		MenuItem zoomInItem = new MenuItem(menu, SWT.NONE);
		zoomInItem.addSelectionListener(ZoomHelper.zoomInListener);
		zoomInItem.setText(CABundle.get("menu.zoomIn"));
		
		MenuItem zoomOutItem = new MenuItem(menu, SWT.NONE);
		zoomOutItem.addSelectionListener(ZoomHelper.zoomOutListener);
		zoomOutItem.setText(CABundle.get("menu.zoomOut"));
		
		// add save out as...
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		final MenuItem saveOutputItem = new MenuItem(menu, SWT.NONE);
		saveOutputItem.addSelectionListener(SaveOutputHelper.getSaveOutputSelectionAdapter());
		SaveOutputHelper.setPopupMenuItem(saveOutputItem);
		
		// update menu item state when showing
		menu.addListener(SWT.Show, new Listener() {

			@Override
			public void handleEvent(Event event) {
				CopyPasteHelper.setPopupCopyMenuItem(copyItem);
				CopyPasteHelper.setPopupSelectAllMenuItem(selectAllItem);
				SaveOutputHelper.setPopupMenuItem(saveOutputItem);
			}
			
		});
	}  // end addPopupMenuFor
	
	
	private static void addRevealInFinderItemFor(final GearController gearView) {
		
		if (!(gearView instanceof IGearFileRevealable)) {
			return;
		}
		
		final Control control = gearView.getControl();
		Menu menu = control.getMenu();
		if (menu == null) {
			menu = new Menu(control);
			control.setMenu(menu);
		}
		
		if (menu.getItemCount() > 0 && 
			((menu.getItem(menu.getItemCount() - 1).getStyle() & SWT.SEPARATOR) == 0)) {
			new MenuItem(menu, SWT.SEPARATOR);
		}
		
		final MenuItem iOpen = new MenuItem(menu, SWT.NONE);
		iOpen.setText(CABundle.get("menu.open"));
		iOpen.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event) {
				if (gearView instanceof IGearFileRevealable) {
					((IGearFileRevealable) gearView).openFileInDocumentViewer();
				}
			}
		});
		
		final MenuItem iShowInFinder = new MenuItem(menu, SWT.NONE);
		iShowInFinder.setText(CABundle.get("menu.showInFinder"));
		iShowInFinder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (gearView instanceof IGearFileRevealable) {
					((IGearFileRevealable) gearView).revealFileInFinder();
				}
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				iShowInFinder.setEnabled( ((IGearFileRevealable)gearView).isRevealEnabled() );
				iOpen.setEnabled( ((IGearFileRevealable)gearView).isRevealEnabled() );
			}
		});
	}
	
	
	private static void addConcordItemFor(final GearController gearView) {
		if (!(gearView instanceof IGearConcordable)) {
			return;
		}
		
		final Control control = gearView.getControl();
		Menu menu = control.getMenu();
		if (menu == null) {
			menu = new Menu(control);
			control.setMenu(menu);
		}
		
		if (menu.getItemCount() > 0 && 
			((menu.getItem(menu.getItemCount() - 1).getStyle() & SWT.SEPARATOR) == 0)) {
			new MenuItem(menu, SWT.SEPARATOR);
		}
		
		final MenuItem iConc = new MenuItem(menu, SWT.NONE);
		iConc.setText(CABundle.get("menu.showConcord"));
		iConc.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				((IGearConcordable) gearView).showConcord();
			}
		});
		
		if (menu.getItemCount() == 1) {
			new MenuItem(menu, SWT.SEPARATOR);
		}
		
		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				iConc.setEnabled( ((IGearConcordable)gearView).isConcordEnabled() );
			}
		});
	}
	
	private static void addCollocItemFor(final GearController gearView) {
		if (!(gearView instanceof IGearCollocatable)) {
			return;
		}
		
		final Control control = gearView.getControl();
		Menu menu = control.getMenu();
		if (menu == null) {
			menu = new Menu(control);
			control.setMenu(menu);
		}
		
		if (menu.getItemCount() > 0 && 
			((menu.getItem(menu.getItemCount() - 1).getStyle() & SWT.SEPARATOR) != 0)) {
			// remove separator
			menu.getItem(menu.getItemCount() - 1).dispose();
		}
		
		final MenuItem iColl = new MenuItem(menu, SWT.NONE);
		iColl.setText(CABundle.get("menu.showCollocates"));
		iColl.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				((IGearCollocatable) gearView).showCollocate();
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				iColl.setEnabled( ((IGearCollocatable)gearView).isCollocateEnabled() );
			}
		});
	}
}
