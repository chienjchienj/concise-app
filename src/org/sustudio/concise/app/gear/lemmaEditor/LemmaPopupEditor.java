package org.sustudio.concise.app.gear.lemmaEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.core.wordlister.Lemma;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

public class LemmaPopupEditor extends Shell {
	
	private static LemmaPopupEditor popupEditor = null;
	
	public static LemmaPopupEditor getInstanceFor(final Shell shell) {
		if (popupEditor == null ||
			popupEditor.isDisposed() ||
			!shell.equals(popupEditor.getShell())) 
		{
			popupEditor = new LemmaPopupEditor(shell);
		}
		return popupEditor;
	}
	
	
	final Shell triShell = new Shell(this, SWT.NO_TRIM);
	private Region region = new Region();
	
	private Lemma lemma;
	
	private Text txtLemma;
	private StyledText txtForms;
	
	
	/**
	 * Create the shell.
	 * @param display
	 */
	public LemmaPopupEditor(final Shell shell) {
		super(shell, SWT.TOOL);
		addShellListener(new ShellAdapter() {
			@Override
			public void shellDeactivated(ShellEvent e) {
				for (Shell shell : getDisplay().getShells()) {
					if (!shell.equals(triShell) &&
						shell.getParent() != null && 
						shell.getParent().equals(LemmaPopupEditor.this)) 
					{
						return;  // dialog prompt
					}
				}
				triShell.setVisible(false);
				setVisible(false);
			}
		});
		setSize(250, 300);
		setLayout(null);
		
		final Group grpLemma = new Group(this, SWT.NONE);
		grpLemma.setBounds(5, 5, 240, 257);
		grpLemma.setText("Lemma");
		grpLemma.setLayout(null);
		
		txtLemma = new Text(grpLemma, SWT.BORDER);
		txtLemma.setBounds(10, 10, 216, 19);
		
		final Group group = new Group(grpLemma, SWT.BORDER);
		group.setBounds(10, 34, 216, 196);
		group.setText("Forms");
		group.setLayout(null);
		
		txtForms = new StyledText(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtForms.setBounds(5, 5, 202, 169);
		
		final Composite composite = new Composite(this, SWT.NO_BACKGROUND);
		composite.setBounds(129, 267, 116, 28);
		composite.setLayout(null);
		
		final Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setBounds(0, 0, 68, 28);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LemmaPopupEditor.this.close();
			}
		});
		btnCancel.setText("Cancel");
		
		final Button btnOk = new Button(composite, SWT.NONE);
		btnOk.setBounds(68, 0, 48, 28);
		btnOk.setText("OK");
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (txtLemma.getText().trim().equals("")) {
					Dialog.inform(getShell(), "Empty Lemma!", "You have to type something.");
					return;
				}
				
				boolean toAdd = false;
				if (lemma == null) {
					toAdd = true;
					lemma = new Lemma(txtLemma.getText().trim());
				}
				else {
					lemma.setWord(txtLemma.getText().trim());
				}
				
				// add forms
				List<String> forms = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(txtForms.getText());
				while (st.hasMoreTokens()) {
					String form = st.nextToken();
					forms.add(form);
				}
				lemma.setForms(forms);
				
				if (toAdd) {
					CAPrefs.LEMMA_LIST.add(lemma);
				}
				
				triShell.setVisible(false);
				LemmaPopupEditor.this.setVisible(false);
				if (Gear.LemmaEditor.getController(Concise.getCurrentWorkspace()) != null) {
					Gear.LemmaEditor.getController(Concise.getCurrentWorkspace()).loadData();
				}
			}
		});
		
		
	}
	
	public void setEditLemma(final Lemma lemma) {
		this.lemma = lemma;
		txtLemma.setText(lemma.getWord());
		
		// find forms
		txtForms.setText("");
		for (String form : lemma.getForms()) {
			txtForms.append(form + System.getProperty("line.separator"));
		}		
	}
	
	
	/**
	 * Open shell near a point
	 * @param pt
	 */
	public void openNear(final Rectangle rect) {
		final Point pt = new Point(rect.x, rect.y);
		
		// try left
		region.add(new int[] { 0, 0, 15, 15, 0, 30 });
		triShell.setSize(15, 30);
		triShell.setBackground(getBackground());
		triShell.setRegion(region);
		triShell.setLocation(pt.x - 15, pt.y - rect.height / 2);
		setLocation(pt.x - getBounds().width - triShell.getSize().x, pt.y - 25);
		
		if (getBounds().y + getBounds().height > getDisplay().getClientArea().height) {
			setLocation(getLocation().x, 
						getDisplay().getClientArea().height - getBounds().height);
		}
		
		
		if (getLocation().x < 0) {
			// try bottom
			region.dispose();
			region = new Region();
			region.add(new int[] { 15, 0, 30, 15, 0, 15 });
			triShell.setSize(30, 15);
			triShell.setRegion(region);
			pt.x += rect.width / 2;
			pt.y += rect.height;
			triShell.setLocation(pt.x - 15, pt.y);
			setLocation(pt.x - triShell.getSize().x, pt.y + 15);
		}
		
		triShell.open();
		open();
	}
	
	public void dispose() {
		region.dispose();
		super.dispose();
	}
	

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
