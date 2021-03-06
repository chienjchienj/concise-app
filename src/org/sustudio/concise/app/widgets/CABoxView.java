package org.sustudio.concise.app.widgets;

import java.util.ArrayList;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;

public abstract class CABoxView extends CTabFolder {

	protected CABox box; 
	
	public CABoxView(final SashForm parent, int style) {
		super(parent, style);
		setSimple(false);
		
		addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				CTabItem item = getItem(new Point(event.x, event.y));
				if (item != null) {
					if (getMaximized())
						performRestore();
					else
						performMaximize();
				}
			}
		});
		
		TabDragController tabDrag = new TabDragController();
		tabDrag.setTabFolder(this);
	}
	
	protected abstract void performMaximize();
	
	protected abstract void performRestore();
	
	protected void setBox(CABox box) {
		this.box = box;
		box.setBoxView(Concise.getCurrentWorkspace(), this);
	}
	
	public void setSelection(Gear gear) {
		GearController c = gear.getController(Concise.getCurrentWorkspace());
		if (c != null && 
			c.getBox().equals(box)) {
			
			for (CTabItem item : getItems()) {
				if (item.getControl().equals(c)) {
					setSelection(item);
					break;
				}
			}
		}
	}
	
	public GearController[] getGearControllers() {
		ArrayList<GearController> list = new ArrayList<GearController>();
		for (Control control : getChildren()) {
			if (control instanceof GearController) 
				list.add((GearController) control);
		}
		return list.toArray(new GearController[0]);
	}
	
	public GearController getSelectedGearController() {
		if (getSelection() == null) return null;
		return (GearController) getSelection().getControl();
	}
	
	public Gear getSelectedGear() {
		if (getSelectedGearController() == null) return null;
		return getSelectedGearController().getGear();
	}
	
}
