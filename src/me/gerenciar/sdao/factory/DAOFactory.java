package me.gerenciar.sdao.factory;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Hashtable;

import me.gerenciar.sdao.DSimpleDAO;
import me.gerenciar.sdao.dao.GenericDAO;
import me.gerenciar.sdao.factory.jdbc.JDBCDAOFactory;

public abstract class DAOFactory
{
	public static final String TYPE_MYSQL = "MySQL";
	public static final String TYPE_HSQLDB = "HSQLDB";
	
	private static Hashtable<Type, DAOFactory> instances = new Hashtable<>();
	
	private Type type;
	
	public Type getType()
	{
		return type;
	}
	
	public static enum Type
	{
		MYSQL(TYPE_MYSQL), HSQLDB(TYPE_HSQLDB);
		
		private String value;
		
		private Type(String value)
		{
			this.value = value;
		}
		
		public String getValue()
		{
			return value;
		}
	};
	
	public static Type getType(String type)
	{
		switch(type)
		{
			case TYPE_MYSQL:
			{
				return Type.MYSQL;
			}
			
			case TYPE_HSQLDB:
			{
				return Type.HSQLDB;
			}
			
			default:
			{
				return null;
			}
		}
	}
	
	protected DAOFactory(Type type)
	{
		this.type = type;
	}
	
	public static DAOFactory getInstance(final Type type)
	{
		DAOFactory instance = instances.get(type);
		
		if(instance == null)
		{
			instance = new JDBCDAOFactory(type)
			{
				@Override
				public Connection newConnection()
				{
					return newConnection(type);
				}
			};
		}
		
		return instance;
	}
	
	public abstract Connection newConnection();
	
	public abstract void close(Connection connection);
	
	public abstract void beginTransaction(Connection connection);
	
	public abstract void commit(Connection connection);
	
	public abstract void rollback(Connection connection);
	
	public abstract void rollback(Connection connection, Savepoint savepoint);
	
	@SuppressWarnings("unchecked")
	public <T extends GenericDAO<?>> T getDAO(String name)
	{
		String implementationPath = DSimpleDAO.getConfiguration().getPath().getDaoImplementationPath();
		
		try
		{
			return (T) Class.forName(implementationPath + "." + name).newInstance();
		}
		catch(ClassNotFoundException | InstantiationException | IllegalAccessException e)
		{
			return null;
		}
	}
}
