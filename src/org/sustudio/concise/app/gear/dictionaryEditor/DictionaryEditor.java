package org.sustudio.concise.app.gear.dictionaryEditor;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;


public class DictionaryEditor extends GearController {
	
	private StackLayout layout;
	private Composite contentPanel;
	
	public DictionaryEditor() {
		super(CABox.ToolBox, Gear.DictionaryEditor);
		
	}
	
	@Override
	protected Control createControl() {

		layout = new StackLayout();
		contentPanel = new Composite(this, SWT.NONE);
		contentPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentPanel.setLayout(layout);
		
		if (CAPrefs.DICTIONARY_WORKING_FILE != null) {
			layout.topControl = new DictionaryContent(contentPanel, SWT.EMBEDDED, this);
		} else {
			layout.topControl = new DictionaryTable(contentPanel, SWT.EMBEDDED, this);
		}
		contentPanel.layout();
		
		return contentPanel;
	}
	
	public File getWorkingDicFile() {
		return CAPrefs.DICTIONARY_WORKING_FILE;
	}
	
	public void showDicContent(File dicFile) {
		CAPrefs.DICTIONARY_WORKING_FILE = dicFile;
		Control control = layout.topControl;
		layout.topControl = new DictionaryContent(contentPanel, SWT.EMBEDDED, this);
		contentPanel.layout();
		control.dispose();
	}
	
	public void showDicTable() {
		CAPrefs.DICTIONARY_WORKING_FILE = null;
		Control control = layout.topControl;
		layout.topControl = new DictionaryTable(contentPanel, SWT.EMBEDDED, this);
		contentPanel.layout();
		control.dispose();
	}
	
	protected void setCopyPasteHelper() {
		//CopyPasteHelper.listenTo(stopWord);
		//CopyPasteHelper.listenTo(wordsList);
	}

	

	@Override
	public void doit(CAQuery query) {
		throw new UnsupportedOperationException("Unsupported");
	}
}
