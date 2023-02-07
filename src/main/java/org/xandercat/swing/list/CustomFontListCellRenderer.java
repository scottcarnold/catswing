package org.xandercat.swing.list;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class CustomFontListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 5315986707057488302L;
	
	private Float fontSize;
	
	public CustomFontListCellRenderer(float fontSize) {
		super(); 
		this.fontSize = Float.valueOf(fontSize);
	}
	
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (fontSize != null) {
			label.setFont(label.getFont().deriveFont(fontSize.floatValue()));
		}
		return label;
	}

}
