package org.sustudio.concise.app.enums;

import org.apache.lucene.index.IndexReader;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.Workspace;
import org.sustudio.concise.app.dialog.ImportDialog;
import org.sustudio.concise.app.dialog.ImportWorkspaceDialog;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.resources.CABundle;
import org.sustudio.concise.app.thread.CADeleteDocumentThread;
import org.sustudio.concise.core.ConciseFile;

public enum CorpusManipulation {
	
	ImportDocuments						(Workspace.INDEX.DOCUMENT,	CABundle.get("corpus.importDocs")),
	ImportTokenizedDocuments			(Workspace.INDEX.DOCUMENT,	CABundle.get("corpus.importTokenizedDocs")),
	ImportDocumentsFromWorkspace		(Workspace.INDEX.DOCUMENT,	"Documents From Other Workspace"),
	ClearDocuments						(Workspace.INDEX.DOCUMENT,	CABundle.get("corpus.clearDocs")),
	
	ImportReferenceDocuments			(Workspace.INDEX.REFERENCE,	CABundle.get("corpus.importReferenceDocs")),
	ImportTokenizedReferenceDocuments	(Workspace.INDEX.REFERENCE,	CABundle.get("corpus.importTokenizedReferenceDocs")),
	ImportReferenceFromWorkspace		(Workspace.INDEX.REFERENCE, "Reference Documents From Other Workspace"),
	ClearReferenceDocuments				(Workspace.INDEX.REFERENCE,	CABundle.get("corpus.clearReferenceDocs")),
	
	RetokenizeDocuments					(Workspace.INDEX.DOCUMENT, CABundle.get("corpus.reTokenizeDocuments")),
	;
	
	private final Workspace workspace = Concise.getCurrentWorkspace();
	private final Workspace.INDEX index;
	private final String label;
	
	CorpusManipulation(Workspace.INDEX index, String label) {
		this.index = index;
		this.label = label;
	}
	
	public String label() {
		return label;
	}
	
	public Workspace.INDEX INDEX() {
		return index;
	}
	
	public ConciseFile indexDir() {
		return workspace.getIndexDir(index);		
	}
	
	public IndexReader indexReader() throws Exception {
		return workspace.getIndexReader(index);
	}
	
	public boolean isTokenized() {
		switch (this) {
		case ImportTokenizedDocuments:
		case ImportTokenizedReferenceDocuments:
			return true;
		default:
			return false;
		}
	}
	
	public Gear manager() {
		switch (index) {
		case REFERENCE:
			return Gear.ReferenceCorpusManager;
		case DOCUMENT:
		default:
			return Gear.CorpusManager;
		}
	}
	
	
	/**
	 * 傳回 {@link SelectionAdpater#widgetSelected} 的動作
	 * @return
	 */
	public SelectionAdapter selectionAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				switch(CorpusManipulation.this) {
				case ClearDocuments:
					if (Dialog.isConfirmed(
							Concise.getActiveApp(),
							"Do you want to reset corpus?", 
							"This will remove all corpus from concise index.")) 
					{
						CADeleteDocumentThread deleteThread = new CADeleteDocumentThread(manager());
						deleteThread.setDeleteAll(true);
						deleteThread.start();
					}
					break;
					
				case ClearReferenceDocuments:
					if (Dialog.isConfirmed(
							Concise.getActiveApp(),
							"Do you want to reset reference corpus?", 
							"This wil remove all reference corpus from concise index.")) 
					{
						CADeleteDocumentThread deleteThread = new CADeleteDocumentThread(manager());
						deleteThread.setDeleteAll(true);
						deleteThread.start();
					}
					break;
					
				case ImportDocuments:
					ImportDialog.open(Workspace.INDEX.DOCUMENT);
					break;
					
				case ImportReferenceDocuments:
					ImportDialog.open(Workspace.INDEX.REFERENCE);
					break;
					
				case ImportDocumentsFromWorkspace:
					ImportWorkspaceDialog.openDialog(Gear.CorpusManager);
					break;
					
				case ImportReferenceFromWorkspace:
					ImportWorkspaceDialog.openDialog(Gear.ReferenceCorpusManager);
					break;
				
				default:
					break;
				}
			}
		};
	}
}
