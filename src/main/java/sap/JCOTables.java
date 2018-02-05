package sap;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JCOTables
 * 
 * @author Kwanil
 *
 */
public class JCOTables {
	private Map<String, List<?>> tables = new HashMap<String, List<?>>();

	void put(String tableName, List<?> list) {
		this.tables.put(tableName, list);
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> table(String name) {
		return (List<T>) this.tables.get(name);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
