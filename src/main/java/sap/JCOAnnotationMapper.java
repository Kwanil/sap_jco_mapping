package sap;

import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Record;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kwanil
 */
public class JCOAnnotationMapper implements JCOMapper<Object>{
	
	private final static Class<JCOValue> annotationClass = JCOValue.class;

	@Override
	public boolean canRead(Class<?> output) {
		return true;
	}

	@Override
	public void mapping(final JCO.Field field, final Object instance) {
		FieldCallback mappingCallback = new FieldCallback() {
			@Override
			public void doWith(Field f) throws IllegalArgumentException, IllegalAccessException {
				setField(f, field, instance);
			}

			private void setField(Field f, final JCO.Field field, final Object instance) throws IllegalAccessException {
				Object value = ObjectUtils.defaultIfNull(field.getValue(), StringUtils.EMPTY);
				ReflectionUtils.makeAccessible(f);
				if(f.getType().equals(String.class) && Date.class.isAssignableFrom(value.getClass())){
					f.set(instance, DateFormatUtils.format(field.getDate(), f.getAnnotation(annotationClass).dateFormat()));
				} else if (String.class.equals(value.getClass())) {
					setFieldAfterConverting(f, instance, String.valueOf(value).trim());
				} else if(equals(f, value)){
					f.set(instance, value);
				}
			}

			private boolean equals(Field f, Object value) {
				return f.getType().equals(value.getClass());
			}
		};
		FieldFilter mappingFieldFilter = new FieldFilter() {
			@Override
			public boolean matches(Field f) {
				if(!f.isAnnotationPresent(annotationClass)) {
					return false;
				}
				JCOValue jcoValue = f.getAnnotation(annotationClass);
				return StringUtils.equals(field.getName(), jcoValue.value());
			}
		};
		ReflectionUtils.doWithFields(instance.getClass(), mappingCallback, mappingFieldFilter);
	}

	@Override
	public void input(final Record jcoObject, final Object input) {
		FieldCallback inputFieldCallback = new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				JCOValue annotation = field.getAnnotation(annotationClass);
				String key = annotation.value();
				ReflectionUtils.makeAccessible(field);
				jcoObject.setValue(String.valueOf(field.get(input)), key);
			}
		};
		FieldFilter inputFieldFilter = new FieldFilter() {
			@Override
			public boolean matches(Field field) {
				return field.isAnnotationPresent(annotationClass);
			}
		};
		ReflectionUtils.doWithFields(input.getClass(), inputFieldCallback, inputFieldFilter);
	}

	void setFieldAfterConverting(Field field, Object instance, String value) throws IllegalArgumentException, IllegalAccessException{
		if(converters.containsKey(field.getType())){
			Converter<?> converter = converters.get(field.getType());
			field.set(instance, converter.convert(value, field));
		}
	}
	
	static final Map<Class<?>,Converter<?>> converters = new HashMap<Class<?>,Converter<?>>();
	static {
		converters.put(String.class, new Converter<String>() {
			public String convert(String value, Field field) {
				return StringUtils.trim(value);
			}
		});
		converters.put(Byte.class, new Converter<Byte>() {
			public Byte convert(String value, Field field) {
				return Byte.parseByte(value);
			}
		});
		converters.put(byte.class, new Converter<Byte>() {
			public Byte convert(String value, Field field) {
				return Byte.parseByte(value);
			}
		});
		converters.put(Short.class, new Converter<Short>() {
			public Short convert(String value, Field field) {
				return Short.parseShort(value);
			}
		});
		converters.put(short.class, new Converter<Short>() {
			public Short convert(String value, Field field) {
				return Short.parseShort(value);
			}
		});
		converters.put(Integer.class, new Converter<Integer>() {
			public Integer convert(String value, Field field) {
				return Integer.parseInt(value);
			}
		});
		converters.put(int.class, new Converter<Integer>() {
			public Integer convert(String value, Field field) {
				return Integer.parseInt(value);
			}
		});
		converters.put(Long.class, new Converter<Long>() {
			public Long convert(String value, Field field) {
				return Long.parseLong(value);
			}
		});
		converters.put(long.class, new Converter<Long>() {
			public Long convert(String value, Field field) {
				return Long.parseLong(value);
			}
		});
		converters.put(Double.class, new Converter<Double>() {
			public Double convert(String value, Field field) {
				return Double.parseDouble(value);
			}
		});
		converters.put(double.class, new Converter<Double>() {
			public Double convert(String value, Field field) {
				return Double.parseDouble(value);
			}
		});
		converters.put(BigInteger.class, new Converter<BigInteger>() {
			public BigInteger convert(String value, Field field) {
				return new BigInteger(value);
			}
		});
		converters.put(BigDecimal.class, new Converter<BigDecimal>() {
			public BigDecimal convert(String value, Field field) {
				return new BigDecimal(value);
			}
		});
		converters.put(Date.class, new Converter<Date>() {
			public Date convert(String value, Field field) {
				try {
					JCOValue annotation = field.getAnnotation(annotationClass);
					return DateUtils.parseDate(value,  annotation.dateFormat());
				} catch (ParseException e) {
					return null;
				}
			}
		});
	}
	
	static interface Converter<T> {
		T convert(String value, Field field);
	}
}
