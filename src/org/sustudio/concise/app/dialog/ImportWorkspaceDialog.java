package org.sustudio.concise.app.dialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.infinitePanel.InfiniteProgressPanel;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.CAConfig;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.RecentWorkspaces;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.app.thread.CAImportWorkspaceThread;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ImportWorkspaceDialog extends Shell {
	
	public static void openDialog(Gear gear) {
		ImportWorkspaceDialog dlg = new ImportWorkspaceDialog(gear);
		dlg.open();
	}
	
	private class Doc {
		ConciseDocument doc;
		boolean checked = false;
	}
	
	private final ArrayList<Doc> documents = new ArrayList<Doc>();
	private final Combo combo;
	private final Combo comboCorpus;
	private final Table table;
	
	public ImportWorkspaceDialog(final Gear gear) {
		super(Concise.getActiveApp(), SWT.SHEET | SWT.RESIZE);
		setSize(500, 400);
		setLayout(new GridLayout(3, false));
		
		Label lblWorkspace = new Label(this, SWT.NONE);
		lblWorkspace.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblWorkspace.setText("Workspace:");
		
		combo = new Combo(this, SWT.READ_ONLY);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// show table content
				showDocuments(combo.getText().trim());
			}
		});
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo.setItems(RecentWorkspaces.getRecentWorkspacePaths());
		if (combo.getItemCount() > 1) {
			combo.select(1);
		}
		
		Button btnBrowse = new Button(this, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 瀏覽要開啟的檔案
				CAOpenFilesDialog dlg = new CAOpenFilesDialog(true);
				dlg.setWorkspaceConfigure();
				String filename = dlg.open();
				if (filename != null && 
					new File(filename).exists()) 
				{
					filename = FilenameUtils.removeExtension(filename);
					combo.setItems(ArrayUtils.add(combo.getItems(), 0, filename));
					combo.select(0);
					
					// show table content
					showDocuments(combo.getText().trim());
				}
			}
		});
		btnBrowse.setText("Browse");
		
		Group grpDocuments = new Group(this, SWT.NONE);
		grpDocuments.setLayout(new GridLayout(1, false));
		grpDocuments.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		grpDocuments.setText("Documents");
		grpDocuments.setBounds(0, 0, 78, 78);
		
		table = new Table(grpDocuments, SWT.BORDER | SWT.CHECK | SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int index = event.index;
				if (event.detail == SWT.CHECK) {
					documents.get(index).checked = !documents.get(index).checked;
					table.clear(index);
				}
			}
		});
		table.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event event) {
				switch (event.keyCode) {
				case SWT.SPACE:
					if (table.getSelectionCount() > 0) {
						// 用第一個checked狀態設定其他的選擇
						boolean checked = !documents.get(table.getSelectionIndex()).checked;
						for (int index : table.getSelectionIndices()) {
							documents.get(index).checked = checked;
							table.clear(index);
						}
					}
					break;
				}
			}
		});
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				item.setText(documents.get(index).doc.filename);
				item.setChecked(documents.get(index).checked);
			}
		});
		CopyPasteHelper.listenTo(table);
		
		ToolBar toolBar = new ToolBar(grpDocuments, SWT.FLAT | SWT.RIGHT);
		
		ToolItem tltmSelectAll = new ToolItem(toolBar, SWT.NONE);
		tltmSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				for (Doc d : documents) {
					d.checked = true;
				}
				table.clearAll();
			}
		});
		tltmSelectAll.setText("Select All");
		
		ToolItem tltmClearAll = new ToolItem(toolBar, SWT.NONE);
		tltmClearAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (Doc d : documents) {
					d.checked = false;
				}
				table.clearAll();
			}
		});
		tltmClearAll.setText("Clear Selection");
		
		Label lblDest = new Label(this, SWT.NONE);
		lblDest.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDest.setText("Into:");
		
		comboCorpus = new Combo(this, SWT.READ_ONLY);
		comboCorpus.setItems(new String[] {"Corupus", "Reference Corpus"});
		comboCorpus.select(Gear.CorpusManager.equals(gear) ? 0 : 1);
		
		new Label(this, SWT.NONE);
		
		final Button btnImport = new Button(this, SWT.NONE);
		btnImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					Workspace sourceWorkspace = getWorkspace(combo.getText().trim());
					if (sourceWorkspace != null) {
						ArrayList<ConciseDocument> docs = new ArrayList<ConciseDocument>();
						for (Doc d : documents) {
							if (d.checked) {
								docs.add(d.doc);
							}
						}
						ConciseThread thread = new CAImportWorkspaceThread(
													gear, 
													sourceWorkspace, 
													docs.toArray(new ConciseDocument[0]));
						thread.start();
						
						close();
					}
				} catch (Exception e) {
					Concise.getCurrentWorkspace().logError(null, e);
					Dialog.showException(e);
				}
				
			}
		});
		btnImport.setText("Import");
		
		final Button btnCancel = new Button(this, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});
		btnCancel.setText("Cancel");
		new Label(this, SWT.NONE);
		
		setDefaultButton(btnImport);
	}
	
	
	public void open() {
		layout();
		super.open();
		if (!combo.getText().trim().isEmpty()) {
			showDocuments(combo.getText().trim());
		}
	}
	
	
	public void close() {
		documents.clear();
		super.close();
	}

	
	protected void showDocuments(final String workpath) {
		table.removeAll();
		final InfiniteProgressPanel panel = InfiniteProgressPanel.getInfiniteProgressPanelFor(this);
		panel.start();
		new Thread() {
			public void run() {
				documents.clear();
				try {
					Workspace w = getWorkspace(workpath);
					if (w != null) {
						for (ConciseDocument cd : new DocumentIterator(w, w.getIndexReader())) {
							Doc d = new Doc();
							d.doc = cd;
							documents.add(d);
						}
						w.close();
					}
					
				} catch (Exception e) {
					Concise.getCurrentWorkspace().logError(null, e);
					Dialog.showException(e);
				}
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						table.setItemCount(documents.size());
						panel.stop();
					}
				});
			}
		}.start();
	}
	
	protected Workspace getWorkspace(String workpath) throws IOException {
		if (!FilenameUtils.getExtension(workpath).equals(CAConfig.WORKSPACE_EXTENSION)) {
			workpath = workpath + "." + CAConfig.WORKSPACE_EXTENSION;
		}
		File file = new File(workpath);
		if (file.exists()) {
			return new Workspace(file);
		}
		return null;
	}
	
	
	public void checkSubclass() {
		// disable subclass check
	}
}
