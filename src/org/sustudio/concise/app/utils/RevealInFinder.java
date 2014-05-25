package org.sustudio.concise.app.utils;

import java.io.File;
import java.io.IOException;

public class RevealInFinder {

	/**
	 * Reveal a file in Finder (Mac OS X only)
	 * @param file
	 * @throws IOException
	 */
	public static void show(File file) throws IOException {
		show(file.getPath());
	}
	
	/**
	 * Reveal a file in Finder (Mac OS X only)
	 * @param path
	 * @throws IOException
	 */
	public static void show(String path) throws IOException {
		if (!Platform.isMac()) return;
		// look further
		// https://chromium.googlesource.com/external/dart/+/0a6ef7b230959556add96b0a4a814babeb66c680/dart/editor/tools/plugins/com.google.dart.tools.ui/src/com/google/dart/tools/ui/actions/ShowInFinderAction.java
		//
		// show in finder
		//
		// mac only
		new ProcessBuilder("/usr/bin/open", "-R", path).start();
		return;
	}
	
}
