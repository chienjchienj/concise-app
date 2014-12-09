package org.sustudio.concise.app.gear.dictionaryEditor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.dialog.CAOpenFilesDialog;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.core.corpus.importer.ConciseFileUtils;

public class DictionaryTable extends Composite {

	final Group group;
	final Table table;
	final ToolItem tltmNew;
	final ToolItem tltmEdit;
	final ToolItem tltmLoad;
	final ToolItem tltmDelete;
	
	public DictionaryTable(Composite parent, int style, final DictionaryEditor editor) {
		super(parent, style);
		setLayout(new GridLayout());
		
		group = new Group(this, SWT.BORDER);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setLayout(new GridLayout());
		
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
				tltmEdit.setEnabled(table.getSelectionCount() > 0);
				tltmDelete.setEnabled(table.getSelectionCount() > 0);
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent event) {
				if (table.getSelectionCount() == 1) {
					File dicFile = new File(Concise.getCurrentWorkspace().getDictionaryDir(), table.getSelection()[0].getText());
					editor.showDicContent(dicFile);
				} 
			}
		});
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(true);
		
		Listener listener = new HoveredListener();
		table.addListener(SWT.Dispose, listener);
		table.addListener(SWT.MouseExit, listener);
		table.addListener(SWT.MouseMove, listener);
		
		table.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				event.height = 40;
				event.width = table.getBounds().width;
			}
		});
		
		final ToolBar toolBar = new ToolBar(group, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		tltmNew = new ToolItem(toolBar, SWT.NONE);
		tltmNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newDictionary();
			}
		});
		tltmNew.setText(CABundle.get("toolbar.new"));
		
		tltmEdit = new ToolItem(toolBar, SWT.NONE);
		tltmEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionCount() == 1) {
					File dicFile = new File(Concise.getCurrentWorkspace().getDictionaryDir(), table.getSelection()[0].getText());
					editor.showDicContent(dicFile);
				} 
			}
		});
		tltmEdit.setText("Edit");
		tltmEdit.setEnabled(false);
		
		new ToolItem(toolBar, SWT.SEPARATOR_FILL);
		
		tltmLoad = new ToolItem(toolBar, SWT.NONE);
		tltmLoad.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loadDictionary();
			}
		});
		tltmLoad.setText(CABundle.get("toolbar.load"));
		
		tltmDelete = new ToolItem(toolBar, SWT.NONE);
		tltmDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeDictionary();
			}
		});
		tltmDelete.setText(CABundle.get("toolbar.delete"));
		tltmDelete.setEnabled(false);
		
		
		// read dic files
		for (File file : Concise.getCurrentWorkspace().getDictionaryFiles()) {
			new TableItem(table, SWT.NONE).setText(file.getName());
		}
	}
	
	
	public void setText(String string) {
		group.setText(string);
	}

	
	private void newDictionary() {
		
		// popup a dialog to enter filename
		
		GridLayout layout = new GridLayout(2, false);
		layout.marginTop = 20;
		layout.marginBottom = 30;
		layout.marginLeft = 30;
		layout.marginRight = 30;
		
		final Shell sh = new Shell(getShell(), SWT.SHEET);
		sh.setLayout(layout);
		
		Image conciseIcon = new Image(getDisplay(), getClass().getResourceAsStream("/org/sustudio/concise/app/icon/concise.png"));
		Label lblIcon = new Label(sh, SWT.NONE);
		lblIcon.setImage(conciseIcon);
		lblIcon.setLayoutData(new GridData(GridData.CENTER, GridData.FILL, false, true, 1, 2));
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		final Group txtGroup = new Group(sh, SWT.SHADOW_ETCHED_IN);
		txtGroup.setText("New Dictionary");
		txtGroup.setLayoutData(gd);
		txtGroup.setLayout(new GridLayout(2, false));
		new Label(txtGroup, SWT.NONE).setText("Filename:");;
		Text dicName = new Text(txtGroup, SWT.BORDER);
		dicName.setText("MyDictionary.dic");
		dicName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button btnCreate = new Button(sh, SWT.NONE);
		btnCreate.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		btnCreate.setText("Create");
		btnCreate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				
				// create an empty text file
				String filename = dicName.getText().trim();
				if (!filename.isEmpty()) {
					if (!filename.endsWith(".dic")) {
						filename += ".dic";
					}
					File dicFile = new File(Concise.getCurrentWorkspace().getDictionaryDir(), filename);
					try {
						dicFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					// add item
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(filename);
					
					CAPrefs.userDictionaries = Concise.getCurrentWorkspace().getDictionaryFiles();
				}
				
				sh.close();
			}
		});
		
		sh.setDefaultButton(btnCreate);
		sh.pack();
		sh.open();
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
			targetFile = ConciseFileUtils.getUniqueFile(targetFile);
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
		}
	}
	
	
	public class HoveredListener implements Listener {

		final int IMAGE_MARGIN = 2;
		
		private Color bg = new Color(Display.getDefault(), 255, 225, 200);
		private TableItem hoveredItem;
		
		public void setBackground(Color color) {
			this.bg = color;
		}
		
		@Override
		public void handleEvent(Event event) {
			if (!(event.widget instanceof Table)) {
				return;	// only works with Table
			}
			
			final Table table = (Table) event.widget;
			switch (event.type) {
			case SWT.Dispose:
			case SWT.MouseExit:
				if (hoveredItem != null) {
					hoveredItem.setBackground(null);
					hoveredItem = null;
				}
				break;
				
			case SWT.MouseMove:
				Point point = new Point(event.x, event.y);
				TableItem item = table.getItem(point);
				if (item == null && hoveredItem != null) {
					hoveredItem.setBackground(null);
					hoveredItem = null;
				}
				else if (item != null && !item.equals(hoveredItem)) {
					if (hoveredItem != null) {
						hoveredItem.setBackground(null);
					}
					hoveredItem = item;
					hoveredItem.setBackground(bg);
				}
				//System.out.println(point);
				break;
			}
		}
	}
}
