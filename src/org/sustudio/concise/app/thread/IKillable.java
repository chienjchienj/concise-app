package org.sustudio.concise.app.thread;

public interface IKillable {

	/**
	 * A way to kill/stop the process.
	 */
	public void kill();
	
	/**
	 * To show if the process is alive.
	 * @return
	 */
	public boolean isKilled();
}
