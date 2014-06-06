package org.sustudio.concise.app.gear.collocationalNetworker;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphItem;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLUtils;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.CAErrorMessageDialog;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.enums.SearchAction;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.IGearCollocatable;
import org.sustudio.concise.app.gear.IGearConcordable;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.app.helper.ZoomHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.CAQueryUtils;
import org.sustudio.concise.app.query.DefaultCollocQuery;
import org.sustudio.concise.app.query.DefaultConcQuery;
import org.sustudio.concise.app.thread.CollocationalNetworkThread;
import org.sustudio.concise.app.toolbar.CASearchAction;
import org.sustudio.concise.app.utils.Formats;
import org.sustudio.concise.app.widgets.CAAutoCompleteText;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.collocation.Collocate;

public class CollocationalNetworker 
	   extends GearController
	   implements IGearConcordable, IGearCollocatable {
	
	private int colorIndex;
	
	
	protected final List<List<Collocate>> collocateList = new ArrayList<List<Collocate>>();
	private Map<String, CAGraphNode> nodesData;
	
	private double maxFilterValue = Double.MIN_VALUE;
	private double minFilterValue = Double.MAX_VALUE;
	
	
	private NetworkGraph networkGraph;
	private NetworkDataPanel dataPanel;
	
	protected CAQuery baseQuery;
		
	public CollocationalNetworker() {
		super(CABox.GearBox, Gear.CollocationalNetworker);
	}

	@Override
	protected Control createControl() {
		final SashForm sash = new SashForm(this, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new FillLayout());
		
		networkGraph = new NetworkGraph(sash, SWT.BORDER);
		networkGraph.setLabelColor(new Color(getDisplay(), CAPrefs.NETWORK_LABEL_RGB));
		networkGraph.setHideNonSelectedEnabled(CAPrefs.NETWORK_HIDE_NON_SELECTED);
		networkGraph.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				int nodeCount = 0;
				for (GraphItem item : networkGraph.getSelection()) {
					if (item instanceof GraphNode)
						nodeCount++;
				}
				if (nodeCount != 1) return;
				
				String nodeWord = null;
				for (GraphNode node : networkGraph.getNodes()) {
					if (networkGraph.getSelection().contains(node)) {
						nodeWord = node.getText();
						break;
					}
				}
				if (nodeWord == null || nodeWord.equals("")) return;
				
				// expand network
				//   build a new concise query
				CAQuery subQuery = new CAQuery(baseQuery);
				subQuery.searchAction = SearchAction.WORD;
				subQuery.searchStr = nodeWord;
				CollocationalNetworkThread thread = new CollocationalNetworkThread(subQuery, true);
				//thread.setCancelWarningMessage("Do you want to cancel current task?");
				thread.start();
			}
		});
		networkGraph.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				dataPanel.selectNodes(networkGraph.getSelectedNodes());
			}
		});
		networkGraph.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.BS:
				case SWT.DEL:
					dataPanel.deleteNodes();
					break;
				}
			}
		});
		
		final Composite sideComposite = new Composite(sash, SWT.EMBEDDED);
		sideComposite.setLayout(new GridLayout());
		
		Group buttons = new Group(sideComposite, SWT.SHADOW_IN);
		buttons.setText("Add Node");
		buttons.setLayout(new FillLayout());
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final CAAutoCompleteText text = new CAAutoCompleteText(buttons, SWT.BORDER | SWT.SEARCH | SWT.CANCEL);
		text.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				if (text.getText().trim().isEmpty()) {
					return;
				}
				
				// create a sub-query
				CAQuery subQuery = new CAQuery(baseQuery);
				subQuery.searchAction = SearchAction.WORD;
				subQuery.searchStr = text.getText().trim();
				CollocationalNetworkThread thread = new CollocationalNetworkThread(subQuery, true);
				thread.start();
			}
		});
		text.setFont(getFont());
		CopyPasteHelper.listenTo(text);
		try {
			text.setIndexReader(Concise.getCurrentWorkspace().getIndexReader());
		} catch (IOException e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
		
		TabFolder tabFolder = new TabFolder(sideComposite, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		TabItem optionsTab = new TabItem(tabFolder, SWT.NONE);
		optionsTab.setText("Options");
		optionsTab.setControl(new NetworkOptionsComposite(tabFolder, SWT.NONE, this));
		
		TabItem dataTab = new TabItem(tabFolder, SWT.NONE);
		dataTab.setText("Data");
		dataPanel = new NetworkDataPanel(tabFolder, SWT.NONE, this);
		dataTab.setControl(dataPanel);
		
		sash.setWeights(new int[] {70, 30});
		
		return sash;
	}
	
	
	/**
	 * Returns the {@link NetworkGraph}.
	 * @return
	 */
	public NetworkGraph getNetworkGraph() {
		return networkGraph;
	}
	
	@Override
	public Control getControl() {
		return networkGraph;
	}
	
	@Override
	protected void setZoomableControls() {
		ZoomHelper.addControls(new Control[] { networkGraph });
	}
	
	@Override
	public Control[] getZoomableControls() {
		return new Control[] { networkGraph };
	}
	
	public void resetStatus() {
		setStatusText(Formats.getNumberFormat(networkGraph.getNodes().size()) + " nodes and " +
		  	  	  	  Formats.getNumberFormat(networkGraph.getConnections().size()) + " connections "
		  	  	  	  + "(" + CAPrefs.COLLOCATION_MODE.label() + ")");
	}
	
	@Override
	public void loadData() {
		
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		try 
		{
			int batchSize = 0;
			// read from database
			SQLiteDB.createTableIfNotExists(CATable.CollocationalNetworker);
			ResultSet rs = SQLiteDB.executeQuery("SELECT MAX(" + DBColumn.BatchIndex.columnName() + ") FROM " + CATable.CollocationalNetworker.name());
			if (rs.next()) {
				batchSize = rs.getInt(1) + 1;
			}
			rs.close();
			
			if (batchSize > 0) {
				// load query
				baseQuery = CAQueryUtils.getQuery(getGear());
			}
			
			for (List<Collocate> list : this.collocateList) {
				list.clear();
			}
			this.collocateList.clear();
			
			for (int i=0; i<batchSize; i++) 
			{
				ArrayList<Collocate> collList = new ArrayList<Collocate>();
				String sql = SQLUtils.selectSyntax(CATable.CollocationalNetworker,
												   DBColumn.BatchIndex.columnName() + " = " + i,
												   DBColumn.NodeFreq,
												   SWT.DOWN);
				rs = SQLiteDB.executeQuery(sql);
				while (rs.next()) {
					String word = rs.getString(DBColumn.Collocate.columnName());
					long signatureO = rs.getLong(DBColumn.SignatureO.columnName());
					long signatureF1 = rs.getLong(DBColumn.SignatureF1.columnName());
					long signatureF2 = rs.getLong(DBColumn.SignatureF2.columnName());
					long signatureN = rs.getLong(DBColumn.SignatureN.columnName());
					long nodeFreq = rs.getLong(DBColumn.NodeFreq.columnName());
					Collocate collocate = new Collocate(word, signatureO, signatureF1, signatureF2, signatureN);
					collocate.setNodeFreq(nodeFreq);
					collList.add(collocate);
				}
				rs.close();
				
				if (!collList.isEmpty()) {
					addInput(collList);
				}
			}
		
		} catch (Exception e) {
			CAErrorMessageDialog.open(getGear(), e);
		}
		
		redrawGraph();
		resetStatus();
		
		spinner.close();
		super.loadData();
	}
	
	@Override
	protected void unloadData() {
		for (List<Collocate> list : collocateList) {
			list.clear();
		}
		collocateList.clear();
		super.unloadData();
	}

	@Override
	public boolean isConcordEnabled() {
		return networkGraph.getHoveredNode() != null;
	}

	@Override
	public void showConcord() {
		if (networkGraph.getHoveredNode() != null) {
			String word = networkGraph.getHoveredNode().getText();
			Gear.Concordancer.open(workspace);
			CASearchAction.doIt(new DefaultConcQuery(word));
		}
	}

	@Override
	public boolean isCollocateEnabled() {
		return networkGraph.getHoveredNode() != null;
	}

	@Override
	public void showCollocate() {
		if (networkGraph.getHoveredNode() != null) {
			String word = networkGraph.getHoveredNode().getText();
			Gear.Collocator.open(workspace);
			CASearchAction.doIt(new DefaultCollocQuery(word));
		}
	}
	
	/**
	 * resize nodes by degree (number of connections).
	 */
	public void resizeNodesByDegree() {
		int max = 0;
		int min = Integer.MAX_VALUE;
		for (Iterator<GraphNode> iterator = networkGraph.getNodes().iterator(); iterator.hasNext(); ) {
			Object node = iterator.next();
			if (node instanceof CAGraphNode) {
				int degree = ((CAGraphNode) node).getDegree();
				max = degree > max ? degree : max;
				min = degree < min ? degree : min;
			}
		}
		
		for (Iterator<GraphNode> iterator = networkGraph.getNodes().iterator(); iterator.hasNext(); ) {
			Object node = iterator.next();
			if (node instanceof CAGraphNode) {
				int size = (((CAGraphNode) node).getDegree() - min) * (CAPrefs.NETWORK_MAX_NODE_SIZE - CAPrefs.NETWORK_MIN_NODE_SIZE) / (max - min == 0 ? 1 : max - min) + CAPrefs.NETWORK_MIN_NODE_SIZE;
				((CAGraphNode) node).setShapeSize(size, size);
			}
		}
	}
	

	/**
	 * Adds collocate lists to the current network graph.
	 * @param colls		collocate list
	 */
	private void addInput(List<Collocate> colls) {
		colorIndex = 0;
		Collections.sort(colls, new Comparator<Collocate>() {

			@Override
			public int compare(Collocate c1, Collocate c2) {
				double v1 = CAPrefs.NETWORK_COMPARATOR.getValue(c1);
				double v2 = CAPrefs.NETWORK_COMPARATOR.getValue(c2);
				if (v1 > maxFilterValue) maxFilterValue = v1;
				if (v2 > maxFilterValue) maxFilterValue = v2;
				if (v1 < minFilterValue) minFilterValue = v1;
				if (v2 < minFilterValue) minFilterValue = v2;
				
				// give priority to nuclear words
				if (c1.getNodeFreq() > c2.getNodeFreq())
					return -1;
				else if (c1.getNodeFreq() < c2.getNodeFreq())
					return 1;
				return 0;
			}
			
		});
		
		// test if the batch is already exists
		if (collListsContainsCollocateList(colls)) {
			return;
		}
		collocateList.add(colls);
	}
	

	/**
	 * Tests if the collocate list already has nuclear word in it.
	 * @param word		nuclear word.
	 * @return			true if nuclear word already exists.
	 */
	private boolean collListsContainsCollocateList(final List<Collocate> collList) {
		
		ArrayList<String> wordsToTest = new ArrayList<String>();
		for (Collocate c : collList) {
			if (c.getNodeFreq() > 0) {
				wordsToTest.add(c.getWord());
			}
		}
		
		for (List<Collocate> colls : collocateList) {
			for (Collocate c : colls) {
				if (c.getNodeFreq() > 0) {
					// test word exists
					for (Iterator<String> iter = wordsToTest.iterator(); iter.hasNext(); ) {
						if (c.getWord().equals(iter.next())) {
							iter.remove();
						}
					}
				}
			}
		}
		
		if (wordsToTest.isEmpty()) {
			return true;
		}
		wordsToTest.clear();
		return false;
	}

	/**
	 * Redraw network graph content
	 */
	protected void redrawGraph() {		
		colorIndex = 0;
		networkGraph.clear();
		
		// reset nodes data
		nodesData = new HashMap<String, CAGraphNode>();
		for (List<Collocate> list : collocateList) {
			createGraphDataFrom(list);
		}
		
		//adjustConnectionWeight();
		nodesData.clear();
		
		networkGraph.setLayoutAlgorithm(CAPrefs.NETWORK_LAYOUT.getAlgorithm(), false);
		networkGraph.applyLayoutNow();
		
		resizeNodesByDegree();
		
		// update data panel
		if (dataPanel != null && !dataPanel.isDisposed()) {
			dataPanel.update();
		}
	}
	
	/**
	 * Returns next color defined in color schemes.
	 * @return		next color in color schemes.
	 */
	protected Color nextNodeColor() {
		if (CAPrefs.NETWORK_COLOR_SCHEME == null)
			CAPrefs.NETWORK_COLOR_SCHEME = new RGB[] {
							new RGB(0, 101, 133), new RGB(255, 99, 49),	new RGB(175, 105, 134),
							new RGB(108, 142, 0), new RGB(237, 165, 62), new RGB(117, 51, 66)
						};
		Color c = SWTResourceManager.getColor(CAPrefs.NETWORK_COLOR_SCHEME[colorIndex % CAPrefs.NETWORK_COLOR_SCHEME.length]);
		colorIndex++;
		return c;
	}

	/**
	 * Creates nodes and links of the {@link NetworkGraph}.
	 */
	private void createGraphDataFrom(List<Collocate> colls) {
		int listIndex = collocateList.indexOf(colls);
		
		// create nodes
		Color nodeColor = nextNodeColor();
		
		List<CAGraphNode> nodeWords = new ArrayList<CAGraphNode>();
		for (Collocate coll : colls) {	
			String word = coll.getWord();
			CAGraphNode node = nodesData.get(word);
			if (node == null) {
				node = new CAGraphNode(networkGraph, SWT.NONE, word);
				node.setBackgroundColor(nodeColor);
				node.setHighlightColor(FigureUtilities.lighter(nodeColor));
				node.setForegroundColor(networkGraph.getLabelColor());
				node.setData("colorIndex", listIndex);
				nodesData.put(word, node);
			}
			
			// reset node words' color.
			if (coll.getNodeFreq() > 0) {
				nodeWords.add(node);
				node.setBackgroundColor(nodeColor);
				node.setHighlightColor(FigureUtilities.lighter(nodeColor));
				node.setData("colorIndex", listIndex);
			}
		}
		
		// create edges
		for (Collocate coll : colls) {
			CAGraphNode node = nodesData.get(coll.getWord());
			
			GraphConnection conn;
			for (CAGraphNode nodeWord: nodeWords) {
				if (nodeWord.equals(node)) {
					continue;	// itself
				}
				if (nodeWord.isConnectedTo(node)) {
					// TODO this is not working....
					continue;		// connection exists
				}
				
				conn = new GraphConnection(networkGraph, SWT.NONE, nodeWord, node);
				conn.setData(coll);
				conn.setCurveDepth(5);
				conn.setLineColor(FigureUtilities.mixColors(networkGraph.getBackground(), FigureUtilities.lighter(nodeColor)));
				conn.setLineColor(conn.getLineColor());
				conn.setHighlightColor(FigureUtilities.lighter(nodeColor));
				String value = "";
				switch (CAPrefs.NETWORK_COMPARATOR) {
				case Cooccurrence:
					value = Formats.getNumberFormat((long) CAPrefs.NETWORK_COMPARATOR.getValue(coll));
					break;
				default:
					value = Formats.getDecimalFormat(CAPrefs.NETWORK_COMPARATOR.getValue(coll));
					break;
				}
				IFigure tooltip = new Label(nodeWord.getText() + " - " + node.getText() + "\n" + 
											value);
				conn.setTooltip(tooltip);
				conn.setData("colorIndex", listIndex);
				conn.setData("collocate", coll);
			}
		}
	}

	
	
	/*
	 * Adjust connection weight.
	 * TODO weight
	private void adjustConnectionWeight() {
		for(Iterator iterator = networkGraph.getConnections().iterator(); 
				iterator.hasNext(); ) {
			Object edge = iterator.next();
			if (edge instanceof GraphConnection) {
				GraphConnection conn = (GraphConnection) edge;
				double value = CAPrefs.NETWORK_COMPARATOR.getValue((Collocate) conn.getData()); 
				double weight = (value - minFilterValue) / (maxFilterValue - minFilterValue);
				conn.setWeight(weight);
			}
		}
	}
	*/
}
