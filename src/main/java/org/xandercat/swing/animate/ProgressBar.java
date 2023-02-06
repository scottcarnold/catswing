package org.xandercat.swing.animate;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.xandercat.swing.label.G2DLabel;

/**
 * ProgressBar is a progress bar that extends JPanel.  A ProgressBar can be updated
 * manually or automatically by using it in combination with a ProgressBarUpdater.
 *   
 * @author Scott C Arnold
 */
public class ProgressBar extends JPanel implements ActionListener {
	
	public static enum Decoration {
		NO_DECORATION, IBEAM, SHADOW;
	}
	
	private static final int ANIMATOR_BAR_SIZE = 7;
	private static final int ANIMATOR_BAR_SPEED = 1;
	private static final Color ANIMATE_COLOR = new Color(255, 255, 255, 40);
	private static final Color BAR_COLOR = new Color(255, 0, 0, 127);
	private static final int[] ANIMATOR_X_POINTS = new int[4];
	private static final int[] ANIMATOR_Y_POINTS = new int[4];
	private static final Color INACTIVE_COLOR = new Color(150, 150, 150, 50);
	
	private static final long serialVersionUID = 2008122701L;
	
	private int percentage;
	private G2DLabel percentageLabel;
	private int barHeight;
	private Decoration barDecoration = Decoration.SHADOW;
	private Timer animator;
	private int animateInc;
	private boolean inactive;
	
	/**
	 * Construct a new progress bar with the given bar height.  Bar width will change dynamically
	 * depending on the width of the panel.
	 * 
	 * @param barHeight   height of the visible progress bar
	 */
	public ProgressBar(int barHeight) {
		this(-1, barHeight);
	}
	
	/**
	 * Construct a new progress bar with the given width and height.  Size of the panel will be 
	 * bound by these values.
	 * 
	 * @param width    the width
	 * @param height   the height
	 */
	public ProgressBar(int width, int height) {
		super();
		if (width > 0 && height > 0) {
			Dimension size = new Dimension(width, height);
			setSize(size);
			setPreferredSize(size);
			setMaximumSize(size);
			setMinimumSize(size);
		}
		this.barHeight = height;
		this.percentageLabel = new G2DLabel(null, Color.BLACK, null, 0, 0, width, height, G2DLabel.CENTER, false);
		this.percentageLabel.setBoundsComponent(this);
	}
	
	public ProgressBar(int width, int height, Font font) {
		this(width, height);
		this.percentageLabel.setFont(font);
	}
	
	public void setBarHeight(int barHeight) {
		this.barHeight = barHeight;
	}
	
	public void setDecoration(Decoration decoration) {
		this.barDecoration = decoration;
	}
	
	public void setAnimated(boolean animate) {
		setAnimated(animate, 150);
	}
	
	public void setAnimated(boolean animate, int timerInterval) {
		if (animate && animator == null) {
			animator = new Timer(timerInterval, this);
			animator.start();
		} else if (!animate && animator != null) {
			animator.stop();
			animator = null;
		}		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int height = getHeight();
		int width = getWidth();
		int barOffset = (height - barHeight) / 2;
		g2d.clearRect(0, 0, width, height);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, barOffset, width, barHeight);
		switch (barDecoration) {
		case IBEAM:
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(1));
			g2d.drawLine(0, barOffset, 0, height - barOffset);
			g2d.drawLine(width-1, barOffset, width-1, height - barOffset);
			g2d.drawLine(0, height-1, width, height-1);
			break;
		case SHADOW:
			g2d.setStroke(new BasicStroke(2));
			int min = 100;
			int inc = (255 - min) / 5;
			for (int i=0; i<5; i++) {
				int ci = min + i * inc;
				Color color = new Color(ci, ci, ci);
				g2d.setColor(color);
				g2d.drawRoundRect(i, barOffset+i, width-i*2, barHeight-i*2, 6, 6);
			}
			break;
		}
		int barWidth = (inactive || percentage < 0)? width : (width * percentage) / 100;
		if (percentage != 0 || inactive) {
			g2d.setColor(inactive? INACTIVE_COLOR : BAR_COLOR);
			g2d.fillRoundRect(0, barOffset, barWidth, barHeight, 8, 8);
		}
		if (animator != null) {
			animateInc += ANIMATOR_BAR_SPEED;
			if (animateInc > ANIMATOR_BAR_SIZE) {
				animateInc -= (ANIMATOR_BAR_SIZE*2);
			}
			ANIMATOR_Y_POINTS[0] = ANIMATOR_Y_POINTS[1] = barOffset;
			ANIMATOR_Y_POINTS[2] = ANIMATOR_Y_POINTS[3] = height - barOffset;
			g2d.setColor(ANIMATE_COLOR);
			for (int i=animateInc; i<barWidth; i+=(ANIMATOR_BAR_SIZE*2)) {
				//g2d.fillRect(i, barOffset, ANIMATOR_BAR_SIZE, barHeight);
				ANIMATOR_X_POINTS[0] = i;
				ANIMATOR_X_POINTS[1] = i + ANIMATOR_BAR_SIZE;
				ANIMATOR_X_POINTS[2] = i + ANIMATOR_BAR_SIZE - 5;
				ANIMATOR_X_POINTS[3] = i - 5;
				g2d.fillPolygon(ANIMATOR_X_POINTS, ANIMATOR_Y_POINTS, 4);
			}
		}
		if (inactive) {
			percentageLabel.setText("--");
		} else if (percentage < 0) {
			percentageLabel.setText("?");
		} else {
			percentageLabel.setText(String.valueOf(percentage) + "%");
		}
		percentageLabel.paint(g2d);
	}
	
	/**
	 * Set the percentage from 0 to 100, or < 0 for percentage unknown.
	 * 
	 * @param percentage
	 */
	public void setPercentage(int percentage) {
		this.inactive = false;
		this.percentage = percentage;
		repaint(); 
	}

	public void setPercentageUnknown() {
		setPercentage(-1);
	}
	
	/**
	 * Gives the progress bar an inactive appearance, disabling animation if active.  
	 * The inactive appearance ends on the next call to setPercentage() or setPercentageUnknown().  
	 */
	public void setInactive() {
		inactive = true;
		setAnimated(false, 150);
	}
	
	public void actionPerformed(ActionEvent event) {
		repaint();
	}
}
