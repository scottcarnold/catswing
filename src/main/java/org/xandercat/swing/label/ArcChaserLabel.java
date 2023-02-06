package org.xandercat.swing.label;

import java.awt.Color;

import org.xandercat.swing.animate.ArcChaserPainter;
import org.xandercat.swing.animate.ArcChaserProperties;

/**
 * JLabel with an "arc chaser" rotating icon.
 * 
 * @author Scott Arnold
 */
public class ArcChaserLabel extends RotatingIconLabel {

	private static final long serialVersionUID = 2010080301L;
	
	public ArcChaserLabel(String label, int delay, int spinnerDiameter, ArcChaserProperties acProps) {
		super(label, new ArcChaserPainter(spinnerDiameter, acProps, 2), acProps.chasers*acProps.segmentsPerChaser, delay);
	}
	
	public ArcChaserLabel(String label, int delay, int spinnerDiameter, ArcChaserProperties acProps, int renderScale) {
		super(label, new ArcChaserPainter(spinnerDiameter, acProps, renderScale), acProps.chasers*acProps.segmentsPerChaser, delay);
	}
	
	public ArcChaserLabel(String label, int delay, int spinnerDiameter, int chasers, int segmentsPerChaser) {
		super(label, 
				new ArcChaserPainter(spinnerDiameter, chasers, segmentsPerChaser), 
				chasers*segmentsPerChaser, 
				delay);
	}
	
	public ArcChaserLabel(String label, int delay, int spinnerDiameter, int chasers, int segmentsPerChaser, float strokeSize) {
		super(label, 
				new ArcChaserPainter(spinnerDiameter, chasers, segmentsPerChaser, strokeSize), 
				chasers*segmentsPerChaser, 
				delay);
	}
	
	public ArcChaserLabel(String label, int delay, int spinnerDiameter, int chasers, int segmentsPerChaser, float strokeSize, Color chaserColor) {
		super(label, 
				new ArcChaserPainter(spinnerDiameter, chasers, segmentsPerChaser, strokeSize, chaserColor), 
				chasers*segmentsPerChaser, 
				delay);
	}
}
