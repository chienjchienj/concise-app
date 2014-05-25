package org.sustudio.concise.app.gear.lemmaEditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.dialog.CAOpenFilesDialog;
import org.sustudio.concise.app.dialog.CASaveFileDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.CCWordsTransfer;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.thread.CAThread;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.wordlister.Lemma;
import org.sustudio.concise.core.wordlister.LemmaList;
import org.sustudio.concise.core.wordlister.Word;

public class LemmaEditor extends GearController {

	private Tree tree;
	
		
	public LemmaEditor() {
		super(CABox.ToolBox, Gear.LemmaEditor);
	}

	protected void init() {
		if (CAPrefs.LEMMA_LIST == null) {
			CAPrefs.LEMMA_LIST = new LemmaList();
		}
	}
	
	@Override
	protected Control createControl() {
		
		final Button lemmaCheckBox = new Button(this, SWT.CHECK);
		lemmaCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		lemmaCheckBox.setText("Enable Lemma");
		lemmaCheckBox.setSelection(CCPrefs.LEMMA_ENABLED);
		lemmaCheckBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CCPrefs.LEMMA_ENABLED = !CCPrefs.LEMMA_ENABLED;
				((Button) event.widget).setSelection(CCPrefs.LEMMA_ENABLED);
			}
		});
		
		// add Toolbar
		final ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT | SWT.SHADOW_OUT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final ToolItem tltmNew = new ToolItem(toolBar, SWT.NONE);
		tltmNew.setImage(SWTResourceManager.getImage(LemmaEditor.class, "/org/sustudio/concise/app/icon/10-medical.png"));
		tltmNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// show popup editor
				Rectangle rect = tltmNew.getBounds();
				Point pt = tltmNew.getParent().toDisplay(new Point(rect.x, rect.y));
				rect = new Rectangle(pt.x, pt.y, rect.width, rect.height);
				
				final LemmaPopupEditor editor = LemmaPopupEditor.getInstanceFor(getShell());
				editor.openNear(rect);
			}
		});
		tltmNew.setText("New");
		
		final ToolItem tltmEdit = new ToolItem(toolBar, SWT.NONE);
		tltmEdit.setImage(SWTResourceManager.getImage(LemmaEditor.class, "/org/sustudio/concise/app/icon/icon_edit.png"));
		tltmEdit.setEnabled(false);
		tltmEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editLemma();
			}
		});
		tltmEdit.setText("Edit");
		
		final ToolItem tltmDel = new ToolItem(toolBar, SWT.NONE);
		tltmDel.setImage(SWTResourceManager.getImage(LemmaEditor.class, "/org/sustudio/concise/app/icon/201-remove.png"));
		tltmDel.setEnabled(false);
		tltmDel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeLemma();
			}
		});
		tltmDel.setText("Del");
		
		final ToolItem sepItem = new ToolItem(toolBar, SWT.SEPARATOR);
		sepItem.setControl(new Label(toolBar, SWT.NONE));
		
		final ToolItem tltmOptions = new ToolItem(toolBar, SWT.DROP_DOWN);
		tltmOptions.setToolTipText("Options");
		tltmOptions.setImage(SWTResourceManager.getImage(LemmaEditor.class, "/org/sustudio/concise/app/icon/19-gear.png"));
		setDropDownMenu(tltmOptions);
		
		tree = new Tree(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		final TreeColumn trclmnLemma = new TreeColumn(tree, SWT.NONE);
		trclmnLemma.setWidth(100);
		trclmnLemma.setText("Lemma");
		
		final TreeColumn trclmnForms = new TreeColumn(tree, SWT.NONE);
		trclmnForms.setWidth(150);
		trclmnForms.setText("Forms");
		
		
		tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				tltmEdit.setEnabled(tree.getSelectionCount() == 1);
				tltmDel.setEnabled(tree.getSelectionCount() > 0);
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				editLemma();
			}
		});
		tree.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.DEL:
				case SWT.BS:
					removeLemma();
					break;
					
				case SWT.ESC:
					tree.deselectAll();
				}
			}
		});
		tree.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				int width = tree.getClientArea().width - trclmnLemma.getWidth();
				if (width < 150) width = 150;
				trclmnForms.setWidth(width);
				sepItem.setWidth(toolBar.getSize().x 
								 - tltmOptions.getBounds().width
								 - tltmDel.getBounds().width
								 - tltmEdit.getBounds().width
								 - tltmNew.getBounds().width);
			}
		});
		
		tree.setData(CAPrefs.LEMMA_LIST);
		tree.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TreeItem item = (TreeItem) event.item;
				TreeItem parentItem = item.getParentItem();
				int index = event.index;
				if (parentItem == null) {
					// lemma word
					Lemma lemma = CAPrefs.LEMMA_LIST.get(index);
					item.setData(lemma);
					item.setText(0, lemma.getWord());
					item.setText(1, lemma.getForms().toString());
					item.setItemCount(lemma.getForms().size());
				}
				else {
					// forms
					Lemma lemma = (Lemma) parentItem.getData();
					String form = lemma.getForms().get(index);
					item.setText(form);
				}
			}
		});
		
		final DragSource source = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY);
	    source.setTransfer(new Transfer[] { TextTransfer.getInstance() });
	    
	    final TreeItem[] dragSourceItem = new TreeItem[1];
	    source.addDragListener(new DragSourceListener() {
	    	public void dragStart(DragSourceEvent event) {
	    		TreeItem[] selection = tree.getSelection();
	    		if (selection.length > 0 && selection[0].getItemCount() == 0) {
	    			event.doit = true;
	    			dragSourceItem[0] = selection[0];
	    		} else {
	    			event.doit = false;
	    		}
	    	};
	    	
	    	public void dragSetData(DragSourceEvent event) {
	    		final TreeItem item = dragSourceItem[0];
	    		event.data = item.getText();
	    		
	    		if (item.getParentItem() == null) {
	    			CAPrefs.LEMMA_LIST.remove(item.getData());
	    		}
	    		else {
	    			((Lemma) item.getParentItem().getData()).removeForm(item.getText());
	    		}
	    		
	    	}
	    	
	    	public void dragFinished(DragSourceEvent event) {
	    		if (event.detail == DND.DROP_MOVE)
	    			dragSourceItem[0].dispose();
	    		dragSourceItem[0] = null;
	    	}
	    });
	    
	    DropTarget target = new DropTarget(tree, DND.DROP_COPY | DND.DROP_MOVE);
	    target.setTransfer(new Transfer[] { CCWordsTransfer.getInstance(), TextTransfer.getInstance() });
	    target.addDropListener(new DropTargetAdapter() {
	    	
	    	public void dragEnter(DropTargetEvent event) {
	    		for (int i = 0; i < event.dataTypes.length; i++) {
	    			if (CCWordsTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
	    				event.currentDataType = event.dataTypes[i];
	    				// CCWord 是從另外一個 Gear 來的，所以永遠都只用 Copy
	    				if (event.detail != DND.DROP_COPY) {
	    					event.detail = DND.DROP_COPY;
	    				}
	    			}
	    		}
	    	}
	    	
	    	public void dragOver(DropTargetEvent event) {
	    		event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
	    		if (event.item != null) {
	    			TreeItem item = (TreeItem) event.item;
	    			Point pt = getDisplay().map(null, tree, event.x, event.y);
	    			Rectangle bounds = item.getBounds();
	    			if (pt.y < bounds.y + bounds.height / 3) {
	    				event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
	    			} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
	    				event.feedback |= DND.FEEDBACK_INSERT_AFTER;
	    			} else if (item.getParentItem() == null) {
	    				event.feedback |= DND.FEEDBACK_SELECT;
	    			} else {
	    				event.feedback = DND.FEEDBACK_NONE;
	    			}
	    		}
	    	}
	    	
	    	public void drop(DropTargetEvent event) {
	    		if (event.data == null) {
	    			event.detail = DND.DROP_NONE;
	    			return;
	    		}
	    		
	    		String text = "";
	    		if (event.data instanceof String) {
	    			text = (String) event.data;
	    		}
	    		else if (event.data instanceof Word[]) {
	    			text = ((Word[]) event.data)[0].getWord();
	    		}
	    		
	    		if (event.item == null) {
	    			TreeItem item = new TreeItem(tree, SWT.NONE);
	    			// create a new lemma
	    			Lemma lemma = new Lemma(text);
	    			CAPrefs.LEMMA_LIST.add(lemma);
	    			item.setData(lemma);
	    			item.setText(0, lemma.getWord());
	    			item.setText(1, lemma.getForms().toString());
	    			
	    		} else {
	    			TreeItem item = (TreeItem) event.item;
	    			Point pt = getDisplay().map(null, tree, event.x, event.y);
	    			Rectangle bounds = item.getBounds();
	    			TreeItem parent = item.getParentItem();
	    			if (parent != null) {
	    				TreeItem[] items = parent.getItems();
	    				int index = 0;
	    				for (int i = 0; i < items.length; i++) {
	    					if (items[i] == item) {
	    						index = i;
	    						break;
	    					}
	    				}
	    				
	    				// add a form
	    				Lemma lemma = (Lemma) parent.getData();
	    				lemma.addForm(text);
	    				
	    				if (pt.y < bounds.y + bounds.height / 5) {
	    					TreeItem newItem = new TreeItem(parent, SWT.NONE, index);
	    					newItem.setText(text);
	    				} else if (pt.y > bounds.y + 2 * bounds.height / 5) {
	    					TreeItem newItem = new TreeItem(parent, SWT.NONE, index + 1);
	    					newItem.setText(text);
	    				} 
	    				parent.setText(1, lemma.getForms().toString());
	    				
	    			} else {  // no parent
	    				TreeItem[] items = tree.getItems();
	    				int index = 0;
	    				for (int i = 0; i < items.length; i++) {
	    					if (items[i] == item) {
	    						index = i;
	    						break;
	    					}
	    				}
	    				if (pt.y < bounds.y + bounds.height / 3) {
	    					TreeItem newItem = new TreeItem(tree, SWT.NONE, index);
	    					Lemma lemma = new Lemma(text);
	    	    			CAPrefs.LEMMA_LIST.add(lemma);
	    	    			newItem.setData(lemma);
	    	    			newItem.setText(0, lemma.getWord());
	    	    			newItem.setText(1, lemma.getForms().toString());
	    	    			
	    				} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
	    					TreeItem newItem = new TreeItem(tree, SWT.NONE, index + 1);
	    					Lemma lemma = new Lemma(text);
	    	    			CAPrefs.LEMMA_LIST.add(lemma);
	    	    			newItem.setData(lemma);
	    	    			newItem.setText(0, lemma.getWord());
	    	    			newItem.setText(1, lemma.getForms().toString());
	    					
	    				} else {
	    					TreeItem newItem = new TreeItem(item, SWT.NONE);
	    					newItem.setText(text);
	    					
	    					Lemma lemma = (Lemma) item.getData();
		    				lemma.addForm(text);
		    				item.setText(1, lemma.getForms().toString());
	    				}
	    			}
	    			
	    		}
	    		
	    	}  // end drop
	    });
	    
	    return tree;
	}
	
	private void editLemma() {
		if (tree.getSelectionCount() == 1) {
			final TreeItem item = tree.getSelection()[0];
			Rectangle rect = item.getBounds();
			Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
			rect = new Rectangle(pt.x, pt.y, rect.width, rect.height);
			
			Lemma lemma = null;
			if (item.getParentItem() == null) {
				lemma = (Lemma) item.getData();
			}
			else {
				// find lemma index
				lemma = (Lemma) item.getParentItem().getData();
			}
			
			final LemmaPopupEditor editor = LemmaPopupEditor.getInstanceFor(getShell());
			editor.setEditLemma(lemma);
			editor.openNear(rect);
		}
	}
	
	public void loadData() {
		super.loadData();
		tree.removeAll();
		tree.setItemCount(CAPrefs.LEMMA_LIST.size());
		tree.redraw();
		setStatusText(tree.getItemCount() + " Lemma" + (tree.getItemCount() > 1 ? "s" : ""));
		tree.setFocus();
	}
	
	
	/**
	 * Set up options menu.
	 * @param item 	option icon.
	 */
	private void setDropDownMenu(final ToolItem item) {
		final Menu menu = new Menu(getShell(), SWT.POP_UP);
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Rectangle rect = item.getBounds();
				Point pt = new Point(rect.x, rect.y + rect.height);
				pt = item.getParent().toDisplay(pt);
				menu.setLocation(pt.x, pt.y);
				menu.setVisible(true);
			}
		});
		
		MenuItem mntmLoad = new MenuItem(menu, SWT.NONE);
		mntmLoad.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (tree.getItemCount() > 0 && 
					Dialog.isConfirmed(
							getShell(), 
							"Do you want to continue loading?", 
							"Load Lemma File will empty current contents."))
				{
					tree.removeAll();
					CAOpenFilesDialog dlg = new CAOpenFilesDialog(true);
					dlg.setOpenTextFileConfigure();
					dlg.setText("Load Lemma File");
					String filename = dlg.open();
					if (filename != null) {
						loadLemma(new File(filename));
					}
				}
				else {
					CAOpenFilesDialog dlg = new CAOpenFilesDialog(true);
					dlg.setOpenTextFileConfigure();
					dlg.setText("Load Lemma File");
					String filename = dlg.open();
					if (filename != null) {
						loadLemma(new File(filename));
					}
				}
			}
		});
		mntmLoad.setText("Load from Text File");
		
		MenuItem mntmSave = new MenuItem(menu, SWT.NONE);
		mntmSave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) 
			{
				CASaveFileDialog dlg = new CASaveFileDialog();
				dlg.setTextFileConfigure();
				String filename = dlg.open();
				if (filename != null) {
					try 
					{
						StringBuilder sb = new StringBuilder();
						sb.append("[ Auto-generated Lemma from Concise App (http://concise.sustudio.org) \r\n");
						sb.append("[ at " + Formats.getDate() + "\r\n");
						
						System.out.println(CAPrefs.LEMMA_LIST.size());
						for (Lemma lemma : CAPrefs.LEMMA_LIST) {
							sb.append(lemma.toString());
							sb.append("\n");
						}
						
						BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
						writer.write(sb.toString());
						writer.close();
						sb.setLength(0);
						sb = null;
						
					} catch (Exception e) {
						CAErrorMessageDialog.open(getGear(), e);
					} finally {
						Dialog.inform(
								getShell(), 
								"File saved!", 
								"\""+new File(filename).getName()+"\" has been saved to \""+new File(filename).getPath()+"\" !");
					}
				}
			}
		});
		mntmSave.setText("Save to File");
		mntmSave.setEnabled(true);
	}
	
	private void loadLemma(final File file) {
		//CAProgressDialog dialog = new CAProgressDialog();
		CAThread loadThread = new CAThread(Gear.LemmaEditor, new CAQuery(Gear.LemmaEditor)) {

			@Override
			public void running() {
				
				if (CAPrefs.LEMMA_LIST != null) {
					CAPrefs.LEMMA_LIST.clear();
				}
				CAPrefs.LEMMA_LIST = new LemmaList();
				
				try 
				{
					final String lemmaDelim = "->";
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line;
					while ( (line = reader.readLine()) != null ) {
						if (line.trim().startsWith("[") || 	// comment
							line.trim().equals("") ||	// empty line
							!line.contains(lemmaDelim))		// no data
						{
							continue;
						}
						
						int delimLocation = line.indexOf(lemmaDelim);
						String word = line.substring(0, delimLocation).trim();
						Lemma lemma = new Lemma(word);
						String formsStr = line.substring(delimLocation + lemmaDelim.length(), line.length()).trim();
						StringTokenizer st = new StringTokenizer(formsStr, " \t,;/\\");
						while (st.hasMoreTokens()) {
							lemma.addForm(st.nextToken());
						}
						CAPrefs.LEMMA_LIST.add(lemma);
					}
					reader.close();
					
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							LemmaEditor.this.loadData();
						}
					});
					
				} catch (Exception e) {
					CAErrorMessageDialog.open(getGear(), e);
				}
			}
			
		};
		loadThread.start();
	}

	private void removeLemma() {
		if (tree.getSelectionCount() < 1) return;
		if (!Dialog.isConfirmed(
				getShell(), 
				"Do you want to remove " + tree.getSelectionCount() + " lemmas and their forms?", "")) 
		{
			return;
		}
		for (TreeItem item: tree.getSelection()) {
			if (item == null || item.isDisposed()) continue;
			TreeItem parentItem = item.getParentItem();
			if (parentItem == null) {
				// root-level item: lemma
				CAPrefs.LEMMA_LIST.remove((Lemma)item.getData());
			}
			else {
				Lemma lemma = (Lemma)parentItem.getData();
				if (lemma != null) {
					lemma.removeForm(item.getText());
				}
			}
		}
		loadData();
	}
}
