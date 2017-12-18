/**
 * 
 */
package com.mcg.batch.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.mcg.batch.exceptions.CacheException;

/**
 * <p>
 * A cache interface that has provisions for different types Name value pairs <br>
 * Currently the cache interface has support for<br>
 * 1) Name,Map<key,Value> <br>
 * 2) Name,List<Value> <br>
 * 3) Name, value <br>
 * 
 * 
 * 
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public interface SmartBatchCache {

	/**
	 * A Method that would be called by the construtor to perform any
	 * initialization
	 */
	public void init() throws CacheException;

	/**
	 * This method would return the current namespace used.
	 * 
	 * @return
	 */
	public String getCurrentNameSpace();

	/**
	 * Adds the specified key, value as a entry to the map identified by the map
	 * name in the cache <br>
	 * Basic HasMap rules should be applied and the values would be overwritten
	 * if the cache already has the key
	 * 
	 * @param cacheName
	 * @param key
	 * @param value
	 */
	public void putToMap(String mapName, Serializable key, Serializable value);

	/**
	 * Adds the specified key, value as a entry to the map identified by the map
	 * name in the cache only if there is no such key
	 * 
	 * @param mapName
	 * @param key
	 * @param value
	 * @return
	 */

	public boolean putIfAbsentToMap(String mapName, Serializable key,
			Serializable value);

	/**
	 * Removes the entry from the map identified by the mapName with the key
	 * specified
	 * 
	 * @param mapName
	 * @param key
	 */
	public void removeFromMap(String mapName, Serializable key);

	/**
	 * Returns the value identified by the key in the map based on the mapName
	 * 
	 * @param mapName
	 * @param key
	 * @return
	 */
	public Serializable getFromMap(String mapName, Serializable key);

	/**
	 * Returns the value identified by the key in the map based on the mapName.
	 * The type of the key is specified by the clazz parameter to this method
	 * 
	 * @param mapName
	 * @param key
	 * @param clazz
	 * @return
	 */
	public <T extends Serializable> T getFromMap(String mapName,
			Serializable key, Class<T> clazz);

	/**
	 * Checks if the key is present in the map identified by map name
	 * 
	 * @param mapName
	 * @param key
	 * @return
	 */
	public boolean hasKeyInMap(String mapName, Serializable key);

	/**
	 * Returns the set of keys from the map identified by the mapNames
	 * 
	 * @param mapName
	 * @return
	 */
	public Set<?> getAllKeys(String mapName);

	/**
	 * Append the value to the tail of the list specified by the
	 * 
	 * @param listName
	 * @param value
	 */
	public long appendTolist(String listName, Serializable value);

	/**
	 * Append the value to the tail of the list specified by the listName
	 * ignoring namespace
	 * 
	 * @param listName
	 * @param value
	 */
	public long appendTolistNoNs(String listName, Serializable value);

	/**
	 * Add the value to the list identified by the listName at the index
	 * specified
	 * 
	 * @param listName
	 * @param value
	 * @param index
	 */
	public void addToList(String listName, Serializable value, long index);

	/**
	 * Add the value to the list identified by the listName at the index
	 * specified ignoring NameSpace
	 * 
	 * @param listName
	 * @param value
	 * @param index
	 */
	public void addToListNoNs(String listName, Serializable value, long index);

	
	/**
	 * 
	 * Get All the list of values from the cache starting from the start index
	 * till the end index specified
	 * 
	 * @param listName
	 * @param start
	 * @param end
	 * @return
	 */

	public List<Serializable> getList(String listName, long start, long end);

	/**
	 * 
	 * Get All the list of values from the cache starting from the start index
	 * till the end index specified ignoring Namespace.
	 * 
	 * @param listName
	 * @param start
	 * @param end
	 * @return
	 */

	public List<Serializable> getListNoNs(String listName, long start, long end);

	/**
	 * 
	 * Get All the list of values from the cache based on list boundries at
	 * cache
	 * 
	 * @param listName
	 * @param start
	 * @param end
	 * @return
	 */

	public List<?> getList(String listName);
	
	/**
	 * 
	 * Get All the list of values from the cache based on list boundries at
	 * cache ignoring Namespace
	 * 
	 * @param listName
	 * @param start
	 * @param end
	 * @return
	 */

	public List<?> getListNoNs(String listName);

	/**
	 * Get a value from the list at the specified index
	 * 
	 * @param listName
	 * @param index
	 * @return
	 */
	public Serializable getFromList(String listName, long index);

	/**
	 * Get a value from the list at the specified index ignoring NameSpace
	 * 
	 * @param listName
	 * @param index
	 * @return
	 */
	public Serializable getFromListNoNs(String listName, long index);
	
	/**
	 * Removes the value from the index based on the count as specified below <br>
	 * count > 0: Remove elements equal to value moving from head to tail.<br>
	 * count < 0: Remove elements equal to value moving from tail to head.<br>
	 * count = 0: Remove all elements equal to value
	 * 
	 * @param listName
	 * @param count
	 * @param value
	 */
	public void removeFromList(String listName, long count, Serializable value);
	
	/**
	 * Removes the value from the index based on the count as specified below ignoringNameSpace<br>
	 * count > 0: Remove elements equal to value moving from head to tail.<br>
	 * count < 0: Remove elements equal to value moving from tail to head.<br>
	 * count = 0: Remove all elements equal to value
	 * 
	 * @param listName
	 * @param count
	 * @param value
	 */
	public void removeFromListNoNs(String listName, long count, Serializable value);


	/**
	 * Add a key value pair that can be identified uniquely by the key. In case
	 * the key already exists then the value is overwritten.
	 * 
	 * @param key
	 * @param value
	 */
	public void add(Serializable key, Serializable value);

	/**
	 * Add a key value pair that can be identified uniquely by the key if doesnt
	 * exists
	 * 
	 * 
	 * @param key
	 * @param value
	 */
	public void putIfAbsent(Serializable key, Serializable value);

	/**
	 * Removes a Entry from cache specified by the key
	 * 
	 * @param key
	 */
	public void remove(Serializable key);

	/**
	 * Returns the value from cache specified by the key
	 * 
	 * @param key
	 * @return
	 */

	public Serializable get(Serializable key);

	/**
	 * Returns the value from cache specified by the key and updates it
	 * atomically to the new value specified
	 * 
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Serializable getAndSet(Serializable key, Serializable value);

	/**
	 * Atomically increment and get a sequence identified by the key
	 * 
	 * @param key
	 * @return
	 */
	public long incrementAndGet(Serializable key);

	/**
	 * Atomically increment with delta value provided and get a sequence
	 * identified by the key
	 * 
	 * @param key
	 * @return
	 */
	public long incrementAndGet(Serializable key, int delta);

	/**
	 * A boolean to indicate if a key exists in cache
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasKey(Serializable key);

	/**
	 * Atomically add and get a sequence identified by the key
	 * 
	 * @param key
	 * @return
	 */
	public long addAndGet(Serializable key, long delta);
	
	/**
	 * Append to map entry.
	 *
	 * @param mapName the map name
	 * @param key the key
	 * @param oldValue the old value
	 * @param newValue the new value
	 */
	public void appendToMapEntry(String mapName, Serializable key, String value);
	
    /**
     * Gets the from map no ns.
     *
     * @param mapName the map name
     * @param key the key
     * @return the from map no ns
     */
    public Serializable getFromMapNoNS(final String mapName, final Serializable key);
	
	/**
	 * Put to map no ns.
	 *
	 * @param mapName the map name
	 * @param key the key
	 * @param value the value
	 */
	public void putToMapNoNS(final String mapName, final Serializable key, final Serializable value);
	
	/**
	 * Removes the from map no ns.
	 *
	 * @param mapName the map name
	 * @param key the key
	 */
	public void removeFromMapNoNS(final String mapName, final Serializable key);
	
	/**
	 * Gets the all keys no ns.
	 *
	 * @param mapName the map name
	 * @return the all keys no ns
	 */
	public Set<?> getAllKeysNoNS(final String mapName) ;

}
