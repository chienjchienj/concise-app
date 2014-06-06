package org.sustudio.concise.app.thread;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.db.CATable;
import org.sustudio.concise.app.db.DBColumn;
import org.sustudio.concise.app.db.SQLiteDB;
import org.sustudio.concise.app.dialog.Dialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.app.preferences.CAPrefsUtils;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.query.CAQueryUtils;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.collocation.Collocate;
import org.sustudio.concise.core.collocation.CollocateIterator;
import org.sustudio.concise.core.collocation.SurfaceCollocateIterator;
import org.sustudio.concise.core.collocation.TextualCollocateIterator;
import org.sustudio.concise.core.collocation.TextualCollocateIterator.BOUNDARY;
import org.sustudio.concise.core.concordance.Conc;

public class CollocationalNetworkThread extends ConciseThread {

	public final boolean expandedSubNetwork;
	
	protected final Comparator<Collocate> comparator = new Comparator<Collocate>() {
		public int compare(Collocate c1, Collocate c2) {
			if (CAPrefs.NETWORK_COMPARATOR.getValue(c1) > CAPrefs.NETWORK_COMPARATOR.getValue(c2))
				return 1;
			if (CAPrefs.NETWORK_COMPARATOR.getValue(c1) < CAPrefs.NETWORK_COMPARATOR.getValue(c2))
				return -1;
			return 0;
		} };

		
	public CollocationalNetworkThread(final CAQuery query) {
		this(query, false);
	}
	
	public CollocationalNetworkThread(final CAQuery query, boolean expanded) {
		super(Gear.CollocationalNetworker, query);
		this.expandedSubNetwork = expanded;
		
		if (!expandedSubNetwork) {
			try {
				Concise.getCurrentWorkspace().logInfo(query.toString());
				CAQueryUtils.logQuery(query);
				
			} catch (Exception e) {
				Concise.getCurrentWorkspace().logError(gear, e);
				Dialog.showException(e);
			}
		}
	}
	
	protected void logQuery() {
		// disable default query log
	}
	
	
	private ArrayList<String> processedNodeWords = new ArrayList<String>();
	private LinkedList<WordAtDepth> wordsTodo = new LinkedList<WordAtDepth>();
	private int batchIndex = 0;
	private PreparedStatement ps;
	
	@Override
	public void running() 
	{
		try 
		{
			CAPrefsUtils.writePrefs();
			
			dialog.setStatus("configuring...");
			if (!expandedSubNetwork) {
				SQLiteDB.dropTableIfExists(CATable.CollocationalNetworker);
			}
			SQLiteDB.createTableIfNotExists(CATable.CollocationalNetworker);
			ps = SQLiteDB.prepareStatement(CATable.CollocationalNetworker);
			
			addRootCollocates();
			if (!expandedSubNetwork) {
				addCollocatesAtDepth();
			}
		
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
	}

	
	protected void done() {
		processedNodeWords.clear();
		processedNodeWords = null;
		wordsTodo.clear();
		wordsTodo = null;
		super.done();
	}
	
	
	protected CollocateIterator getCollocateIterator(Conc conc) throws Exception {
		CollocateIterator collocateIterator;
		switch (CAPrefs.COLLOCATION_MODE) {
		case SentenceTextual:
			collocateIterator = new TextualCollocateIterator(conc, BOUNDARY.SENTENCE, CAPrefs.NETWORK_FILTERS);
			break;
		case ParagraphTextual:
			collocateIterator = new TextualCollocateIterator(conc, BOUNDARY.PARAGRAPH, CAPrefs.NETWORK_FILTERS);
			break;
			
		case Surface:
		default:
			conc.setSpanSize(query.leftSpanSize, query.rightSpanSize);
			collocateIterator = new SurfaceCollocateIterator(conc, CAPrefs.NETWORK_FILTERS);
			break;
		}
		return collocateIterator;
	}
	
	
	private void addRootCollocates() throws Exception {
		PriorityQueue<Collocate> collocateQueue = new PriorityQueue<Collocate> (1, comparator);
		
		if (expandedSubNetwork) {
			String sql = "SELECT MAX(" + DBColumn.BatchIndex.columnName() + ") FROM " + CATable.CollocationalNetworker.name();
			ResultSet rs = SQLiteDB.executeQuery(sql);
			if (rs.next()) {
				batchIndex = rs.getInt(1);
				batchIndex++;
			}
			rs.close();
		}
		
		
		dialog.setStatus("concordancing " + query.searchStr + " ...");
		Workspace workspace = Concise.getCurrentWorkspace();
		Conc conc = new Conc(workspace, query.searchStr, CAPrefs.SHOW_PART_OF_SPEECH);
		
		dialog.setStatus("collocating ...");
		CollocateIterator collocateIterator = getCollocateIterator(conc);
		
		// node 另計
		ArrayList<Collocate> nodes = new ArrayList<Collocate>();
		for (Collocate collocate : collocateIterator) 
		{
			if (isInterrupted()) {
				break;
			}
			
			if (collocate.getNodeFreq() > 0) {
				nodes.add(collocate);
				
				// add to processed node words
				processedNodeWords.add(collocate.getWord());
				continue;
			}
			
			collocateQueue.offer(collocate);
			if (collocateQueue.size() > CAPrefs.TOP_COLLOCATES) {
				collocateQueue.poll();
			}
		}
		collocateIterator = null;
		conc = null;
		
		dialog.setStatus("output data...");
		collocateQueue.addAll(nodes);
		nodes.clear();
		nodes = null;
		
		final List<Collocate> list = new ArrayList<Collocate>();
		list.addAll(collocateQueue);
		
		// write to database
		writeToDatabase(list);
		
		Collocate collocate = null;
		while ( (collocate = collocateQueue.poll()) != null) {
			wordsTodo.add(new WordAtDepth(collocate.getWord(), 1));
		}
		collocateQueue.clear();
		collocateQueue = null;
	}
	
	
	private boolean wordExists(String word) 
	{
		if (processedNodeWords.contains(word)) 
			return true;
		for (WordAtDepth wordAtDepth : wordsTodo) {
			if (wordAtDepth.getWord().equals(word))
				return true;
		}
		return false;
	}
	
	
	private void addCollocatesAtDepth() throws Exception 
	{
		WordAtDepth wordAtDepth = null;
		while ( (wordAtDepth = wordsTodo.poll()) != null ) 
		{
			if (isInterrupted()) {
				break;
			}
			
			if (wordAtDepth.getDepth() < CAPrefs.NETWORK_DEPTH) 
			{
				batchIndex++;
				PriorityQueue<Collocate> collocateQueue = new PriorityQueue<Collocate> (1, comparator);
								
				String nodeWord = wordAtDepth.getWord();
				dialog.setStatus("concordancing " + nodeWord + " ...");
				Workspace workspace = Concise.getCurrentWorkspace();
				Conc conc = new Conc(workspace, wordAtDepth.getWord(), CAPrefs.SHOW_PART_OF_SPEECH);
				
				dialog.setStatus("collocating " + nodeWord + " ... (" + wordAtDepth.getDepth() + ")");
				Collocate node = null;
				CollocateIterator collocateIterator = getCollocateIterator(conc);
				for (Collocate collocate : collocateIterator) {
					if (isInterrupted()) {
						break;
					}
					
					if (collocate.getWord().equals(nodeWord)) {
						if (collocate.getNodeFreq() == 0) {
							collocate.setNodeFreq(collocate.getFreq());
						}
						node = collocate;
						processedNodeWords.add(nodeWord);
						continue;
					}
					
					collocateQueue.offer(collocate);
					if (collocateQueue.size() > CAPrefs.TOP_COLLOCATES) {
						collocateQueue.poll();
					}
				}
				
				if (node != null && !collocateQueue.isEmpty()) {
					final List<Collocate> list = new ArrayList<Collocate>();
					list.add(node);
					list.addAll(collocateQueue);
					
					// write to database
					writeToDatabase(list);
					
					Collocate collocate = null;
					while ( (collocate = collocateQueue.poll()) != null) {
						String word = collocate.getWord();
						if (!wordExists(word) && wordAtDepth.getDepth() + 1 < CAPrefs.NETWORK_DEPTH) 
						{
							wordsTodo.add(new WordAtDepth(word, wordAtDepth.getDepth()+1));
						}
					}
				}
				collocateQueue.clear();
				collocateQueue = null;
			}
		}
	}
	
	
	private void writeToDatabase(List<Collocate> collocateList) throws Exception {
		
		for (Collocate collocate : collocateList) {
			SQLiteDB.addCollocationalNetwork(ps, collocate, batchIndex);
		}
		SQLiteDB.executeBatch(ps);
		
	}
	
	
	private class WordAtDepth {
		
		public String word;
		public int depth;
		
		public WordAtDepth(String word, int depth) {
			this.word = word;
			this.depth = depth;
		}
		
		public String getWord() {
			return word;
		}
		
		public int getDepth() {
			return depth;
		}
		
	}
}
