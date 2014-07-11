package org.sustudio.concise.app.gear;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.app.Workspace;
import org.sustudio.concise.app.enums.CABox;
import org.sustudio.concise.app.gear.collocationalNetworker.CollocationalNetworker;
import org.sustudio.concise.app.gear.concordancePlotter.ConcordancePlotter;
import org.sustudio.concise.app.gear.lemmaEditor.LemmaEditor;
import org.sustudio.concise.app.gear.scatterPlotter.ScatterPlotter;
import org.sustudio.concise.app.gear.wordClouder.WordClouder;
import org.sustudio.concise.app.gear.wordTrender.WordTrender;

public enum Gear {

	/** Gear for Corpus Manager */
	CorpusManager(
			"Corpus Manager",
			"/org/sustudio/concise/app/icon/33-cabinet.png"),
	
	/** Gear for Reference Corpus Manager */
	ReferenceCorpusManager(
			"Reference Corpus Manager",
			"/org/sustudio/concise/app/icon/18-envelope.png"),
	
	/** Gear for Concordancer */
	Concordancer(
			"Concordancer",
			"/org/sustudio/concise/app/icon/97-puzzle.png"),
	
	/** Gear for Collocator */
	Collocator(
			"Collocator",
			"/org/sustudio/concise/app/icon/99-umbrella.png"),
	
	/** Gear for Word Cluster */
	WordCluster(
			"Word Cluster",
			"/org/sustudio/concise/app/icon/98-palette.png"),
	
	/** Gear for Word Lister */
	WordLister(
			"Word Lister",
			"/org/sustudio/concise/app/icon/106-sliders.png"),
	
	/** Gear for Concordance Plotter */
	ConcordancePlotter(
			"Concordance Plotter",
			"/org/sustudio/concise/app/icon/circle-stack.png"),
	
	/** Gear for Keyword Lister */
	KeywordLister(
			"Keyword Lister",
			"/org/sustudio/concise/app/icon/30-key.png"),
	
	/** Gear for Word Clouder */
	WordClouder(
			"Word Clouder",
			"/org/sustudio/concise/app/icon/56-cloud.png"),
	
	/** Gear for Collocational Networker */
	CollocationalNetworker(
			"Collocational Networker",
			"/org/sustudio/concise/app/icon/55-network.png"),
			
	/** Gear for Word Trender */
	WordTrender(
			"Word Trender",
			"/org/sustudio/concise/app/icon/77-ekg.png"),
			
	/** Gear for Scatter Plotter */
	ScatterPlotter(
			"Scatter Plotter",
			"/org/sustudio/concise/app/icon/scatter.png"),
	
	/** Gear for File Viewer */
	DocumentViewer(
			"Document Viewer",
			"/org/sustudio/concise/app/icon/68-paperclip.png"),
	
	/** Gear for Stop Worder */
	StopWorder(
			"Stop Worder",
			"/org/sustudio/concise/app/icon/21-skull.png"),
	
	/** Gear for Lemma Editor */
	LemmaEditor(
			"Lemma Editor",
			"/org/sustudio/concise/app/icon/123-id-card.png");
	
	
	private final String label;
	private final Image image;
	private final HashMap<Workspace, GearController> gearViewMap = new HashMap<Workspace, GearController>();
	
	Gear(final String label, final String imageClasspath) {
		this.label = label;
		this.image = SWTResourceManager.getImage(getClass(), imageClasspath);
	}
	
	public String label() {
		return label;
	}
	
	public Image image() {
		return image;
	}
	
	public void setGearView(Workspace workspace, GearController gearView) {
		if (gearView == null) {
			gearViewMap.remove(workspace);
		} else {
			gearViewMap.put(workspace, gearView);
		}
	}
	
	public GearController getController(Workspace workspace) {
		GearController controller = gearViewMap.get(workspace);
		if (controller == null) {
			switch (this) {
			case CollocationalNetworker:	controller = new CollocationalNetworker();	break;
			case Collocator:				controller = new Collocator();				break;
			case ConcordancePlotter:		controller = new ConcordancePlotter();		break;
			case Concordancer:				controller = new Concordancer();			break;
			case CorpusManager:				controller = new CorpusManager();			break;
			case DocumentViewer:			controller = new DocumentViewer();			break;
			case KeywordLister:				controller = new KeywordLister();			break;
			case LemmaEditor:				controller = new LemmaEditor();				break;
			case ReferenceCorpusManager:	controller = new ReferenceCorpusManager();	break;
			//case SearchWorder:				controller = new SearchWorder();			break;
			case StopWorder:				controller = new StopWorder();				break;
			case WordClouder:				controller = new WordClouder();				break;
			case WordCluster:				controller = new WordCluster();				break;
			case WordLister:				controller = new WordLister();				break;
			case WordTrender:				controller = new WordTrender();				break;
			case ScatterPlotter:			controller = new ScatterPlotter();			break;
			default:	break;
			}
			
			if (controller != null) {
				gearViewMap.put(workspace, controller);
				CTabItem item = new CTabItem(controller.getBox().getBoxView(workspace), SWT.CLOSE);
				item.setText(this.label());
				item.setImage(this.image());
				item.setControl(controller);
			}
		}
		
		return controller;
	}
	
	public GearController open(Workspace workspace) {
		GearController gearView = gearViewMap.get(workspace);
		if (gearView == null) {
			gearView = getController(workspace);
		}
		
		if (gearView != null) {
			gearView.getBox().getBoxView(workspace).setSelection(this);
			if (gearView.getBox().equals(CABox.ToolBox)) {
				// show toolbox
				CTabFolder toolBox = CABox.ToolBox.getBoxView(workspace);
				SashForm container = (SashForm) toolBox.getParent();
				container.setMaximizedControl(null);
			}
		}
		
		return gearView;
	}
	
	public boolean isVisible(Workspace workspace) {
		GearController gearView = gearViewMap.get(workspace);
		if (gearView == null) {
			return false;
		}
		return gearView.isVisible();
	}
	
	
	///////////////////////////////////////////////////
	// Static methods
	///////////////////////////////////////////////////
	
	private static Gear activeGear;
	
	/**
	 * Returns active gear, or null if there isn't any active one.
	 * @return active gear, or null
	 */
	public static Gear getActiveGear() {
		if (activeGear.getController(Concise.getCurrentWorkspace()) == null || 
			activeGear.getController(Concise.getCurrentWorkspace()).isDisposed()) 
		{
			activeGear = null;
		}
		return activeGear;
	}
	
	/**
	 * Set active gear
	 * @param activeGear
	 */
	public static void setActiveGear(Gear activeGear) {
		Gear.activeGear = activeGear;
	}
	
	/**
	 * Returns visible gears (max 2 gears, one in GearBox, and the other in ToolBox)
	 * @return
	 */
	public static Gear[] getVisibleGears() {
		ArrayList<Gear> gears = new ArrayList<Gear>();
		for (Gear g : Gear.values()) {
			if (g.isVisible(Concise.getCurrentWorkspace())) {
				gears.add(g);
			}
		}
		return gears.toArray(new Gear[0]);
	}
	
}
