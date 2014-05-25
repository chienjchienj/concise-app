package org.sustudio.concise.app.thread;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.DocumentWriter;

public class CAImportWorkspaceThread extends CAThread {

	private final Workspace sourceWorkspace;
	private Document[] documents;
	
	public CAImportWorkspaceThread(Workspace source, Document[] documents) {
		super(Gear.ReferenceCorpusManager, new CAQuery(Gear.ReferenceCorpusManager));
		this.sourceWorkspace = source;
		this.documents = documents;
		
		gear.open(Concise.getCurrentWorkspace());
	}

	@Override
	public void running() {
		try {
			
			Concise.getCurrentWorkspace().closeIndexReaderRef();
			
			IndexReader reader = sourceWorkspace.getIndexReader();
			DocumentWriter writer = new DocumentWriter(Concise.getCurrentWorkspace(), Concise.getCurrentWorkspace().getIndexDirRef());
			if (reader.maxDoc() == documents.length) {
				writer.addIndexes(reader);
			}
			else {
				for (Document doc : documents) {
					writer.addDocument(doc);
				}
			}
			writer.close();
			sourceWorkspace.close();
			
			Concise.getCurrentWorkspace().reopenIndexReaderRef();
			
			
		} catch (IOException e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
		
	}

}
