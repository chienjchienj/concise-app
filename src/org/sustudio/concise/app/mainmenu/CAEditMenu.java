package org.sustudio.concise.app.mainmenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.sustudio.concise.app.dialog.CAPreferencesDialog;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.IGearFilterable;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.utils.Platform;

public class CAEditMenu extends CAMenuItem {

	public CAEditMenu(Menu parent) {
		super(parent, CABundle.get("menu.edit"));
	}
	
	@Override
	protected void createMenuItems() {
		
		CopyPasteHelper.setCutMenuItem(
				addItem(CABundle.get("menu.edit.cut"), CopyPasteHelper.getCutSelectionAdapter(), SWT.MOD1 | 'X'));
		
		CopyPasteHelper.setCopyMenuItem(
				addItem(CABundle.get("menu.edit.copy"), CopyPasteHelper.getCopySelectionAdapter(), SWT.MOD1 | 'C'));
		
		CopyPasteHelper.setPasteMenuItem(
				addItem(CABundle.get("menu.edit.paste"), CopyPasteHelper.getPasteSelectionAdapter(), SWT.MOD1 | 'V'));
		
		CopyPasteHelper.setSelectAllMenuItem(
				addItem(CABundle.get("menu.edit.selectAll"), CopyPasteHelper.getSelectAllSelectionAdapter(), SWT.MOD1 | 'A'));

		addSeparator();
		
		final MenuItem iFind = addItem(CABundle.get("menu.edit.find"), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				GearController gearView = GearController.getActiveGearView();
				if (gearView instanceof IGearFilterable) {
					((IGearFilterable) gearView).showFinder();
				}
			}
		}, SWT.MOD1 | 'F');
		
		if (!Platform.isMac()) {
			addSeparator();
			addItem("Preferences", new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					new CAPreferencesDialog().open();
				} }, SWT.MOD1 | ',');
		}
		
		getMenu().addMenuListener(new MenuListener() {

			@Override
			public void menuHidden(MenuEvent event) {
				// always enable item (judges are made by gearview controller)
				iFind.setEnabled(true);
			}

			@Override
			public void menuShown(MenuEvent event) {
				final GearController gearView = GearController.getActiveGearView();
				boolean findEnabled = gearView != null && gearView instanceof IGearFilterable;
				iFind.setEnabled(findEnabled);
			}
			
		});
	}
		
}
