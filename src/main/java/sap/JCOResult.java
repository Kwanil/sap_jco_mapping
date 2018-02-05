package sap;

import com.sap.mw.jco.JCO.ParameterList;
import com.sap.mw.jco.JCO.Structure;
import com.sap.mw.jco.JCO.Table;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Kwanil
 */
public class JCOResult {
	private final ParameterList output;
	private final Map<String, Table> tableMap = new HashMap<String, Table>();

	JCOResult(ParameterList output) {
		this.output = Objects.requireNonNull(output, "output must not be null.");
	}

	public ParameterList getOutput() {
		return output;
	}

	public Structure getStructure(String structureName) {
		return output.getStructure(structureName);
	}

	public Table getTable(String tableName) {
		return tableMap.get(tableName);
	}

	public Table putTable(String tableName, Table table) {
		return tableMap.put(tableName, table);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
