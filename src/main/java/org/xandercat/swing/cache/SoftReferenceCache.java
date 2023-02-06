package org.xandercat.swing.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SoftReferenceCache is a cache whose values are held by soft references.  A value in the 
 * cache will remain there so long as at least one other object (other than the cache itself)
 * in the application contains a reference to it, or the JVM is satisfied that sufficient memory
 * is available to hold on to it despite having no hard references.
 * 
 * @author Scott C Arnold
 *
 * @param <K>	key
 * @param <V>	value
 */
public abstract class SoftReferenceCache<K, V> {
	
	private static final Logger log = LogManager.getLogger(SoftReferenceCache.class);

	private Map<K, SoftReference<V>> cacheMap = new HashMap<K, SoftReference<V>>();
	
	/**
	 * Cache the given value for the given key.
	 * 
	 * @param key		cache key
	 * @param value		value
	 */
	public synchronized void set(K key, V value) {
		this.cacheMap.put(key, new SoftReference<V>(value));
	}
	
	/**
	 * Get the value for the given key, loading the value first if necessary.
	 * 
	 * @param key		cache key
	 * 
	 * @return			value for the given key
	 */
	public synchronized V get(K key) {
		V value = peek(key);
		if (value == null) {
			value = loadValue(key);
			set(key, value);
		}
		return value;
	}
	
	/**
	 * Get the value for the given key, returning null if cache does not contain a value for the key.
	 * 
	 * @param key		cache key
	 * 
	 * @return			value for the given key, or null if value is not cached
	 */
	public synchronized V peek(K key) {
		SoftReference<V> ref = this.cacheMap.get(key);
		V value = null;
		if (ref != null) {
			value = ref.get();
			if (value == null) {
				log.debug("Cached object for key " + key.toString() + " has been garbage collected.");
			}
		}
		return value;
	}
	
	/**
	 * Load the value for the given key.
	 * 
	 * @param key		cache key
	 * 
	 * @return			value for the key
	 */
	protected abstract V loadValue(K key);
}
