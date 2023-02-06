package org.xandercat.swing.dialog;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xandercat.swing.animate.ProgressBar;

/**
 * ProgressMonitor is an alternative to the Swing ProgressMonitor that supports similar functions but
 * does not try to automatically decide when and if to show itself; showing the dialog must be done
 * manually, and the dialog does not automatically disappear when the progress reaches 100 percent.
 * 
 * @author Scott C Arnold
 */
public class ProgressMonitor extends JDialog implements ActionListener {

	private static final long serialVersionUID = 2009071701L;
	
	private JLabel heading;
	private JLabel message;
	private ProgressBar progressBar;
	private JButton cancelButton;
	private long minProgress;
	private long maxProgress;
	private volatile boolean cancelled;
	
	public ProgressMonitor(JFrame parent, String title, String heading, long minProgress, long maxProgress) {
		super(parent, title);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.minProgress = minProgress;
		this.maxProgress = maxProgress;
		this.heading = new JLabel(heading);
		//this.heading.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.message = new JLabel("");
		this.progressBar = new ProgressBar(400,20);
		this.progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(this);
		this.cancelled = false;
		setModal(false);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(this.heading);
		panel.add(this.message);
		panel.add(this.progressBar);
		panel.add(Box.createVerticalStrut(10));
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(this.cancelButton);
		buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(buttonPanel);
		panel.add(Box.createVerticalStrut(10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setContentPane(panel);
		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
	}
	
	public ProgressMonitor(JFrame parent) {
		this(parent, "Progress", "", 0, 100);
	}
	
	public void addActionListener(ActionListener listener) {
		this.cancelButton.addActionListener(listener);
	}
	
	public void setHeading(String heading) {
		this.heading.setText(heading);
	}
	
	public void setMessage(String message) {
		this.message.setText(message);
	}
	
	public void setProgress(long progress) {
		double n = progress - minProgress;
		double d = maxProgress - minProgress;
		this.progressBar.setPercentage((int) Math.min(100, Math.round(100 * n / d)));
	}
	
	public void setProgressUnknown() {
		this.progressBar.setPercentage(-1);
	}
	
	public long getMaximum() {
		return maxProgress;
	}
	
	public void setMaximum(long maximum) {
		this.maxProgress = maximum;
	}
	
	public long getMinimum() {
		return minProgress;
	}
	
	public void setMinimum(long minimum) {
		this.minProgress = minimum;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}

	public void setAnimated(boolean animate) {
		this.progressBar.setAnimated(animate);
	}

	public void cancel() {
		this.cancelled = true;
		setVisible(false);
	}
	
	public void actionPerformed(ActionEvent event) {
		this.cancelled = true;	
		setVisible(false);
	}
}
