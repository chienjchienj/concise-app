package org.sustudio.concise.app.widgets;

import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Widget;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.GearController;

public class ToolBoxView extends CABoxView {

	protected static final Widget CTabItem = null;

	public ToolBoxView(final SashForm parent, int style) {
		super(parent, style);
		setBox(CABox.ToolBox);
		
		setSingle(false);
		setMaximizeVisible(true);
		
		addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void maximize(CTabFolderEvent event) {
				performMaximize();
			}
			
			public void restore(CTabFolderEvent event) {
				performRestore();
			}
			
			public void close(CTabFolderEvent event) {
				if (getItemCount() == 1) {
					parent.setMaximizedControl(CABox.GearBox.getBoxView(Concise.getCurrentWorkspace()));
					setMaximized(false);
				}
				
				CTabItem item = (CTabItem) event.item;
				GearController c = (GearController) item.getControl();
				if (c != null && !c.isDisposed()) {
					c.dispose();
				}
			}
		});
		
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
		CABox.GearBox.getBoxView(Concise.getCurrentWorkspace()).setMaximizeVisible(true);
	}
	
}
