package org.sustudio.concise.app.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.Gear;

public class CAErrorMessageDialog extends MessageBox {

	public CAErrorMessageDialog() {
		this(Display.getDefault().getShells()[0]);
	}
	
	public CAErrorMessageDialog(Shell shell) {
		super(shell, SWT.ICON_ERROR | SWT.OK);
		setText("Error");
	}
		
	public static void open(Gear gear, final Exception e) {
		Concise.getCurrentWorkspace().logError(gear, e);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				CAErrorMessageDialog dlg = new CAErrorMessageDialog();
				dlg.setMessage(e.getMessage() == null ? e.toString() : e.getMessage());
				dlg.open();
			}
		});
		
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
}
