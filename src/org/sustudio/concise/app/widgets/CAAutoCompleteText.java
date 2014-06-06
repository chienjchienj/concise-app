package org.sustudio.concise.app.widgets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.AlreadyClosedException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.core.autocompleter.AutoCompleter;
import org.sustudio.concise.core.wordlister.Word;

/**
 * 依據 Lucene 的 IndexReader 給的 Dictionary ，給出自動完成的選項
 * 
 * @author Kuan-ming Su
 *
 */
public class CAAutoCompleteText extends Text {

	private IndexReader reader;
	
	public CAAutoCompleteText(Composite parent, int style) {
		super(parent, style);
		
		FontData fd = getDisplay().getSystemFont().getFontData()[0];
		fd.setHeight(fd.getHeight() - 2);
		final Font numberFont = new Font(getDisplay(), fd);
		final Color numberColor = SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY);
		
		final Shell popupShell = new Shell(getShell(), SWT.TOOL | SWT.NO_TRIM);
		popupShell.setLayout(new FillLayout());
		final Table table = new Table(popupShell, SWT.SINGLE);
		table.setHeaderVisible(false);
		
		new TableColumn(table, SWT.NONE);
		new TableColumn(table, SWT.RIGHT);
		
		addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				int textSelection = 0;
				switch (event.keyCode) {
				case SWT.ARROW_DOWN:
					int index = 0;
					if (table.getSelectionIndex() != -1) {
						index = (table.getSelectionIndex() + 1) % table.getItemCount();
					}
					table.select(index);
					if (table.getSelectionCount() > 0) {
						setText(table.getSelection()[0].getText());
						textSelection = getText().length();
						setSelection(textSelection);
					}
					event.doit = false;
					return;
				case SWT.ARROW_UP:
					index = table.getSelectionIndex() - 1;
					if (index < 0) index = table.getItemCount() - 1;
					table.select(index);
					if (table.getSelectionCount() > 0) {
						setText(table.getSelection()[0].getText());
						textSelection = getText().length();
						setSelection(textSelection);
					}
					event.doit = false;
					return;
				case SWT.CR:
				case SWT.KEYPAD_CR:
					if (popupShell.isVisible() && table.getSelectionIndex() != -1) {
						setText(table.getSelection()[0].getText());
						popupShell.setVisible(false);
					}
					return;
				case SWT.ESC:
					popupShell.setVisible(false);
					return;
				}
				
				
				String string = getText().trim();
				if (string.isEmpty()) {
					popupShell.setVisible(false);
				}
				else {
					List<Word> result = new ArrayList<Word>();
					try {
						
						if (reader != null)
						result = AutoCompleter.getInstanceFor(reader, CAPrefs.SHOW_PART_OF_SPEECH).lookup(string, 7);
						
					} catch (Exception e) {
						if (e instanceof AlreadyClosedException) {
							try {
								AutoCompleter.removeInstanceFor(reader);
								
								reader = Concise.getCurrentWorkspace().getIndexReader();
								result = AutoCompleter.getInstanceFor(reader, CAPrefs.SHOW_PART_OF_SPEECH).lookup(string, 7);
								
							} catch (Exception e1) {
								// This should not happen
								Concise.getCurrentWorkspace().logError(null, e1);
								Dialog.showException(e1);
							}
						}
						else {
							Concise.getCurrentWorkspace().logError(null, e);
							Dialog.showException(e);
						}
					}
					
					if (result.isEmpty()) {
						popupShell.setVisible(false);
						return;
					}
					table.removeAll();
					for (int i=0; i<result.size(); i++) {
						TableItem item = new TableItem(table, SWT.NONE);
						item.setText(0, result.get(i).word);
						item.setText(1, String.valueOf(result.get(i).totalTermFreq));
						item.setFont(1, numberFont);
						item.setForeground(1, numberColor);
					}
					popupShell.setBounds(toDisplay(0, 0).x,
										 toDisplay(0, 0).y + getBounds().height,
										 getBounds().width, 
										 table.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
					popupShell.setVisible(true);
				}
			}
		});
		
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				setText(table.getSelection()[0].getText());
				popupShell.setVisible(false);
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.ESC) {
					popupShell.setVisible(false);
				}
			}
		});
		
		FocusAdapter focusOutListener = new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				// async is needed to wait until focus reaches its new Control
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (isDisposed() || getDisplay().isDisposed()) return;
						Control control = getDisplay().getFocusControl();
						if (control == null || (control != CAAutoCompleteText.this && control != table)) {
							popupShell.setVisible(false);
						}
					}
				});
			}
		};
		table.addFocusListener(focusOutListener);
		addFocusListener(focusOutListener);
		
		table.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				table.getColumn(0).setWidth(table.getSize().x - 45);
				table.getColumn(1).setWidth(45);
			}
		});
	}
	
	
	public void setIndexReader(IndexReader reader) {
		if (this.reader != reader && this.reader != null) {
			try {
				AutoCompleter.removeInstanceFor(this.reader);
			} catch (IOException e) {
				Concise.getCurrentWorkspace().logError(null, e);
				Dialog.showException(e);
			}
		}
		this.reader = reader;
	}
	
	
	public void checkSubclass() {
		// disable subclass check
	}

}
