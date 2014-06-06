package org.sustudio.concise.app.gear.wordClouder;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.utils.LabelFont;

public class CAFontScheme extends Group {

	private final List<FontData> fontsData = new ArrayList<FontData>();
	
	private final Table table;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public CAFontScheme(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setFont(LabelFont.getFont());
		
		initDefaultFontsData();
		
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 50;
		gd.widthHint = 125;
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		table.setFont(getFont());
		table.setLayoutData(gd);
		
		table.setItemCount(fontsData.size());
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				FontData fd = fontsData.get(index);
				item.setText(fd.getName());
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent event) {
				
				switch(event.keyCode) {
				case SWT.DEL:
				case SWT.BS:
					if (table.getSelectionCount() > 0) {
						removeSelectedFonts();
						notifyFontChange();
					}
					break;
				}
			}
			
		});
				
		table.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseDoubleClick(MouseEvent event) {
				final TableItem item = table.getItem(new Point(event.x, event.y));
				if (item != null) {
					int index = table.indexOf(item);
					FontData fd = fontsData.get(index);
					FontDialog dlg = new FontDialog(getShell());
					dlg.setFontList(new FontData[] { fd });
					FontData fontData = dlg.open();
					if(fontData != null) {
						fd.setName(fontData.getName());
						notifyFontChange();
					}
				}
			}
			
		});
		
		final Composite comp = new Composite(this, SWT.NONE);
		comp.setLayout(new RowLayout(SWT.VERTICAL));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
		
		final Button add = new Button(comp, SWT.FLAT);
		add.setImage(SWTResourceManager.getImage(CloudOptionsComposite.class, "/org/sustudio/concise/app/icon/10-medical.png"));
		add.setToolTipText("Add font...");
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FontDialog dlg = new FontDialog(getShell());
				FontData fontData = dlg.open();
				if(fontData != null) {
					fontsData.add(fontData);
					notifyFontChange();
				}
			}
		});
		
		final Button remove = new Button(comp, SWT.FLAT);
		remove.setToolTipText("Remove selected fonts");
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionCount() > 0) {
					removeSelectedFonts();
					notifyFontChange();
				}
			}
		});
		remove.setImage(SWTResourceManager.getImage(CloudOptionsComposite.class, "/org/sustudio/concise/app/icon/201-remove.png"));
		
	}
	
	
	private void initDefaultFontsData() {
		fontsData.add(getFont().getFontData()[0]);
	}
	
	
	/**
	 * Notify {@link ColorChangedListener} the {@link ColorChangedEvent}. 
	 * Also notify table to make change.
	 */
	private void notifyFontChange() {
		table.removeAll();
		table.setItemCount(fontsData.size());
		table.redraw();
		
		for (FontChangedListener listener : fontChangedListeners) {
			listener.fontChanged(new FontChangedEvent(getFontsData()));
		}
	}
	
	/**
	 * Remove selected fonts
	 */
	private void removeSelectedFonts() {
		if (table.getSelectionCount() > 0) {
			int[] indices = table.getSelectionIndices();
			for (int i = indices.length - 1; i >= 0; i--) {
				fontsData.remove(indices[i]);
			}
		}
	}
	
	public void setFontsData(String[] fontsDataArray) {
		if (fontsDataArray != null) {
			fontsData.clear();
			for (String s : fontsDataArray) {
				FontData fd = new FontData(s);
				if (!fontsData.contains(fd)) {
					fontsData.add(fd);
				}
			}
		}
		notifyFontChange();
	}
	
	/**
	 * Returns current fonts' FontData
	 * @return
	 */
	public FontData[] getFontsData() {
		return fontsData.toArray(new FontData[0]);
	}
	
	
	//////////////////////////////////////////////////////////////
	// Font Changed Listener
	//////////////////////////////////////////////////////////////
	
	HashSet<FontChangedListener> fontChangedListeners = new HashSet<FontChangedListener>();
	
	/**
	 * Add a {@Link ColorChangedListener} to {@link CAFontScheme}.
	 * @param listener
	 */
	public void addFontChangedListener(FontChangedListener listener) {
		fontChangedListeners.add(listener);
	}
	
	/**
	 * Remove a {@Link ColorChangedListener} from {@link CAFontScheme}.
	 * @param listener
	 */
	public void removeFontChangedListener(FontChangedListener listener) {
		fontChangedListeners.remove(listener);
	}
	
	/**
	 * ColorChangedEvent
	 * 
	 * @author Kuan-ming Su
	 *
	 */
	public class FontChangedEvent extends EventObject {
		
		private static final long serialVersionUID = 8856830704822120779L;
		public FontData[] data;
		
		public FontChangedEvent(Object scheme) {
			super(scheme);
			this.data = (FontData[]) scheme;
		}
		
		public FontData[] getFontsData() {
			return data;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder("Font Scheme { ");
			for (FontData fd : data) {
				sb.append(fd.toString() + ", ");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
			sb.append(" }");
			return sb.toString();
		}
	}
	
	public interface FontChangedListener extends EventListener {
		public void fontChanged(FontChangedEvent event);
	}

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
