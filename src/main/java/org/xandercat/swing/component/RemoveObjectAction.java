package org.xandercat.swing.component;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 * RemoveObjectAction is a generic action for removing some object from some other object.
 * 
 * @author Scott C Arnold
 */
public class RemoveObjectAction extends AbstractAction {

	private static final long serialVersionUID = 2009022201L;
	
	private ObjectRemovable objectRemovable;
	private Object object;
	private String actionCommand;
	
	public RemoveObjectAction(ObjectRemovable objectRemovable, Object object, String label, Icon icon, String actionCommand) {
		super(label, icon);
		this.objectRemovable = objectRemovable;
		this.object = object;
		this.actionCommand = actionCommand;
	}
	
	public RemoveObjectAction(ObjectRemovable objectRemovable, Object object, String label, String actionCommand) {
		this(objectRemovable, object, label, null, actionCommand);
	}
	
	public RemoveObjectAction(ObjectRemovable objectRemovable, Object object, Icon icon, String actionCommand) {
		this(objectRemovable, object, null, icon, actionCommand);
	}
	
	public void actionPerformed(ActionEvent event) {
		//JButton button = (JButton) event.getSource();
		// it seems the actionCommand gets lost from the button when using setAction, so 
		// that is why we just hang onto it in this class rather than reading it from the button
		objectRemovable.remove(object, actionCommand);
	}

}
