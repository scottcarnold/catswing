package org.xandercat.swing.panel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Helper class for building an input panel split into horizontally aligned sections or groups.
 * 
 * @author Scott Arnold
 */
public class GroupAlignedPanelBuilder {
	
	private static class Heading {
		private JComponent headingComponent;
		private int paddingTop;
		private int paddingBottom;
		public Heading(JComponent headingComponent, int paddingTop, int paddingBottom) {
			this.headingComponent = headingComponent;
			this.paddingBottom = paddingBottom;
			this.paddingTop = paddingTop;
		}
	}
	
	private static class Row {
		private Component leftComponent;
		private Component rightComponent;
		public Row(Component leftComponent, Component rightComponent) {
			this.leftComponent = (leftComponent == null)? new JLabel() : leftComponent;
			this.rightComponent = (rightComponent == null)? new JLabel() : rightComponent;
		}
	}
	
	private List<Object> rows = new ArrayList<Object>();
	private boolean verticalAlignTop = true;
	private int centerColumnSpacing = 10;
	
	public GroupAlignedPanelBuilder() {
	}
	
	public GroupAlignedPanelBuilder(boolean verticalAlignTop, int centerColumnSpacing) {
		this.verticalAlignTop = verticalAlignTop;
		this.centerColumnSpacing = centerColumnSpacing;
	}
	
	public void setVerticalAlignTop(boolean verticalAlignTop) {
		this.verticalAlignTop = verticalAlignTop;
	}
	
	public int getCenterColumnSpacing() {
		return centerColumnSpacing;
	}

	public void setCenterColumnSpacing(int centerColumnSpacing) {
		this.centerColumnSpacing = centerColumnSpacing;
	}

	public void addVerticalStrut(int height) {
		addRow(Box.createVerticalStrut(height), Box.createVerticalStrut(height));
	}
	
	public void addHeading(String heading, int paddingTop, int paddingBottom) {
		addHeading(new JLabel(heading), paddingTop, paddingBottom);
	}
	
	public void addHeading(JLabel label, int paddingTop, int paddingBottom) {
		addHeading((JComponent)label, paddingTop, 0);
		addHeading(new JSeparator(JSeparator.HORIZONTAL), 0, paddingBottom);
	}
	
	public void addHeading(JComponent headingComponent, int paddingTop, int paddingBottom) {
		rows.add(new Heading(headingComponent, paddingTop, paddingBottom));
	}
	
	public void addRow(Component leftComponent, Component rightComponent) {
		rows.add(new Row(leftComponent, rightComponent));
	}
	
	public JPanel build() {
		JPanel panel = new JPanel(new GridBagLayout());
		int gridy = 0;
		for (Object o : rows) {
			if (o instanceof Heading) {
				Heading heading = (Heading) o;
				GridBagConstraints c = new GridBagConstraints();
				c.gridy = gridy;
				c.gridwidth = 5;
				c.insets = new Insets(heading.paddingTop, 0, heading.paddingBottom, 0);
				c.fill = GridBagConstraints.HORIZONTAL;
				panel.add(heading.headingComponent, c);
			} else {
				Row row = (Row) o;
				GridBagConstraints c = new GridBagConstraints();
				c.gridy = gridy;
				c.weightx = 1;
				panel.add(Box.createHorizontalGlue(), c);
				c = new GridBagConstraints();
				c.gridx = 1;
				c.gridy = gridy;
				c.anchor = GridBagConstraints.EAST;
				panel.add(row.leftComponent, c);
				c = new GridBagConstraints();
				c.gridx = 2;
				c.gridy = gridy;
				panel.add(Box.createHorizontalStrut(this.centerColumnSpacing), c);
				c = new GridBagConstraints();
				c.gridx = 3;
				c.gridy = gridy;
				c.anchor = GridBagConstraints.WEST;
				panel.add(row.rightComponent, c);
				c = new GridBagConstraints();
				c.gridx = 4;
				c.gridy = gridy;
				c.weightx = 1;
				panel.add(Box.createHorizontalGlue(), c);
			}
			gridy++;
		}
		if (this.verticalAlignTop) {
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = gridy;
			c.weighty = 1;
			c.gridwidth = 4;
			panel.add(Box.createVerticalGlue(), c);
		}
		return panel;
	}
}
