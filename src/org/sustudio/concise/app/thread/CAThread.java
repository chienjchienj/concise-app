package org.sustudio.concise.app.thread;

import org.eclipse.swt.widgets.Display;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.dialog.CAProgressDialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.CAQueryUtils;

public abstract class CAThread extends Thread implements IKillable {

	protected Gear gear;
	protected CAQuery query;
	protected CAProgressDialog dialog;
	protected boolean kill = false;
	
	public CAThread(Gear gear, CAQuery query) {
		this.gear = gear;
		this.query = query;
		dialog = new CAProgressDialog(gear.getController(Concise.getCurrentWorkspace()));
		
		// log query to database
		try 
		{
			Concise.getCurrentWorkspace().logInfo(query.toString());
			CAQueryUtils.logQuery(query);
		
		} catch (Exception e) {
			CAErrorMessageDialog.open(gear, e);
		}
		
		dialog.setCancelWarningMessage("Do you want to cancel current " + query.getGear() + " task?");
	}
	
	public void setCancelWarningMessage(String message) {
		dialog.setCancelWarningMessage(message);
	}
	
	public void run() {
		running();
		done();
	}
	
	public abstract void running();
	
	protected void loadData() {
		gear.getController(Concise.getCurrentWorkspace()).loadData();
	}
	
	protected void done() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!dialog.isDisposed())
					dialog.close();
				
				loadData();
			}
		});
	}
	
	public void start() {
		dialog.open();
		super.start();
	}
	
	public void kill() {
		kill = true;
		interrupt();
		Concise.getCurrentWorkspace().logInfo(gear.label() + " process killed by user");
	}
	
	public boolean isKilled() {
		return kill;
	}
}
