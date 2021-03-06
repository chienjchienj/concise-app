package org.sustudio.concise.app;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.mainmenu.SystemMenu;
import org.sustudio.concise.app.preferences.CAPrefsUtils;
import org.sustudio.concise.app.utils.MacOSXUtils;

/**
 * Concise main 起始的 class 
 * 
 * @author Kuan-ming Su
 *
 */
public class Concise {

	private static Set<Workspace> workspaces = new HashSet<Workspace>();
	private static Workspace currentWorkspace;
	
	/**
	 * Launch Concise. <p>
	 * 1.) Open WorkspaceLauncher, decide what to load <br />
	 * 2.) Open Concise GUI <br />
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		Concise concise = new Concise();
		concise.open();
		
	}
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display.setAppName(CAConfig.APP_NAME);
		Display.setAppVersion(CAConfig.APP_VERSION);		
		final Display display = Display.getDefault();
		
		// create global short-cuts
		display.addFilter(SWT.KeyUp, new Listener() {
			
			public void handleEvent(Event event) {
				if (getActiveApp() != null &&
					((event.stateMask & SWT.MOD1) == SWT.MOD1) && 
					event.keyCode >= '1'  && event.keyCode <= '9') 
				{
					// 切換 gear: Command + 1 or 2 or 3 or ... to 9
					int index = event.keyCode - 49;
					if (index < CABox.GearBox.getBoxView(currentWorkspace).getItemCount()) {
						getActiveApp().gearBoxView.setSelection(index);			
					}
				}
			}
		});
		
		Launcher launcher = new Launcher();
		launcher.open();
		
		OpenDocumentEventProcessor openDocProcessor = new OpenDocumentEventProcessor(launcher);
		display.addListener(SWT.OpenDocument, openDocProcessor);
		
		// Only works on Mac OSX (about, preferences, ...)
		SystemMenu.getSystemMenu();	
		
		while (isAppRunning()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
			
			// Open Document Event Loop
			openDocProcessor.openFile();
		}
		
		display.dispose();
	}
	
	/**
	 * Check if any Concise App is still running.
	 * @return true if concise is still running.
	 */
	private boolean isAppRunning() {
		Display display = Display.getCurrent();
		if (display != null) {
			for (Shell shell : display.getShells()) {
				if (!shell.isDisposed()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void setActiveApp(Workspace workspace) {
		if (currentWorkspace != workspace) {
			try {
				CAPrefsUtils.readPrefs();
			} catch (Exception e) {
				workspace.logError(null, e);
				Dialog.showException(e);
			}
		}
		currentWorkspace = workspace;
		ConciseApp app = workspace.getApp();
		app.setActive();
	}
	
	public static ConciseApp getActiveApp() {
		if (currentWorkspace == null) return null;
		return currentWorkspace.getApp();
	}
	
	public static Workspace getCurrentWorkspace() {
		return currentWorkspace;
	}
	
	public static Workspace.Data getData() {
		return currentWorkspace.DATA;
	}

	/**
	 * 藉由工作路徑開啟 ConciseApp
	 * @param workspaceFile
	 */
	public static void openApp(File workspaceFile) {
		
		try {
			
			// 檢查是不是已經載入了
			for (Workspace w : workspaces) {
				if (w.getFile().equals(workspaceFile)) {
					// 已經載入了，只要讓它 active 就行
					setActiveApp(w);
					return;
				}
			}
			
			Workspace ws = null;
			try {	// test if the workspace is locked
				ws = new Workspace(workspaceFile);
			} catch (WorkspaceLockedException lock) {
				Dialog.inform("Workspace is Locked!", lock.getMessage());
				Launcher launcher = new Launcher();
				launcher.open();
				return;
			}
			MacOSXUtils.hideExtension(workspaceFile);
			RecentWorkspaces.setRecentWorkspaces(ws);
			workspaces.add(ws);
			currentWorkspace = ws;
			
			ConciseApp app = new ConciseApp(ws);
			ws.setApp(app);
			app.addDisposeListener(new DisposeListener() {

				@Override
				public void widgetDisposed(DisposeEvent event) {
					// remove from workspaces
					Workspace workspace = ((ConciseApp) event.widget).getWorkspace();
					workspaces.remove(workspace);
				}
				
			});
			app.open();
			setActiveApp(ws);
			
		} catch (Exception e) {
			e.printStackTrace();
			Dialog.showException(e);
		}
	}
	
}
