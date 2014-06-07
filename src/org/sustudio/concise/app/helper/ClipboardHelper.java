package org.sustudio.concise.app.helper;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.sustudio.concise.app.gear.IGearTableBased;

public class ClipboardHelper {

	/**
	 * Copies image data to clipboard.
	 * @param imageData		image data.
	 */
	public static void copyImageToClipboard(ImageData imageData) {
		ImageTransfer imageTransfer = ImageTransfer.getInstance();
		Clipboard clipboard = new Clipboard(Display.getDefault());
		clipboard.setContents(new Object[] { imageData }, new Transfer[] { imageTransfer });
	}
	
	/**
	 * Copies text(s) of control (Table, Tree, List) to clipboard.
	 * @param control	Table or Tree or List.
	 */
	public static void copyItemTextToClipboard(Control control) {
		if (!(control instanceof Table) &&
			!(control instanceof Tree) &&
			!(control instanceof List)) {
			
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		if (control instanceof Table && ((Table) control).getSelectionCount() > 0) {
			copyTableItemToClipboard((Table) control, sb);
		}
		else if (control instanceof List && ((List) control).getSelectionCount() > 0) {
			copyListItemToClipboard((List) control, sb);
		}
		else if (control instanceof Tree && ((Tree) control).getSelectionCount() > 0) {
			copyTreeItemToClipboard((Tree) control, sb);
		}
		else return;
		
		Transfer textTransfer = TextTransfer.getInstance();
		Clipboard cb = new Clipboard(Display.getDefault());
		cb.setContents(new Object[] { sb.toString() }, new Transfer[] { textTransfer });
		sb.setLength(0);
		sb = null;
	}
	
	private static void copyTableItemToClipboard(Table table, StringBuilder sb) {
		if (table.getParent() instanceof IGearTableBased) {
			for (int index : table.getSelectionIndices()) {
				String[] texts = ((IGearTableBased) table.getParent()).getItemTexts(index);
				for (int i = 0; i < texts.length; i++) {
					sb.append((i>0 ? "\t" : "") + texts[i]);
				}
				sb.append("\n");
			}
		}
		else {
			for (TableItem item : table.getSelection()) {
				for (int i=0; i<table.getColumnCount(); i++) {
					sb.append((i>0 ? "\t" : "") + item.getText(i));
				}
				sb.append("\n");
			}
		}
	}
	
	private static void copyListItemToClipboard(List list, StringBuilder sb) {
		for (String item : list.getSelection()) {
			sb.append(item+"\n");
		}
	}
	
	private static void copyTreeItemToClipboard(Tree tree, StringBuilder sb) {
		for (TreeItem item : tree.getSelection()) {
			for (int i=0; i<tree.getColumnCount(); i++) {
				sb.append((i>0 ? "\t" : "") + item.getText(i));
			}
			sb.append("\n");
		}
	}
	
	/**
	 * Tests if clipboard is empty.
	 * @return if clipboard is empty.
	 */
	public static boolean isClipboardEmpty() {
		TextTransfer transfer = TextTransfer.getInstance();
		Clipboard cb = new Clipboard(Display.getDefault());
		String data = (String) cb.getContents(transfer);
		return (data==null);
	}
	
}
