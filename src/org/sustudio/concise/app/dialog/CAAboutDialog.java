package org.sustudio.concise.app.dialog;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.CAConfig;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.utils.Platform;

/**
 * About Concise Dialog. (System Menu)
 */
public class CAAboutDialog extends Shell {

	/**
	 * Create About Concise Dialog.
	 */
	public CAAboutDialog() {
		super(Display.getCurrent(), SWT.DIALOG_TRIM | SWT.ON_TOP);
		
		// disable full screen icon
		if (Platform.isMac()) {
			NSWindow nswindow = view.window();
			nswindow.setCollectionBehavior(0);
			nswindow.setShowsResizeIndicator(false);
		}
		setFullScreen(false);
		
		setSize(284, 218);
		GridLayout gl_shell = new GridLayout(1, false);
		gl_shell.verticalSpacing = 8;
		setLayout(gl_shell);
		
		Label lblConciseImage = new Label(this, SWT.NONE);
		lblConciseImage.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblConciseImage.setImage(SWTResourceManager.getImage(CAAboutDialog.class, "/org/sustudio/concise/app/icon/concise.png"));
		
		Label lblConcise = new Label(this, SWT.NONE);
		lblConcise.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblConcise.setAlignment(SWT.CENTER);
		lblConcise.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblConcise.setText(CAConfig.APP_NAME);
		
		Label lblVersionb = new Label(this, SWT.NONE);
		lblVersionb.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.NORMAL));
		lblVersionb.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblVersionb.setText(CABundle.get("about.version")+" "+CAConfig.APP_VERSION);
		
		Label lblCopyright = new Label(this, SWT.NONE);
		lblCopyright.setAlignment(SWT.CENTER);
		lblCopyright.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.NORMAL));
		lblCopyright.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblCopyright.setText("Copyright © 2011-2014 SUStudio\nAll rights reserved.");
		
		Link link = new Link(this, SWT.NONE);
		link.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.NORMAL));
		link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		link.setText("<a href=\"http://www.sustudio.org\">www.sustudio.org</a>");
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Program.launch(e.text);
			}
		});
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
