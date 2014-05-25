package org.sustudio.concise.app.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;

/**
 * TabDragController serves as user trying to change tab's order in {@link CTabFolder} container.
 * 
 * @author Kuan-ming Su.
 *
 */
public class TabDragController implements DragDetectListener, MouseTrackListener, MouseListener, MouseMoveListener {

	private CTabFolder tabFolder;
	private Point point;
	private boolean drag = false;
	private boolean exitDrag = false;
	private CTabItem dragItem;
	private Cursor cursor = new Cursor(Display.getDefault(), SWT.CURSOR_CROSS);
	private Cursor tabFolderCursor;
	
	/**
	 * assign CTabFolder to TabDragController
	 * @param tabFolder tab container
	 */
	public void setTabFolder(CTabFolder tabFolder) {
		tabFolder.addDragDetectListener(this);
		tabFolder.addMouseListener(this);
		tabFolder.addMouseMoveListener(this);
		tabFolder.addMouseTrackListener(this);
		this.tabFolder = tabFolder;
		this.tabFolderCursor = tabFolder.getCursor();
	}
	
	public void dispose() {
		tabFolder.removeDragDetectListener(this);
		tabFolder.removeMouseListener(this);
		tabFolder.removeMouseMoveListener(this);
		tabFolder.removeMouseTrackListener(this);
	}
	
	public void dragDetected(DragDetectEvent e) {
		point = tabFolder.toControl(Display.getCurrent().getCursorLocation());  // see eclipse bug 43251
		CTabItem item = tabFolder.getItem(point);
		if (item == null) return;
		drag = true;
		exitDrag = false;
		dragItem = item;
		
		Gear gear = ((GearController) item.getControl()).getGear();
		Image image = gear.image();
		cursor = new Cursor(tabFolder.getDisplay(), image.getImageData(), 0, 0);
	}
	
	public void mouseEnter(MouseEvent e) {
		if (exitDrag) {
			exitDrag = false;
			drag = e.button != 0;
		}
	}

	public void mouseExit(MouseEvent e) {
		if (drag) {
			tabFolder.setInsertMark(null, false);
			exitDrag = true;
			drag = false;
			tabFolder.getShell().setCursor(tabFolderCursor);
		}
	}
	
	public void mouseUp(MouseEvent e) {
		if (!drag) return;
		point = new Point(e.x, e.y);
		tabFolder.setInsertMark(null, false);
		CTabItem item = tabFolder.getItem(new Point(point.x, 1));
		if (item != null) {
			int index = tabFolder.indexOf(item);
			int newIndex = tabFolder.indexOf(item);
			int oldIndex = tabFolder.indexOf(dragItem);
			if (newIndex != oldIndex) {
				boolean after = newIndex > oldIndex;
				index = after ? index + 1 : index;
				index = Math.max(0, index);
				
				CTabItem newItem = new CTabItem(tabFolder, SWT.NONE, index);
				newItem.setText(dragItem.getText());
				Control c = dragItem.getControl();
				dragItem.setControl(null);
				newItem.setControl(c);
				newItem.setImage(dragItem.getImage());
				dragItem.dispose();
				
				tabFolder.setSelection(newItem);
			}
		}
		drag = false;
		exitDrag = false;
		dragItem = null;
		tabFolder.getShell().setCursor(tabFolderCursor);
	}
	
	public void mouseMove(MouseEvent e) {
		if (!drag) return;
		point = new Point(e.x, e.y);
		CTabItem item = tabFolder.getItem(new Point(point.x, 2));
		if (item == null) {
			tabFolder.setInsertMark(null, false);
			return;
		}
		Rectangle rect = item.getBounds();
		boolean after = point.x > rect.x + rect.width / 2;
		tabFolder.setInsertMark(item, after);
		tabFolder.getShell().setCursor(cursor);
	}
	
	public void mouseDown(MouseEvent e) { }	
	public void mouseHover(MouseEvent e) { }
	public void mouseDoubleClick(MouseEvent e) { }
}
