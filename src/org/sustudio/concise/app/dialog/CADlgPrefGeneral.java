package org.sustudio.concise.app.dialog;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.IGearFileRevealable;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.widgets.CAColorScheme;
import org.sustudio.concise.app.widgets.CAColorScheme.ColorChangedEvent;
import org.sustudio.concise.app.widgets.CAColorScheme.ColorChangedListener;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;

public class CADlgPrefGeneral extends Composite {

	private enum TopRecords {
		All (-1),
		Top100 (100),
		Top500 (500),
		Top1000 (1000),
		Top5000 (5000),
		Top10000 (10000);
		
		private int records;
		
		TopRecords(int records) {
			this.records = records;
		}
		
		public int getRecords() {
			return records;
		}
		
		public static String[] stringValues() {
			String[] s = new String[0];
			for (TopRecords t : TopRecords.values()) {
				if (t.getRecords() == -1) {
					s = ArrayUtils.add(s, "All");
				} else {
					s = ArrayUtils.add(s, String.valueOf(t.getRecords()));
				}
			}
			return s;
		}
		
		public static int indexOf(int records) {
			for (TopRecords t : TopRecords.values()) {
				if (t.getRecords() == records) {
					return t.ordinal();
				}
			}
			return -1;
		}
		
	}
	
	public CADlgPrefGeneral(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		final Group colorGroup = new Group(this, SWT.BORDER);
		colorGroup.setText(CABundle.get("preferences.general.color") + ":");
		colorGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		colorGroup.setLayout(new GridLayout(2, true));
		
		final CAColorScheme fgColorScheme = new CAColorScheme(colorGroup, SWT.NONE);
		fgColorScheme.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		fgColorScheme.setText("Foreground Highlight");
		fgColorScheme.setColorScheme(CAPrefs.HIGHLIGH_FG_COLOR_SCHEME);
		fgColorScheme.addColorChangedListener(new ColorChangedListener() {

			@Override
			public void colorChanged(ColorChangedEvent event) {
				CAPrefs.HIGHLIGH_FG_COLOR_SCHEME = event.getColorScheme();
				for (GearController gearView : GearController.getVisibleGearViews()) {
					gearView.redraw();
				}
			}
			
		});
		
		final CAColorScheme bgColorScheme = new CAColorScheme(colorGroup, SWT.NONE);
		bgColorScheme.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		bgColorScheme.setText("Background Highlight");
		bgColorScheme.setColorScheme(CAPrefs.HIGHLIGH_BG_COLOR_SCHEME);
		bgColorScheme.addColorChangedListener(new ColorChangedListener() {

			@Override
			public void colorChanged(ColorChangedEvent event) {
				CAPrefs.HIGHLIGH_BG_COLOR_SCHEME = event.getColorScheme();
				for (GearController gearView : GearController.getVisibleGearViews()) {
					gearView.redraw();
				}
			}
			
		});
		
		final Group displayGroup = new Group(this, SWT.BORDER);
		displayGroup.setText(CABundle.get("preferences.general.display") + ":");
		displayGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
		displayGroup.setLayout(new GridLayout(2, false));
		
		final Button btnFullpath = new Button(displayGroup, SWT.CHECK);
		btnFullpath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.SHOW_FULL_FILEPATH = btnFullpath.getSelection();
				for (GearController gearView : GearController.getVisibleGearViews()) {
					if (gearView instanceof IGearFileRevealable) {
						gearView.redraw();
					}
				}
			}
		});
		btnFullpath.setSelection(CAPrefs.SHOW_FULL_FILEPATH);
		btnFullpath.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnFullpath.setText(CABundle.get("preferences.general.showFullFilePath"));
		
		Group grpRecords = new Group(displayGroup, SWT.NONE);
		grpRecords.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		grpRecords.setText("Records");
		grpRecords.setLayout(new GridLayout(3, false));
		
		Label lblShow = new Label(grpRecords, SWT.NONE);
		lblShow.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblShow.setText("Show");
		
		final Combo combo = new Combo(grpRecords, SWT.READ_ONLY);
		combo.setItems(TopRecords.stringValues());
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo.select(TopRecords.indexOf(CAPrefs.TOP_RECORDS));
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				CAPrefs.TOP_RECORDS = TopRecords.values()[combo.getSelectionIndex()].getRecords();
			}
		});
		
		Label lblRecords = new Label(grpRecords, SWT.NONE);
		lblRecords.setText("Records");
		
		final Button btnShowProgramIcon = new Button(displayGroup, SWT.CHECK);
		btnShowProgramIcon.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CAPrefs.SHOW_PROGRAM_ICON = btnShowProgramIcon.getSelection();
				for (GearController gearView : GearController.getVisibleGearViews()) {
					if (gearView instanceof IGearFileRevealable) {
						gearView.redraw();
					}
				}
			}
		});
		btnShowProgramIcon.setSelection(CAPrefs.SHOW_PROGRAM_ICON);
		btnShowProgramIcon.setText(CABundle.get("preferences.general.showProgramIcon"));
	}
}
