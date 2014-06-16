package org.sustudio.concise.app.gear.scatterPlotter;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.util.converter.NumberStringConverter;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.DocumentViewer;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.gear.IGearSortable;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.CAQueryUtils;
import org.sustudio.concise.app.query.DefaultConcQuery;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.statistics.ConciseMultivariate;
import org.sustudio.concise.core.statistics.DocumentPlotData;
import org.sustudio.concise.core.statistics.WordPlotData;
import org.sustudio.concise.core.statistics.ca.ConciseCA;
import org.sustudio.concise.core.statistics.pca.ConcisePCACorr;
import org.sustudio.concise.core.wordlister.Word;

import com.sun.javafx.charts.Legend;

public class ScatterPlotter extends GearController implements IGearSortable {

	private enum MultivariateAnalysis { PrincipalComponentAnalysis, CorrespondenceAnalysis };
	private MultivariateAnalysis analysis;
	private List<WordPlotData> wordData;
	private FXCanvas fxCanvas;
	private ScatterChart<Number, Number> scatterChart;
	private ScatterPlotterDataPanel dataPanel;
	
	public ScatterPlotter() {
		super(CABox.GearBox, Gear.ScatterPlotter);
	}
	
	protected void init() {
		analysis = MultivariateAnalysis.CorrespondenceAnalysis;
	}
	
	@Override
	protected Control createControl() {
		
		final SashForm sash = new SashForm(this, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new FillLayout());
		
		fxCanvas = createChart(sash);
		dataPanel = new ScatterPlotterDataPanel(sash, this);
		
		sash.setWeights(new int[] { 70, 30 } );
		
		return sash;
	}
	
	/**
	 * 圖表
	 * @param parent
	 * @return
	 */
	private FXCanvas createChart(Composite parent) {
		fxCanvas = new FXCanvas(parent, SWT.NONE);
		BorderPane border = new BorderPane();
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(2, 15, 2, 15));
		hbox.setSpacing(10);
		
		ChoiceBox<String> cb = new ChoiceBox<String>(FXCollections.observableArrayList(
			"Principal Component Analysis (PCA)",
			"Correspondence Analysis (CA)")
		);
		cb.getSelectionModel().select(
				ArrayUtils.indexOf(MultivariateAnalysis.values(), analysis)
		);
		cb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number value, Number new_value) {
				analysis = MultivariateAnalysis.values()[new_value.intValue()];
				loadData();
			}			
		});
		cb.setStyle("-fx-font-size: 11px");
		cb.setMaxWidth(Double.MAX_VALUE);
		
		Button btnAutoZoom = new Button("Auto Zoom");
		btnAutoZoom.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				scatterChart.getXAxis().setAutoRanging( true );
				scatterChart.getYAxis().setAutoRanging( true );
				ObservableList<XYChart.Series<Number,Number>> data = scatterChart.getData();
				scatterChart.setData( FXCollections.<XYChart.Series<Number, Number>>emptyObservableList() );
				scatterChart.setData( data );
			}
		});
		Image magnifyImage = new Image(getClass().getResourceAsStream("/org/sustudio/concise/app/icon/06-magnify.png"));
		ImageView magnifyImageView = new ImageView(magnifyImage);
		magnifyImageView.setFitHeight(11);
		magnifyImageView.setPreserveRatio(true);
		btnAutoZoom.setGraphic(magnifyImageView);
		btnAutoZoom.setPrefWidth(140);
		btnAutoZoom.setFont(Font.font(Font.getDefault().getName(), 11));
		
		Button btnClear = new Button("Clear");
		btnClear.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				try {
					SQLiteDB.dropTableIfExists(CATable.ScatterPlotter);
				} catch (SQLException | IOException e) {
					workspace.logError(gear, e);
					Dialog.showException(e);
				} finally {
					loadData();
				}
			}			
		});
		Image trashImage = new Image(getClass().getResourceAsStream("/org/sustudio/concise/app/icon/trash-can.png"));
		ImageView trashImageView = new ImageView(trashImage);
		trashImageView.setFitHeight(11);
		trashImageView.setPreserveRatio(true);
		btnClear.setGraphic(trashImageView);
		btnClear.setPrefWidth(140);
		btnClear.setFont(Font.font(Font.getDefault().getName(), 11));
		
		final Button btnTable = new Button("Hide Table");
		btnTable.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				SashForm sashForm = (SashForm) fxCanvas.getParent();
				if (sashForm.getMaximizedControl() == null) {
					sashForm.setMaximizedControl(fxCanvas);
					btnTable.setText("Show Table");
				}
				else {
					sashForm.setMaximizedControl(null);
					btnTable.setText("Hide Table");
				}
			}
		});
		btnTable.setPrefWidth(140);
		btnTable.setFont(Font.font(Font.getDefault().getName(), 11));
		
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(cb, btnAutoZoom, btnClear, btnTable);
		border.setTop(hbox);
		
		// creating the chart
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		scatterChart = new ScatterChart<Number, Number>(xAxis, yAxis);
		scatterChart.setAnimated(false);
		scatterChart.setLegendSide(Side.BOTTOM);
		border.setCenter(scatterChart);
		addZoomSupportForChart();
		
		// handling legend
		final Legend legend = (Legend) scatterChart.lookup(".chart-legend");
		legend.getChildrenUnmodifiable().addListener(new ListChangeListener<Object>() {
			@Override
			public void onChanged(Change<? extends Object> arg0) {
				for (final Node node : legend.getChildrenUnmodifiable()) {
				if (node instanceof Label) {
					final int index = legend.getChildrenUnmodifiable().indexOf(node);
					node.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override public void handle(MouseEvent event) {
							for (Data<Number, Number> data : scatterChart.getData().get(index).getData()) {
								Node dot = data.getNode();
								data.getNode().setVisible(!dot.isVisible());
							}
						}
					});
					node.setOnMouseEntered(new EventHandler<MouseEvent>() {
						@Override public void handle(MouseEvent event) {
							DropShadow dropShadow = new DropShadow();
							for (Data<Number, Number> data : scatterChart.getData().get(index).getData()) {
								Node dot = data.getNode();
								dot.setScaleX(1.5);
								dot.setScaleY(1.5);
								dot.setEffect(dropShadow);
							}
							node.setEffect(dropShadow);
						}
					});
					node.setOnMouseExited(new EventHandler<MouseEvent>() {
						@Override public void handle(MouseEvent event) {
							// series might be removed
							if (scatterChart.getData().size() > index) {
								for (Data<Number, Number> data : scatterChart.getData().get(index).getData()) {
									Node dot = data.getNode();
									dot.setScaleX(1);
									dot.setScaleY(1);
									dot.setEffect(null);
								}
								node.setEffect(null);
							}
						}
					});
				} }
			}
		});
		
		fxCanvas.setScene(new Scene(border));
		return fxCanvas;
	}
	
	// TODO this is still a test function
	private void addZoomSupportForChart() {
		scatterChart.getXAxis().setAutoRanging( true );
		scatterChart.getYAxis().setAutoRanging( true );
		
		//Panning works via either secondary (right) mouse or primary with ctrl held down
		ChartPanManager panner = new ChartPanManager( scatterChart );
		panner.setMouseFilter( new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				if ( mouseEvent.getButton() == MouseButton.SECONDARY ||
						 ( mouseEvent.getButton() == MouseButton.PRIMARY &&
						   mouseEvent.isShortcutDown() ) ) {
					//let it through
				} else {
					mouseEvent.consume();
				}
			}
		} );
		panner.start();
		
		JFXChartUtil.setupZooming(scatterChart, new EventHandler<MouseEvent>() {
			@Override
			public void handle( MouseEvent mouseEvent ) {
				if ( mouseEvent.getButton() != MouseButton.PRIMARY ||
				     mouseEvent.isShortcutDown() )
					mouseEvent.consume();
			}
		} );
	}
	
	
	public Control getControl() {
		return fxCanvas;
	}
	
	public Control[] getZoomableControls() {
		return new Control[] { dataPanel.getZoomableControl() };
	}
	
	public ScatterChart<Number, Number> getChart() {
		return scatterChart;
	}
	
	public void loadData() {
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		scatterChart.getData().clear();
		if (wordData != null) {
			wordData.clear();
			dataPanel.clearTable();
		}
		
		Thread thread = new Thread() {
			public void run() {
				try {
					// add existing words
					SQLiteDB.createTableIfNotExists(CATable.ScatterPlotter);
					String sql = "SELECT * FROM " + CATable.ScatterPlotter.name();
					ResultSet rs = SQLiteDB.executeQuery(sql);
					ArrayList<String> words = new ArrayList<String>();
					while (rs.next()) {
						final String word = rs.getString(DBColumn.Word.columnName());
						words.add(word);
					}
					rs.close();
					if (!words.isEmpty()) {
						final ConciseMultivariate multivariate;
						switch (analysis) {
						default:
						case CorrespondenceAnalysis:
							multivariate = new ConciseCA(workspace, CAPrefs.SHOW_PART_OF_SPEECH);
							break;
						
						case PrincipalComponentAnalysis:
							multivariate = new ConcisePCACorr(workspace, CAPrefs.SHOW_PART_OF_SPEECH);
							break;
						}
						multivariate.setWords(words);
						
						wordData = multivariate.getRowProjectionData();
						getDisplay().asyncExec(new Runnable() {
							public void run() {
								NumberStringConverter fmt = new NumberStringConverter("#.##%");
								scatterChart.getXAxis().setLabel("Factor 1 (" + fmt.toString(multivariate.getRatesOfInertia()[1]) + ")");
								scatterChart.getYAxis().setLabel("Factor 2 (" + fmt.toString(multivariate.getRatesOfInertia()[2]) + ")");
								
								// sort wordData
								sort();
								
								if (MultivariateAnalysis.CorrespondenceAnalysis.equals(analysis)) {
									Series<Number, Number> dSeries = new Series<Number, Number>();
									dSeries.setName("Documents");
									for (DocumentPlotData pd : multivariate.getColProjectionData()) {
										XYChart.Data<Number, Number> data = 
												new XYChart.Data<Number, Number>(pd.getX(), pd.getY());
										data.setNode(new HoverNode(pd.getDoc().title, pd.getDoc()));
										dSeries.getData().add(data);
									}
									scatterChart.getData().add(dSeries);
								}
								
								// make word node toFront
								for(Data<Number, Number> data : scatterChart.getData().get(0).getData()) {
									data.getNode().toFront();
								}
							}
						});
					}
					
				} catch (Exception e) {
					workspace.logError(gear, e);
					Dialog.showException(e);
				}
				
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						spinner.close();
					}
				});
			}
		};
		thread.setDaemon(true);
		thread.start();
		super.loadData();
	}
	
	public void unloadData() {
		super.unloadData();
		scatterChart.getData().clear();
		
	}
	
	public void sort() {
		Collections.sort(wordData, new Comparator<WordPlotData>() {
			public int compare(WordPlotData o1, WordPlotData o2) {
				DBColumn dbColumn = dataPanel.getSortColumn();
				int dir;
				if (dbColumn.equals(DBColumn.Word)) {
					dir = o1.getWord().getWord().compareTo(o2.getWord().getWord());
				} else {
					if (o1.getWord().totalTermFreq > o2.getWord().totalTermFreq)
						dir = 1;
					else if (o1.getWord().totalTermFreq < o2.getWord().totalTermFreq)
						dir = -1;
					else
						dir = 0;
				}
				
				if (dataPanel.getSortDirection() == SWT.DOWN)
					dir = dir * -1;
				return dir;
			}							
		});
		
		// check if words series already exists
		Series<Number, Number> series;
		if (scatterChart.getData().isEmpty()) {
			series = new Series<Number, Number>();
			series.setName("Words");
		}
		else {
			series = scatterChart.getData().get(0);
			series.getData().clear();
		}
		
		for (WordPlotData pd : wordData) {
			XYChart.Data<Number, Number> data = 
					new XYChart.Data<Number, Number>(pd.getX(), pd.getY());
			data.setNode(new HoverNode(pd.getWord().word, pd.getWord()));
			series.getData().add(data);
		}
		if (scatterChart.getData().isEmpty()) {
			scatterChart.getData().add(series);
		}
		dataPanel.setInput(wordData);
	}
	
	public void doit(CAQuery query) {
		ConciseThread thread = new ConciseThread(gear, query) {
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
	
	class HoverNode extends StackPane {
		
		final String text;
		final Object obj;
		
		public HoverNode(final String text, final Object obj) {
			this.text = text;
			this.obj = obj;
			this.setOpacity(0.8);
			setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent arg0) {
					showLabel();
					setCursor(Cursor.HAND);
				}
			});
			setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent arg0) {
					setCursor(null);
					hideLabel();
				}
			});
			setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent event) {
					hideLabel();
					if (obj instanceof ConciseDocument) {
						// open document viewer
						DocumentViewer dv = (DocumentViewer) Gear.DocumentViewer.open(workspace);
						dv.open(((ConciseDocument) obj).docID);
					}
					else if (obj instanceof Word) {
						// open concordancer
						Gear.Concordancer.open(workspace)
							.doit(new DefaultConcQuery(text));
					}
				}
			});
		}
		
		public void showLabel() {
			final Label label = new Label(text);
			label.setFont(Font.font(Font.getDefault().getName(), 9));
			label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
			//textNode.autosize();
			label.setTranslateY(-10);
			setAlignment(Pos.BOTTOM_CENTER);
			
			getChildren().addAll(label);
			toFront();
			
			DropShadow shadow = new DropShadow();
			setEffect(shadow);
			toFront();
		}
		
		public void hideLabel() {
			getChildren().clear();
			//toBack();
			setEffect(null);
			setScaleX(1.0);
			setScaleY(1.0);
		}
	}	
}
