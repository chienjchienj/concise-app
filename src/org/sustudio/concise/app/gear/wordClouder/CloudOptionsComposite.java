/*******************************************************************************
* Copyright (c) 2011 Stephan Schwiebert. All rights reserved. This program and
* the accompanying materials are made available under the terms of the Eclipse
* Public License v1.0 which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* <p/>
* Contributors: Stephan Schwiebert - initial API and implementation
*******************************************************************************/
package org.sustudio.concise.app.gear.wordClouder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.gef4.cloudio.CloudWord;
import org.eclipse.gef4.cloudio.layout.DefaultLayouter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.sustudio.concise.app.gear.wordClouder.CAFontScheme.FontChangedEvent;
import org.sustudio.concise.app.gear.wordClouder.CAFontScheme.FontChangedListener;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.utils.ColorImage;
import org.sustudio.concise.app.utils.LabelFont;
import org.sustudio.concise.app.widgets.CAColorScheme;
import org.sustudio.concise.app.widgets.CAColorScheme.ColorChangedEvent;
import org.sustudio.concise.app.widgets.CAColorScheme.ColorChangedListener;
import org.sustudio.concise.app.widgets.CASpinner;

/**
 * Provides options to modify the rendering of a {@link TagCloudViewer} using an
 * {@link IEditableCloudLabelProvider}. 
 * 
 * @author sschwieb
 *
 */
public class CloudOptionsComposite extends Composite {

	//private TagCloudViewer viewer;
	private final WordClouder clouder;
	private List<FontData> fonts;
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public CloudOptionsComposite(Composite parent, int style, WordClouder clouder) {
		super(parent, style);
		this.clouder = clouder;
		setLayout(new GridLayout());
		setFont(LabelFont.getFont());
		
		clouder.getCloud().setMaxFontSize(CAPrefs.CLOUDER_MAX_FONT_SIZE);
		clouder.getCloud().setMinFontSize(CAPrefs.CLOUDER_MIN_FONT_SIZE);
		clouder.getCloud().setBoost(CAPrefs.CLOUDER_BOOST);
		clouder.getCloud().setBoostFactor(CAPrefs.CLOUDER_BOOST_FACTOR);
		clouder.getLabelProvider().setAngles(CAPrefs.CLOUDER_ANGLES.getAngles());
		clouder.getCloud().getLayouter().setOption(DefaultLayouter.X_AXIS_VARIATION, CAPrefs.CLOUDER_X_VARIATION);
		clouder.getCloud().getLayouter().setOption(DefaultLayouter.Y_AXIS_VARIATION, CAPrefs.CLOUDER_Y_VARIATION);
		clouder.getLabelProvider().setColors(CAPrefs.CLOUDER_COLOR_SCHEME);
		getFonts();
		clouder.getLabelProvider().setFonts(fonts);
				
		addLayoutButtons();
		addColorButtons();
		addFontButtons();
	}
	
	private void getFonts() {
		fonts = new ArrayList<FontData>();
		if (CAPrefs.CLOUDER_FONTS == null || CAPrefs.CLOUDER_FONTS.length == 0) {
			fonts.add(getFont().getFontData()[0]);
			updateCloudFontsPreference();
		}
		else {
			for (String s : CAPrefs.CLOUDER_FONTS) {
				fonts.add(new FontData(s));
			}
		}
	}
	
	private void updateCloudFontsPreference() {
		CAPrefs.CLOUDER_FONTS = new String[fonts.size()];
		for (int i = 0; i < fonts.size(); i++) {
			CAPrefs.CLOUDER_FONTS[i] = fonts.get(i).toString();
		}
	}
	
	protected Group addLayoutButtons() {
		Group buttons = new Group(this, SWT.SHADOW_IN);
		buttons.setLayout(new GridLayout(2, true));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		Label l = new Label(buttons, SWT.NONE);
		l.setText("Number of Words");
		l.setFont(getFont());
		final Combo words = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		words.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		words.setItems(new String[] {"100","200","300","400","500","600","700","800","900","1000", "1100", "1200", "1300", "1400", "1500","1600", "1700", "1800", "1900", "2000"});
		words.setText(String.valueOf(CAPrefs.CLOUDER_MAX_WORDS));
		words.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.CLOUDER_MAX_WORDS = Integer.parseInt(words.getItem(words.getSelectionIndex()));
				//clouder.setMaxWords(CAPrefs.CLOUDER_MAX_WORDS);
			}
		});
		words.setFont(getFont());
		
		l = new Label(buttons, SWT.NONE);
		l.setText("Max Font Size");
		l.setFont(getFont());
		final Combo font = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		font.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		font.setItems(new String[] {"50", "100", "150", "200", "250", "300", "350", "400", "450", "500"});
		font.setText(String.valueOf(CAPrefs.CLOUDER_MAX_FONT_SIZE));
		font.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.CLOUDER_MAX_FONT_SIZE = Integer.parseInt(font.getItem(font.getSelectionIndex()));
				clouder.getCloud().setMaxFontSize(CAPrefs.CLOUDER_MAX_FONT_SIZE);
			}
		});
		font.setFont(getFont());
		
		l = new Label(buttons, SWT.NONE);
		l.setText("Min Font Size");
		l.setFont(getFont());
		final Combo minFont = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		minFont.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		minFont.setItems(new String[] {"10", "15", "20", "25", "30", "35", "40", "45", "50"});
		minFont.setText(String.valueOf(CAPrefs.CLOUDER_MIN_FONT_SIZE));
		minFont.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.CLOUDER_MIN_FONT_SIZE = Integer.parseInt(minFont.getItem(minFont.getSelectionIndex()));
				clouder.getCloud().setMinFontSize(CAPrefs.CLOUDER_MIN_FONT_SIZE);
			}
		});
		minFont.setFont(getFont());
				
		l = new Label(buttons, SWT.NONE);
		l.setText("Boost");
		l.setFont(getFont());
		final Combo boost = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		boost.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		boost.setItems(new String[] {"0", "1", "2","3","4","5","6","7","8","9","10"});
		boost.setText(String.valueOf(CAPrefs.CLOUDER_BOOST));
		boost.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.CLOUDER_BOOST = Integer.parseInt(boost.getItem(boost.getSelectionIndex()));
				clouder.getCloud().setBoost(CAPrefs.CLOUDER_BOOST);
			}
		});
		boost.setFont(getFont());
				
		l = new Label(buttons, SWT.NONE);
		l.setText("Boost Factor");
		l.setFont(getFont());
		final Combo boostFactor = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		boostFactor.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		boostFactor.setItems(new String[] {"1.0","1.5","2.0","2.5","3.0","3.5", "4.0", "4.5", "5.0"});
		boostFactor.setText(String.valueOf(CAPrefs.CLOUDER_BOOST_FACTOR));
		boostFactor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.CLOUDER_BOOST_FACTOR = Float.parseFloat(boostFactor.getItem(boostFactor.getSelectionIndex()));
				clouder.getCloud().setBoostFactor(CAPrefs.CLOUDER_BOOST_FACTOR);
			}
		});
		boostFactor.setFont(getFont());
		
		
		l = new Label(buttons, SWT.NONE);
		l.setText("Angles");
		l.setFont(getFont());
		final Combo angles = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		angles.setFont(getFont());
		angles.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		angles.setItems(CloudAngle.stringValues());
		angles.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = angles.getSelectionIndex();
				CAPrefs.CLOUDER_ANGLES = CloudAngle.values()[index];
				CloudLabelProvider lp = clouder.getLabelProvider();
				lp.setAngles(CAPrefs.CLOUDER_ANGLES.getAngles());
			}
		});
		angles.select(CAPrefs.CLOUDER_ANGLES.ordinal());
		
		l = new Label(buttons, SWT.NONE);
		l.setText("X Axis Variation");
		l.setFont(getFont());
		final Combo xAxis = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		xAxis.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		xAxis.setItems(new String[] {"0", "10","20","30","40","50", "60", "70", "80", "90", "100"});
		xAxis.setText(String.valueOf(CAPrefs.CLOUDER_X_VARIATION));
		xAxis.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.CLOUDER_X_VARIATION = Integer.parseInt(xAxis.getItem(xAxis.getSelectionIndex()));
				clouder.getCloud().getLayouter().setOption(DefaultLayouter.X_AXIS_VARIATION, CAPrefs.CLOUDER_X_VARIATION);
			}
		});
		xAxis.setFont(getFont());
		
		l = new Label(buttons, SWT.NONE);
		l.setText("Y Axis Variation");
		l.setFont(getFont());
		final Combo yAxis = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		yAxis.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		yAxis.setItems(new String[] {"0", "10","20","30","40","50", "60", "70", "80", "90", "100"});
		yAxis.setText(String.valueOf(CAPrefs.CLOUDER_Y_VARIATION));
		yAxis.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.CLOUDER_Y_VARIATION = Integer.parseInt(yAxis.getItem(yAxis.getSelectionIndex())); 
				clouder.getCloud().getLayouter().setOption(DefaultLayouter.Y_AXIS_VARIATION, CAPrefs.CLOUDER_Y_VARIATION);
			}
		});
		yAxis.setFont(getFont());
		
		Button run = new Button(buttons, SWT.NONE);
		run.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		run.setText("Re-Position");
		run.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CASpinner spinner = new CASpinner(clouder);
				spinner.setMessage("Layouting word cloud...");
				spinner.open();
				clouder.getCloud().layoutCloud(false);
				spinner.close();
			}
		});
		run.setFont(getFont());
		Button layout = new Button(buttons, SWT.NONE);
		layout.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		layout.setText("Re-Layout");
		layout.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				CASpinner spinner = new CASpinner(clouder);
				spinner.setMessage("Layouting word cloud...");
				spinner.open();
				clouder.reLayout();
				spinner.close();
			}
			
		});
		layout.setFont(getFont());
		return buttons;
	}
	
	protected Group addColorButtons() {
		Group buttons = new Group(this, SWT.SHADOW_IN);
		buttons.setLayout(new GridLayout(2, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		CAColorScheme scheme = new CAColorScheme(buttons, SWT.NONE);
		scheme.setText("Color");
		scheme.setColorScheme(CAPrefs.CLOUDER_COLOR_SCHEME);
		scheme.addColorChangedListener(new ColorChangedListener() {

			@Override
			public void colorChanged(ColorChangedEvent event) {
				
				CAPrefs.CLOUDER_COLOR_SCHEME = event.getColorScheme();
				CloudLabelProvider lp = (CloudLabelProvider) clouder.getLabelProvider();
				lp.setColors(CAPrefs.CLOUDER_COLOR_SCHEME);
				List<CloudWord> words = clouder.getCloud().getWords();
				for (CloudWord word : words) {
					word.setColor(lp.getColor(word.data));
				}
				clouder.getCloud().redrawTextLayerImage();
				
			}
			
		});
		
		Composite comp = new Composite(buttons, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan=2;
		comp.setLayout(new GridLayout(2, true));
		comp.setLayoutData(gd);
		final Button bg = new Button(comp, SWT.FLAT);
		bg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		bg.setText("Background");
		bg.setImage(ColorImage.createImage(clouder.getCloud().getBackground(), 16, 16));
		bg.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ColorDialog cd = new ColorDialog(getShell());
				RGB color = cd.open();
				if(color == null) return;
				Color old = clouder.getCloud().getBackground();
				Color c = new Color(getDisplay(), color);
				clouder.getCloud().setBackground(c);
				old.dispose();
				clouder.getCloud().redrawTextLayerImage();
				Image oldImage = bg.getImage();
				Image newImage = ColorImage.createImage(color, 16, 16);
				bg.setImage(newImage);
				oldImage.dispose();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		bg.setFont(getFont());
		final Button sel = new Button(comp, SWT.FLAT);
		sel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sel.setText("Selection");
		sel.setImage(ColorImage.createImage(clouder.getCloud().getSelectionColor(), 16, 16));
		sel.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ColorDialog cd = new ColorDialog(getShell());
				RGB color = cd.open();
				if(color == null) return;
				Color old = clouder.getCloud().getSelectionColor();
				Color c = new Color(getDisplay(), color);
				clouder.getCloud().setSelectionColor(c);
				old.dispose();
				clouder.getCloud().redrawTextLayerImage();
				Image oldImage = sel.getImage();
				Image newImage = ColorImage.createImage(color, 16, 16);
				sel.setImage(newImage);
				oldImage.dispose();
			}
		});
		sel.setFont(getFont());
		return buttons;
	}
	
	protected Group addFontButtons() {
		Group buttons = new Group(this, SWT.SHADOW_IN);
		buttons.setLayout(new GridLayout(1, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		CAFontScheme fontScheme = new CAFontScheme(buttons, SWT.NONE);
		fontScheme.setText("Font");
		fontScheme.setFontsData(CAPrefs.CLOUDER_FONTS);
		fontScheme.addFontChangedListener(new FontChangedListener() {

			@Override public void fontChanged(FontChangedEvent event) {
				fonts = Arrays.asList(event.getFontsData());
				updateCloudFontsPreference();
				CloudLabelProvider lp = (CloudLabelProvider) clouder.getLabelProvider();
				lp.setFonts(fonts);
				List<CloudWord> words = clouder.getCloud().getWords();
				for (CloudWord word : words) {
					int height = word.getFontData()[0].getHeight();
					FontData[] fdata = lp.getFontData(word.data);
					fdata[0].setHeight(height);;
					word.setFontData(fdata);
				}
				clouder.getCloud().redrawTextLayerImage();
			}
			
		});
		return buttons;
	}
}
