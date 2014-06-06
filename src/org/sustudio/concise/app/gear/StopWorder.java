package org.sustudio.concise.app.gear;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.opalDialog.Dialog;
import org.mihalis.opal.promptSupport.PromptSupport;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.dialog.CAOpenFilesDialog;
import org.sustudio.concise.app.dialog.CASaveFileDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CAAutoCompleteText;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.wordlister.Word;

public class StopWorder extends GearController {

	private CAAutoCompleteText stopWord;
	private List wordsList;
	
	public StopWorder() {
		super(CABox.ToolBox, Gear.StopWorder);
		
	}

	@Override
	protected Control createControl() {
		Composite comp = new Composite(this, SWT.EMBEDDED);
		comp.setLayout(new GridLayout(3, false));
		
		Label lblAdd = new Label(comp, SWT.NONE);
		lblAdd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAdd.setText("Add Stop:");
		
		stopWord = new CAAutoCompleteText(comp, SWT.SINGLE | SWT.BORDER);
		stopWord.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				if (stopWord.getText().trim() != "") {
					addWord(stopWord.getText().trim());
				}
			}
		});
		stopWord.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		PromptSupport.setPrompt("add stop word", stopWord);
		stopWord.setFocus();
		CopyPasteHelper.listenTo(stopWord);
		try {
			stopWord.setIndexReader(Concise.getCurrentWorkspace().getIndexReader());
		} catch (IOException e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
		
		ToolBar toolBar = new ToolBar(comp, SWT.SMOOTH);
		
		ToolItem tltmOptions = new ToolItem(toolBar, SWT.DROP_DOWN);
		tltmOptions.setToolTipText("Options");
		tltmOptions.setImage(SWTResourceManager.getImage(StopWorder.class, "/org/sustudio/concise/app/icon/19-gear.png"));
		setDropDownMenu(tltmOptions);
		
		Button stopWordCheckBox = new Button(comp, SWT.CHECK);
		stopWordCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		stopWordCheckBox.setText("Enable Stop Words");
		stopWordCheckBox.setSelection(CCPrefs.STOP_WORDS_ENABLED);
		stopWordCheckBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CCPrefs.STOP_WORDS_ENABLED = !CCPrefs.STOP_WORDS_ENABLED;
				((Button) event.widget).setSelection(CCPrefs.STOP_WORDS_ENABLED);
			}
		});
		
		Group grpWords = new Group(comp, SWT.NONE);
		grpWords.setLayout(new GridLayout(1, false));
		grpWords.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		grpWords.setText("Search Words");
		
		wordsList = new List(grpWords, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		wordsList.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (wordsList.getSelectionCount() < 1)
					return;
				if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
					if (Dialog.isConfirmed("Do you want to remove "+wordsList.getSelectionCount()+" words?", "")) {
						wordsList.remove(wordsList.getSelectionIndices());
						renewLabel();
					}	
				}
			}
		});
		wordsList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		if (CCPrefs.stopWords != null) {
			wordsList.setItems(CCPrefs.stopWords);
		}
		CopyPasteHelper.listenTo(wordsList);
		
		
		// add drop target
		final DropTarget dropTarget = new DropTarget(wordsList , DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		dropTarget.setTransfer(new Transfer[] { CCWordsTransfer.getInstance(), TextTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {

			@Override
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_COPY) != 0 ? DND.DROP_COPY : DND.DROP_NONE;
				}
				
				// Allow dropping CCWord or text
				for (int i = 0, n = event.dataTypes.length; i < n; i++) {
					if (CCWordsTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
					}
					else if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
					}
				}
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (event.data instanceof Word[]) {
					for (Word word : (Word[]) event.data) {
						addWord(word.getWord());
					}
				}
				else if (event.data instanceof String) {
					String[] words = ((String) event.data).split(System.getProperty("line.separator"));
					for (String word : words) {
						addWord(word);
					}
				}
			}
			
		});
		
		return comp;
	}
	
	protected void setCopyPasteHelper() {
		CopyPasteHelper.listenTo(stopWord);
		CopyPasteHelper.listenTo(wordsList);
	}

	
	private void renewLabel() {
		setStatusText(Formats.getNumberFormat(wordsList.getItemCount())+" Stop Words");
		CCPrefs.stopWords = wordsList.getItems();
	}
	
	private void addWord(final String word) {
		if (word.trim().equals("")) return;
		int index = wordsList.indexOf(word);
		if (index == -1) {
			wordsList.add(word);
			index = wordsList.indexOf(word);
			renewLabel();
		}
		wordsList.setSelection(index);
		wordsList.showSelection();
		stopWord.selectAll();
	}
	
	
	/**
	 * Set up options menu.
	 * @param item 	option icon.
	 */
	private void setDropDownMenu(final ToolItem item) {
		final Menu menu = new Menu(Display.getDefault().getActiveShell(), SWT.POP_UP);
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Rectangle rect = item.getBounds();
				Point pt = new Point(rect.x, rect.y + rect.height);
				pt = item.getParent().toDisplay(pt);
				menu.setLocation(pt.x, pt.y);
				menu.setVisible(true);
			}
		});
		
		final MenuItem loadItem = new MenuItem(menu, SWT.PUSH);
		loadItem.setText("Load from text file");
		loadItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CAOpenFilesDialog dlg = new CAOpenFilesDialog();
				dlg.setOpenTextFileConfigure();
				if (dlg.open() != null) {
					for (String filename : dlg.getFileNames()) {
						try {
							BufferedReader reader = new BufferedReader(
														new Tika().parse(new FileInputStream(filename)));
							String line;
							while ( (line=reader.readLine()) != null) {
								addWord(line.trim());
							}
							reader.close();
						} catch (Exception e) {
							workspace.logError(getGear(), e);
							Dialog.showException(e);
						}
					}
				}
			}
		});
		
		final MenuItem saveItem = new MenuItem(menu, SWT.PUSH);
		saveItem.setText("Save to text file");
		saveItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (wordsList.getItemCount() > 0) {
					CASaveFileDialog dlg = new CASaveFileDialog();
					dlg.setTextFileConfigure();
					String filename = dlg.open();
					if (filename != null) {
						StringBuilder sb = new StringBuilder();
						for (String word: wordsList.getItems()) {
							sb.append(word+"\r\n");
						}
						try {
							
							FileUtils.write(new File(filename), sb.toString(), "UTF-8");
							
						} catch (IOException exception) {
							CAErrorMessageDialog.open(getGear(), exception);
						}
						sb.setLength(0);
						sb = null;
					}
				}
				else {
					Dialog.inform("NO Entries!", "Enter something ...");
				}
			}
		});
		
		final MenuItem clearItem = new MenuItem(menu, SWT.PUSH);
		clearItem.setText("Clear Words List");
		clearItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (wordsList.getItemCount() > 0) {
					if (Dialog.isConfirmed("Do you want to clear all Words?", "")) {
						wordsList.removeAll();
						renewLabel();
					}
				}
			}
		});
		
		menu.addMenuListener(new MenuAdapter() {

			@Override
			public void menuShown(MenuEvent arg0) {
				boolean enabled = wordsList.getItemCount() > 0;
				saveItem.setEnabled(enabled);
				clearItem.setEnabled(enabled);
			}
			
		});
	}
}
