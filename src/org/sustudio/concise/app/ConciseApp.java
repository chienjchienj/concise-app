package org.sustudio.concise.app;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.mainmenu.CAMainMenu;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.preferences.CAPrefsUtils;
import org.sustudio.concise.app.toolbar.CAToolBar;
import org.sustudio.concise.app.widgets.GearBoxView;
import org.sustudio.concise.app.widgets.ToolBoxView;
import org.sustudio.concise.core.Workspace.INDEX;
import org.sustudio.concise.core.autocompleter.AutoCompleter;

/**
 * ConciseApp. 作為 UI 和 controller
 * 
 * @author Kuan-ming
 *
 */
public class ConciseApp extends Shell {

	private final Workspace workspace;
	
	public CAToolBar toolBar;
	public GearBoxView gearBoxView;
	public ToolBoxView toolBoxView;

	/**
	 * Create the app shell.
	 * @param display
	 */
	public ConciseApp(final Workspace workspace) {
		super(Display.getCurrent());
		this.workspace = workspace;
		
		// read preferences
		try {
			CAPrefsUtils.readPrefs();
		} catch (Exception e) {
			workspace.logError(null, e);
			Dialog.showException(e);
		}
		
		// create MainMenu 
		CAMainMenu.createMainMenuFor(this);
		
		// create tool bar, gear box, and tool box
		createContainer();
		
		// app 變成 active 時要處理的...
		addShellListener(new ShellAdapter() {

			@Override
			public void shellActivated(ShellEvent event) {
				
				Concise.setActiveApp(workspace);
				
			}

		});
		
		
		// 離開時要做的幾件事情
		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent event) {
				try 
				{
					// 記下離開時開啟的 gears
					ArrayList<Gear> openedGears = new ArrayList<Gear>();
					for (Control control : gearBoxView.getChildren()) {
						if (control instanceof GearController) {
							openedGears.add(((GearController) control).getGear());
						}
					}
					for (Control control : toolBoxView.getChildren()) {
						if (control instanceof GearController) {
							openedGears.add(((GearController) control).getGear());
						}
					}
					CAPrefs.OPENED_GEARS = openedGears.toArray(new Gear[0]);
					CAPrefs.LAST_ACTIVE_GEAR = CABox.GearBox.getSelectedGear(workspace);
					
					CAPrefsUtils.writePrefs();
					ConciseApp.this.workspace.close();
					
				} catch (Exception e) {
					workspace.logError(null, e);
					Dialog.showException(e);
				}
			}
			
		});
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContainer() {
		setSize(850, 550);
		setMinimumSize(650, 200);
		setText(Display.getAppName() + " - " + 
				FilenameUtils.removeExtension(FilenameUtils.getName(workspace.getFile().getName())));
		
		GridLayout gl = new GridLayout(1, false);
		gl.marginTop = 0;
		gl.marginHeight = 0;
		gl.marginBottom = 5;
		setLayout(gl);
		
		// create toolbar
		toolBar = CAToolBar.getToolBarFor(this);
		
		final SashForm boxSash = new SashForm(this, SWT.SMOOTH);
		boxSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		gearBoxView = new GearBoxView(boxSash, SWT.BORDER | SWT.CLOSE);
		toolBoxView = new ToolBoxView(boxSash, SWT.BORDER | SWT.CLOSE);
		
		// initialize gears (gears to open)
		initGears();
		
		boxSash.setWeights(new int[] { (boxSash.getShell().getClientArea().width - boxSash.getSashWidth() - 280), 280});
		boxSash.setMaximizedControl(gearBoxView);
		
	}
	
	
	/**
	 * Initialize Gears
	 */
	protected void initGears() {
		if (CAPrefs.OPENED_GEARS != null && CAPrefs.OPENED_GEARS.length > 0) {
			for (Gear gear : CAPrefs.OPENED_GEARS) {
				gear.getController(workspace);
			}
		}
		else {
			// Default gears shown on Concise open
			Gear.CorpusManager.getController(workspace);
			Gear.Concordancer.getController(workspace);
			Gear.Collocator.getController(workspace);
			Gear.WordLister.getController(workspace);
			Gear.WordCluster.getController(workspace);
		}
		
		// load auto-completer drop down text
		Thread thread = new Thread() { public void run() {
			try {
				
				if (workspace.getIndexReader(INDEX.DOCUMENT) != null) {
					AutoCompleter.getInstanceFor(workspace.getIndexReader(INDEX.DOCUMENT), 
												 CAPrefs.SHOW_PART_OF_SPEECH);
				}
				
			} catch (Exception e) {
				CAErrorMessageDialog.open(null, e);
			}
		} };
		thread.setDaemon(true);
		thread.start();
		
		// async is needed to wait until focus reaches its new Control
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (CAPrefs.LAST_ACTIVE_GEAR == null) {
					// select the first gear
					CABox.GearBox.getBoxView(workspace).setSelection(0);
				}
				else {
					CAPrefs.LAST_ACTIVE_GEAR.open(workspace);
				}
				
				// show toolbox if needed
				if (toolBoxView.getGearControllers().length > 0) {
					SashForm container = (SashForm) toolBoxView.getParent();
					container.setMaximizedControl(null);
					if (toolBoxView.getSelectedGear() == null) {
						toolBoxView.setSelection(0);
					}
				}
			}
		});
	}
	
	/**
	 * 傳回對應的 Workspace
	 * @return 對應的 Workspace
	 */
	public Workspace getWorkspace() {
		return workspace;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
