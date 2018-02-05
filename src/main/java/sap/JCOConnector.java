package sap;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Kwanil
 */
public class JCOConnector {
	private static final int CONNECTIONS = 5;
	private PoolManager singleton = PoolManager.singleton();
	private IRepository repository;
	private Client client;
	private Function function;

	public static JCOConnector create(Properties properties, String repositoryName, String poolName) {
		return new JCOConnector(properties, repositoryName, poolName);
	}

	@SuppressWarnings("deprecation")
	private JCOConnector(Properties properties, String repositoryName, String poolName) {
		if (singleton.getPool(poolName) == null) {
			singleton.addClientPool(poolName, CONNECTIONS, properties);
		}
		client = Objects.requireNonNull(singleton.getClient(poolName));
		repository = Objects.requireNonNull(JCO.createRepository(repositoryName, poolName));
	}

	@SuppressWarnings("deprecation")
	public void release() {
		JCO.releaseClient(client);
	}

	JCOResult executeTable(String functionName, Collection<String> tableNames) {
		JCOResult result = execute(functionName);
		for (String tableName : tableNames) {
			result.putTable(tableName, getTable(functionName, tableName));
		}
		return result;
	}

	JCOResult execute(String functionName) {
		Objects.requireNonNull(functionName, "functionName must not be null");
		client.execute(getFunction(functionName));
		return new JCOResult(getOutput(functionName));
	}

	Function getFunction(String functionName) {
		Objects.requireNonNull(functionName, "functionName must not be null");
		if (this.function == null) {
			this.function = repository.getFunctionTemplate(functionName).getFunction();
		}
		return this.function;
	}

	ParameterList getInput(String functionName) {
		Objects.requireNonNull(functionName, "functionName must not be null");
		return getFunction(functionName).getImportParameterList();
	}

	ParameterList getOutput(String functionName) {
		Objects.requireNonNull(functionName, "functionName must not be null");
		return getFunction(functionName).getExportParameterList();
	}

	Structure getOutputStructure(String functionName, String structureName) {
		Objects.requireNonNull(functionName, "functionName must not be null");
		Objects.requireNonNull(structureName, "structureName must not be null");
		return getFunction(functionName).getExportParameterList().getStructure(structureName);
	}
	
	Structure getInputStructure(String functionName, String structureName) {
		Objects.requireNonNull(functionName, "functionName must not be null");
		Objects.requireNonNull(structureName, "structureName must not be null");
		return getFunction(functionName).getImportParameterList().getStructure(structureName);
	}

	Table getTable(String functionName, String tableName) {
		Objects.requireNonNull(functionName, "functionName must not be null");
		if (StringUtils.isEmpty(tableName)) {
			return null;
		}
		return getFunction(functionName).getTableParameterList().getTable(tableName);
	}
}
