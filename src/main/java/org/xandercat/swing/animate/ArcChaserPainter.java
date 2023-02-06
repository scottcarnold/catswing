package org.xandercat.swing.animate;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * ChaserPainter is a RotatingIconPainter that paints an icon which appears as a set of 
 * spinning "chaser" segments when animated.  The chaser segments appear as segments of a 
 * circle.
 * 
 * renderScale is a multiplier for the image size at time of render that affects how the
 * final images look, and should have a value >= 1.  For example, at the default 
 * renderScale of 2, images are rendered at 2x the requested size, then scaled back down 
 * to the requested size once rendering is complete.  A value of 1 can provide a choppy
 * appearance due to the way arcs are rendered.  A larger value will reduce choppiness,
 * but anti-aliasing effect is reduced.  The default value of 2 works the best under
 * most situations, striking a balance between the choppiness of arc rendering and 
 * the smoothing effect of anti-aliasing.  
 * 
 * @author Scott C Arnold
 */
public class ArcChaserPainter implements RotatingIconPainter {

	private Color chaserColor;
	private int spinnerDiameter;
	private int chasers;
	private int segmentsPerChaser;
	private float strokeSize;
	private int renderScale;
	private int closureType;

	public ArcChaserPainter(int spinnerDiameter, int chasers, int segmentsPerChaser) {
		this(spinnerDiameter, chasers, segmentsPerChaser, 3f);
	}
	
	public ArcChaserPainter(int spinnerDiameter, int chasers, int segmentsPerChaser, float strokeSize) {
		this(spinnerDiameter, chasers, segmentsPerChaser, strokeSize, Color.BLACK);
	}
	
	public ArcChaserPainter(int spinnerDiameter, int chasers, int segmentsPerChaser, float strokeSize, Color chaserColor) {
		this.spinnerDiameter = spinnerDiameter;
		this.chasers = chasers;
		this.segmentsPerChaser = segmentsPerChaser;
		this.strokeSize = strokeSize;
		this.chaserColor = chaserColor;
		this.renderScale = 2;
		this.closureType = Arc2D.OPEN;
	}
	
	public ArcChaserPainter(int spinnerDiameter, ArcChaserProperties acProps) {
		this(spinnerDiameter, acProps, 2);
	}
	
	public ArcChaserPainter(int spinnerDiameter, ArcChaserProperties acProps, int renderScale) {
		this.renderScale = renderScale;
		this.spinnerDiameter = spinnerDiameter;
		this.chasers = acProps.chasers;
		this.segmentsPerChaser = acProps.segmentsPerChaser;
		this.strokeSize = acProps.strokeSize;
		this.chaserColor = acProps.chaserColor;
		this.closureType = acProps.closureType;
	}
	
	public int getHeight() {
		return spinnerDiameter;
	}
	
	public int getWidth() {
		return spinnerDiameter;
	}
	
	public void paint(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		// render at render scale size and then scale it back down
		// this can improve the look of the rendered image
		float bStrokeSize = this.strokeSize * this.renderScale;
		int bSpinnerDiameter = this.spinnerDiameter * this.renderScale;
		BufferedImage scaleUpImage = new BufferedImage(bSpinnerDiameter, bSpinnerDiameter, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d_su = scaleUpImage.createGraphics();
		g2d_su.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d_su.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		int totalSegments = this.chasers * this.segmentsPerChaser;
		double segmentLengthDegrees = 360d / (double)totalSegments;
		double c = 0;
		float[] rgb = this.chaserColor.getRGBColorComponents(null);
		float halfStroke = bStrokeSize / 2f;
		Rectangle2D arcBounds = new Rectangle2D.Float(halfStroke, halfStroke, bSpinnerDiameter - bStrokeSize, bSpinnerDiameter - bStrokeSize);
		g2d_su.setStroke(new BasicStroke(bStrokeSize));
		double circumference = Math.PI * bSpinnerDiameter - bStrokeSize;
		// segment overflow attempts to compensate for larger stroke overpaints
		double segmentOverflow = (this.closureType == Arc2D.OPEN)? 360d * bStrokeSize / circumference : 0;
		for (int i=0; i<totalSegments; i++) {
			Color color = new Color(rgb[0], rgb[1], rgb[2], (((i%this.segmentsPerChaser)+0)*1f/(float)this.segmentsPerChaser));
			g2d_su.setColor(color);
			Arc2D arc = new Arc2D.Double(arcBounds, c+segmentOverflow/2d, segmentLengthDegrees-segmentOverflow, this.closureType);
			if (this.closureType == Arc2D.OPEN) {
				g2d_su.draw(arc);
			} else {
				g2d_su.fill(arc);
			}
			c -= segmentLengthDegrees;
		}
		g2d_su.dispose();
		
		// now scale it back down and draw it into the original graphics
		//scaleUpImage = ImageUtil.scaleImage(scaleUpImage, new Dimension(this.spinnerDiameter, this.spinnerDiameter), true);
		g2d.drawImage(scaleUpImage, 0, 0, this.spinnerDiameter, this.spinnerDiameter, 
				0, 0, bSpinnerDiameter, bSpinnerDiameter, null);
	}
}
