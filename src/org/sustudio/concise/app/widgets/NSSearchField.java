package org.sustudio.concise.app.widgets;
 
import java.lang.reflect.Field;
 
import org.eclipse.swt.internal.cocoa.NSArray;
import org.eclipse.swt.internal.cocoa.NSMenu;
import org.eclipse.swt.internal.cocoa.NSMenuItem;
import org.eclipse.swt.internal.cocoa.NSMutableArray;
import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.internal.cocoa.id;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
 
/**
 * 
 * @author Prakash G.R.
 */
public class NSSearchField extends org.eclipse.swt.internal.cocoa.NSSearchField {
 
	public static final int NSSearchFieldRecentsTitleMenuItemTag = 1000;
	public static final int NSSearchFieldRecentsMenuItemTag = 1001;
	public static final int NSSearchFieldClearRecentsMenuItemTag = 1002;
	public static final int NSSearchFieldNoRecentsMenuItemTag = 1003;
 
	private static final long sel_setSearchMenuTemplate = OS.sel_registerName("setSearchMenuTemplate:");
	private static final long sel_setTag = OS.sel_registerName("setTag:");
	private static final long sel_setRecentSearches = OS.sel_registerName("setRecentSearches:");
 
	public NSSearchField(id id) {
		super(id);
	}
 
	public NSSearchField(Text text) {
		super(text.view);
	}
 
	public void setMenu(Menu menu) {
		try {
			Field field = Menu.class.getDeclaredField("nsMenu");
			field.setAccessible(true);
 
			NSMenu nsMenu = (NSMenu) field.get(menu);
 
			OS.objc_msgSend(this.id, sel_setSearchMenuTemplate, nsMenu.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	public static boolean setTag(MenuItem menuItem, int tag) {
		try {
			Field field = MenuItem.class.getDeclaredField("nsItem");
			field.setAccessible(true);
 
			NSMenuItem nsMenuItem = (NSMenuItem) field.get(menuItem);
			OS.objc_msgSend(nsMenuItem.id, sel_setTag, tag);
 
			// no action for titles
			if (tag == NSSearchFieldRecentsTitleMenuItemTag || tag == NSSearchFieldNoRecentsMenuItemTag) {
				nsMenuItem.setAction(0);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
 
	public String[] getRecentSearches() {
		NSArray recentSearches = super.recentSearches();
		String[] recentSearchStrings = new String[(int) recentSearches.count()];
		for (int i = 0; i < recentSearchStrings.length; i++) {
			recentSearchStrings[i] = (new NSString(recentSearches.objectAtIndex(i))).getString();
		}
		return recentSearchStrings;
	}
 
	public void setRecentSearches(String[] recentSearchStrings) {
   
		NSMutableArray recentSearches = NSMutableArray.arrayWithCapacity(recentSearchStrings.length);
		for (String aRecentSearcb : recentSearchStrings) {
			NSString nsString = NSString.stringWith(aRecentSearcb);
			recentSearches.addObject(nsString);
		}
		OS.objc_msgSend(this.id, sel_setRecentSearches, recentSearches.id);

 	}
}