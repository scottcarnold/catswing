package org.xandercat.swing.animate;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/**
 * SpinnerPainter is a RotatingIconPainter that paints an icon which appears as a set of 
 * spinning "chaser" dots when animated.
 * 
 * @author Scott C Arnold
 */
public class SpinnerPainter implements RotatingIconPainter {

	private int spinnerDiameter;
	private int spinnerDots;
	private double dotRadius;
	
	public SpinnerPainter(int spinnerDiameter, int spinnerDots, double dotRadius) {
		this.spinnerDiameter = spinnerDiameter;
		this.spinnerDots = spinnerDots;
		this.dotRadius = dotRadius;
	}
	public int getHeight() {
		return spinnerDiameter;
	}
	public int getWidth() {
		return spinnerDiameter;
	}
	public void paint(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		double spinnerRadius = spinnerDiameter / 2d;
		double paintCircleRadius = spinnerRadius - dotRadius;
		double p = Math.PI*2d / (double)spinnerDots;
		double c = 0;
		for (int i=0; i<spinnerDots; i++) {
			Color color = new Color(0f, 0f, 0f, (i+1)*1f/(float)spinnerDots);
			g2d.setColor(color);
			double x = spinnerRadius + paintCircleRadius * Math.cos(c) - dotRadius;
			double y = spinnerRadius + paintCircleRadius * Math.sin(c) - dotRadius;
			Ellipse2D dot = new Ellipse2D.Double(x, y, dotRadius*2, dotRadius*2);
			g2d.fill(dot);
			c += p;
		}
	}
}
