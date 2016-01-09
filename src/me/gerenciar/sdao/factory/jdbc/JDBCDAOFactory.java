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
		return newConnection(type, GerenciarmeSDAO.getConfiguration().getDatabase().getAddress(), GerenciarmeSDAO.getConfiguration().getDatabase().getPort(), GerenciarmeSDAO.getConfiguration().getDatabase().getName(), GerenciarmeSDAO.getConfiguration().getDatabase().getUsername(), GerenciarmeSDAO.getConfiguration().getDatabase().getPassword());
	}
	
	public Connection newConnection(Type type, String address, int port, String name, String username, String password)
	{
		try
		{
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
