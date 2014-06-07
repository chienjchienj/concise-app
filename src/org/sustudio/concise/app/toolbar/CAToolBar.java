package org.sustudio.concise.app.toolbar;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.opalDialog.Dialog;
import org.mihalis.opal.promptSupport.PromptSupport;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.ConciseApp;
import org.sustudio.concise.app.enums.CorpusManipulation;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.CAQueryUtils;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.utils.Platform;
import org.sustudio.concise.app.widgets.CAAutoCompleteText;

public class CAToolBar {
	
	private final Spinner leftSpan;
	private final Spinner rightSpan;
	private final ToolItem tltmGo;
	private final ToolItem tltmNgram;
	private final CAAutoCompleteText txtSearch;
	
	private Gear gear;
		
	
	public static CAToolBar getToolBarFor(final ConciseApp app) {
		return new CAToolBar(app);
	}
	
	/**
	 * Concise ToolBar
	 * @param app	- Concise's Shell
	 */
	public CAToolBar(final ConciseApp app) {
		
		final ToolBar toolBar;
		if (Platform.isMac()) {
			toolBar = app.getToolBar();
		}
		else {
			toolBar = new ToolBar(app, SWT.SMOOTH);
		}
		
		final ToolItem tltmImport = new ToolItem(toolBar, SWT.NONE);
		tltmImport.setImage(SWTResourceManager.getImage(CAToolBar.class, "/org/sustudio/concise/app/icon/40-inbox-20x20.png"));
		tltmImport.setText(CABundle.get("toolbar.import"));
		tltmImport.setToolTipText("Import Corpus");
		tltmImport.addSelectionListener(CorpusManipulation.ImportDocuments.selectionAdapter());
		
		txtSearch = new CAAutoCompleteText(toolBar, SWT.BORDER | SWT.SEARCH | SWT.CANCEL);
		PromptSupport.setPrompt("search", txtSearch);
		txtSearch.addSelectionListener(new CAToolBarSearchListener());
		CopyPasteHelper.listenTo(txtSearch);
		try {
			txtSearch.setIndexReader(Concise.getCurrentWorkspace().getIndexReader());
		} catch (IOException e) {
			Concise.getCurrentWorkspace().logError(null, e);
			Dialog.showException(e);
		}
		
		ToolItem tltmSearchbox = new ToolItem(toolBar, SWT.SEPARATOR);
		tltmSearchbox.setText("SearchBox");
		tltmSearchbox.setWidth(200);
		tltmSearchbox.setControl(txtSearch);
		
		tltmGo = new ToolItem(toolBar, SWT.NONE);
		tltmGo.setText("Go");
		tltmGo.setImage(SWTResourceManager.getImage(CAToolBar.class, "/org/sustudio/concise/app/icon/06-magnify-20x20.png"));
		tltmGo.addSelectionListener(new CAToolBarSearchListener());
		
		leftSpan = new Spinner(toolBar, SWT.BORDER);
		leftSpan.setPageIncrement(5);
		leftSpan.setSelection(4);
		leftSpan.setToolTipText("Left Span Size (tokens)");
		leftSpan.pack();
		leftSpan.addSelectionListener(new CAToolBarSearchListener());
		leftSpan.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (tltmNgram.getEnabled() && tltmNgram.getSelection()) {
					rightSpan.setSelection(leftSpan.getSelection());
				}
			}
		});
		
		final ToolItem tltmLeftSpan = new ToolItem(toolBar, SWT.SEPARATOR);
		tltmLeftSpan.setWidth(leftSpan.getSize().x);
		tltmLeftSpan.setControl(leftSpan);
		
		rightSpan = new Spinner(toolBar, SWT.BORDER);
		rightSpan.setPageIncrement(5);
		rightSpan.setSelection(4);
		rightSpan.setToolTipText("Right Span Size (tokens)");
		rightSpan.pack();
		rightSpan.addSelectionListener(new CAToolBarSearchListener());
		
		final ToolItem tltmRightSpan = new ToolItem(toolBar, SWT.SEPARATOR);
		tltmRightSpan.setWidth(rightSpan.getSize().x);
		tltmRightSpan.setControl(rightSpan);
		
		tltmNgram = new ToolItem(toolBar, SWT.CHECK);
		tltmNgram.setImage(SWTResourceManager.getImage(CAToolBar.class, "/org/sustudio/concise/app/icon/82-dog-paw-20x20.png"));
		tltmNgram.setText("N-gram");
		tltmNgram.setToolTipText("N-gram");
		tltmNgram.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean selection = tltmNgram.getSelection();
				txtSearch.setEnabled(!selection);
				rightSpan.setEnabled(!selection);
				
				if (tltmNgram.getSelection()) {
					leftSpan.setSelection(2);
					rightSpan.setSelection(2);
				}
				else {
					leftSpan.setEnabled(true);
					rightSpan.setEnabled(true);
					txtSearch.setFocus();
				}
			}
		});
		
		final ToolItem tltmSep = new ToolItem(toolBar, SWT.SEPARATOR);
		tltmSep.setWidth(SWT.SEPARATOR_FILL);
		if (!Platform.isMac())
			tltmSep.setControl(new Label(toolBar, SWT.NONE));
		
		new CAToolBarGearToolItem(toolBar);
		
		
		// set tab oder
		// this is not working....
		// use self-made tab key listener
		//toolBar.setTabList(new Control[] { txtSearch, leftSpan, rightSpan } );
		
		// tab order
		txtSearch.addKeyListener(new TabKeyListener(rightSpan, leftSpan));
		leftSpan.addKeyListener(new TabKeyListener(txtSearch, rightSpan));
		rightSpan.addKeyListener(new TabKeyListener(leftSpan, txtSearch));
				
				
		
		if (gear == null) {
			setToolBarLayout(Gear.CorpusManager);
		}
	}
	
	
	/**
	 * Congifure ConciseToolBar Layout by different Gear
	 * @param gear
	 */
	
	public void setToolBarLayout(Gear gear) {
		this.gear = gear;
		try {
			CAQuery query = CAQueryUtils.getQuery(gear);
		
			////////////////////////////////////////////////////////////////////////////
			// NGram
			////////////////////////////////////////////////////////////////////////////
			switch (gear) {
			case WordCluster:
				tltmNgram.setEnabled(true);
				break;
			default:
				tltmNgram.setEnabled(false);
				break;
			}
			tltmNgram.setSelection(query.ngram);
			
			
			////////////////////////////////////////////////////////////////////////////
			// txtSearch
			////////////////////////////////////////////////////////////////////////////
			switch (gear) {
			case Concordancer:
			case ConcordancePlotter:
			case WordTrender:
			case ScatterPlotter:
			case Collocator:
			case CollocationalNetworker:
				txtSearch.setEnabled(true);
				break;
			case WordCluster:
				txtSearch.setEnabled(!tltmNgram.getSelection());
				break;
			default:
				txtSearch.setEnabled(false);	
				break;
			}
			txtSearch.setText(query.searchStr);
			if (txtSearch.isEnabled()) {
				txtSearch.setFocus();
				txtSearch.selectAll();
			}
			
			
			////////////////////////////////////////////////////////////////////////////
			// Left and Right span size
			////////////////////////////////////////////////////////////////////////////
			switch(gear) {
			case ConcordancePlotter:
			case WordTrender:
			case ScatterPlotter:
				leftSpan.setEnabled(false);
				rightSpan.setEnabled(false);
				break;
			case Collocator:
			case CollocationalNetworker:
				leftSpan.setEnabled(CAPrefs.COLLOCATION_MODE.isSurface());
				rightSpan.setEnabled(CAPrefs.COLLOCATION_MODE.isSurface());
				break;
			default:
				leftSpan.setEnabled(txtSearch.getEnabled());
				rightSpan.setEnabled(txtSearch.getEnabled());
			}
			leftSpan.setSelection(query.leftSpanSize);
			rightSpan.setSelection(query.rightSpanSize);			
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(getGear(), e);
			Dialog.showException(e);
		}
	}
	
	public Gear getGear() {
		return gear;
	}
	
	/**
	 * Returns search word.
	 * @return search word.
	 */
	public String getSearchWord() {
		return txtSearch.getText().trim();
	}
	
	/**
	 * Sets search word.
	 * @param word search word.
	 */
	public void setSearchWord(String word) {
		txtSearch.setText(word.trim());
	}
	
	/**
	 * Returns left span size.
	 * @return left span size.
	 */
	public int getLeftSpan() {
		return leftSpan.getSelection();
	}
	
	/**
	 * Returns right span size.
	 * @return right span size.
	 */
	public int getRightSpan() {
		return rightSpan.getSelection();
	}
	
	/**
	 * Returns true if Ngram is on; false otherwise.
	 * @return true if Ngram is on; false otherwise.
	 */
	public boolean getNgramSelection() {
		return tltmNgram.getSelection();
	}
	
	/**
	 * Returns true if Ngram is enabled; false otherwise.
	 * @return true if Ngram is enabled; false otherwise.
	 */
	public boolean getNgramEnabled() {
		return tltmNgram.getEnabled();
	}
	
	public CAQuery getQuery() {
		CAQuery q = new CAQuery(gear);
		q.searchStr = txtSearch.getText().trim();
		q.leftSpanSize = leftSpan.getSelection();
		q.rightSpanSize = rightSpan.getSelection();
		q.ngram = tltmNgram.getSelection();
		
		return q;
	}
	
	
	private class TabKeyListener extends KeyAdapter {
		
		private final Control prevControl;
		private final Control nextControl;
		
		public TabKeyListener(Control prevControl, Control nextControl) {
			this.prevControl = prevControl;
			this.nextControl = nextControl;
		}
		
		public void keyPressed(KeyEvent event) {
			if (event.stateMask == SWT.SHIFT && event.keyCode == SWT.TAB) {
				prevControl.forceFocus();
			}
			
			else if (event.stateMask == SWT.NONE && event.keyCode == SWT.TAB) {
				nextControl.forceFocus();
			}
		}
	}
}
