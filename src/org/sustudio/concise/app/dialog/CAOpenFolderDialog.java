package org.sustudio.concise.app.dialog;

import java.io.File;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;

public class CAOpenFolderDialog extends DirectoryDialog {

	public CAOpenFolderDialog() {
		super(Display.getDefault().getActiveShell());
		setText("Load Corpurs Files From A Directory");
	}
	
	public String[] getFileNames() {
		String dir = open();
		if (dir == null) return null;
		
		String[] filenames = new File(dir).list();
		for (int i=0; i<filenames.length; i++) {
			StringBuilder buf = new StringBuilder(dir);
			if (buf.charAt(buf.length()-1) != File.separatorChar)
				buf.append(File.separatorChar);
			buf.append(filenames[i]);
			filenames[i] = buf.toString();
			buf.setLength(0);
		}
		return filenames;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
