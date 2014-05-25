package org.sustudio.concise.app;

import java.io.File;


/**
 * Config
 *
 * 
 * @author Kuan-ming Su
 *
 */
public class CAConfig {

	/** Application name - Concise */
	public static final String APP_NAME = "Concise";
	
	/** Application version - current version */
	public static final String APP_VERSION = "0.3.6";
	
	
	/** Workspace Extension (conciseworkspace) */
	public static final String WORKSPACE_EXTENSION = "conciseworkspace";
	
	
	/** Default Workspace name (file name) */
	public static String DEFAULT_WORKSPACE_NAME = "ConciseWorkspace";
	
	/** Default Workspace path (~/Documents/ConciseWorkspace) */
	public static File DEFAULT_WORKSPACE_PATH = new File(System.getProperty("user.home"), "Documents/" + DEFAULT_WORKSPACE_NAME);
	

}
