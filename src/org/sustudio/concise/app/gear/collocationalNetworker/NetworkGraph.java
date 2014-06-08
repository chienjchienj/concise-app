package org.sustudio.concise.app.gear.collocationalNetworker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphItem;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.gef4.zest.core.widgets.GraphWidget;
import org.eclipse.gef4.zest.core.widgets.LayoutFilter;
import org.eclipse.gef4.zest.core.widgets.ZestStyles;
import org.eclipse.gef4.zest.core.widgets.gestures.ZoomGestureListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * NetworkGraph is a custom Graph adding {@link #getImageData()} and other features.
 * 
 * @author Kuan-ming Su
 *
 */
public class NetworkGraph extends GraphWidget {
	
	private final Cursor moveCursor = new Cursor(getDisplay(), SWT.CURSOR_CROSS);
	
	private Color labelColor = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
	private boolean hideNonSelectedLabel = true;
	private boolean mouseDown = false;
	private PrecisionPoint mouseDownPoint;
	
	private CAGraphNode hoveredNode = null;
	
	private final List<CAGraphNode> selections = new ArrayList<CAGraphNode>();
	private RectangleFigure selectionRect;
	
	
	/**
	 * Constructor.
	 * @param viewer		Network Viewer
	 * @param style			Normally, SWT.NONE
	 */
	public NetworkGraph(Composite viewer, int style) {
		super(viewer, style | ZestStyles.GESTURES_DISABLED);
		
		// add gesture listener (ignore rotate gesture)
		addGestureListener(new ZoomGestureListener());
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				NetworkGraph.this.clear();
			}
		});
		
		addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.ESC:
					selections.clear();
					clearSelectionRectangle();
					break;
				}
			}
		});
		
		// hover listener
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (!NetworkGraph.this.isFocusControl()) {
					NetworkGraph.this.setFocus();
				}
				hoveredNode = null;
				
				// selection area
				if (selectionRect != null && mouseDown) {
					unhighlightAllNodes();
					unhighlightAllConnections();
					fadeoutAllNodes();
					
					// 轉換坐標
					Rectangle rootBounds = getRootLayer().getBounds();
					getRootLayer().translateToAbsolute(rootBounds);
					double zoom = getZoomManager().getZoom();
					double x = (e.x - rootBounds.x) / zoom;
					double y = (e.y - rootBounds.y) / zoom;
					
					Rectangle rect = selectionRect.getBounds();
					rect.x = (int) Math.min(x, mouseDownPoint.x);
					rect.y = (int) Math.min(y, mouseDownPoint.y);
					rect.width = (int) Math.abs(x - mouseDownPoint.x);
					rect.height = (int) Math.abs(y - mouseDownPoint.y);
					selectionRect.setBounds(rect);
					
					for (GraphNode node : getNodes()) {
						if (node.getFigure().getBounds().intersects(selectionRect.getBounds())) {
							node.highlight();
							if (hideNonSelectedLabel) {
								((CAGraphNode) node).showLabel();
							}
						}
					}
					return;
				}
				
				if (selections.size() > 0) {
					return;	// hold selections
				}
				
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
			public void mouseDown(MouseEvent e) {
				mouseDown = true;
				
				// translate mouse point to root coordination
				double zoom = getZoomManager().getZoom();
				Rectangle rootBounds = getRootLayer().getBounds();
				getRootLayer().translateToAbsolute(rootBounds);
				mouseDownPoint = new PrecisionPoint(
										(e.x - rootBounds.x) / zoom,
										(e.y - rootBounds.y) / zoom);
				
				if (hoveredNode == null) {
					setCursor(moveCursor);
					clearSelectionRectangle();
					selectionRect = new RectangleFigure();
					selectionRect.setLocation(mouseDownPoint);
					selectionRect.setBackgroundColor(DARK_BLUE);
					selectionRect.setAlpha(64);
					getRootLayer().add(selectionRect);
				}
			}
			
			public void mouseUp(MouseEvent e) {
				mouseDown = false;
				mouseDownPoint = null;
				selections.clear();
				setCursor(null);
				if (selectionRect != null) {
					for (GraphNode node : getNodes()) {
						if (node.getFigure().getBounds().intersects(selectionRect.getBounds())) {
							selections.add((CAGraphNode) node);							
						}
					}
					select(selections);
					
					selectionRect.setAlpha(selectionRect.getAlpha() / 3);
					Dimension size = selectionRect.getSize();
					if (size.width == 0 && size.height == 0) {
						clearSelectionRectangle();
					}
				}
			}			
		});
		
		addLayoutFilter(new LayoutFilter() {
			public boolean isObjectFiltered(GraphItem item) {
				// only GraphNode can be selected
				if (item instanceof GraphNode) {
					return item.getGraphWidget().getSelection().contains(item)
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
			for (CAGraphNode node : nodes) {
				node.unhighlight();
				if (hideNonSelectedLabel) {
					node.showLabel();
				}
			}
			notifyListeners(SWT.Selection, new Event());
		}
	}
	
	public void clearSelectionRectangle() {
		if (selectionRect != null) {
			selectionRect.getParent().remove(selectionRect);
			selectionRect = null;
		}
		if (selections.isEmpty()) {
			unhighlightAllNodes();
			unhighlightAllConnections();
		}
	}
	
	public List<CAGraphNode> getSelectedNodes() {
		return selections;
	}
	
	/**
	 * Un-highlight all nodes
	 */
	public void unhighlightAllNodes() {
		for (GraphNode node : getNodes()) {
			node.unhighlight();
			if (node instanceof CAGraphNode && hideNonSelectedLabel) {
				((CAGraphNode) node).showLabel();
			}
		}
	}
	
	/**
	 * Un-highlight all connections.
	 */
	public void unhighlightAllConnections() {
		for (GraphConnection conn : getConnections()) {
			GraphConnection connection = (GraphConnection) conn;
			connection.unhighlight();
			setLineAlpha(connection, 0.4);
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
		super.dispose();
	}
}
