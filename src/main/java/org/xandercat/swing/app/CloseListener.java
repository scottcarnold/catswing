package org.xandercat.swing.app;

import java.awt.event.WindowEvent;

/**
 * CloseListener can be implemented by any class wishing to perform some action when the application
 * is closed in an application utilizing an ApplicationFrame.  A CloseListener can cancel the 
 * application close process by returning false from it's closeAction method.
 * 
 * @author Scott C Arnold
 */
public interface CloseListener {

	/**
	 * Action to perform when application is closing.  This method can return false to cancel
	 * application close; otherwise it should return true.  This method should not assume that
	 * the application will actually close, as it's possible that another CloseListener yet to
	 * be executed may cancel the application close down process.
	 * 
	 * @param event		the window closing event
	 * 
	 * @return			whether or not to continue closing application
	 */
	public boolean closeAction(WindowEvent event);
}
