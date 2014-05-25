package org.sustudio.concise.app.enums;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.resources.CABundle;

public enum PrefsEnum {
	
	/** General Preferences */
	GENERAL(CABundle.get("preferences.general"), "/org/sustudio/concise/app/icon/81-dashboard-20x20.png"), 
	
	/** File Preferences */
	FILE(CABundle.get("preferences.file"), "/org/sustudio/concise/app/icon/96-book-20x20.png"), 
	
	///** Tag Preferences */
	//TAG("Tag", "/org/sustudio/concise/app/icon/172-pricetag-20x20.png"), 
	
	///** XML Preferences */
	//XML("XML", "/org/sustudio/concise/app/icon/149-windmill-20x20.png"), 
	
	///** Wild card Preferences */
	//WILDCARD("Wild Card", "/org/sustudio/concise/app/icon/198-card-spades-20x20.png"), 
	
	/** Token Preferences */
	TOKEN(CABundle.get("preferences.token"), "/org/sustudio/concise/app/icon/117-todo-20x20.png"),
	
	///** Font Preferences */
	//FONT(CABundle.get("preferences.font"), "/org/sustudio/concise/app/icon/113-navigation-20x20.png"),
	;
	
	private final String label;
	private final Image image;
	private Control control;
	private ToolItem item;
			
	PrefsEnum(String label, String imageClasspath) {
		this.label = label;
		image = SWTResourceManager.getImage(getClass(), imageClasspath);
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setControl(Control control) {
		this.control = control;
	}
	
	public Control getControl() {
		return control;
	}
	
	public Image getImage() {
		return image;
	}
	
	public void setToolItem(ToolItem item) {
		this.item = item;
	}
	
	public ToolItem getToolItem() {
		return item;
	}
	
}
