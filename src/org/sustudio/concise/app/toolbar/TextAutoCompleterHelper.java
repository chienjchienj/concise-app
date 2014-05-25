package org.sustudio.concise.app.toolbar;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.autocompleter.AutoCompleter;
import org.sustudio.concise.core.wordlister.Word;

public class TextAutoCompleterHelper {
	
	public static void listenTo(final Text text) {
		try {
			Workspace workspace = Concise.getCurrentWorkspace();
			if (workspace.getIndexReader() != null) {
				listenTo(text, workspace.getIndexReader());
			}
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(null, e);
			Dialog.showException(e);
		}
	}
	
	public static void listenTo(final Text text, final IndexReader reader) {
		FontData fd = text.getDisplay().getSystemFont().getFontData()[0];
		fd.setHeight(fd.getHeight() - 2);
		final Font numberFont = new Font(text.getDisplay(), fd);
		final Color numberColor = SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY);
		
		final Shell popupShell = new Shell(text.getShell(), SWT.TOOL | SWT.NO_TRIM);
		popupShell.setLayout(new FillLayout());
		final Table table = new Table(popupShell, SWT.SINGLE);
		table.setHeaderVisible(false);
		
		new TableColumn(table, SWT.NONE);
		new TableColumn(table, SWT.RIGHT);
						
		text.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				int textSelection = 0;
				switch (event.keyCode) {
				case SWT.ARROW_DOWN:
					int index = 0;
					if (table.getSelectionIndex() != -1) {
						index = (table.getSelectionIndex() + 1) % table.getItemCount();
					}
					table.setSelection(index);
					if (table.getSelectionCount() > 0) {
						text.setText(table.getSelection()[0].getText());
						textSelection = text.getText().length();
						text.setSelection(textSelection);
					}
					event.doit = false;
					return;
				case SWT.ARROW_UP:
					index = table.getSelectionIndex() - 1;
					if (index < 0) index = table.getItemCount() - 1;
					table.setSelection(index);
					if (table.getSelectionCount() > 0) {
						text.setText(table.getSelection()[0].getText());
						textSelection = text.getText().length();
						text.setSelection(textSelection);
					}
					event.doit = false;
					return;
				case SWT.CR:
				case SWT.KEYPAD_CR:
					if (popupShell.isVisible() && table.getSelectionIndex() != -1) {
						text.setText(table.getSelection()[0].getText());
						popupShell.setVisible(false);
					}
					return;
				case SWT.ESC:
					popupShell.setVisible(false);
					return;
				}
				
				
				String string = text.getText().trim();
				if (string.isEmpty()) {
					popupShell.setVisible(false);
				}
				else {
					List<Word> result = new ArrayList<Word>();
					try {
						
						result = AutoCompleter.getInstanceFor(reader).lookup(string, 7);
						
					} catch (Exception e) {
						if (e instanceof AlreadyClosedException) {
							try {
								Workspace workspace = Concise.getCurrentWorkspace();
								result = AutoCompleter.getInstanceFor(workspace.getIndexReader()).lookup(string, 7);
								
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
					popupShell.setBounds(text.toDisplay(0, 0).x,
										 text.toDisplay(0, 0).y + text.getBounds().height,
										 text.getBounds().width, 
										 table.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
					popupShell.setVisible(true);
				}
			}
		});
		
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				text.setText(table.getSelection()[0].getText());
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
				text.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (text.isDisposed() || text.getDisplay().isDisposed()) return;
						Control control = text.getDisplay().getFocusControl();
						if (control == null || (control != text && control != table)) {
							popupShell.setVisible(false);
						}
					}
				});
			}
		};
		table.addFocusListener(focusOutListener);
		text.addFocusListener(focusOutListener);
		
		table.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				table.getColumn(0).setWidth(table.getSize().x - 45);
				table.getColumn(1).setWidth(45);
			}
		});
	}
	
}
