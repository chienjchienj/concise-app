package org.sustudio.concise.app.gear;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.embed.swt.FXCanvas;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

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
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.sustudio.concise.core.wordlister.WordUtils;

import com.sun.javafx.charts.Legend;

public class WordTrender extends GearController {

	private final ArrayList<ConciseDocument> docs = new ArrayList<ConciseDocument>();
	private LineChart<String, Number> lineChart;
	
	public WordTrender() {
		super(CABox.GearBox, Gear.WordTrender);
	}
	
	@Override
	protected Control createControl() {
		
		final FXCanvas fxCanvas = new FXCanvas(this, SWT.NONE);
		final CategoryAxis xAxis = new CategoryAxis();
		//xAxis.setLabel("Document");
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Frequency");
		yAxis.setTickUnit(1);
		
		// creating the chart
		lineChart = new LineChart<String, Number>(xAxis, yAxis);
		lineChart.setLegendSide(Side.TOP);
		
		// get legend
		final Legend legend = (Legend) lineChart.lookup(".chart-legend");
		legend.getChildrenUnmodifiable().addListener(new ListChangeListener<Object>() {
			@Override
			public void onChanged(Change<? extends Object> arg0) {
				for (final Node node : legend.getChildrenUnmodifiable()) {
				if (node instanceof Label) {
					final Node imageNode = ((Label) node).getGraphic();
					final int index = legend.getChildrenUnmodifiable().indexOf(node);
					node.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override public void handle(MouseEvent event) {
							CASpinner spinner = new CASpinner(WordTrender.this);
							spinner.open();
							lineChart.getData().remove(index);
							// remove from database
							try {
								String word = ((Label) node).getText();
								String sql = "DELETE FROM " + CATable.WordTrender.name() + " WHERE " + DBColumn.Word.columnName() + " = ?";
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
							Node seriesLine = lineChart.getData().get(index).getNode();
							DropShadow dropShadow = new DropShadow();
							seriesLine.setEffect(dropShadow);
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
							if (lineChart.getData().size() > index) {
								Node seriesLine = lineChart.getData().get(index).getNode();
								seriesLine.setEffect(null);
								node.setEffect(null);
								((Label) node).setGraphic(imageNode);
							}
						}
					});
				} }
			}
		});
		
		fxCanvas.setScene(new Scene(lineChart));
		return fxCanvas;
	}
	
	public void loadData() {
		CASpinner spinner = new CASpinner(this);
		spinner.open();
		try {
			if (docs.size() != workspace.getIndexReader().numDocs()) {
				docs.clear();
				for (ConciseDocument d : new DocumentIterator(workspace)) {
					docs.add(d);
					//d.numWords = DocumentWordIterator.sumTotalTermFreq(workspace, d, CAPrefs.SHOW_PART_OF_SPEECH);
				}
				Collections.sort(docs, new Comparator<ConciseDocument>() {
					public int compare(ConciseDocument o1, ConciseDocument o2) {
						return o1.title.compareTo(o2.title);
					}
				});
			}
			
			// add existing words
			String sql = "SELECT * FROM " + CATable.WordTrender.name();
			ResultSet rs = SQLiteDB.executeQuery(sql);
			while (rs.next()) {
				addSeriesFor(rs.getString(DBColumn.Word.columnName()));
			}
			rs.close();
			
		} catch (Exception e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		}
		spinner.close();
		super.loadData();
	}
	
	public void unloadData() {
		super.unloadData();
		lineChart.getData().clear();
	}
	
	public void dispose() {
		docs.clear();
		lineChart.getData().clear();
		super.dispose();
	}
	
	private void addSeriesFor(String word) throws Exception {
		// check if the word already exists
		for (Series<String, Number> series : lineChart.getData()) {
			if (series.getName().equals(word))
				return;
		}
		
		Series<String, Number> series = new Series<>();
		series.setName(word);
		
		Map<ConciseDocument, Integer> freqTable = WordUtils.wordFreqByDocs(workspace, word, docs);
		for (ConciseDocument doc : docs) {
			if (freqTable.get(doc) != null) {
				XYChart.Data<String, Number> data = 
						new XYChart.Data<String, Number>(
								String.valueOf(docs.indexOf(doc)), 
								freqTable.get(doc).intValue());
				series.getData().add(data);
				data.setNode(new HoveredNode(word, data));
			}
		}
		lineChart.getData().add(series);
	}
	
	class HoveredNode extends StackPane {
		HoveredNode(String word, final XYChart.Data<String, Number> data) {
			final int index = Integer.parseInt(data.getXValue());
			setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent event) {
					final VBox vbox = new VBox(5);
					vbox.getStyleClass().addAll("chart-line-symbol", "chart-series-line");
					
					Text freqTitle = new Text("Frequency:");
					freqTitle.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 11));
					vbox.getChildren().add(freqTitle);
					
					for (Series<String, Number> series : lineChart.getData()) {						
						// find legend Label
						final Legend legend = (Legend) lineChart.lookup(".chart-legend");
						Label legendLabel = (Label) legend.getChildrenUnmodifiable().get(lineChart.getData().indexOf(series));
						WritableImage img = legendLabel.getGraphic().snapshot(new SnapshotParameters(), null);
						
						// set up Label
						XYChart.Data<String, Number> d = series.getData().get(index);
						Label wordCount = new Label(series.getName() + ": " + d.getYValue());
						wordCount.setGraphic(new ImageView(img));
						wordCount.setFont(Font.font(Font.getDefault().getName(), FontWeight.NORMAL, 14));
						wordCount.autosize();
						vbox.getChildren().add(wordCount);
					}
					// append document title to VBox
					Text docTitle = new Text(docs.get(index).title);
					docTitle.setFont(Font.font(Font.getDefault().getName(), FontWeight.NORMAL, 9));
					vbox.getChildren().add(docTitle);
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
				@Override public void handle(MouseEvent event) {
					getChildren().clear();
		        }
			});
			
			setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent event) {
					getChildren().clear();
					
					// open DocumentViewer
					DocumentViewer viewer = (DocumentViewer) Gear.DocumentViewer.open(workspace);
					StringBuilder sb = new StringBuilder();
					for (Series<String, Number> series : lineChart.getData()) {
						if (sb.length() > 0) sb.append(" ");
						sb.append(series.getName());
					}
					CAQuery query = new CAQuery(Gear.DocumentViewer);
					query.searchStr = sb.toString();
					viewer.open(docs.get(index).docID, 1, query);
					sb.setLength(0);
				}
			});
		}
	}
	
	public void doit(CAQuery query) {
		ConciseThread thread = new ConciseThread(gear, query) {
			@Override public void running() {
				try {
					
					dialog.setStatus("configuring...");
					SQLiteDB.createTableIfNotExists(CATable.WordTrender);
					ArrayList<String> existingWords = new ArrayList<String>();
					String sql = "SELECT * FROM " + CATable.WordTrender.name();
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
					
					PreparedStatement ps = SQLiteDB.prepareStatement(CATable.WordTrender);
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
									addSeriesFor(word);
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
