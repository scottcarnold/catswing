package org.xandercat.swing.log;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.xandercat.swing.component.MessageScrollPane;

public class LogFrame extends JFrame {

	private static final long serialVersionUID = 3662623979897390963L;

	public LogFrame(MessageScrollPane messageScrollPane) {
		super("Application Log");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JPanel loggingPanel = new JPanel(new BorderLayout());
		loggingPanel.add(messageScrollPane, BorderLayout.CENTER);
		setContentPane(loggingPanel);
		setSize(800, 400);
		setLocationRelativeTo(null);
	}
}
