package me.gerenciar.sdao.factory.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;

import me.gerenciar.sdao.GerenciarmeSDAO;
import me.gerenciar.sdao.factory.DAOFactory;

public abstract class JDBCDAOFactory extends DAOFactory
{
	public JDBCDAOFactory(Type type)
	{
		super(type);
	}
	
	public Connection newConnection(Type type)
	{
		return newConnection(type, null, null, null, null, null);
	}
	
	public Connection newConnection(Type type, String address, Integer port, String name, String username, String password)
	{
		try
		{
			address = address == null ? GerenciarmeSDAO.getConfiguration().getDatabase().getAddress() : address;
			port = port == null ? GerenciarmeSDAO.getConfiguration().getDatabase().getPort() : port;
			name = name == null ? GerenciarmeSDAO.getConfiguration().getDatabase().getName() : name;
			username = username == null ? GerenciarmeSDAO.getConfiguration().getDatabase().getUsername() : username;
			password = password == null ? GerenciarmeSDAO.getConfiguration().getDatabase().getPassword() : password;
			
			String driverClassName;
			String typeValue;
			
			switch(type)
			{
				
				case MYSQL:
				{
					driverClassName = "com.mysql.jdbc.Driver";
					typeValue = "mysql";
					
					break;
				}
				
				case HSQLDB:
				{
					driverClassName = "org.hsqldb.jdbcDriver";
					typeValue = "hsqldb:hsql";
					
					break;
				}
				
				default:
				{
					throw new RuntimeException("DAOFactory not supported yet!");
				}
			}
			
			Class.forName(driverClassName);
			
			return DriverManager.getConnection("jdbc:" + typeValue + "://" + address + ":" + port + "/" + name, username, password);
		}
		catch(ClassNotFoundException | SQLException exception)
		{
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public void close(Connection connection)
	{
		try
		{
			connection.close();
		}
		catch(Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public void beginTransaction(Connection connection)
	{
		try
		{
			connection.setAutoCommit(false);
		}
		catch(SQLException exception)
		{
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public void commit(Connection connection)
	{
		try
		{
			connection.commit();
			connection.setAutoCommit(true);
		}
		catch(SQLException exception)
		{
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public void rollback(Connection connection)
	{
		try
		{
			connection.rollback();
		}
		catch(SQLException exception)
		{
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public void rollback(Connection connection, Savepoint savepoint)
	{
		try
		{
			connection.rollback(savepoint);
		}
		catch(SQLException exception)
		{
			throw new RuntimeException(exception);
		}
	}
}
