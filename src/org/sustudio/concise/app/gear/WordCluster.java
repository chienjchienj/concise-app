package org.sustudio.concise.app.gear;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sustudio.concise.app.db.CADataUtils;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.helper.SaveOutputHelper;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.DefaultCollocQuery;
import org.sustudio.concise.app.query.DefaultConcQuery;
import org.sustudio.concise.app.thread.CAClusterThread;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.cluster.Cluster;

public class WordCluster 
	   extends GearController 
	   implements IGearSortable, IGearTableBased, IGearFilterable, 
				  IGearConcordable, IGearCollocatable {

	private Table table;
		
	public WordCluster() {
		super(CABox.GearBox, Gear.WordCluster);
	}
	
	@Override
	public Control createControl() {
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setMoveable(true);
		tableColumn.setWidth(100);
		tableColumn.setText("#");
		
		final TableColumn tblclmnFreq = new TableColumn(table, SWT.RIGHT);
		tblclmnFreq.setMoveable(true);
		tblclmnFreq.setWidth(100);
		tblclmnFreq.setText("Freq.");
		tblclmnFreq.setData(_DB_COLUMN, DBColumn.Freq);
		tblclmnFreq.addSelectionListener(columnSortListener);
		
		final TableColumn tblclmnWord = new TableColumn(table, SWT.NONE);
		tblclmnWord.setMoveable(true);
		tblclmnWord.setWidth(100);
		tblclmnWord.setText("Word Cluster");
		tblclmnWord.setData(_DB_COLUMN, DBColumn.Cluster);
		tblclmnWord.addSelectionListener(columnSortListener);
		
		table.setSortColumn(tblclmnFreq);
		table.setSortDirection(SWT.DOWN);
		
		table.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (workspace.DATA.clusterList == null) return;
				
				final TableItem item = (TableItem) event.item;
				final int index = event.index;
				String[] texts = getItemTexts(index);
				if (texts != null) {
					item.setText(texts);
				}
			}
		});
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent event) {
				showConcord();
			}
		});
		
		table.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent event) {
				int width = table.getClientArea().width;
				for (int i = 0; i < table.getColumnCount() - 1; i++) {
					width -= table.getColumn(i).getWidth();
				}
				table.getColumn(table.getColumnCount()-1).setWidth(width);
			}
		});
		
		
		// Cluster[] transfer dragSource
	    Transfer[] types = new Transfer[] { WordClusterTransfer.getInstance() };
	    final DragSource dragSource = new DragSource(table, DND.DROP_COPY | DND.DROP_MOVE);
	    dragSource.setTransfer(types);
	    
	    dragSource.addDragListener(new DragSourceAdapter() {
	    	public void dragStart(DragSourceEvent event) {
	    		if (table.getSelectionCount() > 0) {
	    			event.doit = true;
	    		} else {
	    			event.doit = false;
	    		}
	    	};
	    	
	    	public void dragSetData(DragSourceEvent event) {
	    		if (WordClusterTransfer.getInstance().isSupportedType(event.dataType)) {
	    			List<Cluster> list = new ArrayList<Cluster>();
		    		for (int index : table.getSelectionIndices()) {
		    			Cluster cluster = (Cluster) workspace.DATA.clusterList.get(index);
		    			list.add(cluster);
		    		}
		    		event.data = list.toArray(new Cluster[0]);
	    		}
	    	}
	    	
	    	public void dragFinished(DragSourceEvent event) {
	    		// do nothing
	    	}
	    });
		
		return table;
	}
	
	public void loadData() {
		table.removeAll();
		table.setItemCount(0);
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		final DBColumn sortColumn = (DBColumn) table.getSortColumn().getData(_DB_COLUMN);
		final int sortDirection = table.getSortDirection();
		Thread thread = new Thread() { public void run() {
			int count = 0;
			try 
			{
				CADataUtils.resetClusterList(
									sortColumn,
									sortDirection,
									getFinder().whereSyntax());
				count = workspace.DATA.clusterList.size();
				
			} catch (Exception e) {
				CAErrorMessageDialog.open(getGear(), e);
			} finally {
				final int itemCount = count;
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						setStatusText(Formats.getNumberFormat(itemCount) + " clusters.");
						table.setItemCount(itemCount);
						table.update();
						
						spinner.close();
						SaveOutputHelper.listenTo(getGear());
					}
				});
			}
		} };
		thread.setDaemon(true);
		thread.start();
	}
	
	protected void unloadData() {
		if (workspace.DATA.clusterList != null)
			workspace.DATA.clusterList.clear();
		workspace.DATA.clusterList = null;
		super.unloadData();
	}

	public void sort() {
		try 
		{
			CADataUtils.resetClusterList(
					(DBColumn) table.getSortColumn().getData(_DB_COLUMN),
					table.getSortDirection(),
					getFinder().whereSyntax());
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		} finally {
			table.clearAll();
			table.update();
		}
	}
	
	@Override
	public String[] getItemTexts(int index) {
		try {
			
			Cluster c = workspace.DATA.clusterList.get(index);
			String freq 	= Formats.getNumberFormat(c.freq);
			String cluster 	= c.word;
			
			return new String[] { String.valueOf(index+1), freq, cluster };
			
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		}
		return null;
	}

	@Override
	public boolean isConcordEnabled() {
		return table.getSelectionCount() == 1;
	}

	@Override
	public void showConcord() {
		if (table.getSelectionCount() == 1) {
			String word = workspace.DATA.clusterList.get(table.getSelectionIndex()).word;
			word = "\"" + word.trim() + "\"";	// add quotation mark to make phrase search 
			Gear.Concordancer.open(workspace)
				.doit(new DefaultConcQuery(word));
		}
	}

	@Override
	public boolean isCollocateEnabled() {
		return table.getSelectionCount() == 1;
	}

	@Override
	public void showCollocate() {
		if (table.getSelectionCount() == 1) {
			String word = workspace.DATA.clusterList.get(table.getSelectionIndex()).word;
			word = "\"" + word.trim() + "\"";	// add quotation mark to make phrase search 
			Gear.Collocator.open(workspace)
				.doit(new DefaultCollocQuery(word));
		}
	}

	@Override
	public void showFinder() {
		getFinder().setHidden(false);
	}

	@Override
	public void doit(CAQuery query) {
		ConciseThread thread = new CAClusterThread(query);
		thread.start();
	}
}
