package org.sustudio.concise.app.gear.collocationalNetworker;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.swt.graphics.Color;

public class CAGraphNode extends GraphNode {
	
	protected CANodeFigure figure;
	
	public CAGraphNode(NetworkGraph graph, int style, String text) {
		super(graph, style, text);
	}
	
	public IFigure getFigure() {
		if (figure == null) 
			createFigureForModel();
		return figure;
	}
	
	protected IFigure createFigureForModel() {
		figure = new CANodeFigure((NetworkGraph) graph, getText());
		figure.setAlpha(191);
		
		// this seems a bug
		// see http://vzurczak.wordpress.com/2010/12/20/eclipse-zest-custom-figures-and-nodes-size/
		figure.setSize(-1, -1);
		return figure;
	}
	
	/**
	 * Returns the number of total connections (degree).
	 * @return		the number of total connections (degree).
	 */
	public int getDegree() {
		return getTargetConnections().size() + getSourceConnections().size();
	}
	
	/**
	 * Tests if this node is connected to the given node. (NOT working properly now)
	 * @param node		concise graph node.
	 * @return			true if is connected.
	 * 
	 * TODO not working properly now.  check.
	 */
	public boolean isConnectedTo(CAGraphNode node) {
		for (Object conn : getSourceConnections()) {
			if (conn instanceof GraphConnection) {
				if (((GraphConnection) conn).getSource().equals(node)) {
					return true;
				}
			}
		}
		for (Object connObj : getTargetConnections()) {
			if (connObj instanceof GraphConnection) {
				if (((GraphConnection) connObj).getDestination().equals(node)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isHighlightOn() {
		return highlighted == HIGHLIGHT_ON;
	}
	
	public void setBackgroundColor(Color color) {
		super.setBackgroundColor(color);
		figure.setBackgroundColor(getBackgroundColor());
	}
	
	public void setForegroundColor(Color color) {
		super.setForegroundColor(color);
		figure.setForegroundColor(getForegroundColor());
	}
		
	public void highlight() {
		figure.setForegroundColor(getForegroundColor());
		figure.setBackgroundColor(getHighlightColor());
		figure.setAlpha(255);
		highlighted = HIGHLIGHT_ON;
	}
	
	public void unhighlight() {
		figure.setForegroundColor(getForegroundColor());
		figure.setBackgroundColor(getHighlightColor());
		figure.setAlpha(191);
		highlighted = HIGHLIGHT_NONE;
	}
	
	/**
	 * Fade out node background color
	 */
	public void fadeOut() {
		figure.setForegroundColor(FigureUtilities.mixColors(getBackgroundColor(), getGraphModel().getBackground()));
		figure.setAlpha(63);
	}
	
	public void setShapeSize(int width, int height) {
		figure.setShapeSize(new Dimension(width, height));
		//setSize(figure.getSize().width, figure.getSize().height);
		updateFigureForModel(figure);
		
		// this seems a bug
		// see http://vzurczak.wordpress.com/2010/12/20/eclipse-zest-custom-figures-and-nodes-size/
		figure.setSize(-1, -1);
	}
	
	/**
	 * 隱藏文字標籤
	 */
	public void hideLabel() {
		figure.getLabel().setVisible(false);
	}
	
	/**
	 * 顯示文字標籤
	 */
	public void showLabel() {
		figure.getLabel().setVisible(true);
	}
}
