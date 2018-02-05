package sap;

import org.apache.commons.lang.StringUtils;

import java.lang.annotation.*;
import java.util.Date;

/**
 * Annotation for object relation mapping
 * If you declare a field in Value Object and put the JCO Key in value, it is mapped to the Object field.
 * 
 * @see JCOAnnotationMapper#input(com.sap.mw.jco.JCO.Record, Object)
 * @see JCOAnnotationMapper#mapping(com.sap.mw.jco.JCO.Field, Object)
 * 
 * @author Kwanil
 */
@Documented
@Inherited
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface JCOValue {
	
	/**
	 * required field, put the JCO key
	 * @return jco key
	 */
	String value();
	
	/**
	 * The format is mapped if the class of the jco value is {@link Date} and the field is {@link String}
	 * @return dateformat
	 */
	String dateFormat() default StringUtils.EMPTY;
}
