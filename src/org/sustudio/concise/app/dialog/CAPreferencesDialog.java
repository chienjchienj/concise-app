package org.sustudio.concise.app.dialog;

import org.eclipse.nebula.animation.AnimationRunner;
import org.eclipse.nebula.animation.effects.ResizeEffect;
import org.eclipse.nebula.animation.movement.QuartInOut;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.preferences.CAPrefsUtils;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.thread.CAReTokenizeThread;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.utils.Platform;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.SWTResourceManager;

public class CAPreferencesDialog extends Shell {

	public enum TAB {
		
		/** General Preferences */
		GENERAL(CABundle.get("preferences.general"), "/org/sustudio/concise/app/icon/81-dashboard-20x20.png"), 
		
		/** File Preferences */
		FILE(CABundle.get("preferences.file"), "/org/sustudio/concise/app/icon/96-book-20x20.png"), 
		
		///** Tag Preferences */
		//TAG("Tag", "/org/sustudio/concise/app/icon/172-pricetag-20x20.png"), 
		
		///** XML Preferences */
		//XML("XML", "/org/sustudio/concise/app/icon/149-windmill-20x20.png"), 
		
		///** Wild card Preferences */
		//WILDCARD("Wild Card", "/org/sustudio/concise/app/icon/198-card-spades-20x20.png"), 
		
		/** Token Preferences */
		TOKEN(CABundle.get("preferences.token"), "/org/sustudio/concise/app/icon/117-todo-20x20.png"),
		
		///** Font Preferences */
		//FONT(CABundle.get("preferences.font"), "/org/sustudio/concise/app/icon/113-navigation-20x20.png"),
		
		/** System Preferences */
		SYSTEM("System", "/org/sustudio/concise/app/icon/157-wrench-20x20.png"),
		;
		
		private final String label;
		private final Image image;
		private Control control;
		private ToolItem item;
				
		TAB(String label, String imageClasspath) {
			this.label = label;
			image = SWTResourceManager.getImage(getClass(), imageClasspath);
		}
		
		public String getLabel() {
			return label;
		}
		
		public void setControl(Control control) {
			this.control = control;
		}
		
		public Control getControl() {
			return control;
		}
		
		public Image getImage() {
			return image;
		}
		
		public void setToolItem(ToolItem item) {
			this.item = item;
		}
		
		public ToolItem getToolItem() {
			return item;
		}
		
	}
	
	private static final int minWidth = 400;
	
	private final Composite composite;
	private final StackLayout layout;
	private final TAB defaultPrefTab = TAB.GENERAL;
	
	public CAPreferencesDialog() {
		super(Concise.getActiveApp(), SWT.DIALOG_TRIM | SWT.RESIZE);
		setText(CABundle.get("preferences"));
		setLayout(new GridLayout());
		
		final Composite cToolBar = new Composite(this, SWT.EMBEDDED);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		cToolBar.setLayout(new FillLayout());
		ToolBar toolBar;
		if (Platform.isMac()) {
			toolBar = getToolBar();
			gd.heightHint = 0;
		}
		else
			toolBar = new ToolBar(this, SWT.FLAT);
		toolBar.setLayoutData(gd);
		
		for (final TAB tab : TAB.values()) {
			final ToolItem item = new ToolItem(toolBar, SWT.RADIO);
			item.setImage(tab.getImage());
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (item.getSelection()) {
						openWidget(tab);
					}
				}
			});
			item.setText(tab.getLabel());
			item.setSelection(tab == defaultPrefTab);
			tab.setToolItem(item);
		}
		
		ToolItem sep = new ToolItem(toolBar, SWT.SEPARATOR, toolBar.getItemCount()-1);
		sep.setWidth(SWT.SEPARATOR_FILL);
		
		composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new StackLayout();
		composite.setLayout(layout);
		
		// initial
		TAB.GENERAL.setControl(	new CADlgPrefGeneral(composite, SWT.NONE));
		TAB.FILE.setControl(	new CADlgPrefFile(composite, SWT.NONE));
		TAB.TOKEN.setControl(	new CADlgPrefToken(composite, SWT.NONE));
		TAB.SYSTEM.setControl(	new CADlgPrefSystem(composite, SWT.NONE));
		
		openWidget(defaultPrefTab);
		
		addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent event) {
				try {
					CAPrefsUtils.writePrefs();
				} catch (Exception e) {
					Concise.getCurrentWorkspace().logError(null, e);
					Dialog.showException(e);
				}
				
				
				if (getShell().getModified()) 
				{
					if (getShell().getData("ReTokenize") != null && (Boolean) getShell().getData("ReTokenize")) 
					{
						if (Dialog.isConfirmed(getShell(), "Analyzer or Dictionary has been changed!", "Do you want to re-tokenize documents now?")) 
						{	
							ConciseThread thread = new CAReTokenizeThread();
							thread.start();
						}
					}
					
					if (getShell().getData("TokenChange") != null && (Boolean) getShell().getData("TokenChange"))
					{
						Dialog.inform(getShell(), "Token changed!", "Re-calculating word frequency is required.");
					}
				}
			}
		});
	}
	
	private void openWidget(final TAB tab) {
		boolean animationRequired = layout.topControl != null && 
									!layout.topControl.equals(tab.getControl());
		
		layout.topControl = tab.getControl();
		composite.layout();
		
		if (animationRequired) {
			Point newSize = layout.topControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			newSize.x += 80;
			newSize.y += 150;
			newSize.x = newSize.x < minWidth ? minWidth : newSize.x; 
			
			getToolBar().setEnabled(false);
			new AnimationRunner().runEffect(
					new ResizeEffect(CAPreferencesDialog.this, 
									 getSize(), 
									 newSize, 
									 500,
									 new QuartInOut(),
									 enableToolBar,
									 null));
		}
	}
	
	private Runnable enableToolBar = new Runnable() {
		public void run() {
			CAPreferencesDialog.this.getToolBar().setEnabled(true);
		}
	};
	
	public void open() {
		Point newSize = layout.topControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		newSize.x += 80;
		newSize.y += 150;
		newSize.x = newSize.x < minWidth ? minWidth : newSize.x;
		setSize(newSize);
		super.open();
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
}
