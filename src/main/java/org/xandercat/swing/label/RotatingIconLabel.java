package org.xandercat.swing.label;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;

import org.xandercat.swing.animate.RotatingIconPainter;

/**
 * RotatingIconLabel is a JLabel with an animated rotating icon that can be started and stopped.
 * 
 * @author Scott C Arnold
 */
public class RotatingIconLabel extends JLabel implements ActionListener {
	
	private static final long serialVersionUID = 2009022601L;
	
	private int rotationIncrements;
	private ImageIcon[] icons;
	private Icon staticIcon;
	private int currentIcon;
	private Timer animationTimer;
	
	public RotatingIconLabel(String label, RotatingIconPainter painter, int rotationIncrements, int delay) {
		super();
		setText(label);
		this.rotationIncrements = rotationIncrements;
		this.icons = new ImageIcon[rotationIncrements];
		int width = painter.getWidth();
		int height = painter.getHeight();
		for (int i=0; i<rotationIncrements; i++) {
			BufferedImage workImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			AffineTransform transform = AffineTransform.getRotateInstance(i*2d*Math.PI/(double)rotationIncrements, width/2d, height/2d);
			Graphics2D g2d = workImage.createGraphics();
			g2d.transform(transform);
			painter.paint(g2d);
			icons[i] = new ImageIcon(workImage);
		}
		BufferedImage workImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.staticIcon = new ImageIcon(workImage);
		setIcon(staticIcon);
		this.animationTimer = new Timer(delay, this);
	}
	
	protected void nextIcon() {
		currentIcon++;
		if (currentIcon >= rotationIncrements) {
			currentIcon = 0;
		}
		setIcon(icons[currentIcon]);
	}
	
	/**
	 * Set the icon to be used when icon animation is stopped.
	 * 
	 * @param icon		the icon to use when animation is stopped
	 */
	public void setStaticIcon(Icon icon) {
		this.staticIcon = icon;
		if (!animationTimer.isRunning()) {
			setIcon(icon);
		}
	}
	
	/**
	 * Start the animated icon.
	 */
	public void startAnimate() {
		if (!animationTimer.isRunning()) {
			animationTimer.start();
		}
	}
	
	/**
	 * Stop the animated icon.
	 */
	public void stopAnimate() {
		if (animationTimer.isRunning()) {
			animationTimer.stop();
		}
		setIcon((staticIcon == null)? icons[0] : staticIcon);
	}

	public void actionPerformed(ActionEvent event) {
		nextIcon();
	}
}
