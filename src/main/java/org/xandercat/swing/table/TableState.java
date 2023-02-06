package org.xandercat.swing.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TableState is for storing the state of any JTable that ultimately extends from SortableTable.  
 * State stored includes column widths, column order, which columns are currently shown, and 
 * the sorted order. 
 * 
 * @author Scott C Arnold
 */
public class TableState implements Serializable {

	private static final long serialVersionUID = 2009083001L;
	private static final Logger log = LogManager.getLogger(TableState.class);
	
	private int[] columnWidths;
	private int[] baseColumnIndices;
	private List<? extends RowSorter.SortKey> sortKeys;
	
	/**
	 * Construct a new empty TableState.  TableState will need to be set by calling the store method.
	 */
	public TableState() {
	}
	
	/**
	 * Construct a new TableState for the given table.
	 * 
	 * @param table		table to create state for
	 */
	public TableState(JTable table) {
		store(table);
	}
	
	/**
	 * Construct a new TableState with the give parameters.  This constructor is useful for creating
	 * default table states.
	 * 
	 * @param visibleColumnIndices		visible column ordering		
	 * @param visibleColumnWidths		widths to use for the column indices (use 0 values for column widths not specified)
	 */
	public TableState(int[] visibleColumnIndices, int[] visibleColumnWidths) {
		if (visibleColumnIndices.length != visibleColumnWidths.length) {
			throw new IllegalArgumentException("Array arguments should be of the same length");
		}
		int length = visibleColumnIndices.length;
		this.columnWidths = new int[length];
		this.baseColumnIndices = new int[length];
		System.arraycopy(visibleColumnIndices, 0, this.baseColumnIndices, 0, length);
		System.arraycopy(visibleColumnWidths, 0, this.columnWidths, 0, length);
	}
	
	/**
	 * Set this TableState for the given table.  This will store column widths as well as
	 * which columns are visible when a ColumnToggler is in use.
	 * 
	 * @param table		table to set state for
	 */
	public void store(JTable table) {
		if (table != null) {
			TableColumnModel columnModel = table.getColumnModel();
			int cols = columnModel.getColumnCount();
			this.columnWidths = new int[cols];
			this.baseColumnIndices = new int[cols];
			RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
			if (rowSorter != null) {
				this.sortKeys = rowSorter.getSortKeys();
			}
			for (int i=0; i<cols; i++) {
				TableColumn column = columnModel.getColumn(i);
				this.columnWidths[i] = column.getWidth();
				int baseColumnIndex = i;
				if (column.getIdentifier() instanceof ColumnToggler.Identifier) {
					baseColumnIndex = ((ColumnToggler.Identifier) column.getIdentifier()).getIndex();
				}
				this.baseColumnIndices[i] = baseColumnIndex;
			}		
			log.debug("Table state stored: " + toString());
		} else {
			log.debug("No table to store state for.");
		}
	}
	
	/**
	 * Apply this table state to the given table.  The table should be of the same type
	 * as was used when the table state was created (otherwise, results are indeterminate).
	 * If using a ColumnToggler, the ColumnToggler should already be set up for the table
	 * before calling this method.
	 * 
	 * @param table		table to set state on
	 */
	public void applyTo(JTable table) {
		if (isEmpty()) {
			log.debug("Table state not applied (no state available)");
			return;			// no point in applying state to table when no state has ever been stored
		}
		TableColumnModel columnModel = table.getColumnModel();

		// remove all columns and then add them back in the previously stored order (less any that were toggled off)
		List<TableColumn> columns = new ArrayList<TableColumn>();
		for (int i=0; i<columnModel.getColumnCount(); i++) {
			columns.add(columnModel.getColumn(i));
		}
		for (int i=0; i<columns.size(); i++) {
			columnModel.removeColumn(columns.get(i));
		}
		for (int i=0; i<this.baseColumnIndices.length; i++) {
			columnModel.addColumn(columns.get(this.baseColumnIndices[i]));
		}
		
		// restore column widths
		for (int i=0; i<this.columnWidths.length; i++) {
			columnModel.getColumn(i).setPreferredWidth(this.columnWidths[i]);
		}
		
		// restore any prior sort order
		RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
		if (rowSorter != null && this.sortKeys != null) {
			rowSorter.setSortKeys(this.sortKeys);
		}
		log.debug("Table state applied: " + toString());
	}

	/**
	 * Return whether or not this TableState has ever been populated from a table.
	 * 
	 * @return			whether or not an actual table state is stored
	 */
	public boolean isEmpty() {
		return this.baseColumnIndices == null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TableState[baseColumnIndicies[");
		if (this.baseColumnIndices == null) {
			sb.append("null");
		} else {
			for (int i=0,j=this.baseColumnIndices.length; i<j; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(this.baseColumnIndices[i]);
			}
		}
		sb.append("],columnWidths[");
		if (this.columnWidths == null) {
			sb.append("null");
		} else {
			for (int i=0,j=this.columnWidths.length; i<j; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(this.columnWidths[i]);
			}
		}
		sb.append("],sortKeys[");
		if (this.sortKeys == null) {
			sb.append("null");
		} else {
			for (int i=0,j=this.sortKeys.size(); i<j; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append(this.sortKeys.get(i));
			}
		}
		sb.append("]]");
		return sb.toString();
	}
}
