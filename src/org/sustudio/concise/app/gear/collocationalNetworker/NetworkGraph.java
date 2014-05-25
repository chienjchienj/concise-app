package org.sustudio.concise.app.gear.collocationalNetworker;

import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphItem;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.gef4.zest.core.widgets.GraphWidget;
import org.eclipse.gef4.zest.core.widgets.LayoutFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * NetworkGraph is a custom Graph adding {@link #getImageData()} and other features.
 * 
 * @author Kuan-ming Su
 *
 */
public class NetworkGraph extends GraphWidget {
	
	private Color labelColor = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
	private boolean hideNonSelectedLabel = true;
	private boolean mouseDown = false;
	
	private CAGraphNode hoveredNode = null;
	
	
	/**
	 * Constructor.
	 * @param viewer		Network Viewer
	 * @param style			Normally, SWT.NONE
	 */
	public NetworkGraph(Composite viewer, int style) {
		super(viewer, style);
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				NetworkGraph.this.clear();
			}
		});
		
		// hover listener
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (!NetworkGraph.this.isFocusControl()) {
					NetworkGraph.this.setFocus();
				}
				hoveredNode = null;
				
				IFigure figure = getZoomManager().getViewport().findFigureAt(e.x, e.y);
				for (GraphNode node : getNodes()) {
					if (node instanceof CAGraphNode &&
						figure != null &&
						( ((CAGraphNode) node).getFigure().equals(figure) ||
						  ((CAGraphNode) node).getFigure().equals(figure.getParent())))
					{
						hoveredNode = (CAGraphNode) node;
						
						// check if the node is already highlighted
						if (((CAGraphNode) node).isHighlightOn())
							return;
						
						break;
					}
				}
				
				// unhighlight all node and connections
				unhighlightAllNodes();
				unhighlightAllConnections();
				
				if (hoveredNode != null) {
					// fade out all nodes
					fadeoutAllNodes();
					
					// high light node and its connected nodes
					hightlightNodeAndItsConnectedNodes(hoveredNode);
				}		
			}
		});
		
		// drag
		addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				mouseDown = false;
			}

			public void mouseDown(MouseEvent e) {
				mouseDown = true;
			}
		});
		
		addLayoutFilter(new LayoutFilter() {
			public boolean isObjectFiltered(GraphItem item) {
				// only GraphNode can be selected
				if (item instanceof GraphNode) {
					return item.getGraphModel().getSelection().contains(item)
							&& mouseDown; // MouseDown;
				}
				return false;
			}
		});	
	}
	
	/**
	 * Returns hovered {@link CAGraphNode}.
	 * @return
	 */
	public CAGraphNode getHoveredNode() {
		return hoveredNode;
	}
	
	/**
	 * 設定是否隱藏未選擇的部分
	 * @param hide
	 */
	public void setHideNonSelectedEnabled(boolean hide) {
		hideNonSelectedLabel = hide;
	}
	
	/**
	 * 是否隱藏未選擇的部分
	 * @return
	 */
	public boolean getHideNonSelectedEnabled() {
		return hideNonSelectedLabel;
	}
	
	
	/**
	 * Select given nodes
	 * @param nodes		node list
	 */
	public void select(List<CAGraphNode> nodes) {
		if (nodes != null) {
			fadeoutAllNodes();
			for (CAGraphNode node : nodes)
				node.unhighlight();
				//node.highlight();
		}
	}
	
	/**
	 * Un-highlight all nodes
	 */
	@SuppressWarnings("rawtypes")
	public void unhighlightAllNodes() {
		List nodes = getNodes();
		for (Iterator iterator = nodes.iterator(); iterator.hasNext(); ) {
			Object node = iterator.next();
			if (node instanceof CAGraphNode) {
				((CAGraphNode) node).unhighlight();
				if (hideNonSelectedLabel) {
					((CAGraphNode) node).showLabel();
				}
			}
		}
	}
	
	/**
	 * Un-highlight all connections.
	 */
	@SuppressWarnings("rawtypes")
	public void unhighlightAllConnections() {
		List connctions = getConnections();
		for (Iterator iterator = connctions.iterator(); iterator.hasNext();) {
			Object conn = iterator.next();
			if (conn instanceof GraphConnection) {
				GraphConnection connection = (GraphConnection) conn;
				connection.unhighlight();
				
				setLineAlpha(connection, 0.4);
			}
		}
	}
	
	/**
	 * Fade out all nodes.
	 */
	public void fadeoutAllNodes() {
		List<GraphNode> nodes = getNodes();
		for (Iterator<GraphNode> iterator = nodes.iterator(); iterator.hasNext(); ) {
			Object node = iterator.next();
			if (node instanceof CAGraphNode) {
				((CAGraphNode) node).fadeOut();
				if (hideNonSelectedLabel) {
					((CAGraphNode) node).hideLabel();
				}
			}
		}
		
		List<GraphConnection> connections = getConnections();
		if (connections != null) {
			for (GraphConnection connection : connections) {
				setLineAlpha(connection, 0.2);
			}
		}
	}
	
	/**
	 * Highlight node and its immediate connected nodes
	 * @param node
	 */
	public void hightlightNodeAndItsConnectedNodes(CAGraphNode node) {
		// highlight hover node
		node.highlight();
		if (hideNonSelectedLabel) {
			node.showLabel();
		}
		
		// highlight target nodes
		for (GraphConnection conn : node.getSourceConnections()) {
			GraphConnection edge = (GraphConnection) conn;
			setLineAlpha(edge, 0.8);
			edge.getDestination().highlight();
			if (edge.getDestination() instanceof CAGraphNode && hideNonSelectedLabel) {
				((CAGraphNode) edge.getDestination()).showLabel();;
				
			}
			edge.highlight();
		}
		
		// highlight source nodes
		for (GraphConnection conn : node.getTargetConnections()) {
			GraphConnection edge = (GraphConnection) conn;
			setLineAlpha(edge, 0.8);
			edge.getSource().highlight();
			if (edge.getSource() instanceof CAGraphNode && hideNonSelectedLabel) {
				((CAGraphNode) edge.getSource()).showLabel();
			}
			edge.highlight();
		}
	}
	
	
	public void setBackground(Color color) {
		super.setBackground(color);
		
		// update line colors
		// GraphConnection 沒有 alpha 可以設，只能自己做
		List<GraphConnection> connections = getConnections();
		if (connections != null) {
			for (GraphConnection connection : connections) {
				setLineAlpha(connection, 0.4);
			}
		}
	}
	
	/**
	 * Set alpha value of connections (blend the background and foreground)
	 * @param connection
	 * @param alpha
	 */
	private void setLineAlpha(GraphConnection connection, double alpha) {
		Color c = FigureUtilities.mixColors(
						FigureUtilities.lighter(connection.getSource().getBackgroundColor()),
						getBackground(),
						alpha);
		connection.setLineColor(c);
		connection.setHighlightColor(c);
	}

	/**
	 * set color scheme
	 * @param colorScheme
	 */
	public void setColorScheme(RGB[] colorScheme) {
		List<GraphNode> nodes = getNodes();
		for (GraphNode node : nodes) {
			int colorIdx = (Integer) node.getData("colorIndex");
			Color c = SWTResourceManager.getColor(colorScheme[colorIdx % colorScheme.length]);
			node.setBackgroundColor(c);
			node.setHighlightColor(FigureUtilities.lighter(c));
		}
		
		List<GraphConnection> edges = getConnections();
		for (GraphConnection edge : edges) {
			setLineAlpha(edge, 0.4);
		}
	}
	
	
	/**
	 * Sets label (word) color.
	 * @param color		label color.
	 */
	@SuppressWarnings("rawtypes")
	public void setLabelColor(final Color color) {
		labelColor = color;
		List nodes = getNodes();
		for (Iterator iteration = nodes.iterator(); iteration.hasNext(); ) {
			Object node = iteration.next();
			if (node instanceof CAGraphNode)
				((CAGraphNode) node).setForegroundColor(color);
		}
	}
	

	/**
	 * Returns label (word) color.
	 * @return		label color.
	 */
	public Color getLabelColor() {
		return labelColor;
	}
	
	
	/**
	 * Returns {@link ImageData} for output.
	 * @return		{@link ImageData} for output.
	 */
	public ImageData getImageData() {
		double zoom = getZoomManager().getZoom();
		getZoomManager().setZoom(getZoomManager().getMaxZoom());
		
		Rectangle rect = getContents().getBounds();
		Image image = new Image(null, rect.width, rect.height);
		GC gc = new GC(image);
		SWTGraphics swtGraphics = new SWTGraphics(gc);
		swtGraphics.setBackgroundColor(getBackground());
		swtGraphics.fillRectangle(0, 0, rect.width, rect.height);
		swtGraphics.translate(-1 * rect.x, -1 * rect.y);
		getContents().paint(swtGraphics);
		gc.dispose();
		
		getZoomManager().setZoom(zoom);
		return image.getImageData();
	}
	
	
	/**
	 * Zoom In.
	 */
	public void zoomIn() {
		getZoomManager().zoomIn();
	}
	
	/**
	 * Zoom Out.
	 */
	public void zoomOut() {
		getZoomManager().zoomOut();
	}

	/**
	 * Zoom to Default Size.
	 */
	public void zoomReset() {
		getZoomManager().setZoom(1.0);	
	}
	
	
	public void dispose() {
		labelColor.dispose();
	}
}
