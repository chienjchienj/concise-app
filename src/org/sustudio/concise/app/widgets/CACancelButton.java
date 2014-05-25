package org.sustudio.concise.app.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseEvent;

/**
 * A simple round button with an "X" inside.
 * 
 * @author Kuan-ming Su
 *
 */
public class CACancelButton extends Canvas {
	
	private boolean enabled = true;
	//private boolean selection = false;
	private boolean hover = false;
	private int width = -1;
	private int height = -1;
	
	private final List<SelectionListener> selectionListeners;
	
	/**
	 * SWT.POP_UP or SWT.NONE
	 * @param parent
	 * @param style
	 */
	public CACancelButton(Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		selectionListeners = new ArrayList<SelectionListener>();
		addListeners();
	}
	
	
	/**
	 * Enables the receiver if the argument is <code>true</code>, and disables it otherwise.
	 * <p>
	 * A disabled control is typically not selectable from the user interface and draws with an inactive or "grayed" look.
	 * </p>
	 * 
	 * @param enabled the new enabled state
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setEnabled(final boolean enabled) {
		checkWidget();
		this.enabled = enabled;
	}
	
	/**
	 * Returns <code>true</code> if the receiver is enabled, and <code>false</code> otherwise. 
	 * A disabled control is typically not selectable from the user interface and draws 
	 * with an inactive or "grayed" look.
	 * 
	 * @return the receiver's enabled state
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 * 
	 * @see #getEnabled
	 */
	public boolean isEnabled() {
		checkWidget();
		return this.enabled;
	}
	
	private void addListeners() {
		addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				if (enabled) {
					redraw();
				}
			}
		});
		addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				if (enabled) {
					redraw();
					// fire selectionEvent
					fireSelectionEvent();
				}
			}
		});
		
		addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseEnter(MouseEvent event) {
				hover = true;
				redraw();
			}
			
			public void mouseExit(MouseEvent event) {
				hover = false;
				redraw();
			}
		});
		
		addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {
				CACancelButton.this.paintControl(event);
			}
		});
	}
	
	
	/**
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		checkWidget();
		return new Point(Math.max(getWidth(), wHint), Math.max(getHeight(), hHint));
	}
	
	/**
	 * @return the default size of the item
	 */
	Point computeDefaultSize() {
		FontData fd = getFont().getFontData()[0];
		return new Point(fd.getHeight(), fd.getHeight());
	}


	public int getWidth() {
		checkWidget();
		if (width == -1) {
			return computeDefaultSize().x;
		}
		return width;
	}
	
	public int getHeight() {
		checkWidget();
		if (height == -1) {
			return computeDefaultSize().y;
		}
		return height;
	}
	
	
	/**
	 * Paint the component
	 * @param e event
	 */
	protected void paintControl(final PaintEvent e) {
		final GC gc = e.gc;
		//gc.setAdvanced(true);
		//gc.setAntialias(SWT.ON);

		width = getWidth();
		height = getHeight();

		// draw border
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawOval(0, 0, width-1, height-1);
		
		drawBackground(gc);
		drawButton(gc);
	}
	
	private void drawBackground(final GC gc) {
		if (hover) {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			gc.fillOval(0, 0, width, height);
		}
	}
	
	void drawButton(final GC gc) {
		Color color = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
		if (hover) {
			color = getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		}
		gc.setForeground(color);
		gc.setLineWidth(2);
		int offset = Math.round((float) width / (float) Math.sqrt(2));
		gc.drawLine(offset, offset, width-offset, height-offset);
		gc.drawLine(width-offset, offset, offset, height-offset);
	}
	
	/*
	void drawButton(final GC gc) {
		if (selection) {
			drawBackground(gc);
		}

		int xPosition = computeStartingPosition();

		xPosition += drawImage(gc, xPosition);
		drawText(gc, xPosition);

		if ((getStyle() & SWT.POP_UP) != 0) {
			drawArrow(gc);
		}
	}
	
	private void drawBackground(final GC gc) {
		final AdvancedPath path = new AdvancedPath(getDisplay());
		path.addRoundRectangle(0, 0, width, height, cornerRadius, cornerRadius);
		gc.setClipping(path);

		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.fillRoundRectangle(0, 0, width + cornerRadius, height, cornerRadius, cornerRadius);
		
		gc.setForeground(BORDER_COLOR);
		gc.drawRoundRectangle(0, 0, width - 1, height + 1, cornerRadius, cornerRadius);
		
		gc.setClipping((Rectangle) null);
	}
	*/
	
	/**
	 * Adds the listener to the collection of listeners who will be notified when the control 
	 * is selected by the user, by sending it one of the messages defined in the 
	 * <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 * 
	 * @param listener the listener which should be notified when the control is selected by the user,
	 * 
	 * @exception IllegalArgumentException <ul>
	 *     <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 * 
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(final SelectionListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.selectionListeners.add(listener);
	}
	
	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		selectionListeners.clear();
	}
	
	
	void fireSelectionEvent() {
		final Event event = new Event();
		event.widget = this;
		event.display = getDisplay();
		event.type = SWT.Selection;
		for (final SelectionListener selectionListener : selectionListeners) {
			selectionListener.widgetSelected(new SelectionEvent(event));
		}
	}
	
}
