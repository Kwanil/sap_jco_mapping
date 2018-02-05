# sap_jco_mapping
sap jco connector에서 통신시 java object relation mapping을 하는 방법

- 소스는 https://github.com/hibersap/hibersap-sapjco3 에서 다운받아서 import를 한다.

- 이 아이디어는 spring에서 있는 resttemplate, asyncresttemplate의 방식을 차용하여 
SAP의 테이블 데이터를 가져오는 java object relation mapping을 구현한 것이다.
- 이 로직엔 기본 아이디어만 간단히 구현하였기 때문에, java generics의 super type token에 관한 부분은 처리하지 않았다.
또한, sap의 structure부분도 정상구현하지 않았다.

### AS-IS
SAP의 sample code는 구글 등에서 검색하여 보면 아래의 URL등이 나온다.
 - http://www.erpworkbench.com/java/jco/jco_callfunc.htm
 - https://www.programcreek.com/java-api-examples/index.php?api=com.sap.conn.jco.JCoFunction
 - https://blogs.sap.com/2017/08/25/sap-jco-server-example/
 
위의 로직의 핵심코드는 다음과 같은 소스인데 function의 Table을 파라미터 보내고, 받아서 iterate를 하여 일일히 매핑을 해주는 방식이다.
```
JCoFunction function = destination.getRepository().getFunction("SAP_DATA"); 
JCoTable codes = function.getTableParameterList().getTable("테이블명");
List<Map<String, Object>> outputs = new ArrayList<>(); 
for (int i = 0; i < codes.getNumRows(); i++) { 
      codes.setRow(i);    
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("컬럼1", codes.getString("컬럼1"));
      map.put("컬럼2", codes.getString("컬럼2"));
      map.put("컬럼3", codes.getString("컬럼3"));
      //리스트에 담아서 사용
     outputs.add(map);
}
System.out.println(outputs);
```

### TO-BE (Object relation mapping)
내가 구현한 아이디어는 위의로직을 다음과 같은 심플한 코드로 바꿀수있게하는것이다.

```
JCOInput input = JCOInput.of("SAP_DATA", "테이블명");
List<HashMap> outputs = template.executeTable(input, HashMap.class);
System.out.println(outputs);
```

Map이 아닌 Object도 mapping이 가능한데, 그 연결고리는 `@JCOValue`이다.
위와 동일한 소스를 Map이 아닌 Output Object로 구현한다면 아래와 같은 코드가 될 것이다.
```
@Getter @Setter
class Output {
  @JCOValue("컬럼1")
  private String type;
  
  @JCOValue("컬럼2")
  private String year;
  
  @JCOValue("컬럼3")
  private String name;
  
  @JCOValue(value = "LOGTIME", dateFormat = "yyyy-mm-dd")
  private String logTime;
}

JCOInput input = JCOInput.of("SAP_DATA", "테이블명");
List<Output> outputs = template.executeTable(input, Output.class);
System.out.println(outputs);
```
