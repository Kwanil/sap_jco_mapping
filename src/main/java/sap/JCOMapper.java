package sap;

import com.sap.mw.jco.JCO;

/**
 * @author Kwanil
 */
public interface JCOMapper<T> {

	boolean canRead(Class<?> output);
	
	void mapping(JCO.Field field, T target);
	
	void input(JCO.Record jcoObject, T source);
}
