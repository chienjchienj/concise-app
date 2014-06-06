package org.sustudio.concise.app.thread;

import java.io.IOException;

import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentWriter;

public class CAImportWorkspaceThread extends CAThread {

	private final Workspace sourceWorkspace;
	private ConciseDocument[] documents;
	
	public CAImportWorkspaceThread(Workspace source, ConciseDocument[] documents) {
		super(Gear.ReferenceCorpusManager, new CAQuery(Gear.ReferenceCorpusManager));
		this.sourceWorkspace = source;
		this.documents = documents;
		
		gear.open(Concise.getCurrentWorkspace());
	}

	@Override
	public void running() {
		try {
			
			Concise.getCurrentWorkspace().closeIndexReaderRef();
			
			DocumentWriter writer = new DocumentWriter(Concise.getCurrentWorkspace(), Concise.getCurrentWorkspace().getIndexDirRef());
			writer.addConciseDocuments(documents);
			writer.close();
			sourceWorkspace.close();
			
			Concise.getCurrentWorkspace().reopenIndexReaderRef();
			
			
		} catch (IOException e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
		
	}

}
