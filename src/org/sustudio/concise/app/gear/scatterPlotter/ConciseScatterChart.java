package org.sustudio.concise.app.gear.scatterPlotter;

import java.util.ArrayList;

import org.sustudio.concise.app.gear.scatterPlotter.ScatterPlotter.HoverNode;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ConciseScatterChart extends ScatterChart<Number, Number> {

	public BooleanProperty labelVisibleProperty = new SimpleBooleanProperty(false);
	
	public ConciseScatterChart(Axis<Number> xAxis, Axis<Number> yAxis) {
		super(xAxis, yAxis);
		setAnimated(false);
		
		Node chartNode = null;
		for (Node node : getChartChildren()) {
			if (!(node instanceof Region) && !(node instanceof NumberAxis)) {
				chartNode = node;
			}
		}
		
		chartNode.boundsInParentProperty().addListener(new ChangeListener<Bounds>() {
			public void changed(ObservableValue<? extends Bounds> arg0,
					Bounds oldBounds, Bounds b) {
				
				//System.out.println("zoom or pane or resize");
				recalculateLabelPosition();
			}
		});
		
		getData().addListener(new ListChangeListener<Series<Number, Number>>() {
			public void onChanged(Change<? extends Series<Number, Number>> c) {
				// remove deleted text label
				ArrayList<Text> retain = new ArrayList<Text>();
				for (Node n : getPlotChildren()) {
					HoverNode node = (HoverNode) n;
					if (node.getTextNode() != null) {
						retain.add(node.getTextNode());
					}
				}
				ArrayList<Text> remove = new ArrayList<Text>();
				for (Node n : getChildren()) {
					if (n instanceof Text && !retain.contains(n)) {
						// remove
						remove.add((Text) n);
					}
				}
				getChildren().removeAll(remove);
				remove.clear();
				retain.clear();
			}
		});		
	}
	
	private void recalculateLabelPosition() {
		//double yOffset = localToScene(getBoundsInLocal()).getMinY();
		//double xOffset = localToScene(getBoundsInLocal()).getMinX();
		for (Node n : getPlotChildren()) {
			HoverNode node = (HoverNode) n;
			Bounds b = node.localToScene(node.getBoundsInLocal());
			
			Text text = node.getTextNode(); 
			if (text == null) {
				text = new Text(node.text);
				text.setFont(new Font(9));
				text.setOpacity(0.6);
				getChildren().add(text);
				node.setTextNode(text);
			}
			// FIXME zoom 之後位置不正確
			double x = b.getMinX() + b.getWidth()/2 - text.getBoundsInLocal().getWidth()/2;
			double y = b.getMinY() - 32.0 + b.getHeight()/2 - text.getBoundsInLocal().getHeight()/2;
			//if (node.text.equals("生意"))
			//	System.out.printf("(%.2f,%.2f) (%.2f,%.2f) (%.2f,%.2f) [%s]\n", xOffset, yOffset, b.getMinX(), b.getMinY(), x, y, node.text);
			text.setX( x );
			text.setY( y );
			text.setVisible(isLabelVisible());
		}
	}
	
	public void setLabelVisible(boolean visible) {
		labelVisibleProperty.set(visible);
		for (Node n : getPlotChildren()) {
			HoverNode node = (HoverNode) n;
			node.getTextNode().setVisible(visible);
		}
	}
	
	public boolean isLabelVisible() {
		return labelVisibleProperty.get();
	}
}
