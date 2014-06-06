package org.sustudio.concise.app.gear.concordancePlotter;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CADataUtils;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.DocumentViewer;
import org.sustudio.concise.app.gear.IGearFileRevealable;
import org.sustudio.concise.app.gear.IGearFilterable;
import org.sustudio.concise.app.gear.IGearSortable;
import org.sustudio.concise.app.gear.IGearTableBased;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.utils.RevealInFinder;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class ConcordancePlotter 
	   extends GearController 
	   implements IGearTableBased, IGearSortable, IGearFilterable, IGearFileRevealable {

	private Table table;
	
	public ConcordancePlotter() {
		super(CABox.GearBox, Gear.ConcordancePlotter);
		
	}
	
	protected Control createControl() {
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final TableColumn tableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumn.setMoveable(true);
		tableColumn.setWidth(50);
		tableColumn.setText("#");
		
		final TableColumn tblclmnPlot = new TableColumn(table, SWT.CENTER);
		tblclmnPlot.setMoveable(true);
		tblclmnPlot.setWidth(400);
		tblclmnPlot.setText("Plot");
		tblclmnPlot.setData(_DB_COLUMN, DBColumn.PlotData);
		//tblclmnPlot.addSelectionListener(tableColumnSortListener);		
		
		final TableColumn tblclmnHits = new TableColumn(table, SWT.RIGHT);
		tblclmnHits.setMoveable(true);
		tblclmnHits.setWidth(60);
		tblclmnHits.setText("Hits");
		tblclmnHits.setData(_DB_COLUMN, DBColumn.Hits);
		tblclmnHits.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnWords = new TableColumn(table, SWT.RIGHT);
		tblclmnWords.setMoveable(true);
		tblclmnWords.setWidth(70);
		tblclmnWords.setText("Words");
		tblclmnWords.setData(_DB_COLUMN, DBColumn.Words);
		tblclmnWords.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnPer = new TableColumn(table, SWT.RIGHT);
		tblclmnPer.setMoveable(true);
		tblclmnPer.setWidth(60);
		tblclmnPer.setText("Per 1000");
		tblclmnPer.setData(_DB_COLUMN, DBColumn.Per1000);
		tblclmnPer.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnFile = new TableColumn(table, SWT.NONE);
		tblclmnFile.setMoveable(true);
		tblclmnFile.setWidth(100);
		tblclmnFile.setText("File");
		tblclmnFile.setData(_DB_COLUMN, DBColumn.Filepath);
		tblclmnFile.addSelectionListener(columnSortListener);
		
		table.setSortDirection(SWT.UP);
		table.setSortColumn(tblclmnFile);
		
		
		table.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				String[] texts = getItemTexts(index);
				item.setText(texts);
				
				item.setData(_DOC_ID, workspace.DATA.plotDataList.get(index).docID);
				
				// show program icon
				if (CAPrefs.SHOW_PROGRAM_ICON) {
					final ImageData imageData = Program.findProgram(FilenameUtils.getExtension(item.getText(5))).getImageData();
					item.setImage(5, new Image(getDisplay(), imageData));
				}
			}
		});
		
		table.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent event) {
				int width = table.getClientArea().width;
				for (TableColumn col : table.getColumns()) {
					if (!col.equals(tblclmnPlot)) {
						width -= col.getWidth();
					}
				}
				tblclmnPlot.setWidth(width < 400 ? 400 : width);
			}
		});
		
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				openFileInDocumentViewer();
			}
			
			public void mouseUp(MouseEvent event) {
				final Point pt = new Point(event.x, event.y);
				final TableItem item = table.getItem(pt);
				if (item == null) {
					return;
				}
				// 限制在 plot 的那個 column 裡面才有作用
				Rectangle rect = item.getBounds(1);
				if (item.getData(_WORD_ID) != null && rect.contains(pt)) {
					openFileInDocumentViewer();
					return;
				}
			}
		});
		
		table.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent event) {
				// 計算點點是哪個字
				table.setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
				final TableItem item = table.getItem(new Point(event.x, event.y));
				if (item == null ||
					!item.getBounds(1).contains(event.x, event.y)) 
				{
					return;
				}
				
				// reset
				item.setData(_WORD_ID, null);
				
				final int index = table.indexOf(item);
				final ConcPlotData data = workspace.DATA.plotDataList.get(index);
				
				final Rectangle rect = item.getBounds(1);
				final int radius = rect.height/2-1;
				final int mouseX = event.x - rect.x - 2;
				
				int nearestIndex = -1;
				int distance = radius;  // 圓的半徑，超過這個距離就不被考慮
				for (int i = data.positions.length - 1; i >= 0; i--) {
					final long p = data.positions[i]; 
					int posX = Math.round( rect.width * p / data.words );
					if (posX > mouseX-radius && posX < mouseX+radius) {
						// 取最接近滑鼠指標的點
						if (Math.abs(posX - mouseX) < distance) {
							distance = Math.abs(posX - mouseX);
							nearestIndex = i + 1;
						}
					}
				}
				if (nearestIndex != -1) {
					table.setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
					item.setData(_WORD_ID, nearestIndex);
				}
			}
			
		});
		
		// custom drawing dispersion plot
		table.addListener(SWT.PaintItem, new Listener() {
			public void handleEvent(Event event) {
				if (event.index == 1) {
					final GC gc = event.gc;
					final int itemIndex = table.indexOf((TableItem)event.item);
					final ConcPlotData plotData = workspace.DATA.plotDataList.get(itemIndex);
					plotData.drawPlot(gc);
				}
			}
		});
		
		// custom tooltip label on dispersion plot
		final Listener labelListener = new Listener() {
			public void handleEvent(Event event) {
				Label label = (Label) event.widget;
				Shell shell = label.getShell();
				switch (event.type) {
				case SWT.MouseDown:
				case SWT.MouseExit:
					shell.dispose();
				}
			}
		};
		
		// custom tooltip listener
		Listener tooltipListener = new Listener() {
			Shell tip = null;
			Label label = null;
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Hide:
				case SWT.Dispose:
				case SWT.KeyDown:
				case SWT.MouseMove:
					if (tip != null) {
						tip.dispose();
						tip = null;
						label = null;
					}
					break;
				case SWT.MouseHover:
					Point pt = new Point(event.x, event.y);
					TableItem item = table.getItem(pt);
					if (item != null && 
						item.getBounds(1).contains(pt) &&
						item.getData(_WORD_ID) != null)
					{
						int index = table.indexOf(item);
						ConcPlotData data = workspace.DATA.plotDataList.get(index);
						int positionIndex = ((Integer) item.getData(_WORD_ID)) - 1;
						long p = data.positions[positionIndex];
						
						if (tip != null && !tip.isDisposed())
							tip.dispose();
						tip = new Shell(getShell(), SWT.ON_TOP | SWT.TOOL);
						tip.setLayout(new FillLayout());
						label = new Label(tip, SWT.NONE);
						label.setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
						label.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
						label.setText(data.getWordByPosition(p));
						label.addListener(SWT.MouseEnter, labelListener);
						label.addListener(SWT.MouseDown, labelListener);
						Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
						pt = table.toDisplay(pt);
						tip.setBounds(pt.x, pt.y+16, size.x, size.y);
						tip.setVisible(true);
					}
				}
			}			
		};
		
		// custom tooltip for dispersion plot
		table.addListener(SWT.Dispose, tooltipListener);
		table.addListener(SWT.KeyDown, tooltipListener);
		table.addListener(SWT.MouseMove, tooltipListener);
		table.addListener(SWT.MouseHover, tooltipListener);
		addListener(SWT.Hide, tooltipListener);
		
		return table;
	}
	
	public void loadData() {
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		int count = 0;
		try 
		{
			count = CADataUtils.resetPlotList(
									(DBColumn)table.getSortColumn().getData(_DB_COLUMN), 
									table.getSortDirection(), 
									getFinder().whereSyntax());
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		} finally {
			table.clearAll();
			table.setItemCount(count);
			table.update();
			setStatusText(count + " documents");
			spinner.close();
		}
		super.loadData();
	}
	
	protected void unloadData() {
		if (workspace.DATA.concLineList != null) {
			workspace.DATA.concLineList.clear();
		}
		workspace.DATA.concLineList = null;
		super.unloadData();
	}
	
	public void sort() {
		loadData();
	}
	
	public String[] getItemTexts(int index) {
		ConcPlotData plotData = workspace.DATA.plotDataList.get(index);
		return new String[] {
			String.valueOf(index + 1),
			"",
			Formats.getNumberFormat(plotData.hits),
			Formats.getNumberFormat(plotData.words),
			Formats.getPercentFormat(plotData.per1000),
			plotData.filepath
		};
	}

	@Override
	public boolean isRevealEnabled() {
		if (table.getSelectionCount() == 1) {
			int docID = (Integer) table.getSelection()[0].getData(_DOC_ID);
			try {
				
				final String filepath = workspace.getIndexReader().document(docID).get(ConciseField.FILENAME.field());
				return new File(filepath).exists();
				
			} catch (Exception e) {
				e.printStackTrace();
				Dialog.error(getShell(), "Exception", e.getMessage());
			}
		}
		return false;
	}

	@Override
	public void revealFileInFinder() {
		if (table.getSelectionCount() == 1) {
			int docID = (Integer) table.getSelection()[0].getData(_DOC_ID);
			try {
				
				final String filepath = workspace.getIndexReader().document(docID).get(ConciseField.FILENAME.field());
				if (new File(filepath).exists()) {
					RevealInFinder.show(filepath);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				Dialog.error(getShell(), "Exception", e.getMessage());
			}
		}
	}

	@Override
	public void openFileInDocumentViewer() {
		if (table.getSelectionCount() == 1) {
			final TableItem item = table.getSelection()[0];
			if (item.getData(_DOC_ID) != null) {
				DocumentViewer fv = (DocumentViewer) Gear.DocumentViewer.open(workspace);
				if (item.getData(_WORD_ID) != null) {
					fv.open((Integer) item.getData(_DOC_ID),
							(Integer) item.getData(_WORD_ID),
							Concise.getActiveApp().toolBar.getQuery());
				}
				else {
					fv.open((Integer) item.getData(_DOC_ID));
				}
			}
		}
	}

	@Override
	public void showFinder() {
		getFinder().setHidden(false);
	}

}
