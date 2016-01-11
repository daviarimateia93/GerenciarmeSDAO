package me.gerenciar.sdao.factory;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.Hashtable;
import java.util.Map;

import me.gerenciar.sdao.factory.jdbc.JDBCDAOFactory;

public abstract class DAOFactory
{
	private static final ThreadLocal<Map<Type, DAOFactory>> threadLocalInstances = new ThreadLocal<>();
	private static final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();
	
	public static final String TYPE_MYSQL = "MySQL";
	public static final String TYPE_HSQLDB = "HSQLDB";
	
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
	
	public static class Wrapper<T>
	{
		private T object;
		
		public Wrapper()
		{
		
		}
		
		public Wrapper(T object)
		{
			this.object = object;
		}
		
		public T get()
		{
			return object;
		}
		
		public void set(T object)
		{
			this.object = object;
		}
	}
	
	public static abstract class Runnable<T>
	{
		protected Wrapper<T> wrapper;
		
		public Runnable()
		{
		
		}
		
		public Runnable(Wrapper<T> wrapper)
		{
			this.wrapper = wrapper;
		}
		
		public abstract void run() throws Exception;
	}
	
	protected DAOFactory(Type type)
	{
		this.type = type;
	}
	
	public static DAOFactory getInstance(final Type type)
	{
		if(threadLocalInstances.get() == null)
		{
			threadLocalInstances.set(new Hashtable<Type, DAOFactory>());
		}
		
		Map<Type, DAOFactory> instances = threadLocalInstances.get();
		
		DAOFactory instance = instances.get(type);
		
		if(instance == null)
		{
			instance = new JDBCDAOFactory(type)
			{
				@Override
				public Connection newConnection()
				{
					return newConnection(null, null, null, null, null);
				}
				
				@Override
				public Connection newConnection(String address, Integer port, String name, String username, String password)
				{
					return newConnection(type, address, port, name, username, password);
				}
			};
		}
		
		return instance;
	}
	
	public abstract Connection newConnection();
	
	public abstract Connection newConnection(String address, Integer port, String name, String username, String password);
	
	public abstract void close(Connection connection);
	
	public abstract void beginTransaction(Connection connection);
	
	public abstract void commit(Connection connection);
	
	public abstract void rollback(Connection connection);
	
	public abstract void rollback(Connection connection, Savepoint savepoint);
	
	public void beginTransaction()
	{
		beginTransaction(null, null, null, null, null);
	}
	
	public void beginTransaction(String address, Integer port, String name, String username, String password)
	{
		Connection connection = getConnection() == null ? newConnection(address, port, name, username, password) : getConnection();
		
		beginTransaction(connection);
		
		threadLocalConnection.set(connection);
	}
	
	public Connection getConnection()
	{
		return threadLocalConnection.get();
	}
	
	public void commit()
	{
		Connection connection = threadLocalConnection.get();
		
		commit(connection);
		
		threadLocalConnection.remove();
	}
	
	public void rollback()
	{
		Connection connection = threadLocalConnection.get();
		
		rollback(connection);
		
		threadLocalConnection.remove();
	}
	
	public <T> void transactional(Runnable<T> runnable)
	{
		transactional(runnable, null, null, null, null, null);
	}
	
	public <T> void transactional(Runnable<T> runnable, String address, Integer port, String name, String username, String password)
	{
		boolean newTransaction = false;
		
		if(getConnection() == null)
		{
			beginTransaction(address, port, name, username, password);
			
			newTransaction = true;
		}
		
		try
		{
			runnable.run();
			
			if(newTransaction)
			{
				commit();
			}
		}
		catch(Exception exception)
		{
			if(newTransaction)
			{
				rollback();
			}
		}
	}
}
