package org.xandercat.swing.animate;

/**
 * Progressable can be implemented by any class that requires a progress bar.  
 * 
 * @author Scott C Arnold
 */
public interface Progressable {

	public int getProgessPercentage();
	
	public void cancelExecution();
	
	public boolean isExecutionCancelled();
}
