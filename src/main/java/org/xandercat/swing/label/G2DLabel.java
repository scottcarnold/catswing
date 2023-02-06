package org.xandercat.swing.label;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * G2DLabel provides a simple means for drawing text using Graphics2D.
 * 
 * @author Scott Arnold
 */
public class G2DLabel {

	public static enum Alignment {
		LEFT, CENTER, RIGHT;
	}

	public static final Alignment LEFT = Alignment.LEFT;
	public static final Alignment CENTER = Alignment.CENTER;
	public static final Alignment RIGHT = Alignment.RIGHT;
	
	private String text;
	private Rectangle2D bounds;
	private Alignment alignment;
	private Font baseFont;
	private Font font;
	private Color color;
	private Component boundsComponent;
	
	public G2DLabel(Font font, Color color, String text, Rectangle2D bounds, Alignment alignment, boolean scaleFontToFit) {
		if (text == null) {
			text = "";
		}
		if (scaleFontToFit) {
			this.baseFont = font;
		} else {
			this.font = font;
		}
		this.color = color;
		this.text = text;
		this.bounds = bounds;
		this.alignment = alignment;
	}

	public G2DLabel(Font font, Color color, String text, int x, int y, int width, int height, Alignment alignment, boolean scaleFontToFit) {
		this(font, color, text, new Rectangle2D.Float(x, y, width, height), alignment, scaleFontToFit);
	}
	
	public void setBoundsComponent(Component boundsComponent) {
		this.boundsComponent = boundsComponent;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = (text == null)? "" : text;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public void paint(Graphics2D g2d) {
		if (boundsComponent != null) {
			this.bounds.setRect(0, 0, boundsComponent.getWidth(), boundsComponent.getHeight());
		}
		if (this.font == null && this.baseFont != null) {
			//TODO: Scale Font to Fit needs to be implemented or removed
			this.font = this.baseFont;
		}
		if (this.font != null) {
			g2d.setFont(this.font);
		}
		FontMetrics fm = g2d.getFontMetrics();
		g2d.setColor(this.color);
		float x = 0;
		float y = (float) this.bounds.getCenterY() + (float) fm.getHeight() / 2f - fm.getDescent();
		switch (this.alignment) {
		case CENTER:
			x = (float) this.bounds.getCenterX() - fm.stringWidth(this.text) / 2f;
			break;
		case RIGHT:
			x = (float) this.bounds.getMaxX() - fm.stringWidth(this.text);
			break;
		default:
			x = (float) this.bounds.getX();
			break;
		}
		g2d.drawString(this.text, x, y);
	}
}
