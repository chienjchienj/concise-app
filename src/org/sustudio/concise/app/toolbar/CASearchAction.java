package org.sustudio.concise.app.toolbar;

import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.gear.ScatterPlotter;
import org.sustudio.concise.app.gear.WordTrender;
import org.sustudio.concise.app.query.CAQuery;
import org.sustudio.concise.app.thread.CAClusterThread;
import org.sustudio.concise.app.thread.CollocationThread;
import org.sustudio.concise.app.thread.CAConcThread;
import org.sustudio.concise.app.thread.CAKeywordThread;
import org.sustudio.concise.app.thread.CAThread;
import org.sustudio.concise.app.thread.WordListerThread;
import org.sustudio.concise.app.thread.CollocationalNetworkThread;
import org.sustudio.concise.app.thread.PlotterThread;

public class CASearchAction {
	
	public static void doIt(final CAQuery query) {
		
		CAThread thread = null;
		switch (query.getGear()) {
		case Collocator:	
			thread = new CollocationThread(query);			break;
		case Concordancer:	
			thread = new CAConcThread(query);				break;
		case WordCluster:	
			thread = new CAClusterThread(query);			break;
		case WordLister:	
			thread = new WordListerThread(query);			break;
		case WordClouder:	
			thread = new WordListerThread(query);			break;
		case KeywordLister:	
			thread = new CAKeywordThread(query);			break;
		case CollocationalNetworker:	
			thread = new CollocationalNetworkThread(query);	break;
		case ConcordancePlotter:
			thread = new PlotterThread(query);				break;
		case WordTrender:
			((WordTrender) query.getGear().getController(Concise.getCurrentWorkspace()))
			.doit(query);
			break;
		case ScatterPlotter:
			((ScatterPlotter) query.getGear().getController(Concise.getCurrentWorkspace()))
			.doit(query);
			break;
		default:			break;
		}
		
		if (thread != null)
			thread.start();
	}
	
}
