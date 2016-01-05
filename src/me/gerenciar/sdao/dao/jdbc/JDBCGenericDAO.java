package me.gerenciar.sdao.dao.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.gerenciar.sdao.annotation.Generated;
import me.gerenciar.sdao.annotation.Group;
import me.gerenciar.sdao.annotation.Identifier;
import me.gerenciar.sdao.annotation.TableName;
import me.gerenciar.sdao.dao.GenericDAO;
import me.gerenciar.sdao.factory.DAOFactory;
import me.gerenciar.sdao.utils.MapHelper;

public abstract class JDBCGenericDAO<T extends Serializable> implements GenericDAO<T>
{
	private Connection currentConnection;
	
	private Class<T> type;
	
	private final DAOFactory daoFactory;
	
	@SuppressWarnings("unchecked")
	protected JDBCGenericDAO(DAOFactory.Type type)
	{
		daoFactory = DAOFactory.getInstance(type);
		
		java.lang.reflect.Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
		
		if(types.length == 0)
		{
			throw new RuntimeException("Undefined template type");
		}
		else
		{
			this.type = (Class<T>) types[0];
		}
	}
	
	protected DAOFactory getDAOFactory()
	{
		return daoFactory;
	}
	
	protected Connection getCurrentConnection()
	{
		return currentConnection;
	}
	
	protected abstract String getInjectionEscape();
	
	protected abstract String getLeftEscape();
	
	protected abstract String getRightEscape();
	
	protected String getTableName()
	{
		if(type.isAnnotationPresent(TableName.class))
		{
			TableName tableName = type.getAnnotation(TableName.class);
			
			return tableName.value();
		}
		else
		{
			return type.getSimpleName();
		}
	}
	
	protected String[] getIdentifiersColumns()
	{
		List<String> identifiersColumns = new ArrayList<>();
		
		for(Field field : type.getDeclaredFields())
		{
			field.setAccessible(true);
			
			if(field.isAnnotationPresent(Identifier.class))
			{
				Identifier identifier = field.getAnnotation(Identifier.class);
				
				identifiersColumns.add(identifier.value());
			}
		}
		
		return identifiersColumns.toArray(new String[identifiersColumns.size()]);
	}
	
	protected String[] getGroupColumns()
	{
		List<String> groupColumns = new ArrayList<>();
		
		for(Field field : type.getDeclaredFields())
		{
			field.setAccessible(true);
			
			if(field.isAnnotationPresent(Group.class))
			{
				Group group = field.getAnnotation(Group.class);
				
				groupColumns.add(group.value());
			}
		}
		
		return !groupColumns.isEmpty() ? groupColumns.toArray(new String[groupColumns.size()]) : null;
	}
	
	protected String[] getGeneratedColumns()
	{
		List<String> generatedColumns = new ArrayList<>();
		
		for(Field field : type.getDeclaredFields())
		{
			field.setAccessible(true);
			
			if(field.isAnnotationPresent(Generated.class))
			{
				Generated generated = field.getAnnotation(Generated.class);
				
				generatedColumns.add(generated.value());
			}
		}
		
		return generatedColumns.toArray(new String[generatedColumns.size()]);
	}
	
	protected abstract void parseBean(ResultSet resultSet, T bean) throws Exception;
	
	protected T parseBean(ResultSet resultSet) throws Exception
	{
		try
		{
			T bean = type.newInstance();
			
			parseBean(resultSet, bean);
			
			return bean;
		}
		catch(InstantiationException | IllegalAccessException exception)
		{
			return null;
		}
	}
	
	// Delete grouped records, based on updateBean, from database.
	// Should compare which records from selectedBean are not in updateBean, and
	// delete them.
	protected abstract T mergeBeanOnUpdateToDelete(T updateBean, T selectedBean);
	
	// Insert grouped records, based on updateBean, into database.
	// Should compare which records from updateBean are not in selectedBean, and
	// insert them.
	protected abstract T mergeBeanOnUpdateToInsert(T updateBean, T selectedBean);
	
	// Set generated keys on bean
	protected void mergeGeneratedKeys(T bean, ResultSet resultSet) throws Exception
	{
		if(resultSet.next())
		{
			for(String generatedColumn : getGeneratedColumns())
			{
				for(Field field : type.getDeclaredFields())
				{
					field.setAccessible(true);
					
					if(field.isAnnotationPresent(Generated.class))
					{
						Generated generated = field.getAnnotation(Generated.class);
						
						if(generated.value().equals(generatedColumn))
						{
							field.setAccessible(true);
							field.set(bean, getColumnValue(generatedColumn, field, bean, resultSet));
							
							break;
						}
					}
				}
			}
		}
	}
	
	// Should merge bean to the lastBean
	protected abstract T mergeBean(T lastBean, T bean);
	
	protected T checkMergeBean(T lastBean, T bean)
	{
		if(lastBean == null)
		{
			return bean;
		}
		else
		{
			Map<String, Object> lastBeanMap = getIdentifierMap(parseMaps(lastBean).get(0));
			Map<String, Object> beanMap = getIdentifierMap(parseMaps(bean).get(0));
			
			if(new HashSet<>(lastBeanMap.values()).equals(new HashSet<>(beanMap.values())))
			{
				return mergeBean(lastBean, bean);
			}
			else
			{
				return null;
			}
		}
	}
	
	protected abstract List<Map<String, Object>> parseMaps(T bean);
	
	protected Map<String, Object> getIdentifierMap(Map<String, Object> map)
	{
		return MapHelper.filterMapIncluding(getIdentifiersColumns(), map);
	}
	
	protected Map<String, Object> getGroupMap(Map<String, Object> map)
	{
		return MapHelper.filterMapIncluding(getGroupColumns(), map);
	}
	
	protected T getBean(ResultSet resultSet)
	{
		try
		{
			T bean = null;
			T lastBean = null;
			
			if(getGroupColumns() != null)
			{
				do
				{
					resultSet.next();
					
					lastBean = bean;
				}
				while(resultSet.getRow() > 0 && (bean = checkMergeBean(lastBean, parseBean(resultSet))) != null);
				
				bean = lastBean;
			}
			else
			{
				if(resultSet.next())
				{
					bean = parseBean(resultSet);
				}
			}
			
			return bean;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected List<T> getBeans(ResultSet resultSet)
	{
		try
		{
			List<T> beans = new ArrayList<>();
			
			if(getGroupColumns() != null)
			{
				while(resultSet.next())
				{
					T bean = null;
					T lastBean = null;
					
					while(resultSet.getRow() > 0 && (bean = checkMergeBean(lastBean, parseBean(resultSet))) != null)
					{
						resultSet.next();
						lastBean = bean;
					}
					
					if(lastBean != null)
					{
						bean = lastBean;
						resultSet.previous();
					}
					
					beans.add(bean);
				}
			}
			else
			{
				while(resultSet.next())
				{
					beans.add(parseBean(resultSet));
				}
			}
			
			return beans;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected int executeUpdate(Connection connection, String sql, Object... args) throws SQLException
	{
		return executeUpdate(createPreparedStatement(connection, sql, args));
	}
	
	protected int executeUpdate(PreparedStatement stmt) throws SQLException
	{
		return stmt.executeUpdate();
	}
	
	protected ResultSet executeQuery(Connection connection, String sql, Object... args) throws SQLException
	{
		return executeQuery(createPreparedStatement(connection, sql, args));
	}
	
	protected ResultSet executeQuery(PreparedStatement stmt) throws SQLException
	{
		return stmt.executeQuery();
	}
	
	protected PreparedStatement createPreparedStatement(Connection connection, String sql, Object... args)
	{
		return createPreparedStatement(connection, sql, args, -1);
	}
	
	protected PreparedStatement createPreparedStatement(Connection connection, String sql, int autoGeneratedKeys, Object... args)
	{
		currentConnection = connection;
		
		try
		{
			PreparedStatement stmt = autoGeneratedKeys == -1 ? connection.prepareStatement(sql) : connection.prepareStatement(sql, autoGeneratedKeys);
			populatePreparedStatement(stmt, args);
			
			return stmt;
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private Object getColumnValue(String generatedColumn, Field field, T bean, ResultSet resultSet)
	{
		try
		{
			if(field.get(bean) instanceof String)
			{
				return resultSet.getString(generatedColumn);
			}
			else if(field.get(bean) instanceof Integer)
			{
				return resultSet.getInt(generatedColumn);
			}
			else if(field.get(bean) instanceof Double)
			{
				return resultSet.getDouble(generatedColumn);
			}
			else if(field.get(bean) instanceof Float)
			{
				return resultSet.getDouble(generatedColumn);
			}
			else if(field.get(bean) instanceof Boolean)
			{
				return resultSet.getBoolean(generatedColumn);
			}
			else if(field.get(bean) instanceof Short)
			{
				return resultSet.getShort(generatedColumn);
			}
			else if(field.get(bean) instanceof Array)
			{
				return resultSet.getArray(generatedColumn);
			}
			else if(field.get(bean) instanceof InputStream)
			{
				return resultSet.getBinaryStream(generatedColumn);
			}
			else if(field.get(bean) instanceof BigDecimal)
			{
				return resultSet.getBigDecimal(generatedColumn);
			}
			else if(field.get(bean) instanceof Blob)
			{
				return resultSet.getBlob(generatedColumn);
			}
			else if(field.get(bean) instanceof Byte)
			{
				return resultSet.getByte(generatedColumn);
			}
			else if(field.get(bean) instanceof byte[])
			{
				return resultSet.getBytes(generatedColumn);
			}
			else if(field.get(bean) instanceof Clob)
			{
				return resultSet.getClob(generatedColumn);
			}
			else if(field.get(bean) instanceof Date)
			{
				return resultSet.getDate(generatedColumn);
			}
			else if(field.get(bean) instanceof Ref)
			{
				return resultSet.getRef(generatedColumn);
			}
			else if(field.get(bean) instanceof RowId)
			{
				return resultSet.getRowId(generatedColumn);
			}
			else if(field.get(bean) instanceof SQLXML)
			{
				return resultSet.getSQLXML(generatedColumn);
			}
			else if(field.get(bean) instanceof Time)
			{
				return resultSet.getTime(generatedColumn);
			}
			else if(field.get(bean) instanceof Timestamp)
			{
				return resultSet.getTimestamp(generatedColumn);
			}
			else if(field.get(bean) instanceof URL)
			{
				return resultSet.getURL(generatedColumn);
			}
			else
			{
				return resultSet.getObject(generatedColumn);
			}
		}
		catch(SQLException | IllegalArgumentException | IllegalAccessException e)
		{
			return null;
		}
	}
	
	private PreparedStatement populatePreparedStatement(PreparedStatement stmt, Object... args) throws SQLException
	{
		for(int i = 0, stmtIndex = 1; i < args.length; i++, stmtIndex++)
		{
			Object arg = args[i];
			
			if(arg instanceof String)
			{
				stmt.setString(stmtIndex, (String) arg);
			}
			else if(arg instanceof Integer)
			{
				stmt.setInt(stmtIndex, (Integer) arg);
			}
			else if(arg instanceof Double)
			{
				stmt.setDouble(stmtIndex, (Double) arg);
			}
			else if(arg instanceof Float)
			{
				stmt.setFloat(stmtIndex, (Float) arg);
			}
			else if(arg instanceof Boolean)
			{
				stmt.setBoolean(stmtIndex, (Boolean) arg);
			}
			else if(arg instanceof Short)
			{
				stmt.setShort(stmtIndex, (Short) arg);
			}
			else if(arg instanceof Array)
			{
				stmt.setArray(stmtIndex, (Array) arg);
			}
			else if(arg instanceof InputStream)
			{
				stmt.setBinaryStream(stmtIndex, (InputStream) arg);
			}
			else if(arg instanceof BigDecimal)
			{
				stmt.setBigDecimal(stmtIndex, (BigDecimal) arg);
			}
			else if(arg instanceof Blob)
			{
				stmt.setBlob(stmtIndex, (Blob) arg);
			}
			else if(arg instanceof Byte)
			{
				stmt.setByte(stmtIndex, (Byte) arg);
			}
			else if(arg instanceof byte[])
			{
				stmt.setBytes(stmtIndex, (byte[]) arg);
			}
			else if(arg instanceof Reader)
			{
				stmt.setCharacterStream(stmtIndex, (Reader) arg);
			}
			else if(arg instanceof Clob)
			{
				stmt.setClob(stmtIndex, (Clob) arg);
			}
			else if(arg instanceof Date)
			{
				stmt.setDate(stmtIndex, (Date) arg);
			}
			else if(arg instanceof Ref)
			{
				stmt.setRef(stmtIndex, (Ref) arg);
			}
			else if(arg instanceof RowId)
			{
				stmt.setRowId(stmtIndex, (RowId) arg);
			}
			else if(arg instanceof SQLXML)
			{
				stmt.setSQLXML(stmtIndex, (SQLXML) arg);
			}
			else if(arg instanceof Time)
			{
				stmt.setTime(stmtIndex, (Time) arg);
			}
			else if(arg instanceof Timestamp)
			{
				stmt.setTimestamp(stmtIndex, (Timestamp) arg);
			}
			else if(arg instanceof URL)
			{
				stmt.setURL(stmtIndex, (URL) arg);
			}
			else if(arg == null)
			{
				stmt.setNull(stmtIndex, Types.NULL);
			}
			else
			{
				stmt.setObject(stmtIndex, arg);
			}
		}
		
		return stmt;
	}
	
	protected Date date2SQLDate(java.util.Date date)
	{
		return new Date(date.getTime());
	}
	
	protected Time date2SQLTime(java.util.Date date)
	{
		return new Time(date.getTime());
	}
	
	protected Timestamp date2SQLTimestamp(java.util.Date date)
	{
		return new Timestamp(date.getTime());
	}
	
	protected Object[] getValuesFromKeys(Object[] keys, Map<String, Object> map)
	{
		List<Object> values = new ArrayList<>();
		
		for(Object key : keys)
		{
			values.add(map.get(key));
		}
		
		return values.toArray();
	}
	
	protected String getColumnsSequence(Map<String, Object> map)
	{
		String sequence = "";
		
		String[] keys = map.keySet().toArray(new String[map.keySet().size()]);
		
		for(int i = 0; i < keys.length; i++)
		{
			if(i == 0)
			{
				sequence += "(";
			}
			
			sequence += getLeftEscape() + keys[i] + getRightEscape();
			
			if(i < keys.length - 1)
			{
				sequence += ", ";
			}
			else
			{
				sequence += ")";
			}
		}
		
		return sequence;
	}
	
	protected String getTokensSequence(Map<String, Object> map)
	{
		String sequence = "";
		
		String[] keys = map.keySet().toArray(new String[map.keySet().size()]);
		
		for(int i = 0; i < keys.length; i++)
		{
			if(i == 0)
			{
				sequence += "(";
			}
			
			sequence += "?";
			
			if(i < keys.length - 1)
			{
				sequence += ", ";
			}
			else
			{
				sequence += ")";
			}
		}
		
		return sequence;
	}
	
	protected String getColumnsTokensSequence(Map<String, Object> map)
	{
		String sequence = "";
		
		String[] keys = map.keySet().toArray(new String[map.keySet().size()]);
		
		for(int i = 0; i < keys.length; i++)
		{
			sequence += getLeftEscape() + keys[i] + getRightEscape() + " = ?";
			
			if(i < keys.length - 1)
			{
				sequence += ", ";
			}
		}
		
		return sequence;
	}
	
	protected String getIdentifiersColumnsSequence()
	{
		String sequence = "";
		
		String[] columns = getIdentifiersColumns();
		
		for(int i = 0; i < columns.length; i++)
		{
			sequence += getLeftEscape() + columns[i] + getRightEscape();
			
			if(i < columns.length - 1)
			{
				sequence += ", ";
			}
		}
		
		return sequence;
	}
	
	protected String getIdentifiersColumnsTokensSequence()
	{
		String sequence = "";
		
		String[] columns = getIdentifiersColumns();
		
		for(int i = 0; i < columns.length; i++)
		{
			sequence += getLeftEscape() + columns[i] + getRightEscape() + " = ?";
			
			if(i < columns.length - 1)
			{
				sequence += " AND ";
			}
		}
		
		return sequence;
	}
	
	protected String getGroupColumnsSequence()
	{
		String sequence = "";
		
		String[] columns = getGroupColumns();
		
		for(int i = 0; i < columns.length; i++)
		{
			sequence += getLeftEscape() + columns[i] + getRightEscape();
			
			if(i < columns.length - 1)
			{
				sequence += ", ";
			}
		}
		
		return sequence;
	}
	
	protected String getGroupColumnsTokensSequence()
	{
		String sequence = "";
		
		String[] columns = getGroupColumns();
		
		for(int i = 0; i < columns.length; i++)
		{
			sequence += getLeftEscape() + columns[i] + getRightEscape() + " = ?";
			
			if(i < columns.length - 1)
			{
				sequence += " AND ";
			}
		}
		
		return sequence;
	}
}
