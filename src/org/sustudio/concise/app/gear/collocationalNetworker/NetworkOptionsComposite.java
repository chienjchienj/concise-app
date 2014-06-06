package org.sustudio.concise.app.gear.collocationalNetworker;

import org.eclipse.gef4.layout.algorithms.SpringLayoutAlgorithm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.utils.ColorImage;
import org.sustudio.concise.app.utils.LabelFont;
import org.sustudio.concise.app.widgets.CAColorScheme;
import org.sustudio.concise.app.widgets.CAColorScheme.ColorChangedEvent;
import org.sustudio.concise.app.widgets.CAColorScheme.ColorChangedListener;
import org.sustudio.concise.core.collocation.CollocateMeasurement;

public class NetworkOptionsComposite extends Composite {
	
	private final CollocationalNetworker networker;
	private Runnable r;
	
	public NetworkOptionsComposite(Composite parent, int style, final CollocationalNetworker networker) {
		super(parent, style);
		this.networker = networker;
		setFont(LabelFont.getFont());		
		setLayout(new GridLayout(1, false));
		
		ExpandBar expandBar = new ExpandBar(this, SWT.V_SCROLL);
		expandBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		expandBar.setFont(getFont());
		
		ExpandItem filterItem = new ExpandItem(expandBar, SWT.NONE);
		filterItem.setExpanded(true);
		filterItem.setText("Options");
		filterItem.setControl(addFilterButtons(expandBar));
		filterItem.setHeight(filterItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		ExpandItem layoutItem = new ExpandItem(expandBar, SWT.NONE);
		layoutItem.setExpanded(true);
		layoutItem.setText("Layout");
		layoutItem.setControl(addLayoutButtons(expandBar));
		layoutItem.setHeight(layoutItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		ExpandItem associationFilterItem = new ExpandItem(expandBar, SWT.NONE);
		associationFilterItem.setExpanded(false);
		associationFilterItem.setText("Association Filters");
		associationFilterItem.setControl(addAssociationFilterButtons(expandBar));
		associationFilterItem.setHeight(associationFilterItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		ExpandItem styleItem = new ExpandItem(expandBar, SWT.NONE);
		styleItem.setExpanded(true);
		styleItem.setText("Style");
		styleItem.setControl(addStyleButtons(expandBar));
		styleItem.setHeight(styleItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
	}
	
	private void addIntegerFilterSelection(Composite parent, final CollocateMeasurement measurement) 
	{
		Double value = CAPrefs.NETWORK_FILTERS.get(measurement);
		final Button btn = new Button(parent, SWT.CHECK);
		btn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btn.setText(measurement.name() + " (≥):");
		btn.setSelection(value != null);
		btn.setFont(getFont());
				
		final Spinner spn = new Spinner(parent, SWT.BORDER);
		spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spn.setSelection((int) (value == null ? measurement.defaultCutOff() : value));
		spn.setEnabled(btn.getSelection());
		spn.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				CAPrefs.NETWORK_FILTERS.put(measurement, Double.valueOf(spn.getSelection()));
			}
		});
		spn.setFont(getFont());
		
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				spn.setEnabled(btn.getSelection());
				if (btn.getSelection()) {
					CAPrefs.NETWORK_FILTERS.put(measurement, Double.valueOf(spn.getSelection()));
				}
				else {
					CAPrefs.NETWORK_FILTERS.remove(measurement);
				}
			}
		});
	}
	
	private void addDoubleFilterSelection(Composite parent, final CollocateMeasurement measurement) 
	{
		Double value = CAPrefs.NETWORK_FILTERS.get(measurement);
		final Button btn = new Button(parent, SWT.CHECK);
		btn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btn.setText(measurement.name() + " (≥):");
		btn.setSelection(value != null);
		btn.setFont(getFont());
				
		final Spinner spn = new Spinner(parent, SWT.BORDER);
		spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spn.setPageIncrement(1000);
		spn.setMaximum(50000);
		spn.setMinimum(-50000);
		spn.setIncrement(100);
		spn.setDigits(3);
		spn.setSelection((int) ((value == null ? measurement.defaultCutOff() : value) * 1000));
		spn.setEnabled(btn.getSelection());
		spn.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				CAPrefs.NETWORK_FILTERS.put(measurement, Double.valueOf(spn.getSelection()));
			}
		});
		spn.setFont(getFont());
		
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				spn.setEnabled(btn.getSelection());
				if (btn.getSelection()) {
					CAPrefs.NETWORK_FILTERS.put(measurement, Double.valueOf((double) spn.getSelection() / 1000d));
				}
				else {
					CAPrefs.NETWORK_FILTERS.remove(measurement);
				}
			}
		});
	}
	
	
	private Composite addFilterButtons(Composite composite) {
		
		Group buttons = new Group(composite, SWT.SHADOW_IN);
		buttons.setLayout(new GridLayout(2, true));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Label l = new Label(buttons, SWT.NONE);
		l.setText("Top Collocates");
		l.setFont(getFont());
		final Spinner topCollocates = new Spinner(buttons, SWT.BORDER);
		topCollocates.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.TOP_COLLOCATES = topCollocates.getSelection();
			}
		});
		topCollocates.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		topCollocates.setSelection(CAPrefs.TOP_COLLOCATES);
		topCollocates.setFont(getFont());
		
		l = new Label(buttons, SWT.NONE);
		l.setText("Sort By");
		l.setFont(getFont());
		final Combo filter = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		filter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.NETWORK_COMPARATOR = CollocateMeasurement.values()[filter.getSelectionIndex()];
			}
		});
		filter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		filter.setItems(CollocateMeasurement.stringValues());
		filter.select(CAPrefs.NETWORK_COMPARATOR.ordinal());
		filter.setFont(getFont());
		
		
		l = new Label(buttons, SWT.NONE);
		l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		l.setText("Network Depth:");
		l.setFont(getFont());
		
		final Spinner spn = new Spinner(buttons, SWT.BORDER);
		spn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		spn.setMinimum(1);
		spn.setMaximum(Integer.MAX_VALUE);
		spn.setFont(getFont());
		spn.setEnabled(CAPrefs.NETWORK_DEPTH < Integer.MAX_VALUE);
		spn.setSelection(CAPrefs.NETWORK_DEPTH);
		spn.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				CAPrefs.NETWORK_DEPTH = spn.getSelection();
			}
		});
		
		new Label(buttons, SWT.NONE);
		
		final Button btnDeadEnd = new Button(buttons, SWT.CHECK);
		btnDeadEnd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				spn.setSelection(btnDeadEnd.getSelection() ? Integer.MAX_VALUE : 1);
				spn.setEnabled(!btnDeadEnd.getSelection());
			}
		});
		btnDeadEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnDeadEnd.setText("Dead End");
		btnDeadEnd.setFont(getFont());
		btnDeadEnd.setSelection(CAPrefs.NETWORK_DEPTH == Integer.MAX_VALUE);
				
		addIntegerFilterSelection(buttons, CollocateMeasurement.Cooccurrence);
		
		return buttons;
	}
	
	private Composite addAssociationFilterButtons(Composite composite) {
		
		Group buttons = new Group(composite, SWT.SHADOW_IN);
		buttons.setLayout(new GridLayout(2, true));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		addDoubleFilterSelection(buttons, CollocateMeasurement.MI);
		addDoubleFilterSelection(buttons, CollocateMeasurement.MI3);
		addDoubleFilterSelection(buttons, CollocateMeasurement.TScore);
		addDoubleFilterSelection(buttons, CollocateMeasurement.ZScore);
		addDoubleFilterSelection(buttons, CollocateMeasurement.SimpleLL);
		addDoubleFilterSelection(buttons, CollocateMeasurement.Dice);
		addDoubleFilterSelection(buttons, CollocateMeasurement.OddsRatio);
		addDoubleFilterSelection(buttons, CollocateMeasurement.ChiSquaredCorr);
		addDoubleFilterSelection(buttons, CollocateMeasurement.LogLikelihood);
		
		return buttons;
	}
	
	
	private Composite addLayoutButtons(Composite composite) {
		
		Group buttons = new Group(composite, SWT.SHADOW_IN);
		buttons.setLayout(new GridLayout(2, true));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Combo comLayout = new Combo(buttons, SWT.READ_ONLY);
		comLayout.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		comLayout.setItems(NetworkLayout.stringValues());
		comLayout.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = ((Combo) e.widget).getSelectionIndex();
				NetworkLayout layout = NetworkLayout.values()[index];
				CAPrefs.NETWORK_LAYOUT = layout;
				networker.getNetworkGraph().setLayoutAlgorithm(layout.getAlgorithm(), false);
			}
		});
		comLayout.select(CAPrefs.NETWORK_LAYOUT.ordinal());
		comLayout.setFont(getFont());
		
		Label l = new Label(buttons, SWT.NONE);
		l.setText("Layout");
		l.setFont(getFont());
		
		final Button btnRender = new Button(buttons, SWT.NONE);
		btnRender.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnRender.setText("Run");
		btnRender.setFont(getFont());
		btnRender.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				networker.getNetworkGraph().setDynamicLayout(CAPrefs.NETWORK_LAYOUT == NetworkLayout.Dynamic);
				if (CAPrefs.NETWORK_LAYOUT != NetworkLayout.Dynamic) {
					networker.getNetworkGraph().applyLayout();
				}
				else {
					if (btnRender.getText().equals("Run")) {
						getShell().setDefaultButton(btnRender);
						btnRender.setText("Stop");
						final SpringLayoutAlgorithm algorithm = (SpringLayoutAlgorithm) CAPrefs.NETWORK_LAYOUT.getAlgorithm();
						r = new Runnable() {
							public void run() {
								algorithm.performNIteration(1);
								networker.getNetworkGraph().redraw();
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// eat...
								}
								getDisplay().asyncExec(r);
							}
						};
						getDisplay().asyncExec(r);
					}
					else {
						getShell().setDefaultButton(null);
						r = null;
						btnRender.setText("Run");
					}
				}
			}
		});
		
		return buttons;
	}
	
	private Composite addStyleButtons(Composite composite) {
		
		Composite c = new Composite(composite, SWT.NONE);
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group buttons = new Group(c, SWT.SHADOW_IN);
		buttons.setLayout(new GridLayout(1, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		CAColorScheme colorScheme = new CAColorScheme(buttons, SWT.NONE);
		colorScheme.addColorChangedListener(new ColorChangedListener() {

			@Override
			public void colorChanged(ColorChangedEvent event) {
				RGB[] rgbs = event.getColorScheme();
				CAPrefs.NETWORK_COLOR_SCHEME = rgbs;
				networker.getNetworkGraph().setColorScheme(rgbs);
			}
			
		});
		colorScheme.setColorScheme(CAPrefs.NETWORK_COLOR_SCHEME);
		
		Composite comp = new Composite(buttons, SWT.NONE);
		comp.setLayout(new GridLayout(2, true));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		final Button bg = new Button(comp, SWT.FLAT);
		bg.setFont(getFont());
		bg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		bg.setText("Background");
		bg.setImage(ColorImage.createImage(CAPrefs.NETWORK_BACKGROUND_RGB, 16, 16));
		bg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog cd = new ColorDialog(getShell());
				RGB color = cd.open();
				if(color == null) return;
				CAPrefs.NETWORK_BACKGROUND_RGB = color;
				Color old = networker.getNetworkGraph().getBackground();
				Color c = new Color(getDisplay(), color);
				networker.getNetworkGraph().setBackground(c);
				old.dispose();
				Image oldImage = bg.getImage();
				Image newImage = ColorImage.createImage(color, 16, 16);
				bg.setImage(newImage);
				oldImage.dispose();
			}
		});
		networker.getNetworkGraph().setBackground(new Color(getDisplay(), CAPrefs.NETWORK_BACKGROUND_RGB));
		
		final Button fg = new Button(comp, SWT.FLAT);
		fg.setFont(getFont());
		fg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fg.setText("Label");
		fg.setImage(ColorImage.createImage(CAPrefs.NETWORK_LABEL_RGB, 16, 16));
		fg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog cd = new ColorDialog(getShell());
				RGB color = cd.open();
				if(color == null) return;
				CAPrefs.NETWORK_LABEL_RGB = color;
				Color old = networker.getNetworkGraph().getLabelColor();
				Color c = new Color(getDisplay(), color);
				old.dispose();
				Image oldImage = fg.getImage();
				Image newImage = ColorImage.createImage(color, 16, 16);
				fg.setImage(newImage);
				oldImage.dispose();
				
				CAPrefs.NETWORK_LABEL_RGB = c.getRGB();
				networker.getNetworkGraph().setLabelColor(c);
			}
		});
		
		
		buttons = new Group(c, SWT.SHADOW_IN);
		buttons.setLayout(new GridLayout(2, true));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Label l = new Label(buttons, SWT.NONE);
		l.setText("Max Node Size");
		l.setFont(getFont());
		
		final Combo maxSize = new Combo(buttons, SWT.READ_ONLY);
		l = new Label(buttons, SWT.NONE);
		l.setText("Min Node Size");
		l.setFont(getFont());
		final Combo minSize = new Combo(buttons, SWT.READ_ONLY);
		
		maxSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		maxSize.setItems(new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100" });
		maxSize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int max = Integer.parseInt(maxSize.getText());
				int min = Integer.parseInt(minSize.getText());
				if (min > max) {
					Dialog.inform("Something wrong!", "Max Node Size must be greater or equal to Min Node Size");
					maxSize.setText(String.valueOf(CAPrefs.NETWORK_MAX_NODE_SIZE));
					minSize.setText(String.valueOf(CAPrefs.NETWORK_MIN_NODE_SIZE));
					return;
				}
				CAPrefs.NETWORK_MAX_NODE_SIZE = max;
				CAPrefs.NETWORK_MIN_NODE_SIZE = min;
				
				networker.resizeNodesByDegree();
			}
		});
		maxSize.setText(String.valueOf(CAPrefs.NETWORK_MAX_NODE_SIZE));
		maxSize.setFont(getFont());
		
		minSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		minSize.setItems(new String[] { "0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100" });
		minSize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				int max = Integer.parseInt(maxSize.getText());
				int min = Integer.parseInt(minSize.getText());
				if (min > max) {
					Dialog.inform("Something wrong!", "Max Node Size must be greater or equal to Min Node Size");
					maxSize.setText(String.valueOf(CAPrefs.NETWORK_MAX_NODE_SIZE));
					minSize.setText(String.valueOf(CAPrefs.NETWORK_MIN_NODE_SIZE));
					return;
				}
				CAPrefs.NETWORK_MAX_NODE_SIZE = max;
				CAPrefs.NETWORK_MIN_NODE_SIZE = min;
				
				networker.resizeNodesByDegree();
			}
		});
		minSize.setText(String.valueOf(CAPrefs.NETWORK_MIN_NODE_SIZE));
		minSize.setFont(getFont());
		
		final Button btnHideNonSelected = new Button(c, SWT.CHECK);
		btnHideNonSelected.setText("Hide Non Selected Label");
		btnHideNonSelected.setSelection(CAPrefs.NETWORK_HIDE_NON_SELECTED);
		btnHideNonSelected.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				CAPrefs.NETWORK_HIDE_NON_SELECTED = btnHideNonSelected.getSelection();
				networker.getNetworkGraph().setHideNonSelectedEnabled(CAPrefs.NETWORK_HIDE_NON_SELECTED);
			}
		});
		btnHideNonSelected.setFont(getFont());
		
		return c;
	}
}
