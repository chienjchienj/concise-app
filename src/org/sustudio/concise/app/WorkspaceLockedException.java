package org.sustudio.concise.app;

/**
 * 檢查工作空間中的 concise.lock 檔案，檔案存在表示工作空間正在使用。
 * 
 * @author Kuan-ming Su
 *
 */
public class WorkspaceLockedException extends Exception {
	
	private static final long serialVersionUID = 4170248166821237441L;

	public WorkspaceLockedException(String message) {
		super(message);
	}
}
