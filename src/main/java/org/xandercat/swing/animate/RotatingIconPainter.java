package org.xandercat.swing.animate;

import java.awt.Graphics2D;

/**
 * RotatingIconPainter defines the methods required to paint the icon for a RotatingIconLabel
 * or RotatingIconButton.
 * 
 * @author Scott C Arnold
 */
public interface RotatingIconPainter {

	public void paint(Graphics2D g2d);
	
	public int getWidth();
	
	public int getHeight();
}
