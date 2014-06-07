package org.sustudio.concise.app.gear;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.helper.ZoomHelper;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.CAQueryUtils;
import org.sustudio.concise.app.utils.RevealInFinder;
import org.sustudio.concise.app.widgets.CAAutoCompleteText;
import org.sustudio.concise.app.widgets.CANavigationButton;
import org.sustudio.concise.app.widgets.CANavigationButton.NavigationEvent;
import org.sustudio.concise.app.widgets.CANavigationButton.NavigationListener;
import org.sustudio.concise.app.widgets.CASpinner;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.autocompleter.AutoCompleter;
import org.sustudio.concise.core.concordance.LineAndWhitespaceTokenizer;
import org.sustudio.concise.core.concordance.PartOfSpeechFilter;
import org.sustudio.concise.core.concordance.PartOfSpeechSeparatorFilter;
import org.sustudio.concise.core.corpus.importer.ConciseField;
import org.sustudio.concise.core.corpus.importer.ImportPOSAnalyzer;
import org.sustudio.concise.core.highlighter.DocumentHighlighter;

public class DocumentViewer 
	   extends GearController
	   implements IGearFileRevealable {
	
	private StyledText styledText;
	private Label lblMarker;
	private CAAutoCompleteText txtSearch;
	private Font titleFont;
	
	private IndexReader reader;
	private int highlightDocId = -1;
	private CAQuery query = null;
	
	private Directory tmpDirectory;
	private IndexReader tmpReader;
	
	
	/**
	 * Default constructor.
	 */
	public DocumentViewer() {
		super(CABox.ToolBox, Gear.DocumentViewer);
		try {
			this.reader = workspace.getIndexReader();
		} catch (IOException e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		}
	}
	
	private void showPrev() {
		final StyleRange[] ranges = styledText.getStyleRanges();
		if (ranges.length < 2) {
			return;
		}
		StyleRange targetRange = null;
		for (int i=ranges.length-1; i>0; i--) {
			final StyleRange r = ranges[i];
			if (r.start < styledText.getCaretOffset() - r.length) {
				targetRange = r;
				break;
			}
		}
		// loop from end
		if (targetRange == null) {
			targetRange = ranges[ranges.length-1];
		}
		styledText.setSelection(targetRange.start, targetRange.start + targetRange.length);
		styledText.showSelection();
	}
	
	private void showNext() {
		final StyleRange[] ranges = styledText.getStyleRanges();
		if (ranges.length < 2) {
			return;
		}
		StyleRange targetRange = null;
		for (StyleRange r : ranges) {
			if (r.start > 0 && r.start > styledText.getCaretOffset()) {
				targetRange = r;
				break;
			}
		}
		// loop from beginning
		if (targetRange == null) {
			targetRange = styledText.getStyleRanges()[1];
		}
		styledText.setSelection(targetRange.start, targetRange.start + targetRange.length);
		styledText.showSelection();
	}
	
	private Composite createSearchBlock() {
		final Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		GridLayout gl = new GridLayout(2, false);
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		gl.horizontalSpacing = 5;
		composite.setLayout(gl);
		
		final CANavigationButton btn = new CANavigationButton(composite);
		btn.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		btn.addNavigationListener(new NavigationListener() {

			@Override
			public void prevClicked(NavigationEvent event) {
				showPrev();
			}

			@Override
			public void nextClicked(NavigationEvent event) {
				showNext();
			}
			
		});
		
		txtSearch = new CAAutoCompleteText(composite, SWT.SEARCH | SWT.ICON_SEARCH | SWT.BORDER | SWT.CANCEL);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSearch.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.NORMAL));
		txtSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				CAQuery newQuery = query == null ? new CAQuery(getGear()) : query.copy();
				newQuery.searchStr = txtSearch.getText().trim();
				if (newQuery.searchStr.isEmpty()) {
					// remove highlight
					StyleRange range = styledText.getStyleRanges()[0];
					styledText.setStyleRange(null);
					styledText.setStyleRange(range);
					try {
						File file = new File(reader.document(highlightDocId).get(ConciseField.FILENAME.field()));
						setStatusText(CAPrefs.SHOW_FULL_FILEPATH ? file.getPath() : file.getName());
					} catch (IOException e) {
						Concise.getCurrentWorkspace().logError(gear, e);
						Dialog.showException(e);
					}
					lblMarker.redraw();
				}
				else {
					// test if the queryStr is valid
					if (CAQueryUtils.isValidQuery(newQuery)) {
						open(highlightDocId, 1, newQuery);
					}
				}
			};
		});		
		
		return composite;
	}
	
	@Override
	public Control createControl() {
		final GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		
		// search block
		createSearchBlock();
		
		styledText = new StyledText(this, SWT.FULL_SELECTION | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
		styledText.setRightMargin(3);
		styledText.setLeftMargin(10);
		styledText.setKeyBinding('C' | SWT.MOD1, ST.COPY);
		styledText.setKeyBinding('A' | SWT.MOD1, ST.SELECT_ALL);
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		styledText.setSelectionBackground(new Color(getDisplay(), 255, 155, 50));
		
		lblMarker = new Label(this, SWT.NONE);
		lblMarker.setBackground(styledText.getBackground());
		lblMarker.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (styledText.getStyleRanges().length < 2) {
					return;
				}
				
				e.gc.setAlpha(127);
				
				// highlight block width
				int w = lblMarker.getBounds().width - 1;
				
				final int baseY = styledText.getLocationAtOffset(0).y;
				final int stHeight = styledText.getLocationAtOffset(styledText.getCharCount()-1).y - baseY;
				if (stHeight > styledText.getClientArea().height) {
					
					int lineCount = Math.round((float) stHeight / styledText.getLineHeight());
					int blockHeight = stHeight / lineCount;
					if (blockHeight < 1) {
						blockHeight = 1;
					}
					
					// draw
					for (StyleRange r : styledText.getStyleRanges()) {
						if (r.start == 0) {
							continue;  // title
						}

						int y = Math.round( (float) styledText.getClientArea().height * (float) (styledText.getLocationAtOffset(r.start).y - baseY) / (float) stHeight);
						e.gc.setBackground(r.background);
						e.gc.fillRectangle(0, y, w, blockHeight);
					}

				}
				else {
					for (StyleRange r : styledText.getStyleRanges()) {
						if (r.start == 0) {
							continue;  // title
						}
						int y = styledText.getLocationAtOffset(r.start).y;
						int h = styledText.getLineHeight(r.start) - 3;
						//e.gc.drawRectangle(0, y+1, w, h);
						//e.gc.fillRectangle(1, y+2, w-1, h-1);
						e.gc.setBackground(r.background);
						e.gc.fillRectangle(0, y, w, h);
					}
				}
			}
		});
		lblMarker.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				int baseY = styledText.getLocationAtOffset(0).y;
				int stHeight = styledText.getLocationAtOffset(styledText.getCharCount()-1).y - baseY;
				int y = Math.round( (float) stHeight / lblMarker.getBounds().height * e.y ) + styledText.getLinePixel(0) ;
				int lineIndex = styledText.getLineIndex(y);
				styledText.setTopIndex(lineIndex);
			}
		});
		
		final GridData gd_lblMarker = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_lblMarker.widthHint = 10;
		lblMarker.setLayoutData(gd_lblMarker);
		
		return styledText;
	}
	
	protected void setZoomableControls() {
		ZoomHelper.addControls(new Control[] { styledText, lblMarker });
	}
	
	public Control[] getZoomableControls() {
		return new Control[] { styledText, lblMarker };
	}
	
	private void resetTitleFont() {
		if (titleFont != null && !titleFont.isDisposed()) {
			titleFont.dispose();
		}
		FontData data = styledText.getFont().getFontData()[0];
	    titleFont = new Font(getDisplay(), data.getName(), data.getHeight() + 3, SWT.BOLD);
	}
	
	
	private void buildDocumentContent(final Document doc, final StringBuilder buffer) throws IOException {
		Analyzer analyzer = new Analyzer() {

			@Override
			protected TokenStreamComponents createComponents(String fieldName,
					Reader reader) {
				
				Tokenizer tokenizer = new LineAndWhitespaceTokenizer(Config.LUCENE_VERSION, reader);
				TokenStream result = new PartOfSpeechFilter(tokenizer, CAPrefs.SHOW_PART_OF_SPEECH);
				if (!CCPrefs.POS_SEPARATOR.equals(Config.SYSTEM_POS_SEPERATOR)) {
					result = new PartOfSpeechSeparatorFilter(result);
				}
				return new TokenStreamComponents(tokenizer, result);
			}
			
		};
		
		StringBuilder text = new StringBuilder();
		TokenStream s = doc.getField(ConciseField.CONTENT.field()).tokenStream(analyzer);
		CharTermAttribute t = s.addAttribute(CharTermAttribute.class);
		s.reset();
		while (s.incrementToken()) {
			if (t.toString().equals("\n")) {
				text.append("\n\n");
			}
			else 
				text.append(t.toString() + " ");
		}
		s.close();
		buffer.append(text);
	}
	
	
	/**
	 * Make Document Indexer and add Auto Completer support
	 * @param content
	 * @throws Exception
	 */
	private void makeDocumentIndexer(Document doc) throws Exception {
		if (tmpReader != null) {
			AutoCompleter.removeInstanceFor(tmpReader);
			tmpReader.close();
			tmpDirectory.close();
		}
		
		tmpDirectory = new RAMDirectory();
		IndexWriter writer = new IndexWriter(tmpDirectory, 
									new IndexWriterConfig(
											Config.LUCENE_VERSION, 
											new ImportPOSAnalyzer(Config.LUCENE_VERSION)));
		
		writer.addDocument(doc);
		writer.close();
		
		tmpReader = DirectoryReader.open(tmpDirectory);
		
		// TODO 這段不知道在幹嘛，忘了
		/*
		Terms terms = MultiFields.getTerms(tmpReader, ConciseField.CONTENT.field());
		if (terms != null) {
			TermsEnum te = terms.iterator(null);
			System.out.println(te.next().utf8ToString());
		}
		*/
		// set IndexReader for auto completer
		txtSearch.setIndexReader(tmpReader);
	}
	
	private void openDocument(final int docID) {
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		resetTitleFont();
		
		highlightDocId = docID;
		query = null;
		final ArrayList<StyleRange> styleRanges = new ArrayList<StyleRange>();
		try {
			Document doc = reader.document(docID);
			
			// auto completer support
			makeDocumentIndexer(doc);
			
			final StringBuilder buffer = new StringBuilder();
			buffer.append(doc.get(ConciseField.TITLE.field()));
			StyleRange range = new StyleRange();
			range.start = 0;
			range.length = buffer.length();
			range.font = titleFont;
			styleRanges.add(range);
			
			buffer.append("\n\n");
			
			buildDocumentContent(doc, buffer);
			
			styledText.setEditable(true);
			styledText.setText(buffer.toString());
			styledText.setStyleRanges(styleRanges.toArray(new StyleRange[0]));
			styledText.setEditable(false);
			buffer.setLength(0);
			
			final File file = new File(doc.get(ConciseField.FILENAME.field()));
			setStatusText(CAPrefs.SHOW_FULL_FILEPATH ? file.getPath() : file.getName());
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.error(getShell(), getGear().name() + " Exception", e.toString());
		}
		lblMarker.redraw();
		spinner.close();
	}
	
	
	/**
	 * 打開文件
	 * @param docID document ID (Lucene's)
	 */
	public void open(final int docID) {
		try {
			open(docID, workspace.getIndexReader());
		} catch (Exception e) {
			workspace.logError(gear, e);
			Dialog.showException(e);
		}
	}
	
	/**
	 * 打開文件，並指定 IndexReader （用在 Reference Corpus Manager）
	 * @param docID
	 * @param reader
	 */
	public void open(final int docID, IndexReader reader) {
		this.reader = reader;
		// async is need to wait for UI fully loaded
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (highlightDocId != docID) {
					openDocument(docID);
				}
			}
		});
	}
	
	private String[] getPreColorTags() {
		ArrayList<String> tags = new ArrayList<String>();
		for (int i = 0; i < CAPrefs.HIGHLIGH_BG_COLOR_SCHEME.length; i++) {
			tags.add("<b colorIndex=\"" + i + "\">");
		}
		return tags.toArray(new String[0]);
	}
	
	private void openDocumentAndHighlight(final int docID, final int wordID, final CAQuery query) {
		final CASpinner spinner = new CASpinner(this);
		spinner.open();
		resetTitleFont();
		
	    try {
			
			if (highlightDocId != docID || 
				query == null ||
				!query.equals(this.query)) 
			{
				txtSearch.setText(query.searchStr);
				
				highlightDocId = docID;
				this.query = query;
				
				DocumentViewer.this.query = query;
				final ArrayList<StyleRange> styleRanges = new ArrayList<StyleRange>();
				Document doc = reader.document(docID);
				 
				// add auto-completer support
				makeDocumentIndexer(doc);
				
				final StringBuilder buffer = new StringBuilder();
				buffer.append(doc.get(ConciseField.TITLE.field()));
				StyleRange titleRange = new StyleRange();
				titleRange.start = 0;
				titleRange.length = buffer.length();
				titleRange.font = titleFont;
				styleRanges.add(titleRange);
				
				buffer.append("\n\n");
				int offsetStart = buffer.length();
				
				String content = DocumentHighlighter.highlight(
										workspace,
										docID, 
										query.searchStr, 
										getPreColorTags(), 
										new String[] { "</b>" },
										CAPrefs.SHOW_PART_OF_SPEECH);
				
				if (content != null) {
					buffer.append(content.replace("\n", "\n\n"));
					for (int offset=offsetStart, b = buffer.indexOf("<b colorIndex=", offset);
							 b != -1;
							 offset = b, b = buffer.indexOf("<b colorIndex=", offset))
						{
							int colorNameStart = buffer.indexOf("\"", b) + 1;
							int colorIndex = Integer.valueOf(buffer.substring(colorNameStart, buffer.indexOf("\"", colorNameStart)));
							Color bgColor = new Color(getDisplay(), CAPrefs.HIGHLIGH_BG_COLOR_SCHEME[colorIndex % CAPrefs.HIGHLIGH_BG_COLOR_SCHEME.length]);
							
							StyleRange range = new StyleRange();
							range.start = b;
							buffer.delete(b, buffer.indexOf(">", b)+1);
							
							b = buffer.indexOf("</b>", b);
							range.length = b - range.start;
							buffer.delete(b, b+4);
							range.background = bgColor;
							styleRanges.add(range);
						}
				}
				else { // nothing highlighted
					buildDocumentContent(doc, buffer);
				}
				
				styledText.setEditable(true);
				styledText.setText(buffer.toString());
				styledText.setStyleRanges(styleRanges.toArray(new StyleRange[0]));
				styledText.setEditable(false);
				buffer.setLength(0);
				
				setStatusText(styleRanges.size() - 1 + " highlights");
				
			}  // end if
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.error(getShell(), getGear().name() + " Exception", e.toString());
		}
	    lblMarker.redraw();
		spinner.close();
		
		// show selection;
		if (styledText.getStyleRanges().length > 1) {
			StyleRange r = styledText.getStyleRanges()[wordID];
			styledText.setSelection(r.start, r.start + r.length);
			styledText.showSelection();
		}
	}
		
	public void open(final int docID, final int wordID, final CAQuery query) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				openDocumentAndHighlight(docID, wordID, query);
			}
		});
	}
	
	public void dispose() {
		highlightDocId = -1;
		
		try {
			if (tmpReader != null) {
				AutoCompleter.removeInstanceFor(tmpReader);
				tmpReader.close();
				tmpDirectory.close();
			}
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
		super.dispose();
	}

	@Override
	public boolean isRevealEnabled() {
		try {
			
			final String filepath = reader.document(highlightDocId).get(ConciseField.FILENAME.field());
			return new File(filepath).exists();
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
		return false;
	}

	@Override
	public void revealFileInFinder() {
		try {
			
			final String filepath = reader.document(highlightDocId).get(ConciseField.FILENAME.field());
			RevealInFinder.show(filepath);
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
	}

	/**
	 * This will open file in default program
	 */
	public void openFileInDocumentViewer() {
		try {
			
			final String filepath = reader.document(highlightDocId).get(ConciseField.FILENAME.field());
			Program.launch(filepath);
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
	}

	@Override
	public void doit(CAQuery query) {
		throw new UnsupportedOperationException("Unsupported");
	}
	
}
