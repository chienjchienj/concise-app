package org.sustudio.concise.app.gear.collocationalNetworker;

import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.core.collocation.Collocate;
import org.eclipse.nebula.widgets.pshelf.PShelf;
import org.eclipse.nebula.widgets.pshelf.PShelfItem;

public class NetworkDataPanel extends Composite {
	
	private final NetworkGraph networkGraph;
	private Table tblNode;
	private Table tblEdge;

	public NetworkDataPanel(Composite parent, int style, NetworkGraph networkGraph) {
		super(parent, style);
		this.networkGraph = networkGraph;
		
		setLayout(new FillLayout());
		
		final PShelf shelf = new PShelf(this, SWT.NONE);
		
		createNodeItem(shelf);
		createEdgeItem(shelf);
		
	}
	
	protected PShelfItem createNodeItem(PShelf shelf) {
		PShelfItem nodeItem = new PShelfItem(shelf, SWT.NONE);
		nodeItem.setText("Node");
		nodeItem.getBody().setLayout(new FillLayout());
		
		tblNode = new Table(nodeItem.getBody(), SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL | SWT.CHECK);
		tblNode.setHeaderVisible(true);
		tblNode.setLinesVisible(true);
		tblNode.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				CAGraphNode node = (CAGraphNode) networkGraph.getNodes().get(index);
				item.setText(0, node.getText());
				item.setText(1, String.valueOf(node.isVisible()));
				item.setChecked(node.isVisible());
			}
		});
		tblNode.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.keyCode == SWT.ESC) {
					networkGraph.unhighlightAllConnections();
					networkGraph.unhighlightAllNodes();
					tblNode.setSelection(-1);
				}
				else if (event.keyCode == SWT.SPACE) {
					for (int index : tblNode.getSelectionIndices()) {
						CAGraphNode node = (CAGraphNode) networkGraph.getNodes().get(index);
						node.setVisible(!node.isVisible());
						tblNode.clear(index);
					}
				}
			}			
		});
		tblNode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail == SWT.CHECK) {
					TableItem item = (TableItem) event.item;
					int index = tblNode.indexOf(item);
					CAGraphNode node = (CAGraphNode) networkGraph.getNodes().get(index);
					node.setVisible(!node.isVisible());
					tblNode.clear(index);
				}
				else {
					networkGraph.unhighlightAllConnections();
					networkGraph.unhighlightAllNodes();
					if (tblNode.getSelectionCount() > 0) {
						networkGraph.fadeoutAllNodes();
					}
					for (TableItem item : tblNode.getSelection()) {
						int index = tblNode.indexOf(item);
						CAGraphNode node = (CAGraphNode) networkGraph.getNodes().get(index);
						networkGraph.hightlightNodeAndItsConnectedNodes(node);
					}
				}
			}
		});
		CopyPasteHelper.listenTo(tblNode);
		
		TableColumn tblclmnNode = new TableColumn(tblNode, SWT.NONE);
		tblclmnNode.setWidth(100);
		tblclmnNode.setText("Node");
		
		TableColumn tblclmnVisible = new TableColumn(tblNode, SWT.NONE);
		tblclmnVisible.setWidth(100);
		tblclmnVisible.setText("Visible");
		
		return nodeItem;
	}
	
	protected PShelfItem createEdgeItem(PShelf shelf) {
		PShelfItem edgeItem = new PShelfItem(shelf, SWT.NONE);
		edgeItem.setText("Edge");
		edgeItem.getBody().setLayout(new FillLayout());
		
		tblEdge = new Table(edgeItem.getBody(), SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK | SWT.VIRTUAL);
		tblEdge.setHeaderVisible(true);
		tblEdge.setLinesVisible(true);
		tblEdge.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				GraphConnection edge = (GraphConnection) networkGraph.getConnections().get(index);
				item.setText(0, edge.getSource().getText());
				item.setText(1, edge.getDestination().getText());
				Collocate coll = (Collocate) edge.getData("collocate");
				item.setText(2, String.valueOf(CAPrefs.NETWORK_COMPARATOR.getValue(coll)));
				item.setChecked(edge.isVisible());
			}
		});
		tblEdge.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.keyCode == SWT.ESC) {
					networkGraph.unhighlightAllConnections();
					networkGraph.unhighlightAllNodes();
					tblEdge.setSelection(-1);
				}
			}			
		});
		tblEdge.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail == SWT.CHECK) {
					TableItem item = (TableItem) event.item;
					int index = tblEdge.indexOf(item);
					GraphConnection edge = (GraphConnection) networkGraph.getConnections().get(index);
					edge.setVisible(!edge.isVisible());
					tblEdge.clear(index);
				}
			}
		});
		CopyPasteHelper.listenTo(tblEdge);
		
		TableColumn tblclmnSource = new TableColumn(tblEdge, SWT.NONE);
		tblclmnSource.setWidth(100);
		tblclmnSource.setText("Source");
		
		TableColumn tblclmnDestination = new TableColumn(tblEdge, SWT.NONE);
		tblclmnDestination.setWidth(100);
		tblclmnDestination.setText("Destination");
		
		TableColumn tblclmnNewColumn = new TableColumn(tblEdge, SWT.NONE);
		tblclmnNewColumn.setWidth(150);
		tblclmnNewColumn.setText("Value");
		
		return edgeItem;
	}
	
	public void update() {
		super.update();
		
		// build nodes
		tblNode.clearAll();
		tblNode.setItemCount(networkGraph.getNodes().size());
		
		// build connections
		tblEdge.getColumn(2).setText(CAPrefs.NETWORK_COMPARATOR.name());
		tblEdge.clearAll();
		tblEdge.setItemCount(networkGraph.getConnections().size());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
