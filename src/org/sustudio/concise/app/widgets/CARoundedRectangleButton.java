package org.sustudio.concise.app.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.roundedToolbar.RoundedToolItem;
import org.mihalis.opal.roundedToolbar.RoundedToolbar;

public class CARoundedRectangleButton extends RoundedToolbar {

	final RoundedToolItem item;
	
	public CARoundedRectangleButton(Composite parent) {
		super(parent, SWT.NONE);
		setCornerRadius(16);
		setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.NORMAL));
		
		item = new RoundedToolItem(this);
		item.setHeight(16);
		addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				item.setSelection(true);
				redraw();
			}
		});
		addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				item.setSelection(false);
				redraw();
			}
		});
	}
	
	public void setText(String text) {
		item.setText(text);
	}
	
	public void setImage(Image image) {
		item.setImage(image);
	}
	
	public void setHeight(int height) {
		item.setHeight(height);
	}

	public void addSelectionListener(SelectionListener listener) {
		item.addSelectionListener(listener);
	}
}
