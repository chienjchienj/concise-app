package org.sustudio.concise.app.gear;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.app.helper.PopupMenuHelper;
import org.sustudio.concise.app.helper.SaveOutputHelper;
import org.sustudio.concise.app.helper.ZoomHelper;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.utils.LabelFont;
import org.sustudio.concise.app.widgets.CABoxView;
import org.sustudio.concise.app.widgets.ToolBoxView;
import org.sustudio.concise.app.Workspace;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;

public abstract class GearController extends Composite {
	
	public static final String _DB_COLUMN = "_DBColumn";
	public static final String _DOC_ID = "_DOC_ID";
	public static final String _WORD_ID = "_WORD_ID";
	
	protected final Workspace workspace = Concise.getCurrentWorkspace();
	
	private Finder finder;
	
	protected final ColumnSortListener columnSortListener = new ColumnSortListener();
	protected final Gear gear;
	private final CABox box;
	private Control control;
	private final Label lblStatus;
	
	
	public GearController(final CABox box, final Gear gear) {
		super(box.getBoxView(Concise.getCurrentWorkspace()), SWT.NONE);
		this.gear = gear;
		
		/**
		 * Register {@link GearControl} to {@link GearsEnum}.
		 */
		this.gear.setGearController(workspace, this);
		this.box = box;
		
		// something need to be initiated before loading GUI widgets
		init();
		
		GridLayout gl = new GridLayout(1, false);
		gl.verticalSpacing = 2;
		gl.marginHeight = 1;
		gl.marginWidth = 1;
		gl.marginBottom = 2;
		setLayout(gl);
		if (this instanceof IGearFilterable) {
			loadFinder();
		}
		
		control = createControl();
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		setCopyPasteHelper();
		
		lblStatus = new Label(this, SWT.BORDER | SWT.SHADOW_IN);
		lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblStatus.setText(gear.label());
		lblStatus.setFont(LabelFont.getFont());

		// zoom
		ZoomHelper.addControls(getZoomableControls());
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				unloadData();
				ZoomHelper.removeControls(getZoomableControls());
				GearController.this.gear.setGearController(workspace, null);
			}
		});
		
		final Listener gearDataListener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Show:
					// async is needed to wait until focus reaches its new Control
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (getBox() == CABox.GearBox) {
								Concise.getActiveApp().toolBar.setToolBarLayout(getGear());
							}
							loadData();
							control.setFocus();
						}
					});
					break;
				
				case SWT.Hide:
					unloadData();	
					break;
					
				case SWT.Activate:
					Gear.setActiveGear(getGear());
					SaveOutputHelper.listenTo(GearController.this.getGear());
					break;
				}
			} };
		addListener(SWT.Show, gearDataListener);
		addListener(SWT.Hide, gearDataListener);
		addListener(SWT.Activate, gearDataListener);
		
		PopupMenuHelper.addPopupMenuFor(this);
	}
	
	/**
	 * Initiate before loading SWT's GUI widgets.
	 */
	protected void init() {
		// Override this to initial something before loading GUI
	}
	
	private void loadFinder() {
		finder = new Finder(gear);
		finder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		finder.setHidden(true);
	}
	
	/**
	 * Load data
	 */
	public void loadData() {
		// Override this to load table data when gear visible
		SaveOutputHelper.listenTo(GearController.this.getGear());
	}
	
	protected void unloadData() {
		// Override this to unload table data when gear invisible
		if (SaveOutputHelper.getGear() == getGear()) {
			SaveOutputHelper.listenTo(null);
		}
	}
	
	protected abstract Control createControl();
	
	protected void setCopyPasteHelper() {
		CopyPasteHelper.listenTo(control);
	}
	
	public Control[] getZoomableControls() {
		return new Control[] { control };
	}
	
	/**
	 * Return created control (usually the primary function one).
	 * @return
	 */
	public Control getControl() {
		return control;
	}
			
	public Gear getGear() {
		return gear;
	}
	
	public CABox getBox() {
		return box;
	}
	
	public void setStatusText(final String text) {
		lblStatus.setText(text);
	}
	
	public void dispose() {
		ZoomHelper.removeControls(getZoomableControls());
		gear.setGearController(workspace, null);
		unloadData();
		super.dispose();
	}
	
	public Finder getFinder() {
		return finder;
	}
	
	public void close() {
		CABoxView boxView = getBox().getBoxView(workspace);
		if (boxView instanceof ToolBoxView &&
			boxView.getItemCount() == 1) 
		{
			((SashForm) boxView.getParent()).setMaximizedControl(CABox.GearBox.getBoxView(workspace));
			boxView.setMaximized(false);
		}
		
		dispose();
		if (!getBox().getBoxView(workspace).getSelection().isDisposed()) {
			getBox().getBoxView(workspace).getSelection().dispose();
		}
	}
	
	/**
	 * ToolBar 進行搜尋後會執行的部分，通常會呼叫相關的 ConciseThread 進行
	 * @param query
	 */
	public abstract void doit(CAQuery query);
	
	/**
	 * Static method to return active gear view
	 * @return
	 */
	public static GearController getActiveGearView() {
		Gear g = Gear.getActiveGear();
		if (g != null && 
			g.getController(Concise.getCurrentWorkspace()) != null && 
			!g.getController(Concise.getCurrentWorkspace()).isDisposed()) 
		{
			return g.getController(Concise.getCurrentWorkspace());
		}
		return null;
	}
	
	/**
	 * Returns visible CAGearView.
	 * @return
	 */
	public static GearController[] getVisibleGearViews() {
		GearController[] gearViews = new GearController[Gear.getVisibleGears().length];
		for (int i = 0; i < gearViews.length; i++) {
			gearViews[i] = Gear.getVisibleGears()[i].getController(Concise.getCurrentWorkspace());
		}
		return gearViews;
	}
}
