package org.xandercat.swing.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.xandercat.swing.mouse.RolloverPassthroughListener;

/**
 * Tab component with a close button for closing the tab.
 * 
 * @author Scott Arnold
 */
public class CloseableTab extends JPanel {

	private static final long serialVersionUID = 2010081001L;
	
	private JButton closeButton;
	private final JLabel label;

	public CloseableTab(final JTabbedPane tabbedPane) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setOpaque(false);
		setBackground(new Color(0, 0, 0, 0));
		this.label = new JLabel() {
			private static final long serialVersionUID = 2010081301L;
			@Override
			public String getText() {
				int i = tabbedPane.indexOfTabComponent(CloseableTab.this);
				if (i >= 0) {
					return tabbedPane.getTitleAt(i);
				} else {
					return null;
				}
			}
		};
		this.closeButton = new JButton("X");
		this.closeButton.setFocusable(false);
		this.closeButton.setContentAreaFilled(false);
		this.closeButton.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(), 
				BorderFactory.createEmptyBorder(1, 4, 1, 4)));
		this.closeButton.setBorderPainted(false);
		this.closeButton.setFont(this.closeButton.getFont().deriveFont(Font.BOLD));
		this.closeButton.setForeground(Color.RED);
		this.closeButton.setToolTipText("Close tab");
		this.closeButton.setMargin(new Insets(0, 0, 0, 0));
		this.closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int i = tabbedPane.indexOfTabComponent(CloseableTab.this);
				if (i >= 0) {
					tabbedPane.remove(i);
				}
			}
		});
		this.closeButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				Component component = e.getComponent();
				if (component instanceof AbstractButton) {
					AbstractButton button = (AbstractButton) component;
					button.setBorderPainted(true);
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				Component component = e.getComponent();
				if (component instanceof AbstractButton) {
					AbstractButton button = (AbstractButton) component;
					button.setBorderPainted(false);
				}
			}
		});
		this.closeButton.addMouseListener(new RolloverPassthroughListener(tabbedPane));
		this.label.addMouseListener(new RolloverPassthroughListener(tabbedPane));
		add(label);
		add(Box.createHorizontalStrut(5));
		add(this.closeButton);
	}
	
	@Override
	public void setToolTipText(String toolTipText) {
		this.label.setToolTipText(toolTipText);
	}
	
	/**
	 * Adds an action listener to the close button on the tab.  The close button
	 * takes care of removing the tab; however, if additional close down work is
	 * required, this method can be used to add additional action listeners.
	 * 
	 * @param listener			close buttton action listener
	 */
	public void addActionListener(ActionListener listener) {
		this.closeButton.addActionListener(listener);
	}
}
