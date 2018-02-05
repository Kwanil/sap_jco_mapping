package sap;

import java.util.*;

/**
 * @author Kwanil
 */
public class JCOInput {
	private String functionName;
	private List<String> outputTableNames = new ArrayList<String>();
	
	private Object input;
	private final Map<String, List<Object>> inputTables = new HashMap<String, List<Object>>();

	private JCOInput(String functionName) {
		this(functionName, null);
	}
	
	private JCOInput(String functionName, List<String> outputTableNames) {
		this.functionName = Objects.requireNonNull(functionName, "functionName must not be null");
		if(outputTableNames != null) {
			this.outputTableNames = new ArrayList<>(outputTableNames);
		}
	}

	String getFunctionName() {
		return functionName;
	}

	List<String> getTableNames() {
		return outputTableNames;
	}
	
	Object getInput() {
		return input;
	}
	
	Map<String, List<Object>> getInputTables() {
		return inputTables;
	}
	
	public JCOInput table(String... outputTables) {
		this.outputTableNames.addAll(Arrays.asList(outputTables));
		return this;
	}

	public JCOInput input(Object input){
		this.input = input;
		return this;
	}
	
	public JCOInput input(Map<String, Object> input){
		this.input = input;
		return this;
	}
	
	public JCOInput addInputTable(String tableName, Map<String, Object> inputTable) {
		return addInputTable(tableName, (Object)inputTable);
	}
	
	public JCOInput addInputTable(String tableName, Object inputTable) {
		if(this.inputTables.containsKey(tableName)) {
			List<Object> list = this.inputTables.get(tableName);
			list.add(inputTable);
			return this;
		}
		this.inputTables.put(tableName, new ArrayList<Object>(Arrays.asList(inputTable)));
		return this;
	}

	public JCOInput addInputTable(String tableName, List<? extends Object> inputTable) {
		this.inputTables.put(tableName,new ArrayList<Object>(inputTable));
		return this;
	}
	
	public static JCOInput of(String functionName, String... outputTables){
		return new JCOInput(functionName, Arrays.asList(outputTables));
	}

	public static JCOInput of(String functionName) {
		return new JCOInput(functionName);
	}
}
