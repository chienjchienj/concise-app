package org.sustudio.concise.app.gear.collocationalNetworker;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

public class CANodeFigure extends Figure {
	
	public static enum NodeShape { Ellipse, Rectangle, RoundedRectangle, Triangle }
	
	private Shape shape;
	private Label label;
	private String text;
	
	public CANodeFigure(NetworkGraph graph, String text) {
		this(graph, text, 20, null /* Color */, null /* NodeShape */);
	}
	
	public CANodeFigure(NetworkGraph graph, String text, int size, NodeShape nodeShape) {
		this(graph, text, size, null /* Color */, nodeShape);
	}
	
	public CANodeFigure(NetworkGraph graph, String text, int size, Color color, NodeShape nodeShape) {
		setLayoutManager(new FreeformLayout());
		setFont(graph.getFont());
		setForegroundColor(SWTResourceManager.getColor(51, 51, 51));
		this.text = text;
		if (nodeShape == null)  // default shape is ellipse
			nodeShape = NodeShape.Ellipse;
		
		if (color == null) {
			color = ColorConstants.orange;
		}
		shape = createShape(nodeShape, color, new Dimension(size, size));
		add(shape);
		
		if (text != null) {
			label = createLabel(text);
			add(label);
		}
		recalculateNodeBounds();
	}
	
	protected Shape createShape(NodeShape nodeShape, Color nodeColor, Dimension shapeSize) {
		Shape shape;
		switch (nodeShape) {
		case Ellipse:	shape = new Ellipse();	break;
		case Rectangle:	shape = new RectangleFigure();	break;
		case RoundedRectangle:	shape = new RoundedRectangle();	break;
		case Triangle:	shape = new Triangle();	break;
		default: return null;
		}
		shape.setFill(true);
		shape.setBackgroundColor(nodeColor);
		shape.setForegroundColor(FigureUtilities.darker(nodeColor));
		shape.setOutline(true);
		shape.setLineWidth(2);
		shape.setSize(shapeSize);
		return shape;
	}
	
	protected Label createLabel(String text) {
		if (text == null) return null;
		
		Dimension minSize = FigureUtilities.getTextExtents(text, getFont());
		//minSize.expand(10 + 2, 4 + 2);
		Label label = new Label(text);
		label.setSize(minSize);
		label.setForegroundColor(getForegroundColor());
		return label;
	}
	
	protected void recalculateLabelSize() {
		Dimension minSize = FigureUtilities.getTextExtents(label.getText(), getFont());
		label.setSize(minSize);
	}
	
	protected void recalculateNodeBounds() {
		if (shape != null) {
			setSize(shape.getSize());
		}
		
		if (label != null) {
			Dimension labelSize = label.getSize();
			int width = Math.max(labelSize.width, getSize().width);
			int height = Math.max(labelSize.height, getSize().height);			
			shape.setBounds(new Rectangle(getLocation().x + (width - shape.getSize().width) / 2,
								  		  getLocation().y + (height - shape.getSize().height) / 2,
								  		  shape.getSize().width,
								  		  shape.getSize().height));
			label.setBounds(new Rectangle(getLocation().x + (width - labelSize.width) / 2,
								  		  getLocation().y + (height - labelSize.height) / 2,
								  		  labelSize.width,
								  		  labelSize.height));
			this.setSize(width, height);
		}
		Rectangle bounds = new Rectangle(getLocation(), getSize());
		if (getParent() != null) {
			getParent().setConstraint(this, bounds);
		}
	}
	
	/**
	 * Sets alpha value of the shape.
	 * @param alpha		alpha (0 to 255)
	 */
	public void setAlpha(int alpha) {
		shape.setAlpha(alpha);
	}
	
	/**
	 * Returns alpha value of the shape
	 * @return		alpha (0 to 255)
	 */
	public int getAlpha() {
		return shape.getAlpha();
	}
	
	public void setBackgroundColor(Color color) {
		super.setBackgroundColor(color);
		shape.setBackgroundColor(getBackgroundColor());
		shape.setForegroundColor(FigureUtilities.darker(getBackgroundColor()));
	}
		
	public void setForegroundColor(Color color) {
		super.setForegroundColor(color);
		if (label != null) {
			label.setForegroundColor(getForegroundColor());
		}
	}
		
	public void setShapeSize(Dimension size) {
		shape.setSize(size);
		
		int fontHeight = 10;
		if (size.width > 10) {
			fontHeight = size.width - 10;
		}
		FontData fd = getFont().getFontData()[0];
		fd.setHeight(fontHeight);
		setFont(new Font(Display.getDefault(), fd));
		recalculateLabelSize();
		
		recalculateNodeBounds();
	}
	
	public String getText() {
		return this.text;
	}
	
	public Shape getShape() {
		return this.shape;
	}
	
	public Label getLabel() {
		return this.label;
	}
	
	public Figure getFigure() {
		return this;
	}
	
}
