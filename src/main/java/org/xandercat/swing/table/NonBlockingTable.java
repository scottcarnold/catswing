package org.xandercat.swing.table;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;

/**
 * NonBlockingTable disables processKeyBindings for Control and Alt key combinations.  This prevents
 * cell editor activation from blocking menu accelerators.
 * 
 * @author Scott Arnold
 */
public class NonBlockingTable extends JTable {

	private static final long serialVersionUID = 2009020101L;
	
	public NonBlockingTable() {
		super();
	}
	
	public NonBlockingTable(TableModel tableModel) {
		super(tableModel);
	}
	
	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		if (e.getModifiers() == Event.CTRL_MASK || e.getModifiers() == Event.ALT_MASK) {
			return false; 
		} else {
			return super.processKeyBinding(ks, e,condition, pressed);
		}
	}
}
