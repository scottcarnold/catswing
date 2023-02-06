package org.xandercat.swing.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class TimeLengthRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 2010100201L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		long v = ((Long) value).longValue();
		StringBuilder strValue = new StringBuilder();
		if (v >= 1000) {
			v = v / 1000;
			long hour = v / 3600;
			if (hour > 0) {
				strValue.append(hour).append(":");
			}
			v = v % 3600;
			long min = v / 60;
			long sec = v % 60;
			if (hour > 0 && min < 10) {
				strValue.append("0");
			} 
			strValue.append(min).append(":");
			if (sec < 10) {
				strValue.append("0");
			}
			strValue.append(sec);
		} else {
			strValue.append(v).append(" milliseconds");
		}
		setHorizontalAlignment(SwingConstants.RIGHT);
		return super.getTableCellRendererComponent(table, strValue.toString(), isSelected, hasFocus, row, column);
	}
}
