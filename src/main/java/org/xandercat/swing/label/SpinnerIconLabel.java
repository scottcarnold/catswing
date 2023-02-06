package org.xandercat.swing.label;

import org.xandercat.swing.animate.SpinnerPainter;

/**
 * SpinnerIconLabel is a RotatingIconLabel that uses a SpinnerPainter to paint the icon.
 * 
 * @author Scott C Arnold
 */
public class SpinnerIconLabel extends RotatingIconLabel {
	
	private static final long serialVersionUID = 2009022801L;
	
	public SpinnerIconLabel(String label, int delay, int spinnerDiameter, int spinnerDots, double dotRadius) {
		super(label, new SpinnerPainter(spinnerDiameter, spinnerDots, dotRadius), spinnerDots, delay);
	}
}
