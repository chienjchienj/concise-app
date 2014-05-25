package org.sustudio.concise.app.dialog;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.sustudio.concise.app.CAConfig;
import org.sustudio.concise.app.Concise;

public class CASaveFileDialog extends FileDialog {

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CASaveFileDialog(Shell shell) {
		super(shell, SWT.SAVE | SWT.SHEET);
		setText("Save File");
	}
	
	public CASaveFileDialog() {
		this(Concise.getActiveApp());
	}
	
	public void setWorkspaceConfigure() {
		setFilterNames(new String[] { "Concise Workspace" });
		setFilterExtensions(new String[] { CAConfig.WORKSPACE_EXTENSION });
		setText("Create New Concise Workspace");
	}
	
	public void setNetworkConfigure() {
		setFilterNames(new String[] { "PNG (.png)", 
									  "JPEG (.jpg)",
									  "TIFF (.tif)",
									  "GIF (.gif)",
									  "Windows BMP (.bmp)",
									  "Gephi (.gexf)",
									  "Cytoscape (.xgmml)", 
									  "Pajek (.net)",
									  "NetDraw (.dl)" });
		setFilterExtensions(new String[] { "*.png", "*.jpg", "*.tif", "*.gif", "*.bmp", "*.gexf", "*.xgmml", "*.net", "*.dl" });
		setText("Save Network As...");
	}
	
	public void setTextFileConfigure() {
		setFilterNames(new String[] { "Plain Text File (.txt)", "All Files (*.*)" });
		setFilterExtensions(new String[] { "*.txt", "*.*" });
	}
	
	public void setSaveImageConfigure() {
		setFilterNames(new String[] { "PNG (.png)", "JPEG (.jpg)", "TIFF (.tif)", "GIF (.gif)", "Windows BMP (.bmp)" });
		setFilterExtensions(new String[] { "*.png", "*.jpg", "*.tif", "*.gif", "*.bmp" });
		setText("Save Image As...");
	}
	
	public void setSaveOutputAsConfigure() {
		setFilterNames(new String[] { "Excel Workbook (.xlsx)", "CSV (.csv)", "Tab-separated Text File (.txt)" } );
		setFilterExtensions(new String[] { "*.xlsx", "*.csv", "*.txt" } );
		setText("Save Output As...");
	}
	
	public void setSavePreferencesConfigure() {
		setFilterNames(new String[] { "Concise Preferences (.cpref)" } );
		setFilterExtensions(new String[] { "*.cpref" } );
		setText("Save Concise Preferences");
	}
	
	public void setConciseConfigure() {
		setFilterNames(new String[] { "Concise Settings (.concise)" } );
		setFilterExtensions(new String[] { "*.concise" } );
		setText("Export Concise Settings");
	}
	
	public void setNewFileName(String fileFullPath) {
		File f = new File(fileFullPath);
		setFilterPath(f.getPath());
		setFileName("new_"+f.getName());
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
