package org.sustudio.concise.app.dialog;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.resources.CABundle;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CADlgPrefToken extends Composite {
	
	private Text text;
	
	public CADlgPrefToken(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.marginHeight = 10;
		gridLayout.marginWidth = 30;
		gridLayout.verticalSpacing = 10;
		gridLayout.marginBottom = 20;
		setLayout(gridLayout);
		
		Group grpPartofspeech = new Group(this, SWT.NONE);
		grpPartofspeech.setLayout(new GridLayout(2, false));
		grpPartofspeech.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpPartofspeech.setText("Part-Of-Speech");
		
		Button btnShowPartofspeechTag = new Button(grpPartofspeech, SWT.CHECK);
		btnShowPartofspeechTag.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnShowPartofspeechTag.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CAPrefs.SHOW_PART_OF_SPEECH = ((Button) event.widget).getSelection();
				text.setEnabled(CAPrefs.SHOW_PART_OF_SPEECH);
				getShell().setModified(true);
				getShell().setData("TokenChange", true);
			}
		});
		btnShowPartofspeechTag.setText("Show Part-Of-Speech Tag");
		btnShowPartofspeechTag.setSelection(CAPrefs.SHOW_PART_OF_SPEECH);
		
		Label lblSeparator = new Label(grpPartofspeech, SWT.NONE);
		lblSeparator.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSeparator.setText("Separator:");
		
		text = new Text(grpPartofspeech, SWT.BORDER);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				if (!text.getText().trim().isEmpty())
					CAPrefs.POS_SEPARATOR = text.getText().trim();
			}			
		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.setEnabled(CAPrefs.SHOW_PART_OF_SPEECH);
		text.setText(CAPrefs.POS_SEPARATOR);
		
		Group grpToken = new Group(this, SWT.NONE);
		grpToken.setLayout(new GridLayout(1, false));
		grpToken.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpToken.setText("Token");
		
		Button b = new Button(grpToken, SWT.CHECK);
		b.setText(CABundle.get("preferences.token.showLetterTokens"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.SHOW_TOKEN_LETTER = ((Button) e.widget).getSelection();
				getShell().setModified(true);
				getShell().setData("TokenChange", true);
			}
		});
		b.setSelection(CAPrefs.SHOW_TOKEN_LETTER);
		
		b = new Button(grpToken, SWT.CHECK);
		b.setText(CABundle.get("preferences.token.showNumberTokens"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.SHOW_TOKEN_NUMBER = ((Button) e.widget).getSelection();
				getShell().setModified(true);
				getShell().setData("TokenChange", true);
			}
		});
		b.setSelection(CAPrefs.SHOW_TOKEN_NUMBER);
		
		b = new Button(grpToken, SWT.CHECK);
		b.setText(CABundle.get("preferences.token.showPunctuationTokens"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.SHOW_TOKEN_PUNCTUATION = ((Button) e.widget).getSelection();
				getShell().setModified(true);
				getShell().setData("TokenChange", true);
			}
		});
		b.setSelection(CAPrefs.SHOW_TOKEN_PUNCTUATION);
		
		b = new Button(grpToken, SWT.CHECK);
		b.setText(CABundle.get("preferences.token.showSymbolTokens"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.SHOW_TOKEN_SYMBOL = ((Button) e.widget).getSelection();
				getShell().setModified(true);
				getShell().setData("TokenChange", true);
			}
		});
		b.setSelection(CAPrefs.SHOW_TOKEN_SYMBOL);
		
		b = new Button(grpToken, SWT.CHECK);
		b.setText(CABundle.get("preferences.token.showMarkTokens"));
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.SHOW_TOKEN_MARK = ((Button) e.widget).getSelection();
				getShell().setModified(true);
				getShell().setData("TokenChange", true);
			}
		});
		b.setSelection(CAPrefs.SHOW_TOKEN_MARK);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
	}
}
