package me.gerenciar.sdao;

public class GerenciarmeSDAO
{
	private static boolean configured = false;
	
	private static ImutableConfiguration configuration = new ImutableConfiguration();
	
	private GerenciarmeSDAO()
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
			protected String name = "gerenciarmesdao";
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
		
		protected ImutableDatabase database = new ImutableDatabase();
		protected ImutableFilter filter = new ImutableFilter();
		
		public ImutableDatabase getDatabase()
		{
			return database;
		}
		
		public ImutableFilter getFilter()
		{
			return filter;
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
		
		public void setDatabase(ImutableDatabase database)
		{
			this.database = database;
		}
		
		public void setFilter(ImutableFilter filter)
		{
			this.filter = filter;
		}
	}
	
	public final static class GerenciarmeSDAOException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		public GerenciarmeSDAOException(String message)
		{
			super(message);
		}
	}
	
	public static void configure(ImutableConfiguration configuration) throws GerenciarmeSDAOException
	{
		if(!configured)
		{
			GerenciarmeSDAO.configuration = configuration;
		}
		else
		{
			throw new GerenciarmeSDAOException("GerenciarmeSDAO is already configured!");
		}
	}
}
