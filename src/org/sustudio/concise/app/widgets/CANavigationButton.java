package org.sustudio.concise.app.widgets;

import java.util.EventListener;
import java.util.EventObject;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.roundedToolbar.RoundedToolItem;
import org.mihalis.opal.roundedToolbar.RoundedToolbar;
import org.mihalis.opal.utils.AdvancedPath;
import org.mihalis.opal.utils.SWTGraphicUtil;

/**
 * Prev and Next Navigation Button
 * 
 * @author kuanming
 *
 */
public class CANavigationButton extends RoundedToolbar {

	private static final Color BORDER_COLOR = SWTGraphicUtil.createDisposableColor(157, 157, 157);;

	private final Vector<NavigationListener> navListeners = new Vector<NavigationListener>();
	
	private final RoundedToolItem itemPrev;
	private final RoundedToolItem itemNext;
	
	public CANavigationButton(Composite parent) {
		super(parent, SWT.NONE);
		setCornerRadius(18);
		setMultiselection(false);
		
		itemPrev = new RoundedToolItem(this);
		itemPrev.setHeight(18);
		itemPrev.setImage(SWTResourceManager.getImage(getClass(), "/org/sustudio/concise/app/icon/button-previous.png"));
		
		itemNext = new RoundedToolItem(this);
		itemNext.setHeight(18);
		itemNext.setImage(SWTResourceManager.getImage(getClass(), "/org/sustudio/concise/app/icon/button-next.png"));
		
		addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				// never select
				itemPrev.setSelection(false);
				itemNext.setSelection(false);
				redraw();
				
				final RoundedToolItem item = CANavigationButton.this.getItem(new Point(event.x, event.y));
				NavigationEvent e = new NavigationEvent(this);
				for (NavigationListener listener : navListeners) {
					if (item.equals(itemPrev)) {
						listener.prevClicked(e);
					}
					else if (item.equals(itemNext)) {
						listener.nextClicked(e);
					}
				}
			}
		});
		
	}
	
	
	/**
	 * Paint the component
	 * @param e event
	 */
	protected void paintControl(final PaintEvent e) {
		super.paintControl(e);
		
		final GC gc = e.gc;
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);

		final int width = getSize().x;
		final int height = getSize().y;
		
		// draw borders
		final AdvancedPath path = new AdvancedPath(getDisplay());
		path.addRoundRectangle(0, 0, width, height, getCornerRadius(), getCornerRadius());
		gc.setClipping(path);

		gc.setForeground(BORDER_COLOR);
		gc.drawRoundRectangle(0, 0, width - 1, height - 1, getCornerRadius(), getCornerRadius());

		gc.setClipping((Rectangle) null);

		
	}
	
	public void dispose() {
		navListeners.clear();
		super.dispose();
	}
	
	
	public void addNavigationListener(NavigationListener listener) {
		navListeners.addElement(listener);
	}
	
	public void removeNavigationListener(NavigationListener listener) {
		navListeners.removeElement(listener);
	}
	
	
	public class NavigationEvent extends EventObject {

		private static final long serialVersionUID = -3073193376034269044L;

		public NavigationEvent(Object source) {
			super(source);
		}
		
	}
	
	public interface NavigationListener extends EventListener {
		public void prevClicked(NavigationEvent event);
		public void nextClicked(NavigationEvent event);
	}
}
