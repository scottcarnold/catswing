package org.xandercat.swing.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

/**
 * ExceptionDetailAction launches a dialog showing the details of an exception.
 * 
 * @author Scott C Arnold
 */
public class ExceptionDetailAction extends AbstractAction {

	private static final long serialVersionUID = 2009072201L;

	private Throwable throwable;
	private String dialogTitle;
	
	public ExceptionDetailAction(String label, Icon icon, Throwable throwable, String dialogTitle) {
		super(label, icon);
		this.throwable = throwable;
		this.dialogTitle = (dialogTitle == null)? "Exception Detail" : dialogTitle;
	}

	public ExceptionDetailAction(String label, Throwable throwable, String dialogTitle) {
		this(label, null, throwable, dialogTitle);
	}
	
	public ExceptionDetailAction(Icon icon, Throwable throwable, String dialogTitle) {
		this(null, icon, throwable, dialogTitle);
	}
	
	public void actionPerformed(ActionEvent event) {
		JDialog dialog = new JDialog();
		dialog.setTitle(dialogTitle);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JLabel errorMsg = new JLabel(throwable.getMessage());
		errorMsg.setForeground(Color.RED);
		Border outerBorder = BorderFactory.createEtchedBorder();
		Border innerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		errorMsg.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		mainPanel.add(errorMsg, BorderLayout.NORTH);
		JTextArea textArea = new JTextArea(16, 60);
		textArea.setEditable(false);
		textArea.setLineWrap(false);
		textArea.append("Stack Trace:");
		for (StackTraceElement element : throwable.getStackTrace()) {
			textArea.append("\n\t" + element.toString());
		}
		textArea.setCaretPosition(0);	// ensures that scroll pane is initially scrolled to top
		JScrollPane scrollPane = new JScrollPane(textArea);
		outerBorder = BorderFactory.createEmptyBorder(10, 0, 0, 0);
		innerBorder = BorderFactory.createEtchedBorder();
		scrollPane.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		dialog.setContentPane(mainPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

}
