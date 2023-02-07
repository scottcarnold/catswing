package org.xandercat.swing.label;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * Anti-aliased JLabel.
 * 
 * @author Scott Arnold
 */
public class AALabel extends JLabel {

	private static final long serialVersionUID = 2009102101L;
	
	public AALabel() {
		super();
	}

	public AALabel(Icon icon, int horizontalAlignment) {
		super(icon, horizontalAlignment);
	}

	public AALabel(Icon icon) {
		super(icon);
	}

	public AALabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public AALabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public AALabel(String text) {
		super(text);
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		Graphics2D graphics2D = (Graphics2D) graphics;
		graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(graphics);
	}
}
