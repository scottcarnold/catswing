package org.xandercat.swing.laf;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.xandercat.swing.util.PlatformTool;

public class LookAndFeelSelectionDialog extends JDialog {

	private static final long serialVersionUID = 6905678348571424911L;
	
	private LookAndFeelSelectionPanel lafSelectionPanel;
	private JButton acceptButton;
	private JButton cancelButton;
	private boolean accepted = false;
	
	public LookAndFeelSelectionDialog(Dialog owner) {
		super(owner);
		initialize(owner);
	}

	public LookAndFeelSelectionDialog(Frame owner) {
		super(owner);
		initialize(owner);
	}

	private void initialize(Window owner) {
		setTitle("Look and Feel Selection");
		setModalityType(ModalityType.APPLICATION_MODAL);
		this.lafSelectionPanel = new LookAndFeelSelectionPanel();
		this.acceptButton = new JButton("Accept");
		this.acceptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				accepted = true;
				setVisible(false);
			}
		});
		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});
		JLabel heading = new JLabel("Choose a Look and Feel for the application.");
		JPanel headingPanel = new JPanel();
		headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
		heading.setFont(heading.getFont().deriveFont(16f));
		headingPanel.add(heading);
		headingPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		headingPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout());
		PlatformTool.addOkCancelButtons(buttonPanel, acceptButton, cancelButton);
		topPanel.add(headingPanel, BorderLayout.NORTH);
		topPanel.add(this.lafSelectionPanel, BorderLayout.CENTER);
		topPanel.add(buttonPanel, BorderLayout.SOUTH);
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setContentPane(topPanel);
		getRootPane().setDefaultButton(this.acceptButton);
		pack();
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setLocationRelativeTo(owner);
	}
	
	public boolean showDialog() {
		setVisible(true);
		return accepted;
	}
	
	public String getSelectedLookAndFeelName() {
		return this.lafSelectionPanel.getSelectedLookAndFeelName();
	}
}
