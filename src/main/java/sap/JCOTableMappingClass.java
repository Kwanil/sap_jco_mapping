package sap;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JCOTableMappingClass {
	private final Map<String, Class<?>> mappingInfo = new HashMap<>();

	private JCOTableMappingClass(String tableName, Class<?> mappingClass) {
		put(tableName, mappingClass);
	}
	
	public Map<String, Class<?>> getMappingInfo() {
		return mappingInfo;
	}
	
	public static JCOTableMappingClass of(String tableName, Class<?> mappingClass){
		return new JCOTableMappingClass(tableName, mappingClass);
	}
	
	public JCOTableMappingClass add(String tableName, Class<?> mappingClass) {
		put(tableName, mappingClass);
		return this;
	}
	
	private void put(String tableName, Class<?> mappingClass) {
		Objects.requireNonNull(tableName);
		Objects.requireNonNull(mappingClass);
		mappingInfo.put(tableName, mappingClass);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
