package me.gerenciar.sdao.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReflectionHelper
{
	public static Field getField(String fieldName, Class<?> type)
	{
		try
		{
			return type.getDeclaredField(fieldName);
		}
		catch(NoSuchFieldException | SecurityException exception)
		{
			List<Field> fields = getFields(type);
			
			for(Field field : fields)
			{
				if(field.getName().equals(fieldName))
				{
					return field;
				}
			}
			
			return null;
		}
	}
	
	public static void setValue(String fieldName, Object instance, Object value)
	{
		Field field = ReflectionHelper.getField(fieldName, instance.getClass());
		
		if(field != null)
		{
			try
			{
				field.setAccessible(true);
				field.set(instance, value);
			}
			catch(IllegalArgumentException | IllegalAccessException exception)
			{
			
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getValue(String fieldName, Object instance)
	{
		try
		{
			Field field = getField(fieldName, instance.getClass());
			field.setAccessible(true);
			
			return field != null ? (T) field.get(instance) : null;
		}
		catch(IllegalArgumentException | IllegalAccessException | SecurityException exception)
		{
			return null;
		}
	}
	
	public static List<Field> getFields(Object instance)
	{
		List<Field> fields = new ArrayList<>();
		Class<?> type = instance.getClass();
		
		getFields(fields, type);
		
		return fields;
	}
	
	public static List<Field> getFields(Class<?> type)
	{
		List<Field> fields = new ArrayList<>();
		
		getFields(fields, type);
		
		return fields;
	}
	
	private static void getFields(List<Field> fields, Class<?> type)
	{
		for(Field field : type.getDeclaredFields())
		{
			fields.add(field);
		}
		
		if(type.getSuperclass() != null)
		{
			getFields(fields, type.getSuperclass());
		}
	}
	
	public static List<Field> getEnumFieldsConstants(Class<?> type)
	{
		List<Field> enumFieldsConstants = new ArrayList<>();
		
		List<Field> fields = getFields(type);
		
		for(Field field : fields)
		{
			if(field.isEnumConstant())
			{
				enumFieldsConstants.add(field);
			}
		}
		
		return enumFieldsConstants;
	}
	
	public static String fieldToName(Field field)
	{
		return field.getName();
	}
	
	public static List<String> fieldsToNames(Collection<Field> fields)
	{
		List<String> names = new ArrayList<>();
		
		for(Field field : fields)
		{
			names.add(fieldToName(field));
		}
		
		return names;
	}
	
	public static boolean isNull(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return field.get(instance) == null;
	}
	
	public static boolean isNull(Object object)
	{
		return object == null;
	}
	
	public static boolean isArray(Field field)
	{
		return field.getType().isArray();
	}
	
	public static boolean isArray(Object object)
	{
		return object.getClass().isArray();
	}
	
	public static boolean isNumeric(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return (Short.class.isAssignableFrom(field.getType()) || Integer.class.isAssignableFrom(field.getType()) || Long.class.isAssignableFrom(field.getType()) || Float.class.isAssignableFrom(field.getType()) || Double.class.isAssignableFrom(field.getType())) || isNumeric(field.get(instance));
	}
	
	public static boolean isNumeric(Object object)
	{
		return object instanceof Short || object instanceof Integer || object instanceof Long || object instanceof Float || object instanceof Double;
	}
	
	public static boolean isBoolean(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return Boolean.class.isAssignableFrom(field.getType()) || isBoolean(field.get(instance));
	}
	
	public static boolean isBoolean(Object object)
	{
		return object instanceof Boolean;
	}
	
	public static boolean isCharacter(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return Character.class.isAssignableFrom(field.getType()) || isCharacter(field.get(instance));
	}
	
	public static boolean isCharacter(Object object)
	{
		return object instanceof Character;
	}
	
	public static boolean isString(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return String.class.isAssignableFrom(field.getType()) || isString(field.get(instance));
	}
	
	public static boolean isString(Object object)
	{
		return object instanceof String;
	}
	
	public static boolean isList(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return List.class.isAssignableFrom(field.getType()) || isList(field.get(instance));
	}
	
	public static boolean isList(Object object)
	{
		return object instanceof List;
	}
	
	public static boolean isSet(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return Set.class.isAssignableFrom(field.getType()) || isSet(field.get(instance));
	}
	
	public static boolean isSet(Object object)
	{
		return object instanceof Set;
	}
	
	public static boolean isMap(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return Map.class.isAssignableFrom(field.getType()) || field.get(instance) instanceof Map;
	}
	
	public static boolean isMap(Object object)
	{
		return object instanceof Map;
	}
	
	public static boolean isPrimitive(Field field, Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return (isNumeric(field, instance) || isString(field, instance) || isCharacter(field, instance) || isBoolean(field, instance)) && !isArray(field);
	}
	
	public static boolean isPrimitive(Object object)
	{
		return (isNumeric(object) || isString(object) || isCharacter(object) || isBoolean(object)) && !isArray(object);
	}
	
	public static Object generateDefaultValue(Class<?> type)
	{
		try
		{
			if(Integer.class.isAssignableFrom(type))
			{
				return type.getConstructor(int.class).newInstance(0);
			}
			else if(Short.class.isAssignableFrom(type))
			{
				return type.getConstructor(short.class).newInstance((short) 0);
			}
			else if(Long.class.isAssignableFrom(type))
			{
				return type.getConstructor(long.class).newInstance(0);
			}
			else if(Float.class.isAssignableFrom(type))
			{
				return type.getConstructor(float.class).newInstance(0);
			}
			else if(Double.class.isAssignableFrom(type))
			{
				return type.getConstructor(double.class).newInstance(0);
			}
			else if(Boolean.class.isAssignableFrom(type))
			{
				return type.getConstructor(boolean.class).newInstance(false);
			}
			else if(Character.class.isAssignableFrom(type))
			{
				return type.getConstructor(char.class).newInstance('\u0000');
			}
			else if(Byte.class.isAssignableFrom(type))
			{
				return type.getConstructor(byte.class).newInstance((byte) 0);
			}
			else
			{
				return type.newInstance();
			}
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException exception)
		{
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object generateBasicValue(String value, Class<?> type)
	{
		if(value != null)
		{
			if(String.class.isAssignableFrom(type))
			{
				return value;
			}
			else if(Boolean.class.isAssignableFrom(type))
			{
				return Boolean.valueOf(value);
			}
			else if(Character.class.isAssignableFrom(type))
			{
				return value.charAt(0);
			}
			else if(Integer.class.isAssignableFrom(type))
			{
				return Integer.valueOf(value);
			}
			else if(Short.class.isAssignableFrom(type))
			{
				return Short.valueOf(value);
			}
			else if(Long.class.isAssignableFrom(type))
			{
				return Long.valueOf(value);
			}
			else if(Float.class.isAssignableFrom(type))
			{
				return Float.valueOf(value);
			}
			else if(Double.class.isAssignableFrom(type))
			{
				return Double.valueOf(value);
			}
			else if(Byte.class.isAssignableFrom(type))
			{
				return Byte.valueOf(value);
			}
			else if(Enum.class.isAssignableFrom(type))
			{
				List<Field> fields = getEnumFieldsConstants(type);
				
				for(Field field : fields)
				{
					field.setAccessible(true);
					
					if(field.getName().equals(value))
					{
						return Enum.valueOf((Class<? extends Enum>) type, value);
					}
				}
				
				return null;
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	public static Object generateBasicValue(String value, Field field)
	{
		return generateBasicValue(value, field.getType());
	}
	
	public static Object generateBasicValue(String value, Object defaultValue)
	{
		if(defaultValue != null)
		{
			if(defaultValue instanceof String)
			{
				return value;
			}
			else if(defaultValue instanceof Boolean)
			{
				return Boolean.valueOf(value);
			}
			else if(defaultValue instanceof Character)
			{
				return value.charAt(0);
			}
			else if(defaultValue instanceof Integer)
			{
				return Integer.valueOf(value);
			}
			else if(defaultValue instanceof Short)
			{
				return Short.valueOf(value);
			}
			else if(defaultValue instanceof Long)
			{
				return Long.valueOf(value);
			}
			else if(defaultValue instanceof Float)
			{
				return Float.valueOf(value);
			}
			else if(defaultValue instanceof Double)
			{
				return Double.valueOf(value);
			}
			else if(defaultValue instanceof Byte)
			{
				return Byte.valueOf(value);
			}
			else
			{
				return defaultValue;
			}
		}
		else
		{
			return null;
		}
	}
	
	public static Method getMethod(Class<?> type, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException
	{
		boolean hasSuperclass = type.getSuperclass() != null;
		
		try
		{
			return type.getMethod(methodName, parameterTypes);
			
		}
		catch(NoSuchMethodException exception)
		{
			if(hasSuperclass)
			{
				return getMethod(type.getSuperclass(), methodName, parameterTypes);
			}
			else
			{
				throw exception;
			}
		}
	}
}
