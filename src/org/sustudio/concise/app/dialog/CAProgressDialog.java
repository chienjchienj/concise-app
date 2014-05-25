package org.sustudio.concise.app.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CASpinner;

public class CAProgressDialog extends CASpinner {
	
	private String confirmCancelMessage;	
	private String status;
	private boolean kill = false;

	public CAProgressDialog(GearController gearView) {
		super(gearView);
		elapsedTimer();
	}
	
	/**
	 * Create contents of the dialog.
	 */
	protected void createContents() {
		super.createContents();
		
		Rectangle rect = lblMsg.getBounds();
		lblMsg.setBounds(
				rect.x,
				rect.y,
				rect.width,
				rect.height - 20);
		
		setText("Progress Dialog");
		addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (Dialog.isConfirmed(getShell(), confirmCancelMessage, ""))
					close();
				else
					e.doit = false;
			}
		});
		
		Button btnCancel = new Button(this, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (Dialog.isConfirmed(getShell(), confirmCancelMessage, "")) {
					close();
				}
			}
		});
		btnCancel.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.NORMAL));
		btnCancel.setText("Cancel");
		
		Point size = btnCancel.computeSize(rect.width - 80, SWT.DEFAULT, true);
		btnCancel.setBounds(
				rect.x + 40,
				rect.y + rect.height - 20,
				rect.width - 80,
				size.y);
		
		setDefaultButton(btnCancel);
	}
	
	
	/**
	 * Sets warning message for canceling.
	 * @param msg		canceling message.
	 */
	public void setCancelWarningMessage(String msg) {
		this.confirmCancelMessage = msg;
	}
	
	/**
	 * Update progress dialog's status text
	 * @param status		status text.
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	public void close() {
		kill = true;
		for (Shell shell : getShells()) {
			shell.close();
		}
		super.close();
	}
	
	/**
	 * Update Information (10 times per second)
	 * @param time			elapsed time.
	 */
	private void updateInfo(final long time) {
		getDisplay().syncExec(new Runnable() { public void run() {
			if (kill) return;
			
			// update status and time elapsed
			CAProgressDialog.this.setMessage((status == null) ? Formats.getTime(time) : status + "\n" + Formats.getTime(time));
			
		} });
	}
	
	/**
	 * Timer for update status (1s).
	 */
	private void elapsedTimer() {
		final Thread timeThread = new Thread("ElapsedTimer") {
			public void run() {
				final long timeStart = System.currentTimeMillis();
				while (!kill) {
					long currentTime = System.currentTimeMillis();
					long time = currentTime - timeStart;
					updateInfo(time);
					while ( (System.currentTimeMillis() - currentTime) < 100) {
						// wait for 0.1 second;
					}
				}
			}
		};
		timeThread.setDaemon(true);
		timeThread.start();
	}
	
}
