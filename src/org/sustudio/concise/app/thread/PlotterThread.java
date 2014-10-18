package org.sustudio.concise.app.thread;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.collocation.ConciseTokenAnalyzer;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.corpus.importer.ConciseField;
import org.sustudio.concise.core.highlighter.DocumentHighlighter;

public class PlotterThread extends ConciseThread {

	public PlotterThread(CAQuery query) {
		super(Gear.ConcordancePlotter, query);
		
	}

	@Override
	public void running() {
		
		try {
			
			dialog.setStatus("checking...");
			SQLiteDB.dropTableIfExists(CATable.ConcordancePlotter);
			SQLiteDB.createTableIfNotExists(CATable.ConcordancePlotter);
			PreparedStatement ps = SQLiteDB.prepareStatement(CATable.ConcordancePlotter);
			
			dialog.setStatus("concordancing...");
			Workspace workspace = Concise.getCurrentWorkspace();
			Conc conc = new Conc(workspace, 
								 query.searchStr, 
								 CAPrefs.SHOW_PART_OF_SPEECH);
			
			final String preTag = "<>";
			final String postTag = "</>";
			for (ScoreDoc scoreDoc : conc.hitDocs()) 
			{
				if (isInterrupted()) {
					break;
				}
				
				final int docID = scoreDoc.doc;
				Document doc = conc.searcher.doc(docID);
				DocumentHighlighter highlighter = new DocumentHighlighter(workspace, conc.getQuery(),
																		  docID, 
																		  new String[] { preTag }, 
																		  new String[] { postTag}, 
																		  CAPrefs.SHOW_PART_OF_SPEECH) 
				{
					public Analyzer getAnalyzer() {
						return new ConciseTokenAnalyzer(CAPrefs.SHOW_PART_OF_SPEECH);
					}
				};
				String content = highlighter.getHighlightText();
				content = content.replace(" "+postTag, postTag+" ");
				StringBuilder pos = new StringBuilder();
				
				long wordsCount = 0;
				long hits = 0;
				StringTokenizer st = new StringTokenizer(content, " ");
				StringBuilder wordBuilder = new StringBuilder();
				while (st.hasMoreTokens()) {
					String word = st.nextToken();
					if (word.startsWith(preTag)) {
						wordBuilder.append(word.replace(preTag, "").replace(postTag, ""));
					}
					if (wordBuilder.length() > 0 && 
						!word.startsWith(preTag) && 
						!word.endsWith(postTag)) 
					{
						wordBuilder.append(" " + word);
						continue;
					}
					if (word.endsWith(postTag)) {
						if (pos.length() > 0)
							pos.append('\t');
						if (!word.startsWith(preTag)) {
							wordBuilder.append(" " + word.replace(postTag, ""));
						}
						
						wordBuilder.append(":" + wordsCount);
						pos.append(wordBuilder);
						wordBuilder.setLength(0);
						hits++;
					}
					if (wordBuilder.length() == 0)
						wordsCount++;
				}
				
				File file = new File(doc.get(ConciseField.FILENAME.field()));
				String filepath = CAPrefs.SHOW_FULL_FILEPATH ? file.getCanonicalPath() : file.getName();
				
				ps.setInt	(1, docID);
				ps.setString(2, pos.toString());
				ps.setLong	(3, hits);
				ps.setLong	(4, wordsCount);
				ps.setDouble(5, (double) hits * 1000d / (double)wordsCount);
				ps.setString(6, filepath);
				ps.addBatch();
				
				pos.setLength(0);
			}
			SQLiteDB.executeBatch(ps);
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
		
	}

}
