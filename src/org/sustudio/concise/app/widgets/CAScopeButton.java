package org.sustudio.concise.app.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.utils.AdvancedPath;

public class CAScopeButton extends Canvas {

	private static final int MARGIN_H = 7;
	private static final int MARGIN_V = 2;
	private static final int SPACING = 4;
	private static final int ARROW_WIDTH = SPACING + 5;
	
	private static Color BORDER_COLOR = new Color(Display.getCurrent(), 157, 157, 157);
	
	private int cornerRadius = 14;
	private boolean enabled = true;
	private boolean selection = false;
	private int width = -1;
	private int height = -1;
	private int alignment = SWT.LEFT;
	private String text = "";
	private Image image;
	private Image disabledImage;
	private Image selectionImage;
	
	private final List<SelectionListener> selectionListeners;
	private Menu popupMenu;
	private int selectionIndex = -1;
	
	/**
	 * SWT.POP_UP or SWT.NONE
	 * @param parent
	 * @param style
	 */
	public CAScopeButton(Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		selectionListeners = new ArrayList<SelectionListener>();
		addListeners();
		setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.NORMAL));
		setBackground(parent.getBackground());
		
		if ((style & SWT.POP_UP) != 0) {
			popupMenu = new Menu(this);
			popupMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuHidden(MenuEvent arg0) {
					selection = false;
					redraw();
				}

				@Override
				public void menuShown(MenuEvent arg0) {
					for (MenuItem item : popupMenu.getItems()) {
						item.setSelection(item.getID() == selectionIndex);
					}
				}
				
			});
		}
	}
	
	
	public void add(final String string) {
		final MenuItem item = new MenuItem(popupMenu, SWT.RADIO);
		item.setText(string);
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setText(string);
				item.setSelection(true);
				selectionIndex = item.getID();
				
				// fire selectionEvent
				selection = false;
				redraw();
				fireSelectionEvent();
			}
		});
		item.setID(popupMenu.getItemCount() - 1);
	}
	
	
	public void setItems(final String[] items) {
		// remove existing menu item
		for (MenuItem item : popupMenu.getItems()) {
			item.dispose();
		}
		
		for (String text : items) {
			add(text);
		}
		select(0);
		width = -1;
		height = -1;
		redraw();
	}
	
	public void select(int index) {
		MenuItem item = popupMenu.getItem(index);
		item.setSelection(true);
		selectionIndex = index;
		setText(item.getText());
	}
	
	public int getSelectionIndex() {
		return selectionIndex;
	}
	
	
	/**
	 * Returns the number of items contained in the receiver's list.
	 *
	 * @return the number of items
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public int getItemCount () {
		checkWidget ();
		if ((getStyle() & SWT.POP_UP) != 0) {
			return popupMenu.getItemCount();
		} else {
			return 0;
		}
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
				if (!enabled) return;
				selection = true;
				redraw();
				
				if ((getStyle() & SWT.POP_UP) != 0) {
					popupMenu.setLocation(CAScopeButton.this.toDisplay(0, 0));
					CAScopeButton.this.popupMenu.setVisible(true);
				}
			}
		});
		addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				if (!enabled) return;
				
				if ((getStyle() & SWT.POP_UP) == 0) {
					// fire selectionEvent
					selection = false;
					redraw();
					fireSelectionEvent();
				}
			}
		});
		
		addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {
				CAScopeButton.this.paintControl(event);
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
	 * @return the corner radius
	 */
	public int getCornerRadius() {
		checkWidget();
		return this.cornerRadius;
	}
	
	/**
	 * Paint the component
	 * @param e event
	 */
	protected void paintControl(final PaintEvent e) {
		final GC gc = e.gc;
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);

		width = getWidth();
		height = getHeight();

		drawBorders(gc);
		drawButton(gc);
	}
	
	private void drawBorders(final GC gc) {
		final AdvancedPath path = new AdvancedPath(getDisplay());
		path.addRoundRectangle(0, 0, width, height, cornerRadius, cornerRadius);
		gc.setClipping(path);
		
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRoundRectangle(0, 0, width, height, cornerRadius, cornerRadius);
		
		gc.setForeground(BORDER_COLOR);
		gc.drawRoundRectangle(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

		gc.setClipping((Rectangle) null);
	}
	
	
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
	
	private int computeStartingPosition() {
		final int widthOfTextAndImage = computeSizeOfTextAndImages().x;
		switch (alignment) {
			case SWT.CENTER:
				return (getWidth() - widthOfTextAndImage) / 2;
			case SWT.RIGHT:
				if ((getStyle() & SWT.POP_UP) != 0)
					return getWidth() - widthOfTextAndImage - ARROW_WIDTH;
				return getWidth() - widthOfTextAndImage - MARGIN_H;
			default:
				return MARGIN_H;
		}
	}
	
	
	private int drawImage(final GC gc, final int xPosition) {
		Image image;
		if (!isEnabled()) {
			image = disabledImage;
		} else if (selection) {
			image = selectionImage;
		} else {
			image = getImage();
		}

		if (image == null) {
			return 0;
		}

		final int yPosition = (height - image.getBounds().height) / 2;
		gc.drawImage(image, xPosition, yPosition);
		return image.getBounds().width + SPACING;
	}
	
	public Image getImage() {
		checkWidget();
		return image;
	}
	
	/**
	 * Sets the receiver's image to the argument, which may be
	 * null indicating that no image should be displayed.
	 *
	 * @param image the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li> 
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setImage (Image image) {
		checkWidget ();
		if (image != null && image.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.image = image;
	}

	private void drawText(final GC gc, final int xPosition) {
		gc.setFont(getFont());
		if (selection) {
			// TODO add selection color
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		} else {
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		}

		final Point textSize = gc.stringExtent(getText());
		final int yPosition = (height - textSize.y) / 2;

		gc.drawText(getText(), xPosition, yPosition, true);
	}
	
	private void drawArrow(final GC gc) {
		if (selection) {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		} else {
			gc.setBackground(BORDER_COLOR);
		}
		int xPosition = width - MARGIN_H - 5;
		int upArrow[] = { 
				xPosition, height / 2 - 1,
				xPosition + 2, height / 2 - 4,
				xPosition + 4, height / 2 - 1 
		};
		gc.fillPolygon(upArrow);
		
		int downArrow[] = {
				xPosition, height / 2 + 1,
				xPosition + 2, height / 2 + 4,
				xPosition + 4, height / 2 + 1
		};
		gc.fillPolygon(downArrow);
		
	}

	/**
	 * Returns a value which describes the position of the text in the receiver. 
	 * The value will be one of <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>.
	 * 
	 * @return the alignment
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public int getAlignment() {
		checkWidget();
		return alignment;
	}
	
	
	/**
	 * @param cornerRadius new corner radius
	 */
	public void setCornerRadius(final int cornerRadius) {
		checkWidget();
		this.cornerRadius = cornerRadius;
	}
	
	/**
	 * Returns the receiver's text, which will be an empty
	 * string if it has never been set or if the receiver is
	 * an <code>ARROW</code> button.
	 *
	 * @return the receiver's text
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public String getText () {
		checkWidget();
		return text;
	}

	/**
	 * Sets the receiver's text.
	 * <p>
	 * This method sets the button label.  The label may include
	 * the mnemonic character but must not contain line delimiters.
	 * </p>
	 * <p>
	 * Mnemonics are indicated by an '&amp;' that causes the next
	 * character to be the mnemonic.  When the user presses a
	 * key sequence that matches the mnemonic, a selection
	 * event occurs. On most platforms, the mnemonic appears
	 * underlined but may be emphasized in a platform specific
	 * manner.  The mnemonic indicator character '&amp;' can be
	 * escaped by doubling it in the string, causing a single
	 * '&amp;' to be displayed.
	 * </p><p>
	 * Note that a Button can display an image and text simultaneously
	 * on Windows (starting with XP), GTK+ and OSX.  On other platforms,
	 * a Button that has an image and text set into it will display the
	 * image or text that was set most recently.
	 * </p>
	 * @param string the new text
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setText (String string) {
		checkWidget ();
		if (string == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		text = string;
	}
	

	
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
	 * @return the default size of the item
	 */
	Point computeDefaultSize() {
		final Point sizeOfTextAndImages = computeSizeOfTextAndImages();
		if ((getStyle() & SWT.POP_UP) != 0)
		{
			return new Point(2 * MARGIN_H + sizeOfTextAndImages.x + ARROW_WIDTH,
					 2 * MARGIN_V + sizeOfTextAndImages.y);
		}
		else 
		{
			return new Point(2 * MARGIN_H + sizeOfTextAndImages.x, 2 * MARGIN_V + sizeOfTextAndImages.y);
		}
	}
	
	private Point computeSizeOfTextAndImages() {
		int w = 0, h = 0;
		final Point textSize = new Point(0, 0);
		final boolean textNotEmpty = getText() != null && !getText().equals("");

		if (textNotEmpty) {
			computeTextSize(getText(), textSize);
		}
		if (popupMenu != null) {
			for (MenuItem item : popupMenu.getItems()) {
				if (item.getText() != null && !item.getText().equals(""))
					computeTextSize(item.getText(), textSize);
			}
		}
		w += textSize.x;
		h = textSize.y;

		final Point imageSize = new Point(-1, -1);
		computeImageSize(image, imageSize);
		computeImageSize(selectionImage, imageSize);
		computeImageSize(disabledImage, imageSize);

		if (imageSize.x != -1) {
			w += imageSize.x;
			h = Math.max(imageSize.y, h);
			if (textNotEmpty) {
				width += SPACING;
			}
		}
		return new Point(w, h);
	}
	
	private void computeTextSize(final String text, final Point textSize) {
		if (text == null) return;
		
		final GC gc = new GC(this);
		final Point extent = gc.stringExtent(text);
		gc.dispose();
		textSize.x = Math.max(extent.x, textSize.x);
		textSize.y = Math.max(extent.y, textSize.y);
	}
	
	private void computeImageSize(final Image image, final Point imageSize) {
		if (image == null) {
			return;
		}
		final Rectangle imageBounds = image.getBounds();
		imageSize.x = Math.max(imageBounds.width, imageSize.x);
		imageSize.y = Math.max(imageBounds.height, imageSize.y);
	}
	
	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		selectionListeners.clear();
		image.dispose();
		image = null;
		disabledImage.dispose();
		disabledImage = null;
		selectionImage.dispose();
		selectionImage = null;
		BORDER_COLOR.dispose();
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
