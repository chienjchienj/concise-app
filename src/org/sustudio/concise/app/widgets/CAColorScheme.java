package org.sustudio.concise.app.widgets;

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
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.gear.wordClouder.CloudOptionsComposite;
import org.sustudio.concise.app.utils.ColorImage;
import org.sustudio.concise.app.utils.LabelFont;

public class CAColorScheme extends Group {

	private List<List<RGB>> colorSchemes = new ArrayList<List<RGB>>();
	private List<RGB> colorRGBs = new ArrayList<RGB>();
	
	private final Table table;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public CAColorScheme(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setFont(LabelFont.getFont());
		
		initDefaultColorSchemes();
		
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 125;
		gd.widthHint = 125;
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		table.setFont(getFont());
		table.setLayoutData(gd);
		
		table.setItemCount(colorRGBs.size());
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				RGB rgb = colorRGBs.get(index);
				item.setText(rgb.toString());
				item.setImage(ColorImage.createImage(rgb, 24, 24));
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent event) {
				
				switch(event.keyCode) {
				case SWT.DEL:
				case SWT.BS:
					if (table.getSelectionCount() > 0) {
						removeSelectedColors();
						notifyColorChange();
					}
					break;
				}
			}
			
		});
		
		final Cursor defaultCursor = table.getCursor();
		final Cursor handCursor = new Cursor(getDisplay(), SWT.CURSOR_HAND);
		table.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent event) {
				final Point p = new Point(event.x, event.y);
				final TableItem item = table.getItem(p);
				if (item != null) {
					Cursor cursor = defaultCursor;
					Rectangle rect = item.getImageBounds(0);
					if (rect.contains(p)) {
						cursor = handCursor;
					}
					table.setCursor(cursor);
				}
			}
			
		});
		
		table.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent event) {
				final Point p = new Point(event.x, event.y);
				final TableItem item = table.getItem(p);
				if (item != null) {
					int index = table.indexOf(item);
					Rectangle rect = item.getImageBounds(0);
					if (rect.contains(p)) {
						// color dialog
						ColorDialog cd = new ColorDialog(getShell());
						cd.setRGB(colorRGBs.get(index));
						RGB rgb = cd.open();
						if (rgb != null) {
							colorRGBs.get(index).red 	= rgb.red;
							colorRGBs.get(index).green 	= rgb.green;
							colorRGBs.get(index).blue 	= rgb.blue;
							notifyColorChange();
						}
					}
				}
			}
			
		});
		
		final Composite comp = new Composite(this, SWT.NONE);
		comp.setLayout(new RowLayout(SWT.VERTICAL));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true));
		
		final Button add = new Button(comp, SWT.FLAT);
		add.setImage(SWTResourceManager.getImage(CloudOptionsComposite.class, "/org/sustudio/concise/app/icon/10-medical.png"));
		add.setToolTipText("Add color...");
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ColorDialog cd = new ColorDialog(getShell());
				RGB color = cd.open();
				if(color != null) {
					colorRGBs.add(color);
					notifyColorChange();
				}
			}
		});
		
		final Button remove = new Button(comp, SWT.FLAT);
		remove.setToolTipText("Remove selected colors");
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionCount() > 0) {
					removeSelectedColors();
					notifyColorChange();
				}
			}
		});
		remove.setImage(SWTResourceManager.getImage(CloudOptionsComposite.class, "/org/sustudio/concise/app/icon/201-remove.png"));
		
		final Button toggle = new Button(comp, SWT.FLAT);
		toggle.setToolTipText("Toggle Colors");
		toggle.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				nextColorScheme();
				notifyColorChange();
			}
		});
		toggle.setImage(SWTResourceManager.getImage(CloudOptionsComposite.class, "/org/sustudio/concise/app/icon/05-shuffle.png"));
		
	}
	
	/**
	 * Notify {@link ColorChangedListener} the {@link ColorChangedEvent}. 
	 * Also notify table to make change.
	 */
	private void notifyColorChange() {
		table.removeAll();
		table.setItemCount(colorRGBs.size());
		table.redraw();
		
		for (ColorChangedListener listener : colorChangedListeners) {
			listener.colorChanged(new ColorChangedEvent(getColorScheme()));
		}
	}
	
	/**
	 * Remove selected colors
	 */
	private void removeSelectedColors() {
		if (table.getSelectionCount() > 0) {
			int[] indices = table.getSelectionIndices();
			for (int i = indices.length - 1; i >= 0; i--) {
				colorRGBs.remove(indices[i]);
			}
		}
	}
	
	public void setColorScheme(RGB[] rgbs) {
		if (rgbs != null) {
			// check if this color scheme exists
			boolean exists = false;
			for (List<RGB> scheme : colorSchemes) {
				if (scheme.size() == rgbs.length) {
					exists = true;
					for (int i=0; i<rgbs.length; i++) {
						if (rgbs[i].hashCode() != scheme.get(i).hashCode()) {
							exists = false;
							break;
						}
					}
				}
				if (exists) {
					colorRGBs = scheme;
					break;
				}
			}
					
			if (!exists) {
				colorRGBs = addScheme(rgbs);
			}
		}
		notifyColorChange();
	}
	
	/**
	 * Returns current color scheme
	 * @return
	 */
	public RGB[] getColorScheme() {
		return colorRGBs.toArray(new RGB[0]);
	}
	
	public List<RGB> addScheme(RGB...rgbs) {
		List<RGB> colors = new ArrayList<RGB>();
		for (RGB rgb : rgbs) {
			colors.add(rgb);
		}
		colorSchemes.add(colors);
		return colors;
	}
	
	
	private void initDefaultColorSchemes() {
		addScheme(new RGB(0, 101, 133),	new RGB(255, 99, 49), new RGB(175, 105, 134), new RGB(108, 142, 0), new RGB(237, 165, 62), new RGB(117, 51, 66));
		addScheme(new RGB(222, 177, 17), new RGB(97, 28, 24), new RGB(102,109,17), new RGB(189, 112, 20), new RGB(111, 92, 16),	new RGB(111, 32, 27));
		addScheme(new RGB(1,175,255), new RGB(57,99,213), new RGB(21,49,213), new RGB(30,125,42));
		addScheme(new RGB(255,92,93), new RGB(255,0,0), new RGB(255,41,43), new RGB(182,31,32), new RGB(153,0,0));
		addScheme(new RGB(255,157,0), new RGB(255,206,0), new RGB(40,0,159), new RGB(0,41,156));
		addScheme(new RGB(255,46,0), new RGB(255,255,14), new RGB(183, 183, 183), new RGB(122, 122, 122), new RGB(81, 81, 81), new RGB(61, 61, 61), new RGB(165, 165, 165));
		addScheme(new RGB(255,0,206), new RGB(255,220,0), new RGB(0, 255, 42));
		addScheme(new RGB(89, 79, 69), new RGB(168, 165, 126), new RGB(68, 49, 14), new RGB(86, 68, 34), new RGB(148, 141, 129), new RGB(92, 90, 41));
		addScheme(new RGB(66,71,37), new RGB(85,122,18), new RGB(117,131,49), new RGB(49,45,17));
		addScheme(new RGB(254,213,44), new RGB(255,177,10), new RGB(233,121,0), new RGB(229,109,3), new RGB(202,80,8), new RGB(129,52,7), new RGB(89,47,14));
		addScheme(new RGB(139,124,115), new RGB(91,95,129), new RGB(50,23,18), new RGB(255,251,237));
		
		colorRGBs = colorSchemes.get(0);
	}
	
	/**
	 * change color scheme
	 */
	private void nextColorScheme() {
		int scheme = (colorSchemes.indexOf(colorRGBs) + 1) % colorSchemes.size();
		colorRGBs = colorSchemes.get(scheme);
	}
	
	
	//////////////////////////////////////////////////////////////
	// Color Changed Listener
	//////////////////////////////////////////////////////////////
	
	HashSet<ColorChangedListener> colorChangedListeners = new HashSet<ColorChangedListener>();
	
	/**
	 * Add a {@Link ColorChangedListener} to {@link CAColorScheme}.
	 * @param listener
	 */
	public void addColorChangedListener(ColorChangedListener listener) {
		colorChangedListeners.add(listener);
	}
	
	/**
	 * Remove a {@Link ColorChangedListener} from {@link CAColorScheme}.
	 * @param listener
	 */
	public void removeColorChangedListener(ColorChangedListener listener) {
		colorChangedListeners.remove(listener);
	}
	
	/**
	 * ColorChangedEvent
	 * 
	 * @author Kuan-ming Su
	 *
	 */
	public class ColorChangedEvent extends EventObject {
		
		private static final long serialVersionUID = 8856830704822120779L;
		public RGB[] scheme;
		
		public ColorChangedEvent(Object scheme) {
			super(scheme);
			this.scheme = (RGB[]) scheme;
		}
		
		public RGB[] getColorScheme() {
			return scheme;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder("Color Scheme { ");
			for (RGB rgb : scheme) {
				sb.append(rgb.toString() + ", ");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
			sb.append(" }");
			return sb.toString();
		}
	}
	
	public interface ColorChangedListener extends EventListener {
		public void colorChanged(ColorChangedEvent event);
	}

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
