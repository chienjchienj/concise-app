package org.sustudio.concise.app.dialog;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.Workspace;
import org.sustudio.concise.app.enums.CorpusManipulation;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.thread.CAImportThread;
import org.sustudio.concise.core.corpus.importer.AnalyzerEnum;
import org.sustudio.concise.core.corpus.importer.ConciseFileUtils;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Link;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 輸入文件的對話視窗
 * 
 * @author Kuan-ming Su
 *
 */
public class ImportDialog extends Shell {
	
	public static void open(Workspace.INDEX index) {
		new ImportDialog(index).open();
	}
	
	private final ArrayList<String> files = new ArrayList<String>();
	
	private final Table tblDoc;
	private final Combo comboCorpus;
	private final Button btnTokenize;
	private final Button btnImport;
	
	private Group analyzerGroup;
	private Combo comboAnalyzer;
	private Table tblDic;
	private Group posGroup;
	private Button btnPOSDefaultModel;
	private Text txtPOSModel;
	private Text txtPOSSeparator;
	
	
	public ImportDialog() {
		this(Workspace.INDEX.DOCUMENT);
	}
	
	public ImportDialog(Workspace.INDEX index) {
		super(Concise.getActiveApp(), SWT.SHEET | SWT.RESIZE);
		
		setSize(557, 454);
		setText("Import Documents");
		setLayout(new GridLayout(2, false));
		setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.NORMAL));
		
		Group grpDocuments = new Group(this, SWT.NONE);
		grpDocuments.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpDocuments.setText("Documents");
		GridLayout gl_grpDocuments = new GridLayout(1, false);
		gl_grpDocuments.verticalSpacing = 0;
		gl_grpDocuments.marginWidth = 0;
		gl_grpDocuments.marginHeight = 0;
		gl_grpDocuments.horizontalSpacing = 0;
		grpDocuments.setLayout(gl_grpDocuments);
		
		tblDoc = new Table(grpDocuments, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		tblDoc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tblDoc.setLinesVisible(true);
		tblDoc.setFont(getFont());
		
		ToolBar toolBarDoc = new ToolBar(grpDocuments, SWT.FLAT | SWT.RIGHT);
		
		final ToolItem tltmAddDoc = new ToolItem(toolBarDoc, SWT.NONE);
		tltmAddDoc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// load doc
				CAOpenFilesDialog fd = new CAOpenFilesDialog();
				fd.setOpenCorpusCongifure();
				if (fd.open() == null) {
					return;
				}
				for (String file : fd.getFileNames()) {
					if (!files.contains(file)) {
						files.add(file);
					}
				}
				fd = null;
				tblDoc.removeAll();
				tblDoc.setItemCount(files.size());
				
				btnImport.setEnabled(tblDoc.getItemCount() > 0);
			}
		});
		tltmAddDoc.setText("Add");
		
		final ToolItem tltmRemoveDoc = new ToolItem(toolBarDoc, SWT.NONE);
		tltmRemoveDoc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				tblDoc.remove(tblDoc.getSelectionIndices());
			}
		});
		tltmRemoveDoc.setEnabled(false);
		tltmRemoveDoc.setText("Remove");
		
		tblDoc.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (tblDoc.getSelectionCount() < 1) return;
				if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
					tblDoc.remove(tblDoc.getSelectionIndices());
				}
			}
		});
		tblDoc.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tltmRemoveDoc.setEnabled(tblDoc.getSelectionCount() > 0);
			}
		});
		tblDoc.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				item.setText(files.get(index));
			}
		});
		tblDoc.setItemCount(files.size());
		
		
		
		////////////////////////////////////////////
		// options
		////////////////////////////////////////////
		
		final Group grpOptions = new Group(this, SWT.NONE);
		grpOptions.setLayout(new GridLayout(1, false));
		grpOptions.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2));
		grpOptions.setText("Options");
		
		comboCorpus = new Combo(grpOptions, SWT.READ_ONLY);
		comboCorpus.setItems(new String[] {"Corupus", "Reference Corpus"});
		comboCorpus.select(0);
		comboCorpus.setFont(getFont());
		
		btnTokenize = new Button(grpOptions, SWT.CHECK);
		btnTokenize.setSelection(true);
		btnTokenize.setText("Tokenize");
		btnTokenize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (analyzerGroup != null && !analyzerGroup.isDisposed()) {
					analyzerGroup.setEnabled(btnTokenize.getSelection());
					analyzerGroup.setVisible(btnTokenize.getSelection());
				}
				if (posGroup != null && !posGroup.isDisposed()) {
					posGroup.setEnabled(btnTokenize.getSelection());
					posGroup.setVisible(btnTokenize.getSelection());
				}
			}
		});
		btnTokenize.setFont(getFont());
		
		
		final Link showMoreOptions = new Link(grpOptions, SWT.NONE);
		showMoreOptions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showMoreOptions.dispose();
				analyzerGroup = getAnalyzerGroup(grpOptions);
				posGroup = getPartOfSpeechGroup(grpOptions);
				ImportDialog.this.layout();
			}
		});
		showMoreOptions.setText("<a>Show more options...</a>");
		showMoreOptions.setFont(getFont());
		
		Composite compFooter = new Composite(this, SWT.NONE);
		compFooter.setLayout(new GridLayout(2, false));
		
		btnImport = new Button(compFooter, SWT.NONE);
		btnImport.setText("Import");
		btnImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				importDocs();
			}
		});
		btnImport.setEnabled(false);
		
		final Button btnCancel = new Button(compFooter, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ImportDialog.this.close();
			}
		});
		btnCancel.setText("Cancel");
		
		setDefaultButton(btnImport);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
	}
	
	/**
	 * return analyzer group
	 * @param composite
	 * @return analyzer group
	 */
	private Group getAnalyzerGroup(Composite composite) {
		
		final Group grpAnalyzer = new Group(composite, SWT.NONE);
		grpAnalyzer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		grpAnalyzer.setText("Analyzer");
		grpAnalyzer.setLayout(new GridLayout(1, false));
		
		// analyzer controls layout
		comboAnalyzer = new Combo(grpAnalyzer, SWT.READ_ONLY);
		final Group grpMmsegDictionary = new Group(grpAnalyzer, SWT.NONE);
		tblDic = new Table(grpMmsegDictionary, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		
		final ToolBar toolBar = new ToolBar(grpMmsegDictionary, SWT.FLAT | SWT.RIGHT);
		final ToolItem tltmLoad = new ToolItem(toolBar, SWT.NONE);
		final ToolItem tltmDel = new ToolItem(toolBar, SWT.NONE);
		
		// existing dic files
		for (File f : Concise.getCurrentWorkspace().getDictionaryDir().listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && !file.isHidden();
			}
		})) {
			
			TableItem item = new TableItem(tblDic, SWT.NONE);
			item.setText(f.getName());
		}
		
		
		// analyzer controls settings
		comboAnalyzer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		comboAnalyzer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String analyzer = comboAnalyzer.getText();
				tblDic.setEnabled(analyzer.startsWith("MMSeg "));
				tltmLoad.setEnabled(analyzer.startsWith("MMSeg "));
			}
		});
		comboAnalyzer.setItems(new String[] {"MMSeg Complex", "MMSeg Simple", "Smart Chinese (Simplified Chinese)", "Whitespace"});
		comboAnalyzer.setText("MMSeg Complex");
		comboAnalyzer.setFont(getFont());
		
		
		GridLayout gl_grpMmsegDictionary = new GridLayout(1, false);
		gl_grpMmsegDictionary.verticalSpacing = 0;
		gl_grpMmsegDictionary.marginWidth = 0;
		gl_grpMmsegDictionary.marginHeight = 0;
		gl_grpMmsegDictionary.horizontalSpacing = 0;
		grpMmsegDictionary.setLayout(gl_grpMmsegDictionary);
		grpMmsegDictionary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpMmsegDictionary.setText("MMSeg Dictionary");
		
		tblDic.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (tblDic.getSelectionCount() < 1) return;
				if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
					if (removeDictionary()) {	
						tltmDel.setEnabled(false);
					}
				}
			}
		});
		tblDic.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tltmDel.setEnabled(tblDic.getSelectionCount() > 0);
			}
		});
		tblDic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tblDic.setLinesVisible(true);
		tblDic.setEnabled(CAPrefs.rawDocAnalyzer.label().startsWith("MMSeg"));
		tblDic.setFont(getFont());
		tblDic.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent event) {
				if (tblDic.getSelectionCount() == 1) {
					final Program program = Program.findProgram("txt");
					File dicFile = new File(Concise.getCurrentWorkspace().getDictionaryDir(), tblDic.getSelection()[0].getText());
					program.execute(dicFile.getPath());
				}
			}
		});
		
		tltmLoad.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
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
					targetFile = ConciseFileUtils.getUniqueFile(targetFile);
					try {
						FileUtils.copyFile(file, targetFile, true);
					} catch (IOException e) {
						Concise.getCurrentWorkspace().logError(null, e);
						Dialog.showException(e);
					}
					
					// add item
					TableItem item = new TableItem(tblDic, SWT.NONE);
					item.setText(targetFile.getName());
				}
				fd = null;
			}
		});
		tltmLoad.setText(CABundle.get("toolbar.load"));
		tltmLoad.setEnabled(CAPrefs.rawDocAnalyzer.label().startsWith("MMSeg"));
		
		tltmDel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (removeDictionary()) {
					tltmDel.setEnabled(false);
				}
			}
		});
		tltmDel.setText(CABundle.get("toolbar.delete"));
		tltmDel.setEnabled(false);
		 
		return grpAnalyzer;
	}
	
	/**
	 * 移除MMSeg的自訂詞典
	 */
	private boolean removeDictionary() {
		
		if (Dialog.isConfirmed("Delete Dictionaries?", "Do you want to remove selected dictionaries?")) 
		{
			for (int index : tblDic.getSelectionIndices()) 
			{
				File file = new File(Concise.getCurrentWorkspace().getDictionaryDir(), 
									 tblDic.getItem(index).getText());
				file.delete();
			}
			tblDic.remove(tblDic.getSelectionIndices());
			return true;
		}
		return false;
	}
	
	
	/**
	 * 詞性標注選項的 Group
	 * @param parent 
	 * @return 詞性標注選項的 Group
	 */
	private Group getPartOfSpeechGroup(Composite parent) {
		Group grpPartofspeechTagging = new Group(parent, SWT.NONE);
		grpPartofspeechTagging.setLayout(new GridLayout(2, false));
		grpPartofspeechTagging.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpPartofspeechTagging.setText("Part-of-speech Tagger");
		
		btnPOSDefaultModel = new Button(grpPartofspeechTagging, SWT.CHECK);
		new Label(grpPartofspeechTagging, SWT.NONE);
		txtPOSModel = new Text(grpPartofspeechTagging, SWT.BORDER);
		final Button btnBrowseModel = new Button(grpPartofspeechTagging, SWT.NONE);
		Composite comp = new Composite(grpPartofspeechTagging, SWT.EMBEDDED);
		Label lblSeparator = new Label(comp, SWT.NONE);
		txtPOSSeparator = new Text(comp, SWT.BORDER);
		
		btnPOSDefaultModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = !btnPOSDefaultModel.getSelection();
				txtPOSModel.setEnabled(enabled);
				btnBrowseModel.setEnabled(enabled);
				txtPOSSeparator.setEnabled(enabled);
			}
		});
		btnPOSDefaultModel.setSelection(true);
		btnPOSDefaultModel.setText("Use Default Model");
		btnPOSDefaultModel.setFont(getFont());
		
		txtPOSModel.setEnabled(!btnPOSDefaultModel.getSelection());
		txtPOSModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtPOSModel.setEditable(false);
		txtPOSModel.setFont(getFont());
		
		btnBrowseModel.setEnabled(!btnPOSDefaultModel.getSelection());
		btnBrowseModel.setText("Browse");
		btnBrowseModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				CAOpenFilesDialog fd = new CAOpenFilesDialog(true);
				fd.setOpenPOSTaggerConfigure();
				String filepath = fd.open();
				if (filepath != null) {
					txtPOSModel.setText(filepath);
				}
			}
		});
		btnBrowseModel.setFont(getFont());
		
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 0;
		comp.setLayout(gl_composite);
		
		lblSeparator.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSeparator.setText("Separator:");
		lblSeparator.setFont(getFont());
		
		txtPOSSeparator.setEnabled(!btnPOSDefaultModel.getSelection());
		txtPOSSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtPOSSeparator.setFont(getFont());
		
		new Label(grpPartofspeechTagging, SWT.NONE);
		
		return grpPartofspeechTagging;
	}
	
	
	/**
	 * import docs
	 */
	protected void importDocs() {
		// check docs
		if (tblDoc.getItemCount() == 0) {
			return;
		}
		
		// define corpus manipulation
		boolean corpus = comboCorpus.getSelectionIndex() == 0;
		boolean tokenized = !btnTokenize.getSelection();
		CorpusManipulation manipulation;
		if (corpus && !tokenized)
			manipulation = CorpusManipulation.ImportDocuments;
		else if (corpus && tokenized)
			manipulation = CorpusManipulation.ImportTokenizedDocuments;
		else if (!corpus && !tokenized)
			manipulation = CorpusManipulation.ImportReferenceDocuments;
		else
			manipulation = CorpusManipulation.ImportTokenizedReferenceDocuments;
		
		Concise.getCurrentWorkspace().logInfo(manipulation.label());
		
		// settings
		CAPrefs.userDictionaries = Concise.getCurrentWorkspace().getDictionaryFiles();
		CAPrefs.POS_TAGGER_MODEL = CAPrefs.DEFAULT_POS_TAGGER_MODEL;
		CAPrefs.POS_TAGGER_SEPARATOR = CAPrefs.DEFAULT_POS_TAGGER_SEPARATOR;
		if (!tokenized && analyzerGroup != null && posGroup != null) {
			CAPrefs.rawDocAnalyzer = AnalyzerEnum.valueOfLabel(comboAnalyzer.getText());
			if (!btnPOSDefaultModel.getSelection()) {
				CAPrefs.POS_TAGGER_MODEL = txtPOSModel.getText();
				CAPrefs.POS_TAGGER_SEPARATOR = txtPOSSeparator.getText().trim();
			}
		}		
		
		File[] files = new File[tblDoc.getItemCount()];
		for (int i = 0; i < files.length; i++) {
			files[i] = new File(tblDoc.getItem(i).getText());
		}
		
		CAImportThread thread = new CAImportThread(manipulation);
		thread.setFiles(files);
		thread.start();
		
		close();
	}
	
	
	public void close() {
		files.clear();
		super.close();
	}
	
	
	public void checkSubclass() {
		// disable sub-class check
	}
	
}