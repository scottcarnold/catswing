package org.xandercat.swing.table;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * JScrollPane specifically for JTables where the scroll pane height is set to
 * accommodate a maxRows settings.  If the table has less than or equal to maxRows 
 * rows, the scroll pane preferred height will only be as tall as needed to fit the
 * entire table.  If the table has more than maxRows rows, the scroll pane preferred 
 * height will not exceed the height required to fix maxRows rows.
 * 
 * @author Scott Arnold
 */
public class TableScrollPane extends JScrollPane implements TableModelListener {

	private static final long serialVersionUID = 2010073001L;
	
	private JTable table;
	private int maxRows;
	
	public TableScrollPane(int maxRows) {
		super();
		this.maxRows = maxRows;
	}

	public TableScrollPane(JTable table, int maxRows, int vsbPolicy, int hsbPolicy) {
		super(table, vsbPolicy, hsbPolicy);
		this.table = table;
		this.maxRows = maxRows;
		table.getModel().addTableModelListener(this);
		setupSize();
	}

	public TableScrollPane(JTable table, int maxRows) {
		super(table);
		this.table = table;
		this.maxRows = maxRows;
		table.getModel().addTableModelListener(this);
		setupSize();
	}

	public TableScrollPane(int maxRows, int vsbPolicy, int hsbPolicy) {
		super(vsbPolicy, hsbPolicy);
		this.maxRows = maxRows;
	}

	@Override
	public void setViewportView(Component view) {
		if (!(view instanceof JTable)) {
			throw new UnsupportedOperationException("View component can only be a JTable");
		}
		JTable table = (JTable) view;
		Component viewComponent = getViewport().getView();
		if (viewComponent != null && viewComponent instanceof JTable) {
			((JTable) viewComponent).getModel().removeTableModelListener(this);
		}
		super.setViewportView(table);
		this.table = table;
		table.getModel().addTableModelListener(this);
		setupSize();
	}
	
	private void setupSize() {
		//TODO: Size calculation is not precise and untested with renderers that exceed the normal row height
		int rowsToShow = Math.min(table.getRowCount(), this.maxRows);
		int preferredHeight = (table.getRowHeight() + table.getRowMargin()) * rowsToShow + table.getTableHeader().getPreferredSize().height;
		setPreferredSize(new Dimension(table.getPreferredSize().width, preferredHeight + 5));
		Component parent = getParent();
		if (parent != null) {
			if (parent instanceof JComponent) {
				((JComponent) parent).revalidate();
			} else {
				parent.invalidate();
			}
			getParent().repaint();
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		setupSize();		
	}
	
	
}
