package org.xandercat.swing.worker;

public abstract class SwingWorklet<T, V> {
	 
	private SwingWorkletManager<V> manager;
 
	public SwingWorklet(SwingWorkletManager<V> manager) {
		this.manager = manager;
	}
 
	public abstract T execute() throws Exception;
	
	public abstract long getProgressMaximum();
	
	protected boolean isCancelled() {
		return manager.isCancelled();
	}
	
	protected void publish(V v) {
		manager.workletPublish(v);
	}
	
	protected void setProgress(long progress) {
		manager.workletProgress(progress, getProgressMaximum());
	}
}
