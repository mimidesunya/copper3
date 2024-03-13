package net.zamasoft.cache;

import java.util.Collections;
import java.util.Map;

import net.zamasoft.reader.util.LRUCache;

public class MRUReference<T> {
	private static final Map<Object,Object> CACHE = Collections.synchronizedMap(new LRUCache<Object,Object>());
	private T referent;
	
	public MRUReference(T referent) {
		this.referent = referent;
		CACHE.put(referent, referent);
	}
	
	@SuppressWarnings("unchecked")
	public T get() {
		return (T)CACHE.get(this.referent);
	}
}
