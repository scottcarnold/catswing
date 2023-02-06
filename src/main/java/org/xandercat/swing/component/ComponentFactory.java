package org.xandercat.swing.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Arc2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.xandercat.swing.animate.ArcChaserProperties;
import org.xandercat.swing.label.ArcChaserLabel;
import org.xandercat.swing.label.RotatingIconLabel;

/**
 * Factory for building prefab components.
 * 
 * @author Scott Arnold
 */
public class ComponentFactory {
	
	public static final Color TITLE_COLOR = new Color(0, 0, 160);
	
	public static RotatingIconLabel createSpinnerLabel(String text) {
		ArcChaserProperties acProps = new ArcChaserProperties();
		acProps.chaserColor = TITLE_COLOR;
		acProps.chasers = 2;
		acProps.segmentsPerChaser = 6;
		acProps.closureType = Arc2D.PIE;
		acProps.strokeSize = 1f;
		return new ArcChaserLabel(text, 75, 16, acProps);
	}
	
	public static RotatingIconLabel createSpinnerLabel(JLabel label) {
		RotatingIconLabel spinnerLabel = createSpinnerLabel(label.getText());
		spinnerLabel.setOpaque(label.isOpaque());
		spinnerLabel.setFont(label.getFont());
		spinnerLabel.setForeground(label.getForeground());
		spinnerLabel.setBackground(label.getBackground());
		spinnerLabel.setBorder(label.getBorder());
		return spinnerLabel;
	}
	
	public static JComponent createTitlePanel(String title) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel label = new JLabel(title);
		label.setForeground(TITLE_COLOR);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
		panel.add(label);
		panel.add(new JSeparator(JSeparator.HORIZONTAL));
		panel.add(Box.createVerticalStrut(5));
		return panel;
	}
	
	public static JLabel createInputLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return label;
	}
	
	public static JCheckBox createInputCheckBox(String text) {
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD));
		return checkBox;
	}
	
	public static JLabel createDetailLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		return label;
	}
}
