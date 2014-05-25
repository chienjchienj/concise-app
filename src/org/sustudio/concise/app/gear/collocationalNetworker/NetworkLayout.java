package org.sustudio.concise.app.gear.collocationalNetworker;

import org.eclipse.gef4.layout.LayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.GridLayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.RadialLayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.SpringLayoutAlgorithm;
import org.eclipse.gef4.layout.algorithms.TreeLayoutAlgorithm;

/**
 * Network Layout
 * 
 * @author Kuan-ming Su
 *
 */
public enum NetworkLayout {
	
	Dynamic("Dynamic Spring Layout"),
	Spring("Spring Layout"),
	Grid("Grid Layout"),
	Radial("Radical Layout"),
	Tree("Tree Layout"),
	TreeHorizontal("Horizontal Tree Layout"),
	;
	
	private final String label;
	
	NetworkLayout(final String label) {
		this.label = label;
	}
	
	public String label() {
		return label;
	}
	
	public LayoutAlgorithm getAlgorithm() {
		switch (this) {
		case Dynamic:
		case Spring:				
			if (algorithm == null || !(algorithm instanceof SpringLayoutAlgorithm)) {
				algorithm = new SpringLayoutAlgorithm();
			}
			break;
			
		case Grid:
			if (algorithm == null || !(algorithm instanceof GridLayoutAlgorithm)) {
				algorithm = new GridLayoutAlgorithm();
			}
			break;
			
		case Tree:
			if (algorithm == null || 
				!(algorithm instanceof TreeLayoutAlgorithm) ||
				((TreeLayoutAlgorithm) algorithm).getDirection() != TreeLayoutAlgorithm.TOP_DOWN)
			{
				algorithm = new TreeLayoutAlgorithm(TreeLayoutAlgorithm.TOP_DOWN);
			}
			break;
			
		case TreeHorizontal:
			if (algorithm == null || 
				!(algorithm instanceof TreeLayoutAlgorithm) ||
				((TreeLayoutAlgorithm) algorithm).getDirection() != TreeLayoutAlgorithm.LEFT_RIGHT)
			{
				algorithm = new TreeLayoutAlgorithm(TreeLayoutAlgorithm.LEFT_RIGHT);
			}
			break;
			
		case Radial:			
		default:
			if (algorithm == null || !(algorithm instanceof RadialLayoutAlgorithm)) {
				algorithm = new RadialLayoutAlgorithm();
			}
			break;
		}
		return algorithm;
	}
	
	
	public static LayoutAlgorithm algorithm = null;
	
	public static String[] stringValues() {
		String[] strValues = new String[values().length];
		for (int i = 0; i < values().length; i++) {
			strValues[i] = values()[i].label();
		}
		return strValues;
	}
}