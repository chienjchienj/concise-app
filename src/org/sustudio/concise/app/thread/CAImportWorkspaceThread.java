package org.sustudio.concise.app.thread;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.mihalis.opal.opalDialog.Dialog;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.DocumentIterator;
import org.sustudio.concise.core.corpus.DocumentWriter;
import org.sustudio.concise.core.corpus.importer.ConciseFileUtils;

public class CAImportWorkspaceThread extends ConciseThread {

	private final Workspace sourceWorkspace;
	private ConciseDocument[] documents;
	
	public CAImportWorkspaceThread(Gear gear, Workspace source, ConciseDocument[] documents) {
		super(gear, new CAQuery(gear));
		
		this.sourceWorkspace = source;
		this.documents = documents;
		
		gear.open(Concise.getCurrentWorkspace());
	}

	@Override
	public void running() {
		try {
			Workspace workspace = Concise.getCurrentWorkspace();
			IndexReader reader = workspace.getIndexReaderRef();
			File indexDir = workspace.getIndexDirRef();
			if (Gear.CorpusManager.equals(gear)) {
				reader = workspace.getIndexReader();
				indexDir = workspace.getIndexDir();
			}
			
			// load existing files to check duplication
			// using MD5 to check 
			ArrayList<String> existingMD5s = new ArrayList<String>();
			try {
				DocumentIterator iter = new DocumentIterator(Concise.getCurrentWorkspace(), reader);
				for (ConciseDocument cd : iter) {
					String md5 = ConciseFileUtils.getMD5(cd.documentFile);
					existingMD5s.add(md5);
				}
			} catch (IndexNotFoundException idxNotFoundException) {
				// empty index folder,
				// do nothing
			}
			
			// test file existance
			ArrayList<ConciseDocument> docs = new ArrayList<ConciseDocument>();
			for (ConciseDocument cd : documents) {
				String md5 = ConciseFileUtils.getMD5(cd.documentFile);
				if (!existingMD5s.contains(md5)) {
					docs.add(cd);
				}
				else
					System.out.println(cd.toString());
			}
			
			DocumentWriter writer = new DocumentWriter(workspace, indexDir);
			writer.addConciseDocuments(docs);
			writer.close();
			docs.clear();
			docs = null;
			existingMD5s.clear();
			
			
			// 同時也要匯入分詞的詞典，免得要進行 re-tokenize
			// - 先讀取自己的詞典，然後比對看看有沒有重複
			for (File dic : workspace.getDictionaryFiles()) {
				existingMD5s.add(ConciseFileUtils.getMD5(dic));
			}
			for (File dic : sourceWorkspace.getDictionaryFiles()) {
				String md5 = ConciseFileUtils.getMD5(dic);
				if (!existingMD5s.contains(md5)) {
					// copy dic file
					FileUtils.copyFileToDirectory(dic, workspace.getDictionaryDir());
				}
			}
			
			sourceWorkspace.close();
			existingMD5s.clear();
			existingMD5s = null;
			
			if (Gear.CorpusManager.equals(gear))
				workspace.reopenIndexReader();
			else
				workspace.reopenIndexReaderRef();
			
			
		} catch (Exception e) {
			Concise.getCurrentWorkspace().logError(gear, e);
			Dialog.showException(e);
		}
		
	}

}
