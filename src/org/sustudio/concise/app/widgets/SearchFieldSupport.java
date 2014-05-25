package org.sustudio.concise.app.widgets;
 
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
 
/**
 * 
 * @author Prakash G.R.
 * @author modified by Kuan-ming Su
 * 
 */
public class SearchFieldSupport implements DisposeListener, SelectionListener {

	private final NSSearchField nsSearchField;
	private final Text text;
	private Menu menu;
	
	public SearchFieldSupport(Text text) {
		this.text = text;
		text.addSelectionListener(this);
		text.addDisposeListener(this);
		nsSearchField = new NSSearchField(text);
	}
	
	public Text getText() {
		return this.text;
	}

	public String[] getRecentSearchStrings() {
		return this.nsSearchField.getRecentSearches();
	}
 
	public void setRecentSearchStrings(String[] recentSearches) {
		this.nsSearchField.setRecentSearches(recentSearches);
	}
	
	public void setMenu(Menu menu) {
		this.menu = menu;
		this.nsSearchField.setMenu(menu);
	}
	
	public Menu getMenu() {
		return menu;
	}
	
	public static void setNoRecentSearches(MenuItem menuItem) {
		NSSearchField.setTag(menuItem, NSSearchField.NSSearchFieldNoRecentsMenuItemTag);
	}
 
	public static void setClearRecents(MenuItem menuItem) {
		NSSearchField.setTag(menuItem, NSSearchField.NSSearchFieldClearRecentsMenuItemTag);
	}
	
	public static void setRecentSearchesTitle(MenuItem menuItem) {
		NSSearchField.setTag(menuItem, NSSearchField.NSSearchFieldRecentsTitleMenuItemTag);
	}
 
	public static void setRecentSearches(MenuItem menuItem) {
		NSSearchField.setTag(menuItem, NSSearchField.NSSearchFieldRecentsMenuItemTag);
	}
	
	public void widgetDisposed(DisposeEvent e) {
		text.removeSelectionListener(this);
	}
 
	public void widgetDefaultSelected(SelectionEvent e) {
		String[] recentSearchStrings = getRecentSearchStrings();
		if ((recentSearchStrings.length > 0 && text.getText().equals(recentSearchStrings[0]))
			|| text.getText().trim().equals("") ){
				return;
		}
		String[] newSearchStrings = new String[recentSearchStrings.length + 1];
		System.arraycopy(recentSearchStrings, 0, newSearchStrings, 1, recentSearchStrings.length);
		newSearchStrings[0] = text.getText();
		setRecentSearchStrings(newSearchStrings);
		text.setSelection(0, text.getText().length());
	}
	
	public void widgetSelected(SelectionEvent e) {
		// do nothing
	}

}
