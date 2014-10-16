package com.daviarimateia.dsimpledao;

public class DSimpleDAO
{
	private static boolean configured = false;
	
	private static ImutableConfiguration configuration = new ImutableConfiguration();
	
	private DSimpleDAO()
	{
		
	}
	
	public static ImutableConfiguration getConfiguration()
	{
		return configuration;
	}
	
	public static boolean canBeConfigured()
	{
		return !configured;
	}
	
	public static class ImutableConfiguration
	{
		public static class ImutableDatabase
		{
			protected String address = "localhost";
			protected int port = 3306;
			protected String name = "dsimpledao";
			protected String username = "root";
			protected String password = "";
			
			public String getAddress()
			{
				return address;
			}
			
			public int getPort()
			{
				return port;
			}
			
			public String getName()
			{
				return name;
			}
			
			public String getUsername()
			{
				return username;
			}
			
			public String getPassword()
			{
				return password;
			}
		};
		
		public static class ImutableFilter
		{
			protected String glue = "OR";
			
			public String getGlue()
			{
				return glue;
			}
		}
		
		public static class ImutablePath
		{
			protected String daoImplementationPath = "com.daviarimateia.dsimpledao.repository.implementation";
			
			public String getDaoImplementationPath()
			{
				return daoImplementationPath;
			}
		}
		
		protected ImutableDatabase database = new ImutableDatabase();
		protected ImutableFilter filter = new ImutableFilter();
		protected ImutablePath path = new ImutablePath();
		
		public ImutableDatabase getDatabase()
		{
			return database;
		}
		
		public ImutableFilter getFilter()
		{
			return filter;
		}
		
		public ImutablePath getPath()
		{
			return path;
		}
	};
	
	public static class MutableConfiguration extends ImutableConfiguration
	{
		public static class MutableDatabase extends ImutableDatabase
		{
			public void setAddress(String address)
			{
				this.address = address;
			}
			
			public void setPort(int port)
			{
				this.port = port;
			}
			
			public void setName(String name)
			{
				this.name = name;
			}
			
			public void setUsername(String username)
			{
				this.username = username;
			}
			
			public void setPassword(String password)
			{
				this.password = password;
			}
		};
		
		public static class MutableFilter extends ImutableFilter
		{
			public void setGlue(String glue)
			{
				this.glue = glue;
			}
		}
		
		public static class MutablePath extends ImutablePath
		{
			public void setDaoImplementationPath(String daoImplementationPath)
			{
				this.daoImplementationPath = daoImplementationPath;
			}
		}
		
		public void setDatabase(ImutableDatabase database)
		{
			this.database = database;
		}
		
		public void setFilter(ImutableFilter filter)
		{
			this.filter = filter;
		}
		
		public void setPath(ImutablePath path)
		{
			this.path = path;
		}
	}
	
	public final static class DSimpleDAOException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		public DSimpleDAOException(String message)
		{
			super(message);
		}
	}
	
	public static void configure(ImutableConfiguration configuration) throws DSimpleDAOException
	{
		if(!configured)
		{
			DSimpleDAO.configuration = configuration;
		}
		else
		{
			throw new DSimpleDAOException("DSimpleDAO is already configured!");
		}
	}
}
