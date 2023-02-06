package org.xandercat.swing.animate;

import java.awt.Color;
import java.awt.geom.Arc2D;

/**
 * Simple properties holder for the chasers of an ArcChaserLabel or any other component
 * utilizing an ArcChaserPainter.
 * 
 * For maximum simplicity, properties have public access and can be modified directly.
 * closureType should be one of the closure type values of the Arc2D class.
 * 
 * @author Scott Arnold
 */
public class ArcChaserProperties {
	public int chasers = 1;
	public int segmentsPerChaser = 16;
	public float strokeSize = 3f;
	public Color chaserColor = Color.BLACK;
	public int closureType = Arc2D.OPEN;
}
