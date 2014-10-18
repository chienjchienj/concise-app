package org.sustudio.concise.app.gear.wordTrender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import com.sun.javafx.charts.Legend;

import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CCategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

@SuppressWarnings("restriction")
public class ConciseLineChart extends LineChart<String, Number> {
	
	private final CCategoryAxis xAxis;
	private ScrollPane sp;
	private Text titleText;
	private VBox freqBox;
	
	private HashSet<Node> bigNodes = new HashSet<Node>();
	
	public ConciseLineChart() {
		super(new CCategoryAxis(), new NumberAxis());
		
		xAxis = (CCategoryAxis) getXAxis();
		getYAxis().setLabel("Frequency");
		setLegendSide(Side.BOTTOM);
		
		sp = new ScrollPane();
		VBox vb = new VBox(5);
		titleText = new Text();
		titleText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 11));
		freqBox = new VBox();
		vb.getChildren().addAll(titleText, freqBox);
		sp.setPrefSize(200, 100);
		sp.setContent(vb);
		sp.setLayoutX(100);
		sp.setLayoutY(100);
		sp.getStyleClass().addAll("chart-line-symbol", "chart-series-line");
		sp.setVisible(false);
		getChildren().add(sp);
		
		setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				hideFreqBox();
			}
		});
		
		getData().addListener(new ListChangeListener<Series<String, Number>>() {
			public void onChanged(Change<? extends Series<String, Number>> c) {
				hideFreqBox();
			}
		});
	}
	
	
	public void showFreqBox(Node node) {
		for (Node n : bigNodes) {
			n.setScaleX(1.0);
			n.setScaleY(1.0);
			n.setEffect(null);
		}
		bigNodes.clear();
		
		sp.setVisible(true);
		double spLayoutY = Double.MAX_VALUE;
		double spLayoutX = 0.0;
		
		Bounds b = node.getBoundsInParent();
		double x = (b.getMaxX() + b.getMinX()) / 2.0;
		String category = xAxis.getValueForDisplay(x);
		titleText.setText(category);
		freqBox.getChildren().clear();
		int xIndex = xAxis.getCategories().indexOf(category);
		ArrayList<Label> labels = new ArrayList<Label>();
		for (Series<String, Number> series : getData()) {						
			// find legend Label
			final Legend legend = (Legend) lookup(".chart-legend");
			Label legendLabel = (Label) legend.getChildrenUnmodifiable().get(getData().indexOf(series));
			WritableImage img = legendLabel.getGraphic().snapshot(new SnapshotParameters(), null);
			
			// set up Label
			XYChart.Data<String, Number> d = series.getData().get(xIndex);
			Label wordCount = new Label(series.getName() + ": " + d.getYValue());
			wordCount.setUserData(d.getYValue());
			wordCount.setGraphic(new ImageView(img));
			labels.add(wordCount);
			//wordCount.setFont(Font.font(Font.getDefault().getName(), FontWeight.NORMAL, 14));
			//wordCount.autosize();
			
			d.getNode().setScaleX(2.0);
			d.getNode().setScaleY(2.0);
			//d.getNode().setEffect(new DropShadow());
			bigNodes.add(d.getNode());
			
			spLayoutY = Math.min(spLayoutY, d.getNode().localToScene(d.getNode().getBoundsInLocal()).getMinY());
			spLayoutX = d.getNode().localToScene(d.getNode().getBoundsInLocal()).getMaxX();
		}
		Collections.sort(labels, new Comparator<Label>() {
			@Override
			public int compare(Label o1, Label o2) {
				int n1 = ((Number) o1.getUserData()).intValue();
				int n2 = ((Number) o2.getUserData()).intValue();
				if (n1 > n2) return -1;
				else if (n1 < n2) return 1;
				return 0;
			}
		});
		freqBox.getChildren().addAll(labels);
		
		sp.setLayoutY(spLayoutY);
		sp.setTranslateY(-10);
		
		sp.setLayoutX(spLayoutX);
		sp.setTranslateX(0);
		sp.autosize();
		
		if (sp.getBoundsInParent().getMaxX() > getWidth()) {
			sp.setLayoutX(spLayoutX - sp.getWidth());
			sp.setTranslateX(-20);
		}
	}
	
	public void hideFreqBox() {
		for (Node n : bigNodes) {
			n.setScaleX(1.0);
			n.setScaleY(1.0);
			n.setEffect(null);
		}
		bigNodes.clear();
		sp.setVisible(false);
	}
	
}
