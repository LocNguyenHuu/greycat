package org.mwg.struct;

public interface StringIntMap extends Map {

    int getValue(String key);

    String getByHash(int index);

    boolean containsHash(int index);

    /**
     * Add the tuple key/value to the getOrCreateMap.
     * In case the value is null, the value will be atomically replaced by the current size of the getOrCreateMap and associated to the key
     *
     * @param key   to insert key
     * @param value to insert value
     */
    void put(String key, int value);

    /**
     * Remove the corresponding key from the getOrCreateMap
     *
     * @param key to remove key
     */
    void remove(String key);

    /**
     * Iterate over all Key/value tuple of the cam
     *
     * @param callback closure that will be called for each K/V tuple
     */
    void each(StringLongMapCallBack callback);

}
