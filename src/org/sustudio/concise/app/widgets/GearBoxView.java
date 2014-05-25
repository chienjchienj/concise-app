package org.sustudio.concise.app.widgets;

import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.GearController;

public class GearBoxView extends CABoxView {

	public GearBoxView(SashForm parent, int style) {
		super(parent, style);
		setBox(CABox.GearBox);
		
		setMaximizeVisible(false);
		
		
		addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void maximize(CTabFolderEvent event) {
				performMaximize();
			}
			
			public void restore(CTabFolderEvent event) {
				performRestore();
			}
			
			public void close(CTabFolderEvent event) {
				CTabItem item = (CTabItem) event.item;
				GearController c = (GearController) item.getControl();
				if (c != null && !c.isDisposed()) {
					c.dispose();
				}
			}
		});
		
	}
	
	public CTabItem getSelection() {
		CTabItem item = super.getSelection();
		if (item == null && getItemCount() > 0) {
			setSelection(0);
			return getItem(0);
		}
		return item;
	}

	@Override
	protected void performMaximize() {
		((SashForm) getParent()).setMaximizedControl(this);
		setMaximized(true);
	}

	@Override
	protected void performRestore() {
		((SashForm) getParent()).setMaximizedControl(null);
		setMaximized(false);
	}
	
}
