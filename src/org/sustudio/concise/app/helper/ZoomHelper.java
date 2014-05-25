package org.sustudio.concise.app.helper;

import java.util.ArrayList;

import org.eclipse.gef4.cloudio.TagCloud;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.sustudio.concise.app.gear.collocationalNetworker.NetworkGraph;

public class ZoomHelper {

	private static ArrayList<Control> controls = new ArrayList<Control>();
	
	public static SelectionAdapter zoomInListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			zoom(1);
		}
	};
	
	public static SelectionAdapter zoomOutListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			zoom(-1);
		}
	};
	
	public static SelectionAdapter zoomDefaultListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			zoom(0);
		}
	};
	
	public static void addControls(Control[] controls) {
		if (controls == null) return;
		for (Control c : controls) {
			if (!ZoomHelper.controls.contains(c)) {
				ZoomHelper.controls.add(c);
			}
		}
	}
	
	public static void removeControls(Control[] controls) {
		if (controls == null) return;
		for (Control c : controls) {
			if (ZoomHelper.controls.contains(c)) {
				ZoomHelper.controls.remove(c);
			}
		}
	}
	
		
	private static void zoom(int scale) {
		for (Control control : controls) {
			if (control instanceof TagCloud) {
				zoomTagCloud((TagCloud) control, scale);
				continue;
			}
			
			else if (control instanceof NetworkGraph) {
				zoomNetworkGraph((NetworkGraph) control, scale);
				continue;
			}
			
			Font font = null;
			switch (scale) {
			case 1:
			case -1:
				FontData fd = control.getFont().getFontData()[0];
				int height = fd.getHeight();
				height += scale;
				fd.setHeight(height);
				font = new Font(control.getDisplay(), fd);
				break;
			case 0:
			default:
				font = control.getDisplay().getSystemFont();
				break;
			}
			
			
			if (control instanceof StyledText) {
				// redraw style ranges
				StyledText st = (StyledText) control;
				for (StyleRange r : st.getStyleRanges()) {
					if (r.font != null && r.start == 0) {  // 應該只有標題而已
						FontData fd = r.font.getFontData()[0];
						fd.setHeight((int) font.getFontData()[0].height + 3);
						r.font = new Font(st.getDisplay(), fd);
						st.setStyleRange(r);
						break;
					}
				}
			}
			
			control.setFont(font);
			control.redraw();
		}
	}
	
	private static void zoomTagCloud(TagCloud cloud, int scale) {
		switch (scale) {
		case 1:		cloud.zoomIn();		break;
		case -1: 	cloud.zoomOut(); 	break;
		case 0:		cloud.zoomFit(); 	break;
		}
	}
	
	private static void zoomNetworkGraph(NetworkGraph graph, int scale) {
		switch (scale) {
		case 1: graph.zoomIn(); break;
		case -1: graph.zoomOut(); break;
		case 0: graph.zoomReset(); break;
		}
	}
	
}
