package org.sustudio.concise.app.gear.dictionaryEditor;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.mihalis.opal.obutton.OButton;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.WordClusterTransfer;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.cluster.Cluster;

public class DictionaryContent extends Composite {

	final Label lblFile;
	private List wordsList;
	
	public DictionaryContent(Composite parent, int style, final DictionaryEditor editor) {
		super(parent, style);
		setLayout(new GridLayout());
		
		// create nav bar
		Composite navBar = new Composite(this, SWT.EMBEDDED);
		navBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		navBar.setLayout(new GridLayout(3, false));
		
		OButton btnPrev = new OButton(navBar, SWT.ARROW | SWT.LEFT);
		btnPrev.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// TODO save content
				try {
					FileUtils.writeLines(
							editor.getWorkingDicFile(), 
							Arrays.asList(wordsList.getItems()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				editor.showDicTable();
			}
		});
		btnPrev.setButtonRenderer(OButtonRenderer.getInstance());
		
		lblFile = new Label(navBar, SWT.NONE);
		lblFile.setText(editor.getWorkingDicFile().getName());
		lblFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		OButton btnSave = new OButton(navBar, SWT.PUSH);
		btnSave.setText("Save");
		btnSave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					FileUtils.writeLines(
							editor.getWorkingDicFile(), 
							Arrays.asList(wordsList.getItems()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnSave.setButtonRenderer(OButtonRenderer.getInstance());
		
		
		Composite comp = new Composite(this, SWT.EMBEDDED);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout());
		
		Group grpWords = new Group(comp, SWT.NONE);
		grpWords.setLayout(new GridLayout(2, false));
		grpWords.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpWords.setText("Words");
		
		Label lblAdd = new Label(grpWords, SWT.NONE);
		lblAdd.setText("Add word:");
		lblAdd.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		
		final Text text = new Text(grpWords, SWT.BORDER | SWT.SINGLE);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				String word = text.getText().trim();
				if (!word.isEmpty() && word.indexOf(' ') == -1) {
					addWord(word);
					text.selectAll();
				}
			}
		});
		CopyPasteHelper.listenTo(text);
		
		wordsList = new List(grpWords, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		wordsList.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (wordsList.getSelectionCount() < 1)
					return;
				if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
					if (Dialog.isConfirmed("Do you want to remove "+wordsList.getSelectionCount()+" words?", "")) {
						wordsList.remove(wordsList.getSelectionIndices());
					}	
				}
			}
		});
		wordsList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		if (CCPrefs.stopWords != null) {
			wordsList.setItems(CCPrefs.stopWords);
		}
		CopyPasteHelper.listenTo(wordsList);
		
		
		// add drop target
		final DropTarget dropTarget = new DropTarget(wordsList , DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		dropTarget.setTransfer(new Transfer[] { WordClusterTransfer.getInstance(), TextTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {

			@Override
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_COPY) != 0 ? DND.DROP_COPY : DND.DROP_NONE;
				}
				
				// Allow dropping Cluster or text
				for (int i = 0, n = event.dataTypes.length; i < n; i++) {
					if (WordClusterTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
					}
					else if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
					}
				}
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (event.data instanceof Cluster[]) {
					for (Cluster cluster : (Cluster[]) event.data) {
						String word = cluster.getWord().replace(" ", "");
						addWord(word);
					}
				}
				else if (event.data instanceof String) {
					String[] words = ((String) event.data).split(System.getProperty("line.separator"));
					for (String word : words) {
						word = word.replace(" ", "");
						addWord(word);
					}
				}
			}
			
		});
		
		
		// TODO read content
		try {
			wordsList.setItems(FileUtils.readLines(editor.getWorkingDicFile()).toArray(new String[0]));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	
	private void addWord(final String word) {
		if (word.trim().equals("")) return;
		int index = wordsList.indexOf(word);
		if (index == -1) {
			wordsList.add(word);
			index = wordsList.indexOf(word);
		}
		wordsList.setSelection(index);
		wordsList.showSelection();
	}
	

}
