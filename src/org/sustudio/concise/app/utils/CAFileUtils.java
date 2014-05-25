package org.sustudio.concise.app.utils;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class CAFileUtils {

	/**
	 * Returns unique filename by appending (1) or (2) or (n) to filename
	 * @param file
	 * @return
	 */
	public static File getUniqueFile(File file) {
		while (file.exists()) {
			String filePath = file.getPath();
			String fileExtension = FilenameUtils.getExtension(filePath);
			String fileNoExtension = FilenameUtils.removeExtension(filePath);
			if (fileNoExtension.matches(".*\\(\\d\\)$")) {
				int num = Integer.valueOf(fileNoExtension.replaceAll(".*\\((\\d+)\\)$", "$1"));
				num++;
				fileNoExtension = fileNoExtension.substring(0, fileNoExtension.lastIndexOf('(')) 
									+ "(" + String.valueOf(num) + ")";
			}
			else {
				fileNoExtension += "(1)";
			}
			file = new File(fileNoExtension + "." + fileExtension);
			return getUniqueFile(file);
		}
		return file;
	}
	
}
