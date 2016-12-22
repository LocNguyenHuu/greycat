package org.mwg.core.chunk.heap;

import org.mwg.Constants;
import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.base.BaseNode;
import org.mwg.plugin.Resolver;
import org.mwg.struct.*;
import org.mwg.utility.HashHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class HeapENode implements ENode {

    private final HeapEGraph egraph;
    private final HeapStateChunk chunk;
    private final Graph _graph;
    private final long _id;

    HeapENode(HeapStateChunk p_chunk, HeapEGraph p_egraph, Graph p_graph, long p_id) {
        chunk = p_chunk;
        egraph = p_egraph;
        _graph = p_graph;
        _id = p_id;
    }

    private int _capacity;
    private volatile int _size;
    private long[] _k;
    private Object[] _v;
    private int[] _next;
    private int[] _hash;
    private byte[] _type;
    private boolean _dirty;

    private void declareDirty() {
        if (!_dirty) {
            _dirty = true;
            egraph.declareDirty();
        }
    }

    private int internal_find(final long p_key) {
        if (_size == 0) {
            return -1;
        } else if (_hash == null) {
            for (int i = 0; i < _size; i++) {
                if (_k[i] == p_key) {
                    return i;
                }
            }
            return -1;
        } else {
            final int hashIndex = (int) HashHelper.longHash(p_key, _capacity * 2);
            int m = _hash[hashIndex];
            while (m >= 0) {
                if (p_key == _k[m]) {
                    return m;
                } else {
                    m = _next[m];
                }
            }
            return -1;
        }
    }

    private Object internal_get(final long p_key) {
        //empty chunk, we return immediately
        if (_size == 0) {
            return null;
        }
        int found = internal_find(p_key);
        if (found != -1) {
            return _v[found];
        }
        return null;
    }

    private void internal_set(final long p_key, final byte p_type, final Object p_unsafe_elem, boolean replaceIfPresent, boolean initial) {
        Object param_elem = null;
        //check the param type
        if (p_unsafe_elem != null) {
            try {
                switch (p_type) {
                    case Type.BOOL:
                        param_elem = (boolean) p_unsafe_elem;
                        break;
                    case Type.DOUBLE:
                        param_elem = (double) p_unsafe_elem;
                        break;
                    case Type.LONG:
                        if (p_unsafe_elem instanceof Integer) {
                            int preCasting = (Integer) p_unsafe_elem;
                            param_elem = (long) preCasting;
                        } else {
                            param_elem = (long) p_unsafe_elem;
                        }
                        break;
                    case Type.INT:
                        param_elem = (int) p_unsafe_elem;
                        break;
                    case Type.STRING:
                        param_elem = (String) p_unsafe_elem;
                        break;
                    case Type.MATRIX:
                        param_elem = (Matrix) p_unsafe_elem;
                        break;
                    case Type.LMATRIX:
                        param_elem = (LMatrix) p_unsafe_elem;
                        break;
                    case Type.RELATION:
                        param_elem = (Relation) p_unsafe_elem;
                        break;
                    case Type.EXTERNAL:
                        param_elem = p_unsafe_elem;
                        break;
                    case Type.DOUBLE_ARRAY:
                        double[] castedParamDouble = (double[]) p_unsafe_elem;
                        double[] clonedDoubleArray = new double[castedParamDouble.length];
                        System.arraycopy(castedParamDouble, 0, clonedDoubleArray, 0, castedParamDouble.length);
                        param_elem = clonedDoubleArray;
                        break;
                    case Type.LONG_ARRAY:
                        long[] castedParamLong = (long[]) p_unsafe_elem;
                        long[] clonedLongArray = new long[castedParamLong.length];
                        System.arraycopy(castedParamLong, 0, clonedLongArray, 0, castedParamLong.length);
                        param_elem = clonedLongArray;
                        break;
                    case Type.INT_ARRAY:
                        int[] castedParamInt = (int[]) p_unsafe_elem;
                        int[] clonedIntArray = new int[castedParamInt.length];
                        System.arraycopy(castedParamInt, 0, clonedIntArray, 0, castedParamInt.length);
                        param_elem = clonedIntArray;
                        break;
                    case Type.STRING_TO_LONG_MAP:
                        param_elem = (StringLongMap) p_unsafe_elem;
                        break;
                    case Type.LONG_TO_LONG_MAP:
                        param_elem = (LongLongMap) p_unsafe_elem;
                        break;
                    case Type.LONG_TO_LONG_ARRAY_MAP:
                        param_elem = (LongLongArrayMap) p_unsafe_elem;
                        break;
                    case Type.RELATION_INDEXED:
                        param_elem = (RelationIndexed) p_unsafe_elem;
                        break;
                    default:
                        throw new RuntimeException("Internal Exception, unknown type");
                }
            } catch (Exception e) {
                throw new RuntimeException("mwDB usage error, set method called with type " + Type.typeName(p_type) + " while param object is " + p_unsafe_elem);
            }
        }
        //first value
        if (_k == null) {
            //we do not allocate for empty element
            if (param_elem == null) {
                return;
            }
            _capacity = Constants.MAP_INITIAL_CAPACITY;
            _k = new long[_capacity];
            _v = new Object[_capacity];
            _type = new byte[_capacity];
            _k[0] = p_key;
            _v[0] = param_elem;
            _type[0] = p_type;
            _size = 1;
            if (!initial) {
                declareDirty();
            }
            return;
        }
        int entry = -1;
        int p_entry = -1;
        int hashIndex = -1;
        if (_hash == null) {
            for (int i = 0; i < _size; i++) {
                if (_k[i] == p_key) {
                    entry = i;
                    break;
                }
            }
        } else {
            hashIndex = (int) HashHelper.longHash(p_key, _capacity * 2);
            int m = _hash[hashIndex];
            while (m != -1) {
                if (_k[m] == p_key) {
                    entry = m;
                    break;
                }
                p_entry = m;
                m = _next[m];
            }
        }
        //case already present
        if (entry != -1) {
            if (replaceIfPresent || (p_type != _type[entry])) {
                if (param_elem == null) {
                    if (_hash != null) {
                        //unHash previous
                        if (p_entry != -1) {
                            _next[p_entry] = _next[entry];
                        } else {
                            _hash[hashIndex] = -1;
                        }
                    }
                    int indexVictim = _size - 1;
                    //just pop the last value
                    if (entry == indexVictim) {
                        _k[entry] = -1;
                        _v[entry] = null;
                        _type[entry] = -1;
                    } else {
                        //we need to reHash the new last element at our place
                        _k[entry] = _k[indexVictim];
                        _v[entry] = _v[indexVictim];
                        _type[entry] = _type[indexVictim];
                        if (_hash != null) {
                            _next[entry] = _next[indexVictim];
                            int victimHash = (int) HashHelper.longHash(_k[entry], _capacity * 2);
                            int m = _hash[victimHash];
                            if (m == indexVictim) {
                                //the victim was the head of hashing list
                                _hash[victimHash] = entry;
                            } else {
                                //the victim is in the next, reChain it
                                while (m != -1) {
                                    if (_next[m] == indexVictim) {
                                        _next[m] = entry;
                                        break;
                                    }
                                    m = _next[m];
                                }
                            }
                        }
                    }
                    _size--;
                } else {
                    _v[entry] = param_elem;
                    if (_type[entry] != p_type) {
                        _type[entry] = p_type;
                    }
                }
            }
            if (!initial) {
                declareDirty();
            }
            return;
        }
        if (_size < _capacity) {
            _k[_size] = p_key;
            _v[_size] = param_elem;
            _type[_size] = p_type;
            if (_hash != null) {
                _next[_size] = _hash[hashIndex];
                _hash[hashIndex] = _size;
            }
            _size++;
            declareDirty();
            return;
        }
        //extend capacity
        int newCapacity = _capacity * 2;
        long[] ex_k = new long[newCapacity];
        System.arraycopy(_k, 0, ex_k, 0, _capacity);
        _k = ex_k;
        Object[] ex_v = new Object[newCapacity];
        System.arraycopy(_v, 0, ex_v, 0, _capacity);
        _v = ex_v;
        byte[] ex_type = new byte[newCapacity];
        System.arraycopy(_type, 0, ex_type, 0, _capacity);
        _type = ex_type;
        _capacity = newCapacity;
        //insert the next
        _k[_size] = p_key;
        _v[_size] = param_elem;
        _type[_size] = p_type;
        _size++;
        //reHash
        _hash = new int[_capacity * 2];
        Arrays.fill(_hash, 0, _capacity * 2, -1);
        _next = new int[_capacity];
        Arrays.fill(_next, 0, _capacity, -1);
        for (int i = 0; i < _size; i++) {
            int keyHash = (int) HashHelper.longHash(_k[i], _capacity * 2);
            _next[i] = _hash[keyHash];
            _hash[keyHash] = i;
        }
        if (!initial) {
            declareDirty();
        }
    }

    @Override
    public ENode set(String name, byte type, Object value) {
        internal_set(_graph.resolver().stringToHash(name, true), type, value, true, false);
        return this;
    }

    @Override
    public ENode setAt(long key, byte type, Object value) {
        internal_set(key, type, value, true, false);
        return this;
    }

    @Override
    public Object get(String name) {
        return internal_get(_graph.resolver().stringToHash(name, false));
    }

    @Override
    public Object getAt(long key) {
        return internal_get(key);
    }

    @Override
    public long id() {
        return _id;
    }

    @Override
    public void drop() {
        egraph.drop(this);
    }

    @Override
    public EGraph graph() {
        return egraph;
    }

    @Override
    public Object getOrCreate(String key, byte type) {
        Object previous = get(key);
        if (previous != null) {
            return previous;
        } else {
            return getOrCreateAt(_graph.resolver().stringToHash(key, true), type);
        }
    }

    @Override
    public final Object getOrCreateAt(final long key, final byte type) {
        final int found = internal_find(key);
        if (found != -1) {
            if (_type[found] == type) {
                return _v[found];
            }
        }
        Object toSet = null;
        switch (type) {
            case Type.RELATION:
                toSet = new HeapRelation(chunk, null);
                break;
            case Type.RELATION_INDEXED:
                toSet = new HeapRelationIndexed(chunk);
                break;
            case Type.MATRIX:
                toSet = new HeapMatrix(chunk, null);
                break;
            case Type.LMATRIX:
                toSet = new HeapLMatrix(chunk, null);
                break;
            case Type.EGRAPH:
                toSet = new HeapEGraph(chunk);
                break;
            case Type.STRING_TO_LONG_MAP:
                toSet = new HeapStringLongMap(chunk);
                break;
            case Type.LONG_TO_LONG_MAP:
                toSet = new HeapLongLongMap(chunk);
                break;
            case Type.LONG_TO_LONG_ARRAY_MAP:
                toSet = new HeapLongLongArrayMap(chunk);
                break;
        }
        internal_set(key, type, toSet, true, false);
        return toSet;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        final boolean[] isFirst = {true};
        builder.append("{\"id\":");
        builder.append(id());

        for (int i = 0; i < _size; i++) {
            final Object elem = _v[i];
            final Resolver resolver = _graph.resolver();
            final long attributeKey = _k[i];
            final byte elemType = _type[i];
            if (elem != null) {
                String resolveName = resolver.hashToString(attributeKey);
                if (resolveName == null) {
                    resolveName = attributeKey + "";
                }
                switch (elemType) {
                    case Type.BOOL: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        if ((Boolean) elem) {
                            builder.append("1");
                        } else {
                            builder.append("0");
                        }
                        break;
                    }
                    case Type.STRING: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append("\"");
                        builder.append(elem);
                        builder.append("\"");
                        break;
                    }
                    case Type.LONG: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append(elem);
                        break;
                    }
                    case Type.INT: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append(elem);
                        break;
                    }
                    case Type.DOUBLE: {
                        if (!BaseNode.isNaN((double) elem)) {
                            builder.append(",\"");
                            builder.append(resolveName);
                            builder.append("\":");
                            builder.append(elem);
                        }
                        break;
                    }
                    case Type.DOUBLE_ARRAY: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append("[");
                        double[] castedArr = (double[]) elem;
                        for (int j = 0; j < castedArr.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append(castedArr[j]);
                        }
                        builder.append("]");
                        break;
                    }
                    case Type.RELATION:
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append("[");
                        Relation castedRelArr = (Relation) elem;
                        for (int j = 0; j < castedRelArr.size(); j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append(castedRelArr.get(j));
                        }
                        builder.append("]");
                        break;
                    case Type.LONG_ARRAY: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append("[");
                        long[] castedArr2 = (long[]) elem;
                        for (int j = 0; j < castedArr2.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append(castedArr2[j]);
                        }
                        builder.append("]");
                        break;
                    }
                    case Type.INT_ARRAY: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append("[");
                        int[] castedArr3 = (int[]) elem;
                        for (int j = 0; j < castedArr3.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append(castedArr3[j]);
                        }
                        builder.append("]");
                        break;
                    }
                    case Type.LONG_TO_LONG_MAP: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append("{");
                        LongLongMap castedMapL2L = (LongLongMap) elem;
                        isFirst[0] = true;
                        castedMapL2L.each(new LongLongMapCallBack() {
                            @Override
                            public void on(long key, long value) {
                                if (!isFirst[0]) {
                                    builder.append(",");
                                } else {
                                    isFirst[0] = false;
                                }
                                builder.append("\"");
                                builder.append(key);
                                builder.append("\":");
                                builder.append(value);
                            }
                        });
                        builder.append("}");
                        break;
                    }
                    case Type.RELATION_INDEXED:
                    case Type.LONG_TO_LONG_ARRAY_MAP: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append("{");
                        LongLongArrayMap castedMapL2LA = (LongLongArrayMap) elem;
                        isFirst[0] = true;
                        Set<Long> keys = new HashSet<Long>();
                        castedMapL2LA.each(new LongLongArrayMapCallBack() {
                            @Override
                            public void on(long key, long value) {
                                keys.add(key);
                            }
                        });
                        final Long[] flatKeys = keys.toArray(new Long[keys.size()]);
                        for (int k = 0; k < flatKeys.length; k++) {
                            long[] values = castedMapL2LA.get(flatKeys[k]);
                            if (!isFirst[0]) {
                                builder.append(",");
                            } else {
                                isFirst[0] = false;
                            }
                            builder.append("\"");
                            builder.append(flatKeys[k]);
                            builder.append("\":[");
                            for (int j = 0; j < values.length; j++) {
                                if (j != 0) {
                                    builder.append(",");
                                }
                                builder.append(values[j]);
                            }
                            builder.append("]");
                        }
                        builder.append("}");
                        break;
                    }
                    case Type.STRING_TO_LONG_MAP: {
                        builder.append(",\"");
                        builder.append(resolveName);
                        builder.append("\":");
                        builder.append("{");
                        StringLongMap castedMapS2L = (StringLongMap) elem;
                        isFirst[0] = true;
                        castedMapS2L.each(new StringLongMapCallBack() {
                            @Override
                            public void on(String key, long value) {
                                if (!isFirst[0]) {
                                    builder.append(",");
                                } else {
                                    isFirst[0] = false;
                                }
                                builder.append("\"");
                                builder.append(key);
                                builder.append("\":");
                                builder.append(value);
                            }
                        });
                        builder.append("}");
                        break;
                    }

                }
            }
        }
        builder.append("}");
        return builder.toString();
    }

}
