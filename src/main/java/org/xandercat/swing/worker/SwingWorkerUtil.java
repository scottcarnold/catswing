package org.xandercat.swing.worker;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.SwingWorker;

/**
 * Utility class for SwingWorkers.  Contains method to execute a SwingWorker 
 * that is a workaround for a SwingWorker execution bug in Java 6u18 through
 * Java 7u??. 
 * 
 * @author Scott Arnold
 */
public class SwingWorkerUtil {

	private static Executor swingWorkerExecutor;
	
	/**
	 * Execute a SwingWorker within a thread pool.  This method is necessary due do
	 * a bug in Java 6u18 through Java 7+.  All Java 6 SwingWorkers should be executed
	 * using this method or a similar strategy in order to ensure proper operation
	 * on all systems.
	 * 
	 * See bug database entry http://bugs.sun.com/view_bug.do?bug_id=6880336
	 * 
	 * @param swingWorker			SwingWorker to execute
	 */
	public static synchronized void execute(SwingWorker<?,?> swingWorker) {
		if (swingWorkerExecutor == null) {
			swingWorkerExecutor = Executors.newCachedThreadPool();
		}
		swingWorkerExecutor.execute(swingWorker);
	}
}
