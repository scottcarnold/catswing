package org.xandercat.swing.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * ComponentRenderer can be used when the table column's value is the component to be rendered.
 * 
 * @author Scott C Arnold
 */
public class ComponentRenderer implements TableCellRenderer {

	private static final long serialVersionUID = 2009022201L;
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
		if (value instanceof Component) {
			return (Component) value;
		}
		return new JLabel((value == null)? "" : value.toString());
	}

}
