package org.sustudio.concise.app;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.dialog.CAOpenFilesDialog;
import org.sustudio.concise.app.dialog.CASaveFileDialog;
import org.sustudio.concise.app.resources.CABundle;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * A simple window for selecting the existing workspace or creating the new workspace.
 * 
 * @author Kuan-ming Su
 *
 */
public class Launcher extends Shell {

	private final Combo workspaceCombo;
	
	/**
	 * Create the workspace launcher shell.
	 */
	public Launcher() {
		super(Display.getCurrent());
		
		setText(CAConfig.APP_NAME + " Launcher");
		setSize(550, 320);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		
		Label lblConciseBanner = new Label(this, SWT.NONE);
		lblConciseBanner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblConciseBanner.setImage(SWTResourceManager.getImage(Launcher.class, "/org/sustudio/concise/app/icon/concise-banner.png"));
		GridData gd_lblConciseBanner = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1);
		gd_lblConciseBanner.heightHint = 150;
		gd_lblConciseBanner.widthHint = 500;
		lblConciseBanner.setLayoutData(gd_lblConciseBanner);
		
		Label lblWorkspace = new Label(this, SWT.NONE);
		lblWorkspace.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblWorkspace.setText(CABundle.get("launcher.workspace") + ":");
		
		workspaceCombo = new Combo(this, SWT.READ_ONLY);
		workspaceCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				launchConcise();
			}
		});
		workspaceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		workspaceCombo.setItems(RecentWorkspaces.getRecentWorkspacePaths());
		workspaceCombo.select(0);
		
		
		Button btnBrowse = new Button(this, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				// 瀏覽要開啟的檔案
				CAOpenFilesDialog dlg = new CAOpenFilesDialog(true);
				dlg.setWorkspaceConfigure();
				String filename = dlg.open();
				if (filename != null && 
					new File(filename).exists()) 
				{
					filename = FilenameUtils.removeExtension(filename);
					workspaceCombo.setItems(ArrayUtils.add(workspaceCombo.getItems(), 0, filename));
					workspaceCombo.select(0);
				}
				
			}
		});
		btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnBrowse.setText(CABundle.get("button.browse"));
		
		
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Button btnCreate = new Button(this, SWT.NONE);
		btnCreate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				CASaveFileDialog dlg = new CASaveFileDialog(getShell());
				dlg.setWorkspaceConfigure();
				dlg.setFileName(CAConfig.DEFAULT_WORKSPACE_NAME);
				String filename = dlg.open();
				if (filename != null) {
					if (!new File(filename).exists()) {
						new File(filename).mkdir();
					}
					filename = FilenameUtils.removeExtension(filename);
					workspaceCombo.setItems(ArrayUtils.add(workspaceCombo.getItems(), 0, filename));
					workspaceCombo.select(0);
					launchConcise();
				}
			}
		});
		btnCreate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnCreate.setText(CABundle.get("button.create"));
		
		
		Button btnUseAsDefault = new Button(this, SWT.CHECK);
		btnUseAsDefault.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.NORMAL));
		btnUseAsDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean checked = ((Button) e.widget).getSelection();
				RecentWorkspaces.prefs.putBoolean("use_as_default", checked);
			}
		});
		btnUseAsDefault.setText(CABundle.get("launcher.useWorkspaceAsDefault"));
		btnUseAsDefault.setSelection(RecentWorkspaces.prefs.getBoolean("use_as_default", false));
		btnUseAsDefault.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Button btnCancel = new Button(this, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Launcher.this.close();
			}
		});
		btnCancel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnCancel.setText(CABundle.get("button.cancel"));
		
		Button btnOpen = new Button(this, SWT.NONE);
		btnOpen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				launchConcise();
			}
		});
		btnOpen.setText(CABundle.get("button.open"));
		setDefaultButton(btnOpen);
		
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
	}
	
	public void open() {
		if (RecentWorkspaces.prefs.getBoolean("use_as_default", false)) 
		{
			launchConcise();
			return;
		}
		
		// center on the current (mouse location) monitor
		Point mousePoint = getDisplay().getCursorLocation();
		for (Monitor m : getDisplay().getMonitors()) {
			if (m.getBounds().contains(mousePoint)) {
				Rectangle bounds = m.getBounds();
				Rectangle rect = getBounds();
				int x = bounds.x + (bounds.width - rect.width) / 2;
				int y = bounds.y + (bounds.height - rect.height) / 3;
				setLocation(x, y);
				break;
			}
		}
		super.open();
	}	
	
	private void launchConcise() {
		String filename = workspaceCombo.getText().trim();
		if (!FilenameUtils.isExtension(filename, CAConfig.WORKSPACE_EXTENSION)) {
			filename += "." + CAConfig.WORKSPACE_EXTENSION;
		}
		File ws = new File(filename);
		if (!ws.exists()) {
			// Pop a dialog to confirm to create
			if (Dialog.isConfirmed(
					this, 
					"Workspace does not exists. Do you want to create it?", 
					"This will create \"" + FilenameUtils.removeExtension(ws.getName()) + "\"."))
			{
				ws.mkdir();
			}
			else {
				return;
			}
		}
		
		Concise.openApp(ws);
		Launcher.this.close();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
