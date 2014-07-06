package org.sustudio.concise.app.helper;

import javafx.embed.swt.FXCanvas;
import javafx.embed.swt.SWTFXUtils;
import javafx.scene.image.WritableImage;

import org.eclipse.gef4.cloudio.WordCloud;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

public class CopyPasteHelper {

	private static Control activeControl;
	private static MenuItem cutItem;
	private static MenuItem copyItem;
	private static MenuItem pasteItem;
	private static MenuItem selectAllItem;
	
	public static void setPopupCopyMenuItem(final MenuItem item) {
		item.setText(copyItem.getText());
		item.setEnabled(copyItem.getEnabled());
	}
	
	public static void setPopupSelectAllMenuItem(MenuItem item) {
		item.setText(selectAllItem.getText());
		item.setEnabled(selectAllItem.getEnabled());
	}
	
	public static void setCutMenuItem(MenuItem item) {
		cutItem = item;
	}
	
	public static void setCopyMenuItem(MenuItem item) {
		copyItem = item;
	}
	
	public static void setPasteMenuItem(MenuItem item) {
		pasteItem = item;
	}
	
	public static void setSelectAllMenuItem(MenuItem item) {
		selectAllItem = item;
	}
	
	private static SelectionAdapter cutSelectionAdapter = new SelectionAdapter() { 
		public void widgetSelected(SelectionEvent event) {
			cut();
		} };
	
	public static SelectionAdapter getCutSelectionAdapter() {
		return cutSelectionAdapter;
	}
	
	private static SelectionAdapter copySelectionAdapter = new SelectionAdapter() { 
		public void widgetSelected(SelectionEvent event) {
			copy();
		} };
	
	public static SelectionAdapter getCopySelectionAdapter() {
		return copySelectionAdapter;
	}
	
	private static SelectionAdapter pasteSelectionAdapter = new SelectionAdapter() { 
		public void widgetSelected(SelectionEvent event) {
			paste();
		} }; 
	
	public static SelectionAdapter getPasteSelectionAdapter() {
		return pasteSelectionAdapter;
	}
	
	private static SelectionAdapter selectAllSelectionAdapter = new SelectionAdapter() { 
		public void widgetSelected(SelectionEvent event) {
			selectAll();
			refresh();
		} };
	
	public static SelectionAdapter getSelectAllSelectionAdapter() {
		return selectAllSelectionAdapter;
	}
	
	
	public static void listenTo(final Control control) {
		// check supported instance
		if (!(control instanceof Text) &&
			!(control instanceof Table) &&
			!(control instanceof Grid) &&
			!(control instanceof Tree) &&
			!(control instanceof List) &&
			!(control instanceof StyledText) &&
			!(control instanceof WordCloud) &&
			!(control instanceof FXCanvas)) {
			
			return;
		}
		
		// focus 的時候奪取 menu item 的控制權
		// 設置 menu item 的初始狀態
		control.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {
				setActiveControl(control);
			}
			public void focusLost(FocusEvent event) {
				setActiveControl(null);
			}
		});
		
		control.addListener(SWT.Selection, refreshListener);
		if (control instanceof Text) {
			//control.addListener(SWT.Selection, refreshListener);
			control.addListener(SWT.KeyUp, refreshListener);
			control.addListener(SWT.MouseUp, refreshListener);
		}
		
	}
	
	private static Listener refreshListener = new Listener() {
		public void handleEvent(Event event) {
			refresh();
		}
	};
	
	private static void setActiveControl(Control control) {
		activeControl = control;		
		if (control == null) {
			cutItem.setEnabled(false);
			copyItem.setEnabled(false);
			pasteItem.setEnabled(false);
			selectAllItem.setEnabled(false);
		}
		else {
			refresh();
		}
	}
	
	private static void refresh() {
		boolean cutEnabled = false;
		boolean copyEnabled = false;
		boolean pasteEnabled = false;
		boolean selectAllEnabled = false;
		
		if (activeControl instanceof Text) {
			Text textControl = (Text) activeControl;
			cutEnabled = textControl.getSelectionCount() > 0;
			copyEnabled = textControl.getSelectionCount() > 0;
			selectAllEnabled = textControl.getText().length() > 0;
			
			// check clipboard content
			pasteEnabled = !ClipboardHelper.isClipboardEmpty();
		}
		
		else if (activeControl instanceof Table) {
			Table table = (Table) activeControl;
			selectAllEnabled = table.getItemCount() > 0;
			copyEnabled = table.getSelectionCount() > 0;
		}
		
		else if (activeControl instanceof Grid) {
			Grid grid = (Grid) activeControl;
			selectAllEnabled = grid.getItemCount() > 0;
			copyEnabled = grid.getSelectionCount() > 0;
		}
		
		else if (activeControl instanceof Tree) {
			Tree tree = (Tree) activeControl;
			selectAllEnabled = tree.getItemCount() > 0;
			copyEnabled = tree.getSelectionCount() > 0;
		}
		
		else if (activeControl instanceof List) {
			List list = (List) activeControl;
			selectAllEnabled = list.getItemCount() > 0;
			copyEnabled = list.getSelectionCount() > 0;
		}
		
		else if (activeControl instanceof StyledText) {
			StyledText styledText = (StyledText) activeControl;
			copyEnabled = styledText.getSelectionCount() > 0;
			selectAllEnabled = styledText.getText().length() > 0;
		}
		
		else if (activeControl instanceof FXCanvas ||
				 activeControl instanceof WordCloud)
		{
			copyEnabled = true;
		}
		
		if (cutItem != null)
			cutItem.setEnabled(cutEnabled);
		if (copyItem != null)
			copyItem.setEnabled(copyEnabled);
		if (pasteItem != null)
			pasteItem.setEnabled(pasteEnabled);
		if (selectAllItem != null)
			selectAllItem.setEnabled(selectAllEnabled);
	}
	
	private static void cut() {
		if (activeControl instanceof Text) {
			((Text) activeControl).cut();
		}
	}
	
	private static void copy() {
		if (activeControl instanceof Text) {
			((Text) activeControl).copy();
		}
		else if (activeControl instanceof StyledText) {
			((StyledText) activeControl).copy();
		}
		else if (activeControl instanceof WordCloud) {
			ImageData imageData = ((WordCloud) activeControl).getImageData();
			ClipboardHelper.copyImageToClipboard(imageData);
		}
		else if (activeControl instanceof FXCanvas) {
			WritableImage writableImage = ((FXCanvas) activeControl).getScene().snapshot(null);
			ImageData imageData = SWTFXUtils.fromFXImage(writableImage, null);
			ClipboardHelper.copyImageToClipboard(imageData);
		}
		else {
			ClipboardHelper.copyItemTextToClipboard(activeControl);
		}
	}
	
	private static void paste() {
		if (activeControl instanceof Text) {
			((Text) activeControl).paste();
		}
	}
	
	private static void selectAll() {
		if (activeControl instanceof Text) {
			((Text) activeControl).selectAll();
		}
		else if (activeControl instanceof Table) {
			((Table) activeControl).selectAll();
			// selectAll did not trigger selectionEvent,
			// we have to do it manually
			for (Listener listener : activeControl.getListeners(SWT.Selection)) {
				Event event = new Event();
				event.widget = activeControl;
				listener.handleEvent(event);
			}
		}
		else if (activeControl instanceof Grid) {
			((Grid) activeControl).selectAll();
		}
		else if (activeControl instanceof Tree) {
			((Tree) activeControl).selectAll();
		}
		else if (activeControl instanceof List) {
			((List) activeControl).selectAll();
		}
		else if (activeControl instanceof StyledText) {
			((StyledText) activeControl).selectAll();
		}
	}
}
