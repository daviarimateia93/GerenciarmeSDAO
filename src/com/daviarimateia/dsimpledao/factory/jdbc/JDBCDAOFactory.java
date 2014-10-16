package com.daviarimateia.dsimpledao.factory.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;

import com.daviarimateia.dsimpledao.DSimpleDAO;
import com.daviarimateia.dsimpledao.factory.DAOFactory;

public abstract class JDBCDAOFactory extends DAOFactory
{
	public JDBCDAOFactory(Type type)
	{
		super(type);
	}
	
	public Connection newConnection(Type type)
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
			
			return DriverManager.getConnection("jdbc:" + typeValue + "://" + DSimpleDAO.getConfiguration().getDatabase().getAddress() + ":" + DSimpleDAO.getConfiguration().getDatabase().getPort() + "/" + DSimpleDAO.getConfiguration().getDatabase().getName(), DSimpleDAO.getConfiguration().getDatabase().getUsername(), DSimpleDAO.getConfiguration().getDatabase().getPassword());
		}
		catch(ClassNotFoundException | SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close(Connection connection)
	{
		try
		{
			connection.close();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void beginTransaction(Connection connection)
	{
		try
		{
			connection.setAutoCommit(false);
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
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
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void rollback(Connection connection)
	{
		try
		{
			connection.rollback();
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void rollback(Connection connection, Savepoint savepoint)
	{
		try
		{
			connection.rollback(savepoint);
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
