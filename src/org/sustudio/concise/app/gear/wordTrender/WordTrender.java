package org.sustudio.concise.app.gear.wordTrender;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swt.FXCanvas;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.DocumentViewer;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.GearController;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.CAQueryUtils;
import org.sustudio.concise.app.thread.ConciseThread;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.Workspace.INDEX;
import org.sustudio.concise.core.collocation.ConciseTokenAnalyzer;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.sustudio.concise.core.highlighter.DocumentHighlighter;
import org.sustudio.concise.core.wordlister.WordUtils;

import com.sun.javafx.charts.Legend;

public class WordTrender extends GearController {

	protected static final ConciseDocument ALL_DOCUMENTS = new ConciseDocument();
	protected static int SEGMENTS = 100;
	
	private ArrayList<ConciseDocument> docs;
	private ConciseLineChart lineChart;
	private ConciseDocument doc;
	private boolean collapse = false;
	
	public WordTrender() {
		super(CABox.GearBox, Gear.WordTrender);
	}
	
	protected void init() {
		// a dummy ConciseDocument represents All Documents
		doc = ALL_DOCUMENTS;
		ALL_DOCUMENTS.title = "All Documents";
		ALL_DOCUMENTS.docID = -1;
		
		try {
			if (docs == null)
				docs = new ArrayList<ConciseDocument>();
			if (docs != null &&
				docs.size() != workspace.getIndexReader(INDEX.DOCUMENT).numDocs()) 
			{
				docs.clear();
				for (ConciseDocument d : new DocumentIterator(workspace)) {
					docs.add(d);
				}
				Collections.sort(docs, new Comparator<ConciseDocument>() {
					public int compare(ConciseDocument o1, ConciseDocument o2) {
						return o1.title.compareTo(o2.title);
					}
				});
			}
		} catch (Exception e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		}
	}
	
	@Override
	protected Control createControl() {
		// solve problem: Not on FX application thread; currentThread = JavaFX Application Thread
		// see http://stackoverflow.com/questions/21083945/how-to-avoid-not-on-fx-application-thread-currentthread-javafx-application-th
		Platform.setImplicitExit(false);
		
		final FXCanvas fxCanvas = new FXCanvas(this, SWT.NONE);
		
		BorderPane border = new BorderPane();
		border.setTop(getLineChartToolBar());
		
		lineChart = new ConciseLineChart();
		border.setCenter(lineChart);
		
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
							removeSeries(((Label) node).getText());
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
		
		fxCanvas.setScene(new Scene(border));
		return fxCanvas;
	}
	
	/**
	 * LineChart 上方的工具列
	 * @return LineChart 上方的工具列
	 */
	private Node getLineChartToolBar() {
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(2, 15, 2, 15));
		hbox.setSpacing(10);
		
		ObservableList<ConciseDocument> docList = FXCollections.observableArrayList();
		// add a dummy ConciseDocument to represent ALL Documents
		docList.add(ALL_DOCUMENTS);
		for (ConciseDocument d : docs) {
			docList.add(d);
		}
		
		ChoiceBox<ConciseDocument> cb = new ChoiceBox<ConciseDocument>(docList);
		cb.getSelectionModel().select(doc);
		cb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ConciseDocument>() {
			public void changed(
					ObservableValue<? extends ConciseDocument> ov,
					ConciseDocument oldDoc, ConciseDocument newDoc) 
			{
				doc = newDoc;
				loadDocument(doc);
			}
			
		});
		cb.setConverter(new StringConverter<ConciseDocument>() {
			public String toString(ConciseDocument cd) {
				return cd.title;
			}
			public ConciseDocument fromString(String string) {
				return null;	// now allowed
			}
		});
		cb.setMaxWidth(Double.MAX_VALUE);
		cb.setStyle("-fx-font-size: 11px");
		cb.setPrefWidth(350);
		
		CheckBox cbCollapse = new CheckBox("Collapse");
		cbCollapse.setPrefWidth(200);
		cbCollapse.setSelected(collapse);
		cbCollapse.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override public void changed(ObservableValue<? extends Boolean> ov,
					Boolean old_val, Boolean new_val) 
			{
				collapse = new_val;
				loadDocument(doc);
			}
		});
		cbCollapse.setFont(Font.font(Font.getDefault().getName(), 11));
		
		Button btnClear = new Button("Clear");
		btnClear.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				try {
					SQLiteDB.dropTableIfExists(CATable.WordTrender);
				} catch (SQLException | IOException e) {
					workspace.logError(gear, e);
					Dialog.showException(e);
				} finally {
					lineChart.getData().clear();
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
		
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(cb, cbCollapse, btnClear);
		
		return hbox;
	}
	
	public Control[] getZoomableControls() {
		// TODO add zoom support
		return null;
	}
	
	public void loadData() {
		loadDocument(doc);
		super.loadData();
	}
	
	/**
	 * 載入特定文件的 word trend
	 * @param cd
	 */
	public void loadDocument(ConciseDocument cd) {
		CASpinner spinner = new CASpinner(this);
		spinner.open();
		try {
			lineChart.getData().clear();
			
			SQLiteDB.createTableIfNotExists(CATable.WordTrender);
			// add existing words
			String sql = "SELECT * FROM " + CATable.WordTrender.name();
			ResultSet rs = SQLiteDB.executeQuery(sql);
			ArrayList<String> words = new ArrayList<String>();
			while (rs.next()) {
				String word = rs.getString(DBColumn.Word.columnName());
				words.add(word);
			}
			rs.close();
			
			if (collapse) {
				if (cd.equals(ALL_DOCUMENTS))
					addCollapseSeries(words);
				else
					addCollapseSeriesFor(words, doc);
			}
			else {
				for (String word: words) {
					if (cd.equals(ALL_DOCUMENTS))
						addSeriesFor(word);
					else
						addSeriesFor(word, doc);
				}
			}
			words.clear();
			
		} catch (Exception e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		}
		spinner.close();
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
	
	/**
	 * 刪除 series
	 * @param word
	 */
	private void removeSeries(final String word) {
		final CASpinner spinner = new CASpinner(WordTrender.this);
		spinner.open();
		Thread thread = new Thread() {
			public void run() {
				
				// remove from database
				try {
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
				
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						for (Series<String, Number> series : lineChart.getData()) {
							if (series.getName().equals(word)) {
								int index = lineChart.getData().indexOf(series);
								lineChart.getData().remove(index);
								break;
							}
						}
						spinner.close();
					}
				});
			}
		};
		thread.setDaemon(true);
		thread.start();
	}
	
	private void addSeriesData(String seriesName, Map<ConciseDocument, Integer> freqTable) {
		Series<String, Number> series = new Series<>();
		series.setName(seriesName);
		
		for (ConciseDocument cd : docs) {
			int freq = 0;
			if (freqTable.get(cd) != null) {
				freq = freqTable.get(cd).intValue();
			}
			XYChart.Data<String, Number> data =
					new XYChart.Data<String, Number>(
							//docs.indexOf(cd),
							cd.title,
							freq);
			series.getData().add(data);
			data.setNode(new HoveredNode(series.getName(), cd, docs.indexOf(cd)));
		}
		lineChart.getData().add(series);
	}
	
	private void addCollapseSeries(List<String> words) throws Exception {
		Map<ConciseDocument, Integer> freqTable = new HashMap<ConciseDocument, Integer>();
		for (String word : words) {
			Map<ConciseDocument, Integer> fTable = WordUtils.wordFreqByDocs(workspace, word, docs);
			for (Iterator<ConciseDocument> iterator = fTable.keySet().iterator(); iterator.hasNext(); ) {
				ConciseDocument key = iterator.next();
				if (freqTable.containsKey(key)) {
					freqTable.put(key, freqTable.get(key) + fTable.get(key));
				} else {
					freqTable.put(key, fTable.get(key));
				}
			}
			fTable.clear();
		}
		addSeriesData(StringUtils.join(words, ","), freqTable);
	}
	
	private void addSeriesFor(String word) throws Exception {
		// check if the word already exists
		for (Series<String, Number> series : lineChart.getData()) {
			if (series.getName().equals(word))
				return;
		}
		
		Map<ConciseDocument, Integer> freqTable = WordUtils.wordFreqByDocs(workspace, word, docs);
		addSeriesData(word, freqTable);
	}
	
	private void addSeriesData(String seriesName, Map<Integer, Integer> freqTable, ConciseDocument cd) {
		Series<String, Number> series = new Series<>();
		series.setName(seriesName);
		
		for (int i = 0; i < SEGMENTS; i++) {
			int freq = 0;
			if (freqTable.get(Integer.valueOf(i)) != null) {
				freq = freqTable.get(Integer.valueOf(i)).intValue();
			}
			XYChart.Data<String, Number> data =
					new XYChart.Data<String, Number>(String.valueOf(i+1), freq);
			series.getData().add(data);
			data.setNode(new HoveredNode(series.getName(), cd, i));
		}
		lineChart.getData().add(series);
	}
	
	private void addCollapseSeriesFor(List<String> words, ConciseDocument cd) {
		
		Map<Integer, Integer> freqTable = new HashMap<Integer, Integer>();
		for (String word : words) {
			Map<Integer, Integer> fTable = getSegmentFreqTable(word, cd);
			for (Iterator<Integer> iterator = fTable.keySet().iterator(); iterator.hasNext(); ) {
				Integer key = iterator.next();
				if (freqTable.containsKey(key)) {
					freqTable.put(key, freqTable.get(key) + fTable.get(key));
				} else {
					freqTable.put(key, fTable.get(key));
				}
			}
			fTable.clear();
		}
		
		addSeriesData(StringUtils.join(words, ","), freqTable, cd);
	}
	
	private void addSeriesFor(String word, ConciseDocument cd) {
		// check if the word already exists
		for (Series<String, Number> series : lineChart.getData()) {
			if (series.getName().equals(word))
				return;
		}
		
		// start putting line chart series
		Map<Integer, Integer> freqTable = getSegmentFreqTable(word, cd);
		addSeriesData(word, freqTable, cd);
	}
	
	// TODO 寫得很潦草，需要改寫
	private Map<Integer, Integer> getSegmentFreqTable(String word, ConciseDocument cd) {
		// 調用 concordance plotter 的方法
		HashMap<Integer, Integer> freqTable = new HashMap<Integer, Integer>();
		try {
			final String preTag = "<>";
			final String postTag = "</>";
			Conc conc = new Conc(workspace, 
								 word, 
								 CAPrefs.SHOW_PART_OF_SPEECH);
			DocumentHighlighter highlighter = 
					new DocumentHighlighter(
							workspace, 
							conc.getQuery(),
							cd.docID,
							new String[] { preTag },
							new String[] { postTag},
							CAPrefs.SHOW_PART_OF_SPEECH) 
			{
				public Analyzer getAnalyzer() {
					return new ConciseTokenAnalyzer(CAPrefs.SHOW_PART_OF_SPEECH);
				}
			};
			
			String content = highlighter.getHighlightText();
			if (content != null) {
				content = content.replace(" "+postTag, postTag+" ");
				ArrayList<Integer> positions = new ArrayList<Integer>();
				
				int wordsCount = 0;
				StringTokenizer st = new StringTokenizer(content, " \n");
				while (st.hasMoreTokens()) {
					String w = st.nextToken();
					if (w.endsWith(postTag)) {
						positions.add(wordsCount);
					}
					wordsCount++;
				}
				
				// mapping positions into segments
				for (int p : positions) {
					int x = Math.round( SEGMENTS * p / wordsCount );
					Integer count = freqTable.get(x);
					if (count == null)
						freqTable.put(x, 1);
					else
						freqTable.put(x, count + 1);
				}
				positions.clear();
			}
			
		} catch (Exception e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		}
		return freqTable;
	}
	
	class HoveredNode extends StackPane {
		HoveredNode(String word, final ConciseDocument cd, final int xIndex) {
			setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent event) {
					lineChart.showFreqBox(HoveredNode.this);
					setCursor(Cursor.HAND);
				}
			});
			setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent event) {
					//lineChart.hideFreqBox();
					setCursor(null);
		        }
			});
			setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent event) {
					getChildren().clear();
					
					// open DocumentViewer
					DocumentViewer viewer = (DocumentViewer) Gear.DocumentViewer.open(workspace);
					StringBuilder sb = new StringBuilder();
					for (Series<String, Number> series: lineChart.getData()) {
						if (sb.length() > 0) sb.append(" ");
						sb.append(series.getName());
					}
					CAQuery query = new CAQuery(Gear.DocumentViewer);
					query.searchStr = sb.toString();
					viewer.open(cd.docID, 1, query);
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
