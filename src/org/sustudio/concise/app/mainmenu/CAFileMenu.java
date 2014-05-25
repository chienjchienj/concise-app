package org.sustudio.concise.app.mainmenu;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.CAConfig;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.ConciseApp;
import org.sustudio.concise.app.RecentWorkspaces;
import org.sustudio.concise.app.dialog.CAOpenFilesDialog;
import org.sustudio.concise.app.dialog.CASaveFileDialog;
import org.sustudio.concise.app.enums.CorpusManipulation;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.IGearFileRevealable;
import org.sustudio.concise.app.helper.SaveOutputHelper;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.thread.CAReTokenizeThread;
import org.sustudio.concise.app.thread.CAThread;
import org.sustudio.concise.app.utils.Platform;
import org.sustudio.concise.app.widgets.CABoxView;

public class CAFileMenu extends CAMenuItem {

	public CAFileMenu(Menu parent) {
		super(parent, CABundle.get("menu.file"));
	}
	
	@Override
	protected void createMenuItems() {
		
		addItem(CABundle.get("menu.file.newWorkspace"), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CASaveFileDialog dlg = new CASaveFileDialog();
				dlg.setWorkspaceConfigure();
				dlg.setFileName(CAConfig.DEFAULT_WORKSPACE_NAME);
				String filename = dlg.open();
				if (filename != null) {
					File filepath = new File(filename);
					if (!filepath.exists()) {
						filepath.mkdir();
					}
					Concise.openApp(filepath);
				}
			}
		}, SWT.MOD1 | 'N');
		
		addItem(CABundle.get("menu.file.openWorkspace"), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CAOpenFilesDialog dlg = new CAOpenFilesDialog(true);
				dlg.setWorkspaceConfigure();
				String workpath = dlg.open();
				if (workpath != null && 
					new File(workpath).exists()) 
				{
					Concise.openApp(new File(workpath));
				}
			}
		}, SWT.MOD1 | 'O');
		
		final Menu workspaceMenu = addSubMenu(menu, CABundle.get("menu.file.openRecents"));
		workspaceMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent event) {
				for (MenuItem item : workspaceMenu.getItems()) {
					item.dispose();
				}
				
				for (String workspace : RecentWorkspaces.getRecentWorkspacePaths()) {
					addWorkspaceItem(workspaceMenu, workspace);
				}
				if (RecentWorkspaces.getRecentWorkspacePaths().length > 0) {
					addSeparator(workspaceMenu);
					addItem(workspaceMenu, 
							CABundle.get("menu.file.clear"), 
							new SelectionAdapter() {
								public void widgetSelected(SelectionEvent event) {
									RecentWorkspaces.clearRecentWorkspacePaths();
								} },
							SWT.NONE);
				}
			}
			
		});
				
		addSeparator();
		
		final Menu importMenu = addSubMenu(menu, "Import...");
		addItem(importMenu, 
				CorpusManipulation.ImportDocuments.label(), 
				CorpusManipulation.ImportDocuments.selectionAdapter(),
				SWT.NONE);
		addSeparator(importMenu);
		addItem(importMenu,
				CorpusManipulation.ImportReferenceDocuments.label(),
				CorpusManipulation.ImportReferenceDocuments.selectionAdapter(),
				SWT.NONE);
		addItem(importMenu,
				CorpusManipulation.ImportReferenceFromWorkspace.label(),
				CorpusManipulation.ImportReferenceFromWorkspace.selectionAdapter(), 
				SWT.NONE);
		
		
		final Menu clearMenu = addSubMenu(menu, "Clear...");
		addItem(clearMenu,
				CorpusManipulation.ClearDocuments.label(),
				CorpusManipulation.ClearDocuments.selectionAdapter(),
				SWT.NONE);
		addSeparator(clearMenu);
		addItem(clearMenu,
				CorpusManipulation.ClearReferenceDocuments.label(),
				CorpusManipulation.ClearReferenceDocuments.selectionAdapter(),
				SWT.NONE);
				
		addSeparator();
		
		addItem(CABundle.get("menu.file.reTokenizeDocs"), new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) 
			{
				if (Dialog.isConfirmed(
						Concise.getActiveApp(), 
						"Do you want to re-tokenize documents now?", 
						"All data will be reset.")) 
				{	
					CAThread thread = new CAReTokenizeThread();
					thread.start();
				}
			}
			
		});
		
		addSeparator();
		
		addItem("Close Workspace", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Shell activeShell = Display.getCurrent().getActiveShell();
				activeShell.close();
			}
		}, SWT.MOD1 | SWT.MOD2 | 'W');
		
		final MenuItem iCloseTab = addItem(CABundle.get("menu.file.closeTab"), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (Display.getCurrent().getActiveShell() != Concise.getActiveApp()) {
					Display.getCurrent().getActiveShell().close();
					return;
				}
				ConciseApp app = Concise.getActiveApp();
				if (app != null) {
					GearController gearView = GearController.getActiveGearView();
					if (gearView != null) {
						gearView.close();
					}
					else {
						app.close();
					}
				}
			}
		}, SWT.MOD1 | 'W');
		
		final MenuItem iCloseAllTabs = addItem(CABundle.get("menu.file.closeAllTabs"), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				GearController gearView = GearController.getActiveGearView();
				if (gearView == null) {
					return;
				}
				CABoxView boxView = gearView.getBox().getBoxView(Concise.getCurrentWorkspace());
				for (CTabItem item : boxView.getItems()) {
					item.dispose();
				}
			}
		});
		
		addSeparator();
		
		// reveal in finder
		final MenuItem iReveal = addItem(CABundle.get("menu.file.revealInFinder"), new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				GearController gearView = GearController.getActiveGearView();
				if (gearView != null && gearView instanceof IGearFileRevealable) {
					((IGearFileRevealable) gearView).revealFileInFinder();
				}
			}
		}, SWT.MOD1 | SWT.SHIFT | 'R');
		
		
		addSeparator();
		
		
		SaveOutputHelper.setMenuItem(
				addItem(CABundle.get("menu.file.saveOutputAs"), 
						SaveOutputHelper.getSaveOutputSelectionAdapter(),
						SWT.MOD1 | SWT.MOD2 | 'S'));
		
		if (!Platform.isMac()) {
			addSeparator();
			addItem("Exit", null);
		}
		
		
		getMenu().addMenuListener(new MenuListener() {

			@Override
			public void menuHidden(MenuEvent event) {
				// always enable item (judges are made by gearview controller)
				iReveal.setEnabled(true);
				iCloseTab.setEnabled(true);
				iCloseAllTabs.setEnabled(true);
			}

			@Override
			public void menuShown(MenuEvent event) {
				boolean revealEnabled = false;
				final GearController gearView = GearController.getActiveGearView();
				if (gearView != null && gearView instanceof IGearFileRevealable) {
					revealEnabled = ((IGearFileRevealable) gearView).isRevealEnabled();
				}
				iReveal.setEnabled(revealEnabled);
				
				iCloseTab.setEnabled(gearView != null);
			}
			
		});
	}
	
	private void addWorkspaceItem(Menu menu, final String workspace) {
		SelectionAdapter listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String ws = workspace;
				if (!FilenameUtils.isExtension(ws, CAConfig.WORKSPACE_EXTENSION)) {
					ws = ws + "." + CAConfig.WORKSPACE_EXTENSION;
				}
				if (new File(ws).exists())
					Concise.openApp(new File(ws));
			}
		};
		addItem(menu, workspace, listener, SWT.NONE);
	}
}
