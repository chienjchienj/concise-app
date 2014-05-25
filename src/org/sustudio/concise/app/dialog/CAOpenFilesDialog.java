package org.sustudio.concise.app.dialog;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.sustudio.concise.app.CAConfig;

public class CAOpenFilesDialog extends FileDialog {

	/**
	 * Create the dialog.
	 */
	public CAOpenFilesDialog() {
		this(false);
	}
	
	public CAOpenFilesDialog(boolean singleFile) {
		super(Display.getDefault().getActiveShell(), SWT.SHEET | (singleFile ? SWT.OPEN : SWT.MULTI));
		setText("Open File" + (singleFile ? "" : "s"));
	}
	
	public String[] getFileNames() {
		String[] filenames = super.getFileNames();
		if (filenames == null) return null;
		
		for (int i=0; i<filenames.length; i++) {
			StringBuffer buf = new StringBuffer(getFilterPath());
			if (buf.charAt(buf.length()-1) != File.separatorChar)
				buf.append(File.separatorChar);
			buf.append(filenames[i]);
			filenames[i] = buf.toString();
		}
		return filenames;
	}
	
	public void setWorkspaceConfigure() {
		setFilterNames(new String[] { "Concise Workspace" });
		setFilterExtensions(new String[] { CAConfig.WORKSPACE_EXTENSION });
		setText("Browse Concise Workspace");
	}
	
	/** 
	 * @deprecated
	 */
	public void setLoadPreferencesConfigure() {
		setFilterNames(new String[] { "Concise Preferences (.pref)" } );
		setFilterExtensions(new String[] { "*.cpref" } );
		setText("Load Concise Preferences");
	}
	
	public void setOpenPOSTaggerConfigure() {
		setFilterExtensions(new String[] { "*.tagger", "*.*" } );
		setFilterNames(new String[] { "POS Tagger (*.tagger)", "All Files (*.*)" });
		setText("Open POS Tagger File");
	}
	
	public void setOpenTextFileConfigure() {
		setFilterExtensions(new String[] { "*.txt", "*.*" } );
		setFilterNames(new String[] { "Plain Text Files (*.txt)", "All Files (*.*)" });
		setText("Open Text File");
	}
	
	public void setOpenCorpusCongifure() {
		setFilterExtensions(new String[] { "*.docx; *.doc; *.pdf; *.txt; *.txt.gz", 
										   "*.docx", 
										   "*.doc",
										   "*.txt", 
										   "*.txt.gz", 
										   "*.*" });
		setFilterNames(new String[] { "All Supported Types (*.docx; *.doc; *.pdf; *.txt; *.txt.gz)", 
									  "Word Document (*.docx)", 
									  "Word 97-2004 Document (*.doc)",
									  "Plain Text (*.txt)", 
									  "Gzipped Plain Text (*.txt.gz)", 
									  "All (*.*)" });
		setText("Import Corpus Files");
	}
	
	public void setOpenDictionaryCongifure() {
		setFilterExtensions(new String[] { "*.dic", "*.txt" });
		setFilterNames(new String[] { "Dictionary (*.dic)", "Plain Text (*.txt)" });
		setText("Import Dictionary Files");
	}
	
	/**
	 * @deprecated use {@link #setOpenCorpusCongifure()}.
	 */
	public void setOpenRawDocumentCongifure() {
		setFilterExtensions(new String[] { "*.docx; *.doc; *.txt; *.txt.gz", "*.docx", "*.doc", "*.txt", "*.txt.gz" });
		setFilterNames(new String[] { "All Supported Types (*.docx; *.doc; *.txt; *.txt.gz)", "Word Document (*.docx)", "Word 97-2004 Document (*.doc)", "Plain Text (*.txt)", "Gzipped Plain Text (*.txt.gz)" });
		setText("Import Raw Documents");
	}
			
	public void setFilePath(String path) {
		setFilterPath(path);
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
