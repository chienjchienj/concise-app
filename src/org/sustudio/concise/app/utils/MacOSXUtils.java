package org.sustudio.concise.app.utils;

import java.io.File;
import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class MacOSXUtils {

	/**
	 * Reveal a file in Finder (Mac OS X only)
	 * @param file
	 * @throws IOException
	 */
	public static void revealInFinder(File file) throws IOException {
		revealInFinder(file.getPath());
	}
	
	/**
	 * Reveal a file in Finder (Mac OS X only)
	 * @param pathname
	 * @throws IOException
	 */
	public static void revealInFinder(String pathname) throws IOException {
		if (!Platform.isMac()) return;
		// look further
		// https://chromium.googlesource.com/external/dart/+/0a6ef7b230959556add96b0a4a814babeb66c680/dart/editor/tools/plugins/com.google.dart.tools.ui/src/com/google/dart/tools/ui/actions/ShowInFinderAction.java
		//
		// show in finder (Mac OS X only)
		new ProcessBuilder("/usr/bin/open", "-R", pathname).start();
		return;
	}
	
	/**
	 * Hide file extension (using apple script)
	 * @param file
	 * @throws ScriptException
	 */
	public static void hideExtension(File file) throws ScriptException {
		String script = "tell application \"Finder\" to set extension hidden of (POSIX file \""+ file.getPath() + "\" as alias) to true";
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("AppleScript");
		engine.eval(script);
	}
}
