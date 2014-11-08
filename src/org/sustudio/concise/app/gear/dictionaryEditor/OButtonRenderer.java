package org.sustudio.concise.app.gear.dictionaryEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.mihalis.opal.obutton.AbstractButtonRenderer;
import org.mihalis.opal.utils.SWTGraphicUtil;

public class OButtonRenderer extends AbstractButtonRenderer {

	private static OButtonRenderer instance;
	//private static final Color FIRST_BACKGROUND_COLOR = SWTGraphicUtil.getColorSafely(121, 187, 255);
	private static final Color FIRST_BACKGROUND_COLOR = SWTGraphicUtil.getColorSafely(255, 255, 255);
	//private static final Color SECOND_BACKGROUND_COLOR = SWTGraphicUtil.getColorSafely(56, 142, 229);
	private static final Color SECOND_BACKGROUND_COLOR = SWTGraphicUtil.getColorSafely(255, 255, 229);

	private OButtonRenderer() {
		super();
	}

	@Override
	protected Color getFontColor() {
		//return Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	}

	@Override
	protected Color getFirstBackgroundColor() {
		return FIRST_BACKGROUND_COLOR;
	}

	@Override
	protected Color getSecondBackgroundColor() {
		return SECOND_BACKGROUND_COLOR;
	}

	public static OButtonRenderer getInstance() {
		if (instance == null) {
			instance = new OButtonRenderer();
		}
		return instance;
	}
	
}
