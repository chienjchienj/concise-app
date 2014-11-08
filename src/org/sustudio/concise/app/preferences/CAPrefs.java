package org.sustudio.concise.app.preferences;


import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import org.sustudio.concise.app.enums.CollocationMode;
import org.sustudio.concise.app.enums.EffectSizeMeasurement;
import org.sustudio.concise.app.enums.KeynessMeasurement;
import org.sustudio.concise.app.enums.SignificanceMeasurement;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.collocationalNetworker.NetworkLayout;
import org.sustudio.concise.app.gear.scatterPlotter.ScatterPlotter.Analysis;
import org.sustudio.concise.app.gear.wordClouder.CloudAngle;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.collocation.CollocateMeasurement;

/**
 * Preferences of Concise App.
 * 
 * @author Kuan-ming Su
 *
 */
public class CAPrefs extends CCPrefs implements Serializable {
	
	/** UID */
	private static final long serialVersionUID = -4802421598254758709L;

	/** 記錄關閉前開啟的 gear */
	public static Gear[] OPENED_GEARS;
	
	/** 記錄關閉前正在使用的 gear */
	public static Gear LAST_ACTIVE_GEAR;
	
	
	/** 顯示詞性（part-of-speech）標籤 */
	public static boolean SHOW_PART_OF_SPEECH = false;
	
	
	///////////////////////////////////////////////////////////////////////
	// System Settings
	///////////////////////////////////////////////////////////////////////
	
	/** 預設的暫存資料夾 */
	public static File TEMPORARY_FOLDER = null;
	
	///////////////////////////////////////////////////////////////////////
	// General Settings
	///////////////////////////////////////////////////////////////////////

	/**
	 * Shows Full File path (true) or filename (false) only
	 */
	public static boolean SHOW_FULL_FILEPATH = false;
	
	/**
	 * Shows program icon when available
	 */
	public static boolean SHOW_PROGRAM_ICON = true;
	
	/**
	 * Display a number of Top Records each table
	 */
	public static int TOP_RECORDS = -1;
	
	public static Locale LOCALE = Locale.getDefault();
	
	
	///////////////////////////////////////////////////////////////////////
	// Highlight Color
	///////////////////////////////////////////////////////////////////////

	public static RGB[] HIGHLIGH_FG_COLOR_SCHEME = new RGB[] {
		new RGB(238, 19, 10),
		new RGB(9, 105, 239),
		new RGB(28, 160, 23),
		new RGB(255, 0, 206)
	};
	
	public static RGB[] HIGHLIGH_BG_COLOR_SCHEME = new RGB[] {
		new RGB(255, 0, 206),	
		new RGB(255, 220, 0), 
		new RGB(0, 255, 42)
	};
	
	
	///////////////////////////////////////////////////////////////////////
	// Measurement Settings
	///////////////////////////////////////////////////////////////////////
	
	public static CollocationMode COLLOCATION_MODE = CollocationMode.Surface;
	
	public static EffectSizeMeasurement EFFECT_SIZE = EffectSizeMeasurement.MI;
	
	public static SignificanceMeasurement SIGNIFICANCE = SignificanceMeasurement.Tscore;
	
	public static KeynessMeasurement KEYNESS = KeynessMeasurement.LogLikelihood;
	
	public static boolean SHOW_NEGATIVE_KEYWORDS = true;


	///////////////////////////////////////////////////////////////////////
	// Word Clouder Settings
	///////////////////////////////////////////////////////////////////////
	
	
	public static int CLOUDER_MAX_WORDS = 300;
	
	public static int CLOUDER_MAX_FONT_SIZE = 100;
	
	public static int CLOUDER_MIN_FONT_SIZE = 15;
	
	public static int CLOUDER_BOOST = 0;
	
	public static float CLOUDER_BOOST_FACTOR = 1;
	
	public static CloudAngle CLOUDER_ANGLES = CloudAngle.HorizontalOnly;
	
	public static int CLOUDER_X_VARIATION = 10;
	
	public static int CLOUDER_Y_VARIATION = 10;
	
	public static RGB[] CLOUDER_COLOR_SCHEME = new RGB[] {
		new RGB(255,46,0),
		new RGB(255,255,14), 
		new RGB(183, 183, 183), 
		new RGB(122, 122, 122),
		new RGB(81, 81, 81),
		new RGB(61, 61, 61),
		new RGB(165, 165, 165)
	};
	
	public static RGB CLOUDER_BACKGROUND_RGB = new RGB(0, 0, 0);
	
	public static RGB CLOUDER_SELECTION_RGB = new RGB(255, 0, 0);
	
	public static String[] CLOUDER_FONTS;
	
	///////////////////////////////////////////////////////////////////////
	// Collocational Network Settings
	///////////////////////////////////////////////////////////////////////
	
	/** Collocational Network Layout */
	public static NetworkLayout NETWORK_LAYOUT = NetworkLayout.Radial;

	/** Top collocates of collocational network */
	public static int TOP_COLLOCATES = 30;

	/** collocate filter type */
	public static CollocateMeasurement NETWORK_COMPARATOR = CollocateMeasurement.Cooccurrence;

	/** filters */
	public static Map<CollocateMeasurement, Double> NETWORK_FILTERS = new HashMap<CollocateMeasurement, Double>();

	/** Depth of Collocational Network */
	public static int NETWORK_DEPTH = 1;
	
	/** Max node size */
	public static int NETWORK_MAX_NODE_SIZE = 40;
	
	/** Min node size */
	public static int NETWORK_MIN_NODE_SIZE = 20;


	/** Network Color Scheme */ 
	public static RGB[] NETWORK_COLOR_SCHEME = new RGB[] {
		new RGB(0, 101, 133),	
		new RGB(255, 99, 49), 
		new RGB(175, 105, 134), 
		new RGB(108, 142, 0), 
		new RGB(237, 165, 62), 
		new RGB(117, 51, 66)
	};
	

	/** Network Label color */
	public static RGB NETWORK_LABEL_RGB = new RGB(51, 51, 51);
	
	/** Network background color */
	public static RGB NETWORK_BACKGROUND_RGB = new RGB(255, 255, 255);

	/** Network hide non-selected nodes */
	public static boolean NETWORK_HIDE_NON_SELECTED = true;
	
	
	
	///////////////////////////////////////////////////////////////////////
	// Scatter Plotter Settings
	///////////////////////////////////////////////////////////////////////
	
	public static Analysis SCATTER_PLOT_ANALYSIS = Analysis.CA;
	
	
	///////////////////////////////////////////////////////////////////////
	// DictionaryEditor Settings
	///////////////////////////////////////////////////////////////////////
	
	public static File DICTIONARY_WORKING_FILE = null;
}
