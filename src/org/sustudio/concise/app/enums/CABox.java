package org.sustudio.concise.app.enums;

import java.util.HashMap;

import org.sustudio.concise.app.Workspace;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.widgets.CABoxView;

public enum CABox {

	GearBox,
	
	ToolBox;
	
	private final HashMap<Workspace, CABoxView> boxViewMap = new HashMap<Workspace, CABoxView>();
	//private CABoxView boxView;
	
	public void setBoxView(Workspace workspace, CABoxView boxView) {
		if (boxView == null) {
			boxViewMap.remove(workspace);
		}
		else {
			boxViewMap.put(workspace, boxView);
		}
	}
	
	public CABoxView getBoxView(Workspace workspace) {
		return boxViewMap.get(workspace);
	}
	
	public GearController getSelectedGearView(Workspace workspace) {
		return boxViewMap.get(workspace).getSelectedGearController();
	}
	
	public Gear getSelectedGear(Workspace workspace) {
		return boxViewMap.get(workspace).getSelectedGear();
	}
	
}
