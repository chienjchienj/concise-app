package org.sustudio.concise.app.dialog;

import java.io.File;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.preferences.CAPrefs;

public class CADlgPrefSystem extends Composite {
	
	private final Button btnRam;
	private final Button btnWorkspace;
	private final Button btnSystem;
	
	private final SelectionAdapter listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			if (btnRam.getSelection()) {
				CAPrefs.TEMPORARY_FOLDER = null;
			}
			else if (btnWorkspace.getSelection()) {
				CAPrefs.TEMPORARY_FOLDER = Concise.getCurrentWorkspace().getFile();
			}
			else if (btnSystem.getSelection()) {
				CAPrefs.TEMPORARY_FOLDER = new File(System.getProperty("java.io.tmpdir"));
			}
		}
	};
	
	public CADlgPrefSystem(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginHeight = 10;
		gridLayout.marginWidth = 30;
		gridLayout.verticalSpacing = 10;
		gridLayout.marginBottom = 20;
		setLayout(gridLayout);
		
		Group grpPartofspeech = new Group(this, SWT.NONE);
		grpPartofspeech.setLayout(new GridLayout(1, false));
		grpPartofspeech.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpPartofspeech.setText("Temporary Directory");
		
		btnRam = new Button(grpPartofspeech, SWT.RADIO);
		btnRam.setText("RAM");
		btnRam.addSelectionListener(listener);
		
		btnWorkspace = new Button(grpPartofspeech, SWT.RADIO);
		btnWorkspace.setText("Inside Workspace");
		btnWorkspace.addSelectionListener(listener);
		
		btnSystem = new Button(grpPartofspeech, SWT.RADIO);
		btnSystem.setText("System Temporary Directory");
		btnSystem.addSelectionListener(listener);
		
		if (CAPrefs.TEMPORARY_FOLDER != null &&
			CAPrefs.TEMPORARY_FOLDER.equals(Concise.getCurrentWorkspace().getFile())) 
		{
			btnWorkspace.setSelection(true);
		}
		else if (CAPrefs.TEMPORARY_FOLDER != null &&
				 CAPrefs.TEMPORARY_FOLDER.equals(new File(System.getProperty("java.io.tmpdir")))) 
		{
			btnSystem.setSelection(true);
		}
		else {
			btnRam.setSelection(true);
		}
	}
}
