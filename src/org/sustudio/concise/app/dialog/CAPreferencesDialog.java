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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.enums.PrefsEnum;
import org.sustudio.concise.app.preferences.CAPrefsUtils;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.thread.CAReTokenizeThread;
import org.sustudio.concise.app.thread.CAThread;
import org.sustudio.concise.app.utils.Platform;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;

public class CAPreferencesDialog extends Shell {

	private static final int minWidth = 400;
	
	private final Composite composite;
	private final StackLayout layout;
	private final PrefsEnum defaultPrefTab = PrefsEnum.GENERAL;
	
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
		
		for (final PrefsEnum pref : PrefsEnum.values()) {
			final ToolItem item = new ToolItem(toolBar, SWT.RADIO);
			item.setImage(pref.getImage());
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (item.getSelection()) {
						openWidget(pref);
					}
				}
			});
			item.setText(pref.getLabel());
			item.setSelection(pref == defaultPrefTab);
			pref.setToolItem(item);
		}
		
		composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new StackLayout();
		composite.setLayout(layout);
		
		// initial
		PrefsEnum.GENERAL.setControl(new CADlgPrefGeneral(composite, SWT.NONE));
		PrefsEnum.FILE.setControl(new CADlgPrefFile(composite, SWT.NONE));
		PrefsEnum.TOKEN.setControl(new CADlgPrefToken(composite, SWT.NONE));
		
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
							CAThread thread = new CAReTokenizeThread();
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
	
	private void openWidget(final PrefsEnum pref) {
		boolean animationRequired = layout.topControl != null && 
									!layout.topControl.equals(pref.getControl());
		
		layout.topControl = pref.getControl();
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
