package org.xandercat.swing.table;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.xandercat.swing.mouse.PopupListener;

/**
 * ColumnToggler sets up a popup menu on the table header that allows the user to 
 * toggle table columns on and off.  ColumnToggler makes use of TableColumn identifiers,
 * and will set each column's identifier to a ColumnToggler.Identifier that stores 
 * the original index of the column.
 * 
 * To use a ColumnToggler, simply create an instance of it; it will register itself
 * as a TableColumnModelListener on the table, after which there is really no need
 * to maintain a separate reference to the ColumnToggler.
 * 
 * @author Scott Arnold
 */
public class ColumnToggler implements TableColumnModelListener {

	public static class Identifier {
		public Identifier(int index) {
			this.index = index;
		}
		private int index;
		public int getIndex() {
			return index;
		}
	}
	
	private final JTable table;
	private final List<TableColumn> columns = new ArrayList<TableColumn>();
	private final List<JCheckBoxMenuItem> menuItems = new ArrayList<JCheckBoxMenuItem>();
	
	public ColumnToggler(JTable table) {
		this.table = table;
		this.table.getColumnModel().addColumnModelListener(this);
		TableModel model = this.table.getModel();
		JTableHeader header = this.table.getTableHeader();
		
		Enumeration<TableColumn> iter = this.table.getColumnModel().getColumns();
		while (iter.hasMoreElements()) {
			TableColumn column = iter.nextElement();
			column.setIdentifier(new Identifier(columns.size()));
			this.columns.add(column);
		}
		
		JPopupMenu popupMenu = new JPopupMenu();
		for (int i=0,j=model.getColumnCount(); i<j; i++) {
			final int idx = i;
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(model.getColumnName(i), true);
			menuItem.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
					if (selected) {
						ColumnToggler.this.table.getColumnModel().addColumn(columns.get(idx));
					} else {
						ColumnToggler.this.table.getColumnModel().removeColumn(columns.get(idx));
					}
				}
			});
			menuItems.add(menuItem);
			popupMenu.add(menuItem);
		}
		header.addMouseListener(new PopupListener(popupMenu));
	}

	private void refreshCheckBoxStates() {	
		List<TableColumn> activeColumns = new ArrayList<TableColumn>();
		Enumeration<TableColumn> enumColumns = this.table.getColumnModel().getColumns();
		while (enumColumns.hasMoreElements()) {
			activeColumns.add(enumColumns.nextElement());
		}
		for (int i=0,j=columns.size(); i<j; i++) {
			JCheckBoxMenuItem menuItem = menuItems.get(i);
			if (menuItem.getState() != activeColumns.contains(columns.get(i))) {
				ItemListener listener = menuItem.getItemListeners()[0];	// assuming there is only one
				menuItem.removeItemListener(listener);
				menuItem.setState(!menuItem.getState());
				menuItem.addItemListener(listener);
			}
		}
	}
	
	@Override
	public void columnAdded(TableColumnModelEvent e) {
		refreshCheckBoxStates();		
	}
	
	@Override
	public void columnRemoved(TableColumnModelEvent e) {
		refreshCheckBoxStates();	
	}
	
	@Override
	public void columnMarginChanged(ChangeEvent e) {
		// no action required
	}

	@Override
	public void columnMoved(TableColumnModelEvent e) {
		// no action required
	}

	@Override
	public void columnSelectionChanged(ListSelectionEvent e) {
		// no action required
	}
}
