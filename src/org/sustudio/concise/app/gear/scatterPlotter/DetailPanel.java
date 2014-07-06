package org.sustudio.concise.app.gear.scatterPlotter;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.helper.CopyPasteHelper;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.statistics.ca.ResultCA;
import org.sustudio.concise.core.statistics.pca.ResultPCA;
import org.sustudio.concise.core.wordlister.Word;
import org.sustudio.concise.core.wordlister.WordUtils;

public class DetailPanel extends Shell {

	private TabFolder tabFolder;
	
	/**
	 * Create the shell.
	 * @param display
	 */
	public DetailPanel() {
		super(SWT.SHELL_TRIM);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		setText("Detail Panel");
	}
	
	public void setData(final Object data) {
		super.setData(data);
		if (tabFolder != null && !tabFolder.isDisposed())
			tabFolder.dispose();
		
		if (data != null && data instanceof ResultCA) {
			createResult((ResultCA) data);
		}
		else if (data != null && data instanceof ResultPCA) {
			createResult((ResultPCA) data);
		}
		layout();
	}
	
	private void createResult(ResultPCA result) {
		tabFolder = new TabFolder(this, SWT.BORDER);
		
		TabItem summaryItem = new TabItem(tabFolder, SWT.NONE);
		summaryItem.setText("Summary");
		
		Composite comp = new Composite(tabFolder, SWT.EMBEDDED);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));
		summaryItem.setControl(comp);
		
		Label lblInfo = new Label(comp, SWT.NONE);
		
		Table summary = new Table(comp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		summary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		summary.setHeaderVisible(true);
		summary.setLinesVisible(true);
		CopyPasteHelper.listenTo(summary);
		
		TableColumn tblclmnDim = new TableColumn(summary, SWT.NONE);
		tblclmnDim.setText("PCs");
		tblclmnDim.setWidth(60);
		
		TableColumn tblclmnSingular = new TableColumn(summary, SWT.RIGHT);
		tblclmnSingular.setText("Singular value");
		tblclmnSingular.setWidth(80);
		
		TableColumn tblclmnLambda = new TableColumn(summary, SWT.RIGHT);
		tblclmnLambda.setText("Eigenvalue");
		tblclmnLambda.setWidth(80);
		
		TableColumn tblclmnPct = new TableColumn(summary, SWT.RIGHT);
		tblclmnPct.setText("Percentage");
		tblclmnPct.setWidth(80);
		
		TableColumn tblclmnCumulative = new TableColumn(summary, SWT.RIGHT);
		tblclmnCumulative.setText("Cumulative");
		tblclmnCumulative.setWidth(80);
		
		final NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		
		final NumberFormat pf = NumberFormat.getPercentInstance();
		pf.setMinimumFractionDigits(4);
		pf.setMaximumFractionDigits(4);
		
		// Summary
		StringBuilder info = new StringBuilder();
		info.append("Input Table (Rows x Columns): ")
			.append(result.getRowDimension() + " x " + result.getColumnDimension())
			.append(" (Words x Documents)");
		lblInfo.setText(info.toString());
		info.setLength(0);
		double[] singularvalues = result.getSingularvalues();
		double[] eigenvalues = result.getEigenvalues();
		double tot = 0.0;
		for (int i = 0; i < eigenvalues.length; i++) {
			tot += eigenvalues[i];
		}
		double cuml = 0.0;
		for (int i = 0; i < eigenvalues.length; i++) {
			TableItem item = new TableItem(summary, SWT.NONE);
			item.setText(0, "PC" + (i+1));
			item.setText(1, nf.format(singularvalues[i]));
			item.setText(2, nf.format(eigenvalues[i]));
			item.setText(3, pf.format(eigenvalues[i] / tot));
			cuml += eigenvalues[i] / tot;
			item.setText(4, pf.format(cuml));
		}
		
		// Words
		TabItem wordItem = new TabItem(tabFolder, SWT.NONE);
		wordItem.setText("Word");
		Grid wordGrid = createPCAGrid(tabFolder, "Word", result.getEigenvalues().length);
		wordItem.setControl(wordGrid);
		
		Word[] words = result.getWords();
		for (int i = 0; i < words.length; i++) {
			Word word = words[i];
			GridItem item = new GridItem(wordGrid, SWT.NONE);
			item.setHeaderText(String.valueOf(i+1));
			item.setText(0, word.getWord());
			for (int pc = 0; pc < result.getEigenvalues().length; pc++) {
				item.setText(pc+1, nf.format(result.getRowPrincipalComponents()[i][pc]));
			}
		}
		
		
		// Documents
		TabItem docItem = new TabItem(tabFolder, SWT.NONE);
		docItem.setText("Document");
		Grid docGrid = createPCAGrid(tabFolder, "Document", result.getEigenvalues().length);
		docItem.setControl(docGrid);
		
		ConciseDocument[] docs = result.getDocs();
		for (int i = 0; i < docs.length; i++) {
			ConciseDocument doc = docs[i];
			GridItem item = new GridItem(docGrid, SWT.NONE);
			item.setHeaderText(String.valueOf(i+1));
			item.setText(0, doc.title);
			for (int pc = 0; pc < result.getEigenvalues().length; pc++) {
				item.setText(pc+1, nf.format(result.getColumnPrincipalComponents()[i][pc]));
			}
		}
		docGrid.getColumn(0).pack();
		
		
		// Input Data
		TabItem inputItem = new TabItem(tabFolder, SWT.NONE);
		inputItem.setText("Input Data");
		Grid inputGrid = createInputDataGrid(words, docs);
		inputItem.setControl(inputGrid);
	}
	
	
	private void createResult(ResultCA result) {
		tabFolder = new TabFolder(this, SWT.BORDER);
		
		TabItem summaryItem = new TabItem(tabFolder, SWT.NONE);
		summaryItem.setText("Summary");
		
		Composite comp = new Composite(tabFolder, SWT.EMBEDDED);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));
		summaryItem.setControl(comp);
		
		Label lblInfo = new Label(comp, SWT.NONE);
		lblInfo.setText("Input Table: \nTrace: ");
		
		Table summary = new Table(comp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		summary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		summary.setHeaderVisible(true);
		summary.setLinesVisible(true);
		CopyPasteHelper.listenTo(summary);
		
		TableColumn tblclmnDim = new TableColumn(summary, SWT.RIGHT);
		tblclmnDim.setText("No. of Dims");
		tblclmnDim.setWidth(60);
		
		TableColumn tblclmnLambda = new TableColumn(summary, SWT.RIGHT);
		tblclmnLambda.setText("Lambda");
		tblclmnLambda.setToolTipText("Lambda (Eigenvalues)");
		tblclmnLambda.setWidth(80);
		
		TableColumn tblclmnRate = new TableColumn(summary, SWT.RIGHT);
		tblclmnRate.setText("Rate");
		tblclmnRate.setToolTipText("Rates of Inertia");
		tblclmnRate.setWidth(80);
		
		TableColumn tblclmnCumulative = new TableColumn(summary, SWT.RIGHT);
		tblclmnCumulative.setText("Cumulative");
		tblclmnCumulative.setWidth(80);
		
		final NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		
		final NumberFormat pf = NumberFormat.getPercentInstance();
		pf.setMinimumFractionDigits(4);
		pf.setMaximumFractionDigits(4);
		
		// Summary
		StringBuilder info = new StringBuilder();
		info.append("Input Table (Rows x Columns): ")
			.append(result.getRowDimension() + " x " + result.getColumnDimension())
			.append(" (Words x Documents)\n")
			.append("Trace: " + nf.format(result.getTrace()));
		lblInfo.setText(info.toString());
		info.setLength(0);
		for (int i = 0; i < result.getRatesOfInertia().length; i++) 
		{
			if (i == result.getMaxDimension()) break;
			
			TableItem item = new TableItem(summary, SWT.NONE);
			item.setText(0, String.valueOf(i+1));
			item.setText(1, nf.format(result.getLambda()[i]));
			item.setText(2, pf.format(result.getRatesOfInertia()[i]));
			item.setText(3, pf.format(result.getCumulativeInertia()[i]));
		}
		
		// Words
		TabItem wordItem = new TabItem(tabFolder, SWT.NONE);
		wordItem.setText("Word");
		Grid wordGrid = createCAGrid(tabFolder, "Word", result.getMaxDimension());
		wordItem.setControl(wordGrid);
		
		Word[] words = result.getWords();
		for (int i = 0; i < words.length; i++) {
			Word word = words[i];
			GridItem item = new GridItem(wordGrid, SWT.NONE);
			item.setHeaderText(String.valueOf(i+1));
			item.setText(0, word.getWord());
			item.setText(1, nf.format(result.getRowQuality()[i]));
			item.setText(2, nf.format(result.getRowMass()[i]));
			item.setText(3, nf.format(result.getRowInertia()[i]));
			
			for (int k = 1; k <= result.getMaxDimension(); k++) {
				item.setText(3*k+1, nf.format(result.getRowProjections()[i][k]));
				item.setText(3*k+2, nf.format(result.getRowCorrelations()[i][k]));
				item.setText(3*k+3, nf.format(result.getRowContributions()[i][k]));
			}
		}
		
		
		// Documents
		TabItem docItem = new TabItem(tabFolder, SWT.NONE);
		docItem.setText("Document");
		Grid docGrid = createCAGrid(tabFolder, "Document", result.getMaxDimension());
		docItem.setControl(docGrid);
		
		ConciseDocument[] docs = result.getDocs();
		for (int i = 0; i < docs.length; i++) {
			ConciseDocument doc = docs[i];
			GridItem item = new GridItem(docGrid, SWT.NONE);
			item.setHeaderText(String.valueOf(i+1));
			item.setText(0, doc.title);
			item.setText(1, nf.format(result.getColumnQuality()[i]));
			item.setText(2, nf.format(result.getColumnMass()[i]));
			item.setText(3, nf.format(result.getColumnInertia()[i]));
			
			for (int k = 1; k <= result.getMaxDimension(); k++) {
				item.setText(3*k+1, nf.format(result.getColumnProjections()[i][k]));
				item.setText(3*k+2, nf.format(result.getColumnCorrelations()[i][k]));
				item.setText(3*k+3, nf.format(result.getColumnContributions()[i][k]));
			}
		}
		docGrid.getColumn(0).pack();
		
		
		// Input Data
		TabItem inputItem = new TabItem(tabFolder, SWT.NONE);
		inputItem.setText("Input Data");
		Grid inputGrid = createInputDataGrid(words, docs);
		inputItem.setControl(inputGrid);
	}
	
	
	private Grid createPCAGrid(TabFolder tabFolder, String label, int nPC) {
		Grid grid = new Grid(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grid.setHeaderVisible(true);
		grid.setRowHeaderVisible(true);
		//grid.setCellSelectionEnabled(true);
		CopyPasteHelper.listenTo(grid);
		
		GridColumn gridColumn = new GridColumn(grid, SWT.NONE);
		gridColumn.setText(label);
		gridColumn.setWidth(120);
		
		for (int i = 0; i < nPC; i++) {
			GridColumn col = new GridColumn(grid, SWT.RIGHT);
			col.setText("PC" + (i+1));
			col.setWidth(90);
		}
		
		return grid;
	}
	
	
	private Grid createCAGrid(TabFolder tabFolder, String label, int nDimension) {
		Grid grid = new Grid(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grid.setHeaderVisible(true);
		grid.setRowHeaderVisible(true);
		//grid.setCellSelectionEnabled(true);
		CopyPasteHelper.listenTo(grid);
		
		GridColumn gridColumn = new GridColumn(grid, SWT.NONE);
		gridColumn.setText(label);
		gridColumn.setWidth(120);
		
		GridColumn colQuality = new GridColumn(grid, SWT.RIGHT);
		colQuality.setText("Quality");
		colQuality.setWidth(90);
		
		GridColumn colMass = new GridColumn(grid, SWT.RIGHT);
		colMass.setText("Mass");
		colMass.setWidth(90);
		
		GridColumn colInertia = new GridColumn(grid, SWT.RIGHT);
		colInertia.setText("Relative Inertia");
		colInertia.setWidth(110);
		
		for (int i = 0; i < nDimension; i++) {
			GridColumnGroup grp = new GridColumnGroup(grid, SWT.NONE);
			grp.setExpanded(i < 3);
			grp.setText("Dimension " + (i+1) );
			GridColumn c1 = new GridColumn(grp, SWT.RIGHT);
			c1.setText("Projection");
			c1.setWidth(90);
			GridColumn c2 = new GridColumn(grp, SWT.RIGHT);
			c2.setText("Correlation");
			c2.setWidth(90);
			GridColumn c3 = new GridColumn(grp, SWT.RIGHT);
			c3.setText("Contribution");
			c3.setWidth(100);
		}
		
		return grid;
	}
	
	

	private Grid createInputDataGrid(Word[] words, ConciseDocument[] docs) {
		Grid grid = new Grid(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grid.setHeaderVisible(true);
		grid.setRowHeaderVisible(true);
		grid.setCellSelectionEnabled(true);
		CopyPasteHelper.listenTo(grid);
		
		for (int j = 0; j < docs.length; j++) {
			GridColumn col = new GridColumn(grid, SWT.RIGHT);
			col.setText(docs[j].title);
			col.setWidth(90);
			col.setHeaderWordWrap(true);
		}
		
		for (int i = 0; i < words.length; i++) {
			GridItem item = new GridItem(grid, SWT.NONE);
			item.setHeaderText(words[i].getWord());
			try {
				Map<ConciseDocument, Integer> freqMap = WordUtils.wordFreqByDocs(Concise.getCurrentWorkspace(), words[i].getWord(), Arrays.asList(docs));
				for (int j = 0; j < docs.length; j++) {
					item.setText(j, NumberFormat.getIntegerInstance().format(freqMap.get(docs[j])));
				}
				
			} catch (Exception e) {
				Concise.getCurrentWorkspace().logError(Gear.ScatterPlotter, e);
				Dialog.showException(e);
			}
		}
		
		return grid;
	}
	

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
