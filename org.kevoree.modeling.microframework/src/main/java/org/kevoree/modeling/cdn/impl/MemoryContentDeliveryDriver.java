package org.kevoree.modeling.cdn.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.cdn.KContentPutRequest;
import org.kevoree.modeling.cdn.KMessageInterceptor;
import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.event.KEventMultiListener;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KUniverse;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.event.impl.LocalEventListeners;
import org.kevoree.modeling.memory.struct.map.KStringMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap;
import org.kevoree.modeling.message.KMessage;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MemoryContentDeliveryDriver implements KContentDeliveryDriver {

    private final KStringMap<String> backend = new ArrayStringMap<String>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    private LocalEventListeners _localEventListeners = new LocalEventListeners();

    public static boolean DEBUG = false;

    @Override
    public void atomicGetIncrement(KContentKey key, KCallback<Short> cb) {
        String result = backend.get(key.toString());
        short nextV;
        short previousV;
        if (result != null) {
            try {
                previousV = Short.parseShort(result);
            } catch (Exception e) {
                e.printStackTrace();
                previousV = Short.MIN_VALUE;
            }
        } else {
            previousV = 0;
        }
        if (previousV == Short.MAX_VALUE) {
            nextV = Short.MIN_VALUE;
        } else {
            nextV = (short) (previousV + 1);
        }
        backend.put(key.toString(), "" + nextV);
        cb.on(previousV);
    }

    @Override
    public void get(KContentKey[] keys, KCallback<String[]> callback) {
        String[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != null) {
                values[i] = backend.get(keys[i].toString());
            }
            if (DEBUG) {
                System.out.println("GET " + keys[i] + "->" + values[i]);
            }
        }
        if (callback != null) {
            callback.on(values);
        }
    }

    @Override
    public synchronized void put(KContentPutRequest p_request, KCallback<Throwable> p_callback) {
        for (int i = 0; i < p_request.size(); i++) {
            backend.put(p_request.getKey(i).toString(), p_request.getContent(i));
            if (DEBUG) {
                System.out.println("PUT " + p_request.getKey(i).toString() + "->" + p_request.getContent(i));
            }
        }
        if (p_callback != null) {
            p_callback.on(null);
        }
    }

    @Override
    public void remove(String[] keys, KCallback<Throwable> callback) {
        for (int i = 0; i < keys.length; i++) {
            backend.remove(keys[i]);
        }
        if (callback != null) {
            callback.on(null);
        }
    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        if (callback != null) {
            callback.on(null);
        }
    }

    @Override
    public void close(KCallback<Throwable> callback) {
        _localEventListeners.clear();
        backend.clear();
        callback.on(null);
    }


    @Override
    public void registerListener(long groupId, KObject p_origin, KEventListener p_listener) {
        _localEventListeners.registerListener(groupId, p_origin, p_listener);
    }

    @Override
    public void unregisterGroup(long groupId) {
        _localEventListeners.unregister(groupId);
    }

    @Override
    public void registerMultiListener(long groupId, KUniverse origin, long[] objects, KEventMultiListener listener) {
        _localEventListeners.registerListenerAll(groupId, origin.key(), objects, listener);
    }

    @Override
    public void send(KMessage msgs) {
        //NO REMOTE MANAGEMENT
        if (additionalInterceptors != null) {
            for (int i = 0; i < additionalInterceptors.length; i++) {
                KMessageInterceptor interceptor = additionalInterceptors[i];
                if(interceptor != null){
                    if (interceptor.on(msgs)) {
                        return; //exit if intercepted
                    }
                }
            }
        }
        _localEventListeners.dispatch(msgs);
    }

    private KMessageInterceptor[] additionalInterceptors = null;

    /** @native ts
     * if(this.additionalInterceptors == null){this.additionalInterceptors = []; }
     * var previousSize = this.additionalInterceptors.length;
     * this.additionalInterceptors.push(p_interceptor);
     * return previousSize;
     * */
    @Override
    public synchronized int addMessageInterceptor(KMessageInterceptor p_interceptor) {
        if (additionalInterceptors == null) {
            additionalInterceptors = new KMessageInterceptor[1];
            additionalInterceptors[0] = p_interceptor;
            return 0;
        } else {
            int id = additionalInterceptors.length;
            KMessageInterceptor[] newInterceptors = new KMessageInterceptor[id + 1];
            System.arraycopy(additionalInterceptors, 0, newInterceptors, 0, id);
            newInterceptors[id] = p_interceptor;
            additionalInterceptors = newInterceptors;
            return id;
        }
    }

    /** @native ts
     * if(this.additionalInterceptors == null){ return }
     * delete this.additionalInterceptors[id];
     * */
    @Override
    public synchronized void removeMessageInterceptor(int id) {
        if (additionalInterceptors != null) {
            additionalInterceptors[id] = null;
        }
    }

    @Override
    public void setManager(KMemoryManager manager) {
        _localEventListeners.setManager(manager);
    }


}