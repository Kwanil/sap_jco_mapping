package sap;

import com.sap.mw.jco.JCO.Field;
import com.sap.mw.jco.JCO.Record;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Kwanil
 */
@SuppressWarnings("rawtypes")
public class JCOMapMapper implements JCOMapper<Map> {
	@Override
	public boolean canRead(Class<?> output) {
		return Map.class.isAssignableFrom(output);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void mapping(Field field, Map map) {
		Object value = field.getValue();
		if(value instanceof String) {
			value = ((String) value).trim();
		}
		map.put(field.getName(), value);
	}

	@Override
	public void input(Record jcoObject, Map map) {
		for(Object key :  map.keySet()) {
			Object value = ObjectUtils.defaultIfNull(map.get(key), StringUtils.EMPTY);
			jcoObject.setValue(String.valueOf(value), String.valueOf(key));
		}
	}

}
