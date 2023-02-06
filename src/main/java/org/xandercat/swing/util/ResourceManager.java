package org.xandercat.swing.util;

import java.util.HashMap;
import java.util.Map;

/**
 * ResourceManager provides an alternative to using singletons when the same object (or resource) is
 * needed throughout many areas of a program.
 * 
 * @author Scott C Arnold
 */
public class ResourceManager {

	private static ResourceManager instance = new ResourceManager();
	
	private Map<Class<?>,Object> objectMap = new HashMap<Class<?>,Object>();
	
	public static ResourceManager getInstance() {
		return instance;
	}
	
	private ResourceManager() {
		this.objectMap = new HashMap<Class<?>,Object>();
	}
	
	public synchronized void register(Object object) {
		this.objectMap.put(object.getClass(), object);
	}
	
	public synchronized void unregister(Object object) {
		this.objectMap.remove(object.getClass());
	}
	
	public synchronized void unregister(Class<?> objectClass) {
		this.objectMap.remove(objectClass);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T getResource(Class<T> clazz) {
		return (T) this.objectMap.get(clazz);
	}
}
