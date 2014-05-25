package org.sustudio.concise.app;

import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import org.apache.commons.io.FilenameUtils;

public class RecentWorkspaces {

	static final Preferences prefs = Preferences.userNodeForPackage(Concise.class);
	
	/**
	 * 內部用，把指定的路徑塞到第一筆，然後傳回最近10筆工作路徑
	 * @param dir 指定的路徑（準備要開啟的檔案）
	 * @return 最近10筆工作路徑
	 */
	protected static String[] getRecentWorkspacePaths(final String dir) {
		ArrayList<String> items = new ArrayList<String>();
		if (dir != null &&
			new File(dir).exists() && 
			new File(dir).isDirectory() &&
			FilenameUtils.isExtension(dir, CAConfig.WORKSPACE_EXTENSION))
		{
			items.add(FilenameUtils.removeExtension(dir));
		}
		for (int i=0; i<10; i++) {
			String item = prefs.get("workspace"+i, null);
			if (item != null) 
			{
				File itemFile = new File(item);
				if (itemFile.exists()) 
				{
					// hide package extension
					if (FilenameUtils.isExtension(item, CAConfig.WORKSPACE_EXTENSION)) {
						item = FilenameUtils.removeExtension(item);
					}
					if (!items.contains(item)) {
						items.add(item);
					}
				}
			}
		}
		if (items.isEmpty()) {
			return new String[] { CAConfig.DEFAULT_WORKSPACE_PATH.getPath() };
		}
		return items.toArray(new String[0]);
	}

	/**
	 * 傳回最近10筆工作路徑
	 * @return 最近10筆工作路徑
	 */
	public static String[] getRecentWorkspacePaths() {
		return getRecentWorkspacePaths(null);
	}

	/**
	 * 清除記憶的最近幾筆工作路徑
	 */
	public static void clearRecentWorkspacePaths() {
		for (int i=0; i<10; i++) {
			RecentWorkspaces.prefs.remove("workspace"+i);
		}
	}

	/**
	 * 記憶最近的工作空間
	 * @param items
	 * @param workspace
	 */
	public static void setRecentWorkspaces(Workspace workspace) {
		if (workspace == null) {
			return;
		}
		ArrayList<String> recentWorkspaces = new ArrayList<String>();
		recentWorkspaces.add(workspace.getFile().getPath());
		String[] items = getRecentWorkspacePaths();
		for (int i=0; i<9 && i<items.length; i++) 
		{
			if (!FilenameUtils.isExtension(items[i], CAConfig.WORKSPACE_EXTENSION)) {
				items[i] += "." + CAConfig.WORKSPACE_EXTENSION;
			}
			if (!items[i].equals(workspace.getFile().getPath())) 
			{
				recentWorkspaces.add(items[i]);
			}
		}
		
		for (int i=0; i<recentWorkspaces.size(); i++) {
			prefs.put("workspace"+i, recentWorkspaces.get(i));
		}
		recentWorkspaces.clear();
	}	
}
