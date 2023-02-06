package org.xandercat.swing.table;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;

/**
 * JTableButtonMouseListener handles button clicks on JButtons in a JTable.
 * 
 * Logic courtesy of article on www.devx.com by Daniel F Savarese.
 * 
 * http://www.devx.com/getHelpOn/10MinuteSolution/20425
 * 
 * @author Scott C Arnold
 */
public class JTableButtonMouseListener implements MouseListener {

	private JTable table;
	
	public JTableButtonMouseListener(JTable table) {
		this.table = table;
	}
	
	private void forwardEventToButton(MouseEvent event) {
		TableColumnModel columnModel = table.getColumnModel();
		int column = columnModel.getColumnIndexAtX(event.getX());
		int row = event.getY() / table.getRowHeight();
		if (row >= table.getRowCount() || row < 0 || column >= table.getColumnCount() || column < 0) {
			return;
		}
		Object value = table.getValueAt(row, column);
		if (value instanceof JButton) {
			JButton button = (JButton) value;
			MouseEvent buttonEvent = (MouseEvent) SwingUtilities.convertMouseEvent(table, event, button);
			button.dispatchEvent(buttonEvent);
			if (event.getID() == MouseEvent.MOUSE_CLICKED) {
				button.getAction().actionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, button.getActionCommand()));
				//TODO: shouldn't have to do this
			}
			table.repaint();
		} 
	}

	public void mouseClicked(MouseEvent event) {
		forwardEventToButton(event);
	}

	public void mouseEntered(MouseEvent event) {
		forwardEventToButton(event);
	}

	public void mouseExited(MouseEvent event) {
		forwardEventToButton(event);
	}

	public void mousePressed(MouseEvent event) {
		forwardEventToButton(event);
	}

	public void mouseReleased(MouseEvent event) {
		forwardEventToButton(event);
	}
}
