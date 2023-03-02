package gov.nist.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface MultiValueMap<K,V> extends Map<K,List<V>>, Serializable {
    @Override
	public boolean remove( Object key, Object item );
}
