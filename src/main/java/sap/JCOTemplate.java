package sap;

import com.sap.mw.jco.JCO.FieldIterator;
import com.sap.mw.jco.JCO.ParameterList;
import com.sap.mw.jco.JCO.Record;
import com.sap.mw.jco.JCO.Table;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.core.io.ClassPathResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Class that make JCOClient calls and Object Relation Mapping This class create
 * a {@link JCOConnector} and map the Object through {@link JCOMapper}. When
 * JCOTemplate create a JCOConnector, JCOTemplate load the properties you set in
 * {@link #setResourcePath(String)}. The default of {@link JCOMapper} are
 * {@link JCOMapMapper} and {@link JCOAnnotationMapper}
 *
 * @author Kwanil
 *
 * @see JCOConnector
 * @see JCOMapper
 */
public class JCOTemplate {

	private String resourcePath = "jco.properties";
	private String repositoryName = "JCO";

	// The output must have key / value to pass the validation.
	private String successKey = "EV_RETCD";
	private String successValue = "S";
	private boolean skipValidation = false;

	private String poolName = String.valueOf(Calendar.getInstance().getTimeInMillis());

	@SuppressWarnings("rawtypes")
	private List<JCOMapper> mappers = new ArrayList<JCOMapper>();
	{
		mappers.add(new JCOMapMapper());
		mappers.add(new JCOAnnotationMapper());
	}

	public void setMappers(@SuppressWarnings("rawtypes") List<JCOMapper> mappers) {
		this.mappers = mappers;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public void setSuccessKey(String successKey) {
		this.successKey = successKey;
	}

	public void setSuccessValue(String successValue) {
		this.successValue = successValue;
	}

	public void setSkipValidation(boolean skipValidation) {
		this.skipValidation = skipValidation;
	}

	Properties load(String propName) throws IOException {
		ClassPathResource resource = new ClassPathResource(propName);
		if (!resource.exists()) {
			throw new FileNotFoundException("file not found : " + propName);
		}
		Properties properties = new Properties();
		properties.load(resource.getInputStream());
		return properties;
	}

	/**
	 * Call JCOClient and object mapping
	 *
	 * @param input
	 *            {@link JCOInput}
	 * @param outputClass
	 *            {@link Map} or Value Objects
	 * @return List (mapped {@link Map} or Value Objects)
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	public <T> List<T> executeTable(JCOInput input, Class<T> outputClass) throws IOException, ReflectiveOperationException {
		Objects.requireNonNull(input);
		Objects.requireNonNull(outputClass);
		LinkedList<String> tableNames = new LinkedList<>(input.getTableNames());
		if(tableNames.isEmpty() || tableNames.getFirst() == null) {
			throw new IllegalArgumentException("tableName must not be empty");
		}
		return executeTables(input, outputClass).table(tableNames.getFirst());
	}

	/**
	 * Call JCOClient and object mapping
	 *
	 * @param input
	 *            {@link JCOInput}
	 * @param outputClass
	 *            {@link Map} or Value Objects
	 * @return JCOTables {@link JCOTables}
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	public JCOTables executeTables(JCOInput input, Class<?> outputClass) throws IOException, ReflectiveOperationException {
		Objects.requireNonNull(input);
		Objects.requireNonNull(outputClass);
		List<String> tableNames = input.getTableNames();
		JCOTableMappingClass mappingClass = null;
		for(String tableName : tableNames) {
			mappingClass = (mappingClass == null) ?  JCOTableMappingClass.of(tableName, outputClass) : mappingClass.add(tableName, outputClass);
		}
		return executeTables(input, mappingClass);
	}

	/**
	 * Call JCOClient and object mapping
	 *
	 * @param input
	 *            {@link JCOInput}
	 * @param mappingClass
	 *            {@link Map} or Value Objects
	 * @return JCOTables {@link JCOTables}
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	public JCOTables executeTables(JCOInput input, JCOTableMappingClass mappingClass) throws IOException, ReflectiveOperationException {
		Objects.requireNonNull(input);
		Objects.requireNonNull(mappingClass);
		JCOConnector connector = null;
		try {
			connector = createConnector();
			mappingJCOInput(connector, input);
			Collection<String> tableNames = CollectionUtils.union(mappingClass.getMappingInfo().keySet(), input.getTableNames());
			if(tableNames.isEmpty()) {
				throw new IllegalArgumentException("tableName must not be empty");
			}
			JCOResult jcoResult = connector.executeTable(input.getFunctionName(), tableNames);
			if (!isValid(jcoResult.getOutput())) {
				throw new IllegalStateException("Fail to result state : " + jcoResult);
			}
			JCOTables result = new JCOTables();
			Map<String, Class<?>> mappingInfo = mappingClass.getMappingInfo();
			for (String tableName : tableNames) {
				result.put(tableName, mappingList(jcoResult.getTable(tableName), mappingInfo.get(tableName)));
			}
			return result;
		} finally {
			releaseConnection(connector);
		}
	}

	/**
	 * Call JCOClient and object mapping
	 *
	 * @param input
	 *            {@link JCOInput}
	 * @param outputClass
	 *            {@link Map} or Value Objects
	 * @return (mapped {@link Map} or Value Objects)
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	public <T> T executeOutput(JCOInput input, Class<T> outputClass) throws IOException, ReflectiveOperationException {
		Objects.requireNonNull(input);
		Objects.requireNonNull(outputClass);
		JCOConnector connector = null;
		try {
			connector = createConnector();
			mappingJCOInput(connector, input);
			JCOResult jcoResult = connector.execute(input.getFunctionName());
			if (!isValid(jcoResult.getOutput())) {
				throw new IllegalStateException("Fail to result state : " + jcoResult);
			}
			return mappingObject(jcoResult.getOutput(), outputClass);
		} finally {
			releaseConnection(connector);
		}
	}

	private JCOConnector createConnector() throws IOException {
		JCOConnector jcoConnector = JCOConnector.create(load(resourcePath), repositoryName, poolName);
		return jcoConnector;
	}

	private void releaseConnection(JCOConnector connector) {
		if (connector != null) {
			connector.release();
		}
	}

	void mappingJCOInput(JCOConnector connector, JCOInput input) {
		String functionName = input.getFunctionName();
		Object inputObj = input.getInput();
		if(inputObj != null) {
			mappingInputObject(connector.getInput(functionName), inputObj);
		}
		Map<String, List<Object>> inputTables = input.getInputTables();
		if(!inputTables.isEmpty()) {
			for (Entry<String, List<Object>> entry : inputTables.entrySet()) {
				Table table = connector.getTable(functionName, entry.getKey());
				for (Object value : entry.getValue()) {
					table.appendRow();
					mappingInputObject(table, value);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	void mappingInputObject(final Record record, Object input) {
		if (record == null || input == null) {
			return;
		}
		selectMapper(input.getClass()).input(record, input);
	}

	<T> T mappingObject(Record record, Class<T> outputClass) throws ReflectiveOperationException {
		if (record == null || outputClass == null) {
			return null;
		}
		return mappingObject(record, outputClass, selectMapper(outputClass));
	}

	<T> List<T> mappingList(Table table, Class<T> outputClass) throws ReflectiveOperationException {
		if (table == null || outputClass == null) {
			return Collections.emptyList();
		}
		return mappingList(table, outputClass, selectMapper(outputClass));
	}

	@SuppressWarnings("rawtypes")
	private <T> List<T> mappingList(Table table, Class<T> outputClass, JCOMapper jcoMapper)	throws ReflectiveOperationException {
		List<T> list = new ArrayList<T>();
		for (int i = 0; i < table.getNumRows(); i++) {
			table.setRow(i);
			list.add(mappingObject(table, outputClass, jcoMapper));
		}
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> T mappingObject(Record record, Class<T> outputClass, JCOMapper jcoMapper) throws ReflectiveOperationException {
		T instance = outputClass.newInstance();
		for (FieldIterator fields = record.fields(); fields.hasMoreElements();) {
			jcoMapper.mapping(fields.nextField(), instance);
		}
		return instance;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	<T> JCOMapper selectMapper(Class<T> clazz) {
		for (JCOMapper mapper : mappers) {
			if (mapper.canRead(clazz)) {
				return mapper;
			}
		}
		throw new IllegalStateException("Not found Mapper!" + ToStringBuilder.reflectionToString(mappers));
	}

	boolean isValid(ParameterList output) {
		try {
			if (skipValidation) {
				return true;
			}
			return StringUtils.equals(String.valueOf(Objects.requireNonNull(output).getValue(successKey)), successValue);
		} catch (Exception e) {
			return false;
		}
	}

}
