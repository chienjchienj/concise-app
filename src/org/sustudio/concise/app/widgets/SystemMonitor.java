package org.sustudio.concise.app.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.systemMonitor.SampleIdentifier;

public class SystemMonitor extends org.mihalis.opal.systemMonitor.SystemMonitor {

	static Shell shell;
	
	public static void open() {
		
		if (shell == null || shell.isDisposed()) {
			shell = new Shell(Display.getCurrent());
			shell.setText("System Monitor");
			shell.setLayout(new GridLayout(1, false));
		
	        final SystemMonitor cpu = new SystemMonitor(shell, SWT.NONE, SampleIdentifier.CPU_USAGE);
	        cpu.setLayoutData(createLayoutData());
	
	        final SystemMonitor heap = new SystemMonitor(shell, SWT.NONE, SampleIdentifier.HEAP_MEMORY);
	        heap.setLayoutData(createLayoutData());
	
	        final SystemMonitor physical = new SystemMonitor(shell, SWT.NONE, SampleIdentifier.PHYSICAL_MEMORY);
	        physical.setLayoutData(createLayoutData());
	
	        final SystemMonitor threads = new SystemMonitor(shell, SWT.NONE, SampleIdentifier.THREADS);
	        threads.setLayoutData(createLayoutData());
	        
	        shell.pack();
	        shell.open();
		}
		else {
			shell.forceActive();
		}
	}
	
	private static GridData createLayoutData() {
        final GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
        gd.widthHint = 300;
        gd.heightHint = 80;
        return gd;
	}
	
	
	private SystemMonitor(Composite parent, int style,
			SampleIdentifier identifier) {
		super(parent, style, identifier);
	}
}
