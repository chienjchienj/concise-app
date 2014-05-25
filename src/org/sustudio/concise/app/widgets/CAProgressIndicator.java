package org.sustudio.concise.app.widgets;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;

public class CAProgressIndicator extends Canvas {

	private Thread thread;
	private boolean stopped = false;
	private Image loadingImage;
	
	public CAProgressIndicator(Composite parent, int style) {
		super(parent, style);
		
		final GC gc = new GC(this);
		final ImageLoader loader = new ImageLoader();
		final ImageData[] data = loader.load(getClass().getResourceAsStream("/org/sustudio/concise/app/icon/loader.gif"));
		
		setBackground(parent.getBackground());
		setSize(loader.logicalScreenWidth, loader.logicalScreenHeight);
		
		thread = new Thread() {
			public void run() {
				Image offScreenImage = new Image(Display.getCurrent(), loader.logicalScreenWidth, loader.logicalScreenHeight);
				GC offScreenImageGC = new GC(offScreenImage);
				offScreenImageGC.fillRectangle(0, 0, loader.logicalScreenWidth, loader.logicalScreenHeight);
				
				try {
					int imageDataIndex = 0;
					ImageData imageData = data[imageDataIndex];
					if (loadingImage != null && !loadingImage.isDisposed()) loadingImage.dispose();
					loadingImage = new Image(Display.getCurrent(), imageData);
					offScreenImageGC.drawImage(loadingImage, 0, 0, imageData.width, imageData.height, imageData.x, imageData.y, imageData.width, imageData.height);
										
					while (!stopped) {
						imageDataIndex = (imageDataIndex + 1) % data.length;
						imageData = data[imageDataIndex];
						loadingImage.dispose();
						loadingImage = new Image(Display.getCurrent(), imageData);
						offScreenImageGC.drawImage(loadingImage, 0, 0, imageData.width, imageData.height, imageData.x, imageData.y, imageData.width, imageData.height);
						
						gc.drawImage(loadingImage, 0, 0);
						try {
							Thread.sleep(imageData.delayTime * 10);
						} catch (InterruptedException e) {
							// eat...
						}
					}
				} catch (Exception e) {
					CAErrorMessageDialog.open(null, e);
				} finally {
					if (offScreenImage != null && !offScreenImage.isDisposed()) offScreenImage.dispose();
					if (offScreenImageGC != null && !offScreenImageGC.isDisposed()) offScreenImageGC.dispose();
					if (loadingImage!= null && !loadingImage.isDisposed()) loadingImage.dispose();
				}
			}
		};
		thread.setDaemon(true);
	}
	
	public void startAnimation() {
		stopped = false;
		thread.start();
	}

	public void stopAnimation() {
		stopped = true;
	}
	
}
