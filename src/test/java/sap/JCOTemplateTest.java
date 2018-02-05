package sap;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JCOTemplateTest {
	JCOTemplate template = new JCOTemplate();

	@Test
	public void load() throws Exception {
		Properties properties = template.load("jco.properties");
		assertThat(properties.getProperty("jco.client.lang"), is("KO"));
	}

	@Test
	public void executeTableClientMap() throws Exception {
		JCOInput input = JCOInput.of("FUNCTION_NAME", "TABLE_NAME");
		List<HashMap> list = template.executeTable(input, HashMap.class);
		System.out.println(list);
		assertTrue(!list.isEmpty());
	}

	@Test
	public void executeTableClientInputMap() throws Exception {
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("IV_TYPE", "A");
		inputMap.put("IV_YEAR", "2017");
		inputMap.put("IV_NAME", "테스트한글");

		JCOInput input = JCOInput.of("TEST", "DATA").input(inputMap);
		List<HashMap> list = template.executeTable(input, HashMap.class);
		System.out.println(list);
	}

	@Test
	public void executeTableClientObject() throws Exception {
		JCOInput input = JCOInput.of("FUNCTION_NAME", "TABLE_NAME");
		List<Result> list = template.executeTable(input, Result.class);
		System.out.println(list);
		assertTrue(!list.isEmpty());
		System.out.println(list.size());
	}

	@Test
	public void executeTableClientInputObject() throws Exception {
		Input inputObject = new Input();
		inputObject.setName("테스트한글");
		inputObject.setType("A");
		inputObject.setYear("2017");

		JCOInput input = JCOInput.of("FUNCTION_NAME", "TABLE_NAME").input(inputObject);
		List<Result> list = template.executeTable(input, Result.class);

		assertTrue(list.size() > 0);
	}

	@Test
	public void executeOutputClientMap() throws Exception {
		JCOInput input = JCOInput.of("FUNCTION_NAME", "TABLE_NAME");
		HashMap output = template.executeOutput(input, HashMap.class);
		System.out.println(output);
		assertTrue(!output.isEmpty());
	}


	@Test
	public void executeOutputClientObject() throws Exception {
		JCOInput input = JCOInput.of("FUNCTION_NAME", "TABLE_NAME");
		Result output = template.executeOutput(input, Result.class);
		System.out.println(output);
		assertTrue(output != null);
	}

	@Test
	public void executeOutputClientInputObject() throws Exception {
		Input inputObject = new Input();
		inputObject.setName("테스트한글");
		inputObject.setType("A");
		inputObject.setYear("2017");

		JCOInput input = JCOInput.of("FUNCTION_NAME", "TABLE_NAME").input(inputObject);
		Result output = template.executeOutput(input, Result.class);
		System.out.println(output);
		assertTrue(output != null);
	}

	static class Input {
		@JCOValue("IV_TYPE")
		private String type;
		@JCOValue("IV_YEAR")
		private String year;
		@JCOValue("IV_NAME")
		private String name;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getYear() {
			return year;
		}

		public void setYear(String year) {
			this.year = year;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	static class Result {
		@JCOValue("NAME")
		private String name;
		@JCOValue(value = "LOGTIME", dateFormat = "yyyy-mm-dd")
		private String logTime;
		@JCOValue("KEY")
		private int key;
		@JCOValue("MESSAGE")
		private String message;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLogTime() {
			return logTime;
		}

		public void setLogTime(String logTime) {
			this.logTime = logTime;
		}

		public int getKey() {
			return key;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
}