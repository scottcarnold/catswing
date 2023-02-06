package org.xandercat.swing.table;

import java.text.Format;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * FormatRenderer is a TableCellRenderer that renders the cell value using a text formatter.
 * 
 * @author Scott C Arnold
 */
public class FormatRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 2008123001L;
	
	private Format formatter;
	
	public FormatRenderer(Format formatter) {
		this.formatter = formatter;
	}

	public FormatRenderer(Format format, int alignment) {
		this(format);
		setHorizontalAlignment(alignment);
	}
	
	@Override
	protected void setValue(Object value) {
		setText((value == null)? "" : formatter.format(value));
	}
}
