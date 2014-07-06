package org.sustudio.concise.app.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.resources.CABundle;

public class CASpinner extends Shell {
	
	protected int width = 210;
	protected int height = 206;
	protected int radius = 20;
	
	protected final GearController controller;
	protected CLabel lblMsg;
	
	public CASpinner(GearController controller) {
		super(controller.getShell(), SWT.NO_TRIM);
		this.controller = controller;
		
		createContents();
		layout();
	}
	
	protected void setLocation() {
		final Control control = controller.getControl();
		setLocation(control.toDisplay(0, 0).x + control.getSize().x / 2 - getSize().x / 2,
				 	controller.toDisplay(0, 0).y + controller.getClientArea().height / 3 - getSize().y / 2);
	}
	
	protected void createContents() {
		Region region = createRoundedRectangleRegion();
		setRegion(region);
		setSize(region.getBounds().width, region.getBounds().height);
		setAlpha(225);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		
		setLocation();
		final Listener locationListener = new Listener() 
		{
			public void handleEvent(Event event) 
			{
				CASpinner.this.setLocation();
			}
		};
		controller.addListener(SWT.Show, locationListener);
		controller.addListener(SWT.Resize, locationListener);
		controller.addListener(SWT.Move, locationListener);
		
		final CAProgressIndicator spinner = new CAProgressIndicator(this, SWT.NONE);
		spinner.setBounds(
				width / 2 - spinner.getSize().x / 2,
				height * 2 / 5 - spinner.getSize().y / 2,
				spinner.getSize().x,
				spinner.getSize().y);
		spinner.startAnimation();
		
		// SWT.SHADOW_NONE can perform vertical centered alignment
		lblMsg = new CLabel(this, SWT.SHADOW_NONE);
		lblMsg.setText(CABundle.get("caspinner.loading"));
		lblMsg.setAlignment(SWT.CENTER);
		lblMsg.setBackground(getBackground());
		lblMsg.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		lblMsg.setBounds(
				radius,
				spinner.getBounds().y + spinner.getSize().y,
				width - radius * 2,
				height - spinner.getBounds().y - spinner.getSize().y - radius);
		
		// stop spinner animation
		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent event) {
				spinner.stopAnimation();
				CASpinner.this.controller.removeListener(SWT.Show, locationListener);
				CASpinner.this.controller.removeListener(SWT.Resize, locationListener);
				CASpinner.this.controller.removeListener(SWT.Move, locationListener);
			}
			
		});
	
	}
	
	
	/**
	 * Set message 
	 * @param message
	 */
	public void setMessage(String message) {
		lblMsg.setText(message);
	}
	
	public void open() {
		controller.getBox().getBoxView(Concise.getCurrentWorkspace()).setEnabled(false);
		super.open();
	}
	
	public void close() {		
		
		Runnable run = new Runnable() {
			public void run() {
				
				if (CASpinner.this.isDisposed()) {
					return;
				}

				int alpha = getAlpha();
				alpha -= 4;

				if (alpha <= 0) {
					CASpinner.this.setAlpha(0);
					CASpinner.this.dispose();
					controller.getBox().getBoxView(Concise.getCurrentWorkspace()).setEnabled(true);
					return;
				}

				setAlpha(alpha);
				
				getDisplay().timerExec(1000 / 120, this);
			}
		};
		
		getDisplay().timerExec(0, run);
	}
	
	/**
	 * Create rounded rectangle Region
	 * @return
	 */
	protected Region createRoundedRectangleRegion() {
		Region region = new Region();
		region.add(circle(radius, radius));
		region.add(circle(width - radius, radius));
		region.add(circle(radius, height - radius));
		region.add(circle(width - radius, height - radius));
		region.add(new Rectangle(radius, 0, width - 2 * radius, height));
		region.add(new Rectangle(0, radius, width, height - 2 * radius));
		return region;
	}
	
	private int[] circle(int offsetX, int offsetY) {
		int[] polygon = new int[8 * radius + 4];
		// x^2 + y^2 = r^2
		for (int i = 0; i < 2 * radius + 1; i++) {
			int x = i - radius;
			int y = (int) Math.sqrt(radius * radius - x * x);
			polygon[2 * i] = offsetX + x;
			polygon[2 * i + 1] = offsetY + y;
			polygon[8 * radius - 2 * i - 2] = offsetX + x;
			polygon[8 * radius - 2 * i - 1] = offsetY - y;
		}
		return polygon;
	}
	
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
