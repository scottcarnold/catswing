package org.xandercat.swing.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.border.Border;

public class ButtonFactory {

	public static JButton makeTransparent(JButton button) {
		button.setContentAreaFilled(false);
		return button;
	}
	
	public static JButton makeLink(final JFrame parent, JButton button) {
		makeTransparent(button);
		button.setForeground(Color.BLUE);
		Border innerBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLUE);
		if (button.getBorder() == null) {
			button.setBorder(innerBorder);
		} else {
			button.setBorder(BorderFactory.createCompoundBorder(button.getBorder(), innerBorder));
		}	
		if (parent != null) {
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					super.mouseEntered(e);
					parent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				@Override
				public void mouseExited(MouseEvent e) {
					super.mouseExited(e);
					parent.setCursor(null);		// restore default
				}
			});
			// it's possible to lose focus without a mouseExited event, so reset cursor on focus lost as well
			button.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					// do nothing
				}
				@Override
				public void focusLost(FocusEvent e) {
					parent.setCursor(null);	
				}
			});
		}
		return button;
	}
}
