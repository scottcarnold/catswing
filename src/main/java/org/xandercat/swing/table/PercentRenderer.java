package org.xandercat.swing.table;

import java.text.NumberFormat;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class PercentRenderer extends DefaultTableCellRenderer {
	
	private static final long serialVersionUID = 2012120601L;
	
	private NumberFormat formatter;
	private boolean showPercentSymbol;
	
	public PercentRenderer(int decimalPlaces, boolean showPercentSymbol) {
		super();
		this.showPercentSymbol = showPercentSymbol;
		this.formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumFractionDigits(decimalPlaces);
		formatter.setMaximumFractionDigits(decimalPlaces);
		setHorizontalAlignment(SwingConstants.RIGHT);
	}
	
	@Override
	protected void setValue(Object value) {
		if (value instanceof Number) {
			value = Double.valueOf(((Number) value).doubleValue() * 100d);
		} 
		if (value == null) {
			setText("");
		} else {
			setText(showPercentSymbol? formatter.format(value) + "%" : formatter.format(value));
		}
	}
}
