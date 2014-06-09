package org.sustudio.concise.app.gear;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.sustudio.concise.app.db.CADataUtils;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.helper.SaveOutputHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.DefaultCollocQuery;
import org.sustudio.concise.app.query.DefaultConcQuery;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.thread.WordListerThread;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.wordlister.Lemma;
import org.sustudio.concise.core.wordlister.Word;

public class WordLister 
	   extends GearController 
	   implements IGearSortable, IGearTableBased, IGearFilterable, 
				  IGearConcordable, IGearCollocatable {

	private static final String CCWORD = "CCWORD";
	private Tree tree;
	
	public WordLister() {
		super(CABox.GearBox, Gear.WordLister);
	}
	
	@Override
	public Control createControl() {
		tree = new Tree(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		/*
		final TreeColumn tableColumn = new TreeColumn(tree, SWT.NONE);
		tableColumn.setMoveable(true);
		tableColumn.setWidth(100);
		tableColumn.setText("#");
		*/
		
		final TreeColumn tblclmnWord = new TreeColumn(tree, SWT.NONE);
		tblclmnWord.setMoveable(true);
		tblclmnWord.setWidth(100);
		tblclmnWord.setText("Words");
		tblclmnWord.setData(_DB_COLUMN, DBColumn.Word);
		tblclmnWord.addSelectionListener(columnSortListener);
		
		final TreeColumn tblclmnFreq = new TreeColumn(tree, SWT.RIGHT);
		tblclmnFreq.setMoveable(true);
		tblclmnFreq.setWidth(100);
		tblclmnFreq.setText("Freq.");
		tblclmnFreq.setData(_DB_COLUMN, DBColumn.Freq);
		tblclmnFreq.addSelectionListener(columnSortListener);
		
		final TreeColumn tblclmnDocFreq = new TreeColumn(tree, SWT.RIGHT);
		tblclmnDocFreq.setMoveable(true);
		tblclmnDocFreq.setWidth(100);
		tblclmnDocFreq.setText("Doc Freq.");
		tblclmnDocFreq.setData(_DB_COLUMN, DBColumn.DocFreq);
		tblclmnDocFreq.addSelectionListener(columnSortListener);
		
		final TreeColumn tblclmnLemma = new TreeColumn(tree, SWT.NONE);
		tblclmnLemma.setMoveable(true);
		tblclmnLemma.setWidth(250);
		tblclmnLemma.setText("Lemma");
		//tblclmnLemma.setData(DB_COLUMN, DBColumn.DocFreq);
		//tblclmnLemma.addSelectionListener(columnSortListener);
		
		tree.setSortColumn(tblclmnFreq);
		tree.setSortDirection(SWT.DOWN);
		
		tree.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				final TreeItem item = (TreeItem) event.item;
				final TreeItem parentItem = item.getParentItem();
				int index = event.index;
				
				if (parentItem == null) {
					item.setText(getItemTexts(index));
					item.setItemCount(workspace.DATA.wordlist.get(index).getChildrenCount());
					item.setData(CCWORD, workspace.DATA.wordlist.get(index));
				}
				else {
					final Word parent = (Word) parentItem.getData(CCWORD);
					Word word = parent.getChildren()[index];
					createNewItem(item, word);
				}
			}
		});
		
		/*
		tree.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				showConcord();
			}
		});
		*/
		
		// CCWord[] transfer dragSource
	    Transfer[] types = new Transfer[] { WordsTransfer.getInstance() };
	    final DragSource dragSource = new DragSource(tree, DND.DROP_COPY | DND.DROP_MOVE);
	    dragSource.setTransfer(types);
	    
	    dragSource.addDragListener(new DragSourceAdapter() {
	    	public void dragStart(DragSourceEvent event) {
	    		TreeItem[] selection = tree.getSelection();
	    		if (selection.length > 0) {
	    			for (TreeItem item : selection) {
	    				if (item.getItemCount() != 0) {
	    					event.doit = false;
	    					return;
	    				}
	    			}
	    			event.doit = true;
	    		} else {
	    			event.doit = false;
	    		}
	    	};
	    	
	    	public void dragSetData(DragSourceEvent event) {
	    		if (WordsTransfer.getInstance().isSupportedType(event.dataType)) {
	    			List<Word> list = new ArrayList<Word>();
		    		for (TreeItem item : tree.getSelection()) {
		    			Word word = (Word) item.getData(CCWORD);
		    			list.add(word);
		    		}
		    		event.data = list.toArray(new Word[0]);
	    		}
	    	}
	    	
	    	public void dragFinished(DragSourceEvent event) {
	    		if (event.detail == DND.DROP_MOVE) {
	    			for (TreeItem item : tree.getSelection()) {
	    				item.dispose();
	    				item = null;
	    			}
	    		}
	    	}
	    });
	    
	    DropTarget target = new DropTarget(tree, DND.DROP_MOVE);
	    target.setTransfer(types);
	    target.addDropListener(new DropTargetAdapter() {
	    	
	    	@Override
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
	    	
	    	@Override
	    	public void drop(DropTargetEvent event) {
	    		if (event.data == null) {
	    			event.detail = DND.DROP_NONE;
	    			return;
	    		}
	    		
	    		Word[] words = (Word[]) event.data;
	    		if (event.item != null) {
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
	    				
	    				if (pt.y < bounds.y + bounds.height / 3) {
	    					TreeItem newItem = new TreeItem(parent, SWT.NONE, index);
	    					createNewItem(newItem, words[0]);
	    				} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
	    					TreeItem newItem = new TreeItem(parent, SWT.NONE, index + 1);
	    					createNewItem(newItem, words[0]);
	    				} 
	    				
	    				// add a form word
	    				addFormWord((Word) parent.getData(CCWORD), words[0]);
	    				
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
	    					createNewItem(newItem, words[0]);
	    					
	    					// 檢查不是只有root item移來移去而已
	    					boolean remove = removeFormWord(words[0]);  
	    					if (remove) {
	    						addLemmaWord(words[0]);
	    					}
	    	    			
	    				} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
	    					TreeItem newItem = new TreeItem(tree, SWT.NONE, index + 1);
	    					createNewItem(newItem, words[0]);
	    					
	    					// 檢查不是只有root item移來移去而已
	    					boolean remove = removeFormWord(words[0]);  
	    					if (remove) {
	    						addLemmaWord(words[0]);
	    					}
	    					
	    				} else {
	    					TreeItem newItem = new TreeItem(item, SWT.NONE);
	    					createNewItem(newItem, words[0]);
	    					addFormWord((Word) item.getData(CCWORD), words[0]);
	    				}
	    			}
	    			
	    		}
	    		
	    		// ask LemmaEditorView to load new data
    			// TODO sync UI
	    		Gear.LemmaEditor.getController(workspace).loadData();
	    		
	    	}  // end drop
	    });
	    
		return tree;
	}
	
	private void addLemmaWord(final Word lemmaWord) {
		final Lemma lemma = new Lemma(lemmaWord.getWord());
		CAPrefs.LEMMA_LIST.add(lemma);
	}
	
	private void addFormWord(final Word lemmaWord, final Word formWord) {
		Lemma targetLemma = CAPrefs.LEMMA_LIST.get(lemmaWord.getWord());
		if (targetLemma == null) {
			targetLemma = new Lemma(lemmaWord.getWord());
			CAPrefs.LEMMA_LIST.add(targetLemma);
		}
		targetLemma.addForm(formWord.getWord());
	}
	
	private boolean removeFormWord(final Word formWord) {
		Lemma lemma = CAPrefs.LEMMA_LIST.getLemmaWithForm(formWord.getWord());
		if (lemma != null) {
			return lemma.removeForm(formWord.getWord());
		}
		return false;
	}
	
	private void createNewItem(TreeItem item, Word word) {
		item.setText(new String[] {
				word.word,
				Formats.getNumberFormat(word.totalTermFreq),
				Formats.getNumberFormat(word.docFreq) });
			item.setData(CCWORD, word);
	}
	
	public void loadData() {
		CASpinner spinner = new CASpinner(this);
		spinner.open();
		tree.removeAll();
		tree.setItemCount(0);
		DBColumn sortColumn = (DBColumn) tree.getSortColumn().getData(_DB_COLUMN);
		int sortDirection = tree.getSortDirection();
		int count = 0;
		try {
			CADataUtils.resetWordList(sortColumn, 
									  sortDirection, 
									  getFinder().whereSyntax());
			count = workspace.DATA.wordlist.size();
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		} finally {			
			tree.setItemCount(count);
			tree.update();
			setStatusText(Formats.getNumberFormat(workspace.DATA.totalTermFreq) + " tokens and " +
					  	  Formats.getNumberFormat(count) + " types.");
			
			spinner.close();
			SaveOutputHelper.listenTo(getGear());
		}
	}
	
	protected void unloadData() {
		if (workspace.DATA.wordlist != null)
			workspace.DATA.wordlist.clear();
		workspace.DATA.wordlist = null;
		super.unloadData();
	}

	public void sort() {
		try 
		{	
			CADataUtils.resetWordList(
					(DBColumn) tree.getSortColumn().getData(_DB_COLUMN),
					tree.getSortDirection(),
					getFinder().whereSyntax());
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		} finally {
			tree.clearAll(true);
			tree.update();
		}
	}
	

	@Override
	public String[] getItemTexts(int index) {
		if (workspace.DATA.wordlist == null) 
			return new String [] { "", "", "", "", "" };
		
		Word word = workspace.DATA.wordlist.get(index);
		return new String[] {
			//String.valueOf(index + 1),
			word.word,
			Formats.getNumberFormat(word.totalTermFreq),
			Formats.getNumberFormat(word.docFreq),
			word.getChildrenToString() };
	}

	@Override
	public boolean isConcordEnabled() {
		return tree.getSelectionCount() == 1;
	}

	@Override
	public void showConcord() {
		if (tree.getSelectionCount() == 1) {
			final Word word = (Word) tree.getSelection()[0].getData(CCWORD);
			StringBuilder sb = new StringBuilder();
			if (word.getChildrenCount() > 0) {
				for (Word w : word.getChildren()) {
					sb.append(sb.length() > 0 ? " " : "");
					sb.append(w.getWord());
				}
			}
			else {
				sb.append(word.getWord());
			}
			Gear.Concordancer.open(workspace)
				.doit(new DefaultConcQuery(sb.toString()));
			sb.setLength(0);
		}
	}

	@Override
	public boolean isCollocateEnabled() {
		return tree.getSelectionCount() == 1;
	}

	@Override
	public void showCollocate() {
		if (tree.getSelectionCount() == 1) {
			final Word word = (Word) tree.getSelection()[0].getData(CCWORD);
			StringBuilder sb = new StringBuilder();
			if (word.getChildrenCount() > 0) {
				for (Word w : word.getChildren()) {
					sb.append(sb.length() > 0 ? " " : "");
					sb.append(w.getWord());
				}
			}
			else {
				sb.append(word.getWord());
			}
			Gear.Collocator.open(workspace)
				.doit(new DefaultCollocQuery(sb.toString()));
			sb.setLength(0);
		}
	}
	
	@Override
	public void showFinder() {
		getFinder().setHidden(false);
	}

	@Override
	public void doit(CAQuery query) {
		ConciseThread thread = new WordListerThread(query);
		thread.start();
	}
	
}
