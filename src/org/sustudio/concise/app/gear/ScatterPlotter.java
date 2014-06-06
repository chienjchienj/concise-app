package org.sustudio.concise.app.gear;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import javafx.collections.ListChangeListener;
import javafx.embed.swt.FXCanvas;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.converter.NumberStringConverter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.CAQueryUtils;
import org.sustudio.concise.app.query.DefaultConcQuery;
import org.sustudio.concise.app.thread.CAThread;
import org.sustudio.concise.app.toolbar.CASearchAction;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.statistics.ConcisePCACorr;
import org.sustudio.concise.core.statistics.ConcisePCACorr.PC;
import org.sustudio.concise.core.wordlister.Word;

import com.sun.javafx.charts.Legend;

public class ScatterPlotter extends GearController {

	private ConcisePCACorr pca = null;
	private ScatterChart<Number, Number> scatterChart;
	
	public ScatterPlotter() {
		super(CABox.GearBox, Gear.ScatterPlotter);
	}
	
	@Override
	protected Control createControl() {
		
		final FXCanvas fxCanvas = new FXCanvas(this, SWT.NONE);
		BorderPane border = new BorderPane();
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(2, 15, 2, 15));
		hbox.setSpacing(10);
		Button btnReset = new Button("Reset");
		btnReset.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				try {
					SQLiteDB.dropTableIfExists(CATable.ScatterPlotter);
				} catch (SQLException | IOException e) {
					workspace.logError(gear, e);
					Dialog.showException(e);
				} finally {
					scatterChart.getData().clear();
				}
			}			
		});
		hbox.setAlignment(Pos.CENTER_RIGHT);
		hbox.getChildren().add(btnReset);
		border.setTop(hbox);
		
		// creating the chart
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		scatterChart = new ScatterChart<Number, Number>(xAxis, yAxis);
		//scatterChart.setLegendVisible(false);
		scatterChart.setLegendSide(Side.RIGHT);
		border.setCenter(scatterChart);
		
		// handling legend
		final Legend legend = (Legend) scatterChart.lookup(".chart-legend");
		legend.getChildrenUnmodifiable().addListener(new ListChangeListener<Object>() {
			@Override
			public void onChanged(Change<? extends Object> arg0) {
				for (final Node node : legend.getChildrenUnmodifiable()) {
				if (node instanceof Label) {
					final Node imageNode = ((Label) node).getGraphic();
					final int index = legend.getChildrenUnmodifiable().indexOf(node);
					node.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override public void handle(MouseEvent event) {
							CASpinner spinner = new CASpinner(ScatterPlotter.this);
							spinner.open();
							scatterChart.getData().remove(index);
							// remove from database
							try {
								String word = ((Label) node).getText();
								String sql = "DELETE FROM " + CATable.ScatterPlotter.name() + " WHERE " + DBColumn.Word.columnName() + " = ?";
								PreparedStatement ps = SQLiteDB.prepareStatement(sql);
								ps.setString(1, word);
								ps.executeUpdate();
								Concise.getCurrentWorkspace().getConnection().commit();
								ps.close();
								
							} catch (Exception e) {
								workspace.logError(gear, e);
								Dialog.showException(e);
							}
							spinner.close();
						}
					});
					node.setOnMouseEntered(new EventHandler<MouseEvent>() {
						@Override public void handle(MouseEvent event) {
							Node dot = scatterChart.getData().get(index).getData().get(0).getNode();
							dot.setScaleX(1.5);
							dot.setScaleY(1.5);
							DropShadow dropShadow = new DropShadow();
							dot.setEffect(dropShadow);
							node.setEffect(dropShadow);
							Image image = new Image(getClass().getResourceAsStream("/org/sustudio/concise/app/icon/trash-can.png"));
							ImageView imageView = new ImageView(image);
							imageView.setFitWidth(10);
							imageView.setPreserveRatio(true);
							((Label) node).setGraphic(imageView);
						}
					});
					node.setOnMouseExited(new EventHandler<MouseEvent>() {
						@Override public void handle(MouseEvent event) {
							// series might be removed
							if (scatterChart.getData().size() > index) {
								Node dot = scatterChart.getData().get(index).getData().get(0).getNode();
								dot.setScaleX(1);
								dot.setScaleY(1);
								dot.setEffect(null);
								node.setEffect(null);
								((Label) node).setGraphic(imageNode);
							}
						}
					});
				} }
			}
		});
		
		fxCanvas.setScene(new Scene(border));
		return fxCanvas;
	}
	
	public void loadData() {
		CASpinner spinner = new CASpinner(this);
		spinner.open();
		try {
			if (pca == null) {
				pca = new ConcisePCACorr(workspace, CAPrefs.SHOW_PART_OF_SPEECH);
			}
			
			NumberStringConverter fmt = new NumberStringConverter("#.##%");
			scatterChart.getXAxis().setLabel("PC1 (" + fmt.toString(pca.getExplainedByDimension(1)) + ")");
			scatterChart.getYAxis().setLabel("PC2 (" + fmt.toString(pca.getExplainedByDimension(2)) + ")");
			
			// add existing words
			SQLiteDB.createTableIfNotExists(CATable.ScatterPlotter);
			String sql = "SELECT * FROM " + CATable.ScatterPlotter.name();
			ResultSet rs = SQLiteDB.executeQuery(sql);
			while (rs.next()) {
				addWord(rs.getString(DBColumn.Word.columnName()));
			}
			rs.close();
			
		} catch (Exception e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		}
		spinner.close();
		super.loadData();
	}
	
	private PC getPrincipalComponent(String strWord) {
		for (PC pc : pca.getPrincipalComponents()) {
			if (pc.getWord().getWord().equals(strWord))
				return pc;
		}
		return null;
	}
	
	public void unloadData() {
		super.unloadData();
		scatterChart.getData().clear();
		if (pca != null) {
			pca.clear();
			pca = null;
		}
	}
	
	private void addWord(String strWord) throws Exception {
		
		// check if the word already exists
		for (Series<Number, Number> series : scatterChart.getData()) {
			if (series.getName().equals(strWord)) {
				return;
			}
		}
		
		PC pc = getPrincipalComponent(strWord);
		if (pc != null) {
			Series<Number, Number> series = new Series<Number, Number>();
			series.setName(strWord);
			XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(pc.getPC(1), pc.getPC(2)); 
			series.getData().add(data);
			data.setNode(new HoveredNode(pc.getWord()));
			scatterChart.getData().add(series);
		}
	}
	
	class HoveredNode extends StackPane {
		HoveredNode(final Word word) {
			setOnMouseEntered(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {
					final VBox vbox = new VBox(5);
					vbox.getStyleClass().addAll("chart-line-symbol", "chart-series-line");
					
					Text txtWord = new Text(word.getWord());
					txtWord.setFont(Font.font(Font.getDefault().getName(), 
									FontWeight.BOLD, 
									Font.getDefault().getSize()));
					txtWord.autosize();
					Text freqTitle = new Text("Frequency:");
					freqTitle.setFont(Font.font(Font.getDefault().getName(), 
									  FontWeight.BOLD, 
									  11));
					Text freq = new Text(new NumberStringConverter("#,###,###,###,###").toString(word.getTotalTermFreq()));
					freq.setTextAlignment(TextAlignment.RIGHT);
					vbox.getChildren().addAll(txtWord, freqTitle, freq);
					vbox.autosize();
					
					getChildren().setAll(vbox);
					
					// set alignment
					vbox.setTranslateY(-10);
					vbox.setTranslateX(-10);
					if (getLayoutY() - vbox.getHeight() - 10 > 0) {
						if (getLayoutX() - vbox.getWidth() - 10 < 0)	{
							vbox.setTranslateX(10);
							setAlignment(Pos.BOTTOM_LEFT);
						}
						else
							setAlignment(Pos.BOTTOM_RIGHT);
					}
					else {
						vbox.setTranslateY(10);
						if (getLayoutX() - vbox.getWidth() - 10 < 0) {
							vbox.setTranslateX(10);
							setAlignment(Pos.TOP_LEFT);
						}
						else
							setAlignment(Pos.TOP_RIGHT);
					}
					
					toFront();
					setCursor(Cursor.HAND);
				}
			});
			setOnMouseExited(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent event) {
					getChildren().clear();
		        }
			});
			
			setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent event) {
					getChildren().clear();
					Gear.Concordancer.open(workspace);
					CASearchAction.doIt(new DefaultConcQuery(word.getWord()));
				}
			});
		}
	}
	
	public void doit(CAQuery query) {
		CAThread thread = new CAThread(gear, query) {
			@Override public void running() {
				try {
					
					dialog.setStatus("configuring...");
					SQLiteDB.createTableIfNotExists(CATable.ScatterPlotter);
					ArrayList<String> existingWords = new ArrayList<String>();
					String sql = "SELECT * FROM " + CATable.ScatterPlotter.name();
					ResultSet rs = SQLiteDB.executeQuery(sql);
					while (rs.next()) {
						existingWords.add(rs.getString(DBColumn.Word.columnName()));
					}
					rs.close();
					
					Concise.getCurrentWorkspace().logInfo(query.toString());
					CAQueryUtils.logQuery(query);
					// TODO lemma translation and stop words
					Conc conc = new Conc(workspace, query.searchStr, CAPrefs.SHOW_PART_OF_SPEECH);
					ArrayList<String> words = new ArrayList<String>();
					words.addAll(conc.getSearchWords());
					Collections.sort(words);
					
					PreparedStatement ps = SQLiteDB.prepareStatement(CATable.ScatterPlotter);
					for (final String word : words) {
						if (!CAPrefs.SHOW_PART_OF_SPEECH && word.contains(CAPrefs.POS_SEPARATOR))
							continue;
						if (existingWords.contains(word))
							continue;
						
						// log to database
						ps.setString(1, word);
						ps.addBatch();
						getDisplay().asyncExec(new Runnable() {
							public void run() {
								try {
									addWord(word);
								} catch (Exception e) {
									workspace.logError(gear, e);
									Dialog.showException(e);
								}
							}
						});
					}
					SQLiteDB.executeBatch(ps);
					
				} catch (Exception e) {
					workspace.logError(gear, e);
					Dialog.showException(e);
				}
			}
			
		};
		thread.setDaemon(true);
		thread.start();
	}
}
