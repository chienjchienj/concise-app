package org.sustudio.concise.app.utils;

/**
 * Examine operating system  
 * 
 * @author Kuan-ming Su.
 *
 */
public class Platform {

	private static final String OS = System.getProperty("os.name");
	
	/**
	 * Returns true if os is mac.
	 * @return		true if os is mac.
	 */
	public static final boolean isMac() {		
		return OS.toLowerCase().contains("mac");
	}
	
	
	/**
	 * Returns true if os is windows.
	 * @return		true if os is windows.
	 */
	public static final boolean isWindows() {
		return OS.toLowerCase().contains("windows");
	}
	
	
	/**
	 * Returns true if is is linux.
	 * @return		true if os is linux.
	 */
	public static final boolean isLinux() {
		return OS.toLowerCase().contains("linux");
	}
	
}
