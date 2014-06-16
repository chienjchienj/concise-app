package org.sustudio.concise.app.gear;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.converter.NumberStringConverter;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.CAQueryUtils;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.concordance.Conc;
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
	private Table dataTable;
	private FXCanvas fxCanvas;
	private ScatterChart<Number, Number> scatterChart;
	
	
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
		
		dataTable = new Table(sash, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);
		dataTable.setHeaderVisible(true);
		dataTable.setLinesVisible(true);
		CopyPasteHelper.listenTo(dataTable);
		
		SelectionAdapter columnSortListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				TableColumn column = (TableColumn) event.widget;
				DBColumn dbColumn = (DBColumn) column.getData(GearController._DB_COLUMN);
				if (dbColumn != null) {
					Table table = column.getParent();
					int dir = table.getSortDirection();
					
					if (column == table.getSortColumn())
						dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
					else
						table.setSortColumn(column);
					table.setSortDirection(dir);
					
					sort();
				}
			}
		};
		
		final TableColumn tblclmWord = new TableColumn(dataTable, SWT.NONE);
		tblclmWord.setData(_DB_COLUMN, DBColumn.Word);
		tblclmWord.addSelectionListener(columnSortListener);
		tblclmWord.setText("Word");
		tblclmWord.setWidth(100);
		final TableColumn tblclmFreq = new TableColumn(dataTable, SWT.RIGHT);
		tblclmFreq.setData(_DB_COLUMN, DBColumn.Freq);
		tblclmFreq.addSelectionListener(columnSortListener);
		tblclmFreq.setText("Frequency");
		tblclmFreq.setWidth(120);
		
		dataTable.setSortDirection(SWT.DOWN);
		dataTable.setSortColumn(tblclmFreq);
		
		dataTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = event.index;
				if (wordData != null) {
					Word word = wordData.get(index).getWord();
					item.setText(0, word.getWord());
					item.setText(1, new NumberStringConverter("#,###,###,###").toString(word.totalTermFreq));
				}
			}
		});
		dataTable.setItemCount(0);
		
		dataTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int seriesIndex = 1;
				if (MultivariateAnalysis.PrincipalComponentAnalysis.equals(analysis)) {
					seriesIndex = 0;
				}
				for (Data<Number, Number> data : scatterChart.getData().get(seriesIndex).getData()) {
					HoverNode node = (HoverNode) data.getNode();
					node.hideLabel();
				}
				for (int i : dataTable.getSelectionIndices()) {
					HoverNode node = (HoverNode) scatterChart.getData().get(seriesIndex).getData().get(i).getNode();
					node.setScaleX(2.0);
					node.setScaleY(2.0);
					node.showLabel();
				}
			}
		});
		
		dataTable.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.DEL:
				case SWT.BS:
					// TODO add warning
					try {
						if (wordData == null) return;
						String sql = "DELETE FROM " + CATable.ScatterPlotter.name() + " WHERE " + DBColumn.Word.columnName() + " = ?";
						PreparedStatement ps = SQLiteDB.prepareStatement(sql);
						for (int i : dataTable.getSelectionIndices()) {
							String word = wordData.get(i).getWord().word;
							ps.setString(1, word);
							ps.addBatch();
						}
						SQLiteDB.executeBatch(ps);
						loadData();
						
					} catch (SQLException | IOException e) {
						workspace.logError(gear, e);
						Dialog.showException(e);
					}
					
				case SWT.ESC:
					dataTable.setSelection(-1);
					int seriesIndex = 1;
					if (MultivariateAnalysis.PrincipalComponentAnalysis.equals(analysis)) {
						seriesIndex = 0;
					}
					for (Data<Number, Number> data : scatterChart.getData().get(seriesIndex).getData()) {
						HoverNode node = (HoverNode) data.getNode();
						node.hideLabel();
					}
					break;
				}
			}
		});
		
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
		
		
		Button btnReset = new Button("Reset");
		btnReset.setOnMousePressed(new EventHandler<MouseEvent>() {
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
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(cb, btnReset);
		border.setTop(hbox);
		
		// creating the chart
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		scatterChart = new ScatterChart<Number, Number>(xAxis, yAxis);
		scatterChart.setAnimated(false);
		scatterChart.setLegendSide(Side.BOTTOM);
		border.setCenter(scatterChart);
		
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
	
	
	public Control getControl() {
		return fxCanvas;
	}
	
	public Control[] getZoomableControls() {
		// TODO enable zoom
		return new Control[] { dataTable };
	}
	
	public void loadData() {
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		scatterChart.getData().clear();
		if (wordData != null) {
			wordData.clear();
			dataTable.setItemCount(wordData.size());
			dataTable.clearAll();
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
								
								if (MultivariateAnalysis.CorrespondenceAnalysis.equals(analysis)) {
									Series<Number, Number> dSeries = new Series<Number, Number>();
									dSeries.setName("Docs");
									for (DocumentPlotData pd : multivariate.getColProjectionData()) {
										XYChart.Data<Number, Number> data = 
												new XYChart.Data<Number, Number>(pd.getX(), pd.getY());
										data.setNode(new HoverNode(pd.getDoc().title));
										dSeries.getData().add(data);
									}
									scatterChart.getData().add(dSeries);
								}
								// sort wordData
								sort();
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
				DBColumn dbColumn = (DBColumn) dataTable.getSortColumn().getData(_DB_COLUMN);
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
				
				if (dataTable.getSortDirection() == SWT.DOWN)
					dir = dir * -1;
				return dir;
			}							
		});
		
		dataTable.setItemCount(wordData.size());
		dataTable.clearAll();
		
		// check if words series already exists
		if (scatterChart.getData().size() > 1) {
			scatterChart.getData().remove(1);
		}
		
		Series<Number, Number> series = new Series<Number, Number>();
		series.setName("Words");
		for (WordPlotData pd : wordData) {
			XYChart.Data<Number, Number> data = 
					new XYChart.Data<Number, Number>(pd.getX(), pd.getY());
			data.setNode(new HoverNode(pd.getWord().word));
			series.getData().add(data);
		}
		scatterChart.getData().add(series);
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
		
		public HoverNode(final String text) {
			this.text = text;
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
		}
		
		public void showLabel() {
			final Text textNode = new Text(text);
			textNode.setFont(Font.font(Font.getDefault().getName(), 9));
			textNode.autosize();
			textNode.setTranslateY(-10);
			setAlignment(Pos.BOTTOM_CENTER);
			
			getChildren().addAll(textNode);
			toFront();
			
			DropShadow shadow = new DropShadow();
			setEffect(shadow);
		}
		
		public void hideLabel() {
			getChildren().clear();
			toBack();
			setEffect(null);
			setScaleX(1.0);
			setScaleY(1.0);
		}
	}	
}
