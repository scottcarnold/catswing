package org.xandercat.swing.animate;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.mouse.PopupListener;

/**
 * ProgressBarUpdator is a thread that periodically updates a ProgressBar using data from a class
 * defined as Progressable.
 * 
 * @author Scott C Arnold
 */
public class ProgressBarUpdater extends Thread {

	private static final Logger log = LogManager.getLogger(ProgressBarUpdater.class);
	
	private ProgressBar progressBar;
	private Progressable progressable;
	private MouseListener popupListener;
	private long updateFrequencyMillis = 500;
	private boolean complete;
	private boolean hideProgressBarOnCompletion = true;
	private boolean animate;
	
	public ProgressBarUpdater(ProgressBar progressBar, Progressable progressable, boolean allowPopupCancel, boolean animate) {
		this.progressBar = progressBar;
		this.progressable = progressable;
		this.animate = animate;
		if (allowPopupCancel) {
			JPopupMenu popup = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("Cancel");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					cancelExecution();
				}
			});
			popup.add(menuItem);
			this.popupListener = new PopupListener(popup);
			this.progressBar.addMouseListener(popupListener);
		}
	}
	
	private void cancelExecution() {
		this.progressable.cancelExecution();
	}
	
	public void setUpdateFrequencyMillis(long updateFrequencyMillis) {
		this.updateFrequencyMillis = updateFrequencyMillis;
	}
	
	public void setHideProgressBarOnCompletion(boolean h) {
		this.hideProgressBarOnCompletion = h;
	}
	
	@Override
	public void run() {
		log.debug("ProgressBarUpdater launched.");
		progressBar.setPercentage(0);
		progressBar.setVisible(true);
		if (animate) {
			progressBar.setAnimated(true);
		}
		while (!complete) {
			try {
				Thread.sleep(this.updateFrequencyMillis);
			} catch (InterruptedException ie) {
				// do nothing
			}
			int percentage = progressable.getProgessPercentage();
			progressBar.setPercentage(percentage);
			log.debug("Progress: " + percentage + " %");
			if (percentage >= 100 || progressable.isExecutionCancelled()) {
				complete();
			}
		}
	}
	
	public void complete() {
		complete = true;
		if (popupListener != null) {
			this.progressBar.removeMouseListener(popupListener);
		}
		if (hideProgressBarOnCompletion) {
			this.progressBar.setVisible(false);
		}
	}
}
