package org.xandercat.swing.cache;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * WeakReferenceCache is a cache whose values are held by weak references.  A value in the 
 * cache will remain there so long as at least one other object (other than the cache itself)
 * in the application contains a reference to it, at which time the cached object will 
 * immediately be garbage collected and will no longer be available in the cache (and will
 * have to be reloaded when needed again).  If you need a cache where the cached objects
 * remain around even when no other objects contain references to them, use a soft reference
 * cache.
 * 
 * @author Scott C Arnold
 *
 * @param <K>	key
 * @param <V>	value
 */
public abstract class WeakReferenceCache<K, V> {

	private static final Logger log = LogManager.getLogger(WeakReferenceCache.class);

	private Map<K, WeakReference<V>> cacheMap = new HashMap<K, WeakReference<V>>();
	
	/**
	 * Cache the given value for the given key.
	 * 
	 * @param key		cache key
	 * @param value		value
	 */
	public synchronized void set(K key, V value) {
		this.cacheMap.put(key, new WeakReference<V>(value));
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
		WeakReference<V> ref = this.cacheMap.get(key);
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
