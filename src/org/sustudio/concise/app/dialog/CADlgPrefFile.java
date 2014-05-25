package org.sustudio.concise.app.dialog;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.utils.CAFileUtils;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.corpus.importer.AnalyzerEnum;

public class CADlgPrefFile extends Composite {
	
	private Table table;
	private ToolItem tltmLoad;
	private ToolItem tltmDelete;

	public CADlgPrefFile(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 10;
		gridLayout.marginWidth = 30;
		gridLayout.verticalSpacing = 10;
		setLayout(gridLayout);
		
		Label lblAnalyzer = new Label(this, SWT.NONE);
		lblAnalyzer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblAnalyzer.setText(CABundle.get("preferences.file.analyzer") + ":");
		
		final Combo comboAnalyzer = new Combo(this, SWT.READ_ONLY);
		comboAnalyzer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboAnalyzer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				String analyzer = comboAnalyzer.getText();
				if (!analyzer.equals(CCPrefs.rawDocAnalyzer.label())) {
					CADlgPrefFile.this.getShell().setModified(true);
					getShell().setData("ReTokenize", true);
				}
				
				CCPrefs.rawDocAnalyzer = AnalyzerEnum.valueOfLabel(analyzer);
				switch (CCPrefs.rawDocAnalyzer) {
				case MMSegComplex:
				case MMSegSimple:
					table.setEnabled(true);
					tltmLoad.setEnabled(true);
					break;
				default:
					table.setEnabled(false);
					tltmLoad.setEnabled(false);
					break;
				}
			}
		});
		comboAnalyzer.setItems(AnalyzerEnum.labels());
		comboAnalyzer.setText(CCPrefs.rawDocAnalyzer.label());
		
		Label lblCustomDictionaries = new Label(this, SWT.NONE);
		lblCustomDictionaries.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		lblCustomDictionaries.setAlignment(SWT.RIGHT);
		lblCustomDictionaries.setText(CABundle.get("preferences.file.mmsegCustomDictionaries") + ":");
		
		Group group = new Group(this, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new GridLayout(1, false));
		
		table = new Table(group, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (table.getSelectionCount() < 1) return;
				if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
					removeDictionary();	
				}
			}
		});
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tltmDelete.setEnabled(table.getSelectionCount() > 0);
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent event) {
				if (table.getSelectionCount() == 1) {
					final Program program = Program.findProgram("txt");
					File dicFile = new File(Concise.getCurrentWorkspace().getDictionaryDir(), table.getSelection()[0].getText());
					program.execute(dicFile.getPath());
				}
			}
		});
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setEnabled(CCPrefs.rawDocAnalyzer.label().startsWith("MMSeg"));
		
		ToolBar toolBar = new ToolBar(group, SWT.FLAT | SWT.RIGHT);
		
		tltmLoad = new ToolItem(toolBar, SWT.NONE);
		tltmLoad.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loadDictionary();
			}
		});
		tltmLoad.setText(CABundle.get("toolbar.load"));
		tltmLoad.setEnabled(CCPrefs.rawDocAnalyzer.label().startsWith("MMSeg"));
		
		tltmDelete = new ToolItem(toolBar, SWT.NONE);
		tltmDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeDictionary();
			}
		});
		tltmDelete.setText(CABundle.get("toolbar.delete"));
		tltmDelete.setEnabled(false);
		
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		// existing dic files
		for (File f : Concise.getCurrentWorkspace().getDictionaryDir().listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && !file.isHidden();
			}
		})) {
			
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(f.getName());
		}
	}

	private void loadDictionary() {
		CAOpenFilesDialog fd = new CAOpenFilesDialog();
		fd.setOpenDictionaryCongifure();
		if (fd.open() == null) {
			return;
		}
		String[] files = fd.getFileNames();
		
		for (String f : files) {
			// copy dictionary files to concise workspace
			File file = new File(f);
			File targetFile = new File(Concise.getCurrentWorkspace().getDictionaryDir(), file.getName());
			targetFile = CAFileUtils.getUniqueFile(targetFile);
			try {
				FileUtils.copyFile(file, targetFile, true);
			} catch (IOException e) {
				Concise.getCurrentWorkspace().logError(null, e);
				Dialog.showException(e);
			}
			
			// add item
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(targetFile.getName());
		}
		fd = null;
		
		CAPrefs.userDictionaries = Concise.getCurrentWorkspace().getDictionaryFiles();
		
		// Notify Shell to make re-calculation
		CADlgPrefFile.this.getShell().setModified(true);
		getShell().setData("ReTokenize", true);
	}
	
	private void removeDictionary() {
		if (Dialog.isConfirmed("Delete Dictionaries?", "Do you want to remove selected dictionaries?")) 
		{
			for (int index : table.getSelectionIndices()) 
			{
				File file = new File(Concise.getCurrentWorkspace().getDictionaryDir(), 
									 table.getItem(index).getText());
				file.delete();
			}
			table.remove(table.getSelectionIndices());
			tltmDelete.setEnabled(false);
			
			CAPrefs.userDictionaries = Concise.getCurrentWorkspace().getDictionaryFiles();
			
			// Notify Shell to make re-calculation
			getShell().setModified(true);
			getShell().setData("ReTokenize", true);
		}
	}
}
