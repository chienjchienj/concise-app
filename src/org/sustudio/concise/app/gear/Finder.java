package org.sustudio.concise.app.gear;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.widgets.CACancelButton;
import org.sustudio.concise.app.widgets.CAScopeButton;

public class Finder extends Composite {

	protected enum OPERATOR { 
		CONTAINS("contains"), 
		STARTSWITH("starts with"), 
		ENDSWITH("ends with"), 
		GREATER(">"), 
		LESS("<"), 
		EQUAL("="),
		;
		
		final String label;
		OPERATOR(String label) {
			this.label = label;
		}
		
		public String makeSyntaxWithColumnAndValue(String column, String string) {
			switch (this) {
			case CONTAINS:		return column + " LIKE '%" + string + "%'";
			case ENDSWITH:		return column + " LIKE '%" + string + "'"; 
			case STARTSWITH:	return column + " LIKE '" + string + "%'";
			default:			break;
			}
			
			switch (this) {
			case GREATER:
			case LESS:
			case EQUAL:			return column + " " + label + " " + string;
			default:			return null;
			}
		}
		
		
		public static OPERATOR labelValueOf(String label) {
			for (OPERATOR operator : OPERATOR.values()) {
				if (operator.label.equals(label)) {
					return operator;
				}
			}
			return null;
		}
	}
	
	private String whereSyntax = null;
	private boolean hidden;
	private final Gear gear;
	private final Label lblFinder;
	private final Text finderText;
	
	private final Font font = SWTResourceManager.getFont("Lucida Grande", 11, SWT.NORMAL);
	private CAScopeButton column;
	private CAScopeButton operator;
	
	public Finder(Gear gear) {
		super(gear.getController(Concise.getCurrentWorkspace()), SWT.EMBEDDED);
		this.gear = gear;
		setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
		GridLayout gridLayout = new GridLayout(5, false);
		gridLayout.marginRight = 3;
		gridLayout.marginLeft = 3;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
		
		lblFinder = new Label(this, SWT.RIGHT);
		lblFinder.setText("Find");
		lblFinder.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblFinder.setFont(font);
		
		column = new CAScopeButton(this, SWT.POP_UP);
		column.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setComboOperator(column.getSelectionIndex());
			}
		});
		column.setFont(font);
		
		operator = new CAScopeButton(this, SWT.POP_UP);
		operator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		operator.setFont(font);
		
		finderText = new Text(this, SWT.BORDER | SWT.SEARCH);
		GridData gd_filterText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_filterText.widthHint = 150;
		gd_filterText.heightHint = 15;
		finderText.setLayoutData(gd_filterText);
		finderText.setFont(font);
		finderText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					setHidden(true);
				}
			}
		});
		finderText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				OPERATOR op = OPERATOR.labelValueOf(operator.getText());
				if (op == null) return;
				String value = finderText.getText().trim();
				switch (op) {
				case GREATER:
				case LESS:
				case EQUAL:
					try {
						// check number format
						Double.parseDouble(value);
					} catch (NumberFormatException e) {
						Dialog.error("Wrong Format!", value + " is not a valid number.");
						return;
					}
					break;
				default:
					break;
				}
				
				String syntax = op.makeSyntaxWithColumnAndValue(column.getText(), value);
				Finder.this.whereSyntax = syntax;
				Finder.this.gear.getController(Concise.getCurrentWorkspace()).loadData();
			}
		});
		
		final CACancelButton btnDone = new CACancelButton(this, SWT.NONE);
		btnDone.setToolTipText("Done");
		btnDone.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setHidden(true);
			}
		});		
		
		setComboColumn();
		
	}
	
	public String whereSyntax() {
		return whereSyntax;
	}
	
	public void setHidden(boolean hidden) {
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.heightHint = hidden ? 0 : computeSize(getClientArea().width, SWT.DEFAULT).y;
		setLayoutData(gridData);
		getParent().layout();
		this.hidden = hidden;
		
		if (hidden) {
			whereSyntax = null;
			
			// reload gear's original data
			if (gear.getController(Concise.getCurrentWorkspace()).getControl() != null) {
				gear.getController(Concise.getCurrentWorkspace()).loadData();
			}
		}
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	
	private void setComboColumn() {
		switch (gear) {
		case CorpusManager:
		case ReferenceCorpusManager:
		case Collocator:
		case Concordancer:
		case KeywordLister:
		case WordCluster:
		case WordLister:
			String[] items = CATable.valueOf(gear.name()).columns();
			column.setItems(items);
			break;
		
		default:
			break;
		
		}
		if (column.getItemCount() > 0) {
			column.select(0);
			setComboOperator(0);
		}
	}
	
	
	private void setComboOperator(int index) {
		DBColumn[] dbColumns = CATable.valueOf(gear.name()).dbColumns();
		DBColumn column = dbColumns[index];
		if (column.getDataType().isNumericType()) {
			operator.setItems(new String[] {
				OPERATOR.GREATER.label, 
				OPERATOR.LESS.label,
				OPERATOR.EQUAL.label	
			});
		}
		else if (column.getDataType().isTextType()) {
			operator.setItems(new String[] {
				OPERATOR.CONTAINS.label,
				OPERATOR.STARTSWITH.label,
				OPERATOR.ENDSWITH.label	
			});
		}
		if (operator.getItemCount() > 0) {
			operator.select(0);
		}
		layout();
	}
}
