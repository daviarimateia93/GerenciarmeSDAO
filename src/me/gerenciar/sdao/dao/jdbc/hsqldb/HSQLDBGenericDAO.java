package me.gerenciar.sdao.dao.jdbc.hsqldb;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import me.gerenciar.sdao.DSimpleDAO;
import me.gerenciar.sdao.dao.filter.DAOFilter;
import me.gerenciar.sdao.dao.jdbc.JDBCGenericDAO;
import me.gerenciar.sdao.factory.DAOFactory;

public abstract class HSQLDBGenericDAO<T extends Serializable> extends JDBCGenericDAO<T>
{
	protected HSQLDBGenericDAO()
	{
		super(DAOFactory.Type.HSQLDB);
	}
	
	@Override
	protected String getInjectionEscape()
	{
		return "\\";
	}
	
	@Override
	protected String getLeftEscape()
	{
		return "\"";
	}
	
	@Override
	protected String getRightEscape()
	{
		return "\"";
	}
	
	@Override
	public T selectOne(Connection connection, Object... identifiers) throws Exception
	{
		String sql = "SELECT * FROM " + getLeftEscape() + getTableName() + getRightEscape() + " WHERE " + getIdentifiersColumnsTokensSequence();
		
		return getBean(executeQuery(connection, sql, identifiers));
	}
	
	@Override
	public List<T> selectAll(Connection connection, long starterIndex, long endIndex) throws Exception
	{
		List<DAOFilter> daoFilters = null;
		
		String[] identifierColumns = getIdentifiersColumns();
		
		for(String identifierColumn : identifierColumns)
		{
			if(daoFilters == null)
			{
				daoFilters = new ArrayList<>();
			}
			
			DAOFilter daoFilter = new DAOFilter();
			daoFilter.setName(identifierColumn);
			daoFilter.setOrder("ASC");
			
			daoFilters.add(daoFilter);
		}
		
		return select(connection, starterIndex, endIndex, daoFilters);
	}
	
	@Override
	public List<T> select(Connection connection, long starterIndex, long endIndex, List<DAOFilter> daoFilters) throws Exception
	{
		List<Object> args = new ArrayList<>();
		
		String filter = "";
		
		String where = "";
		String order = "";
		String limit = " LIMIT ?, ?";
		
		String[] groupColumns = getGroupColumns();
		
		if(groupColumns != null)
		{
			if(daoFilters == null)
			{
				daoFilters = new ArrayList<>();
			}
			
			for(int i = groupColumns.length - 1; i >= 0; i--)
			{
				DAOFilter daoFilter = null;
				
				for(int j = 0; j < daoFilters.size(); j++)
				{
					if(daoFilters.get(j).getName().equals(groupColumns[i]))
					{
						daoFilter = daoFilters.remove(j);
						break;
					}
				}
				
				if(daoFilter == null)
				{
					daoFilter = new DAOFilter();
					daoFilter.setName(groupColumns[i]);
					daoFilter.setOrder("ASC");
				}
				
				daoFilters.add(0, daoFilter);
			}
		}
		
		where += parseWhere(daoFilters, args);
		order += parseOrder(daoFilters);
		
		if(groupColumns != null)
		{
			String limitFixSQL = "SELECT COUNT(*) AS " + getLeftEscape() + "TOTAL" + getRightEscape() + " FROM " + getLeftEscape() + getTableName() + getRightEscape() + where + " GROUP BY " + getGroupColumnsSequence() + limit;
			
			List<Object> limitFixArgs = new ArrayList<>();
			limitFixArgs.addAll(args);
			limitFixArgs.add(starterIndex);
			limitFixArgs.add(endIndex);
			
			ResultSet limitFixRS = executeQuery(connection, limitFixSQL, limitFixArgs.toArray());
			
			long total = 0;
			
			while(limitFixRS.next())
			{
				if(limitFixRS.getLong("TOTAL") != 0)
				{
					total += limitFixRS.getLong("TOTAL");
				}
			}
			
			starterIndex *= total;
			endIndex *= total;
		}
		
		args.add(starterIndex);
		args.add(endIndex);
		
		filter = where + order + limit;
		
		String sql = "SELECT * FROM " + getLeftEscape() + getTableName() + getRightEscape() + filter;
		
		return getBeans(executeQuery(connection, sql, args.toArray()));
	}
	
	@Override
	public long countAll(Connection connection) throws Exception
	{
		return count(connection, null);
	}
	
	@Override
	public long count(Connection connection, List<DAOFilter> daoFilters) throws Exception
	{
		List<Object> args = new ArrayList<>();
		
		String where = parseWhere(daoFilters, args);
		String group = getGroupColumns() != null ? getGroupColumns().length > 0 ? " GROUP BY " + getGroupColumnsSequence() : "" : "";
		
		String sql;
		
		if(getGroupColumns() != null)
		{
			sql = "SELECT COUNT(*) AS " + getLeftEscape() + "TOTAL" + getRightEscape() + " FROM (SELECT COUNT(*) FROM " + getLeftEscape() + getTableName() + getRightEscape() + where + group + ") AS " + getLeftEscape() + "TMP_USERS" + getRightEscape();
		}
		else
		{
			sql = "SELECT COUNT(*) AS " + getLeftEscape() + "TOTAL" + getRightEscape() + " FROM " + getLeftEscape() + getTableName() + getRightEscape() + where + group;
		}
		
		ResultSet rs = executeQuery(connection, sql, args.toArray());
		
		if(rs.next())
		{
			return rs.getLong("TOTAL");
		}
		else
		{
			return 0;
		}
	}
	
	@Override
	public int insert(Connection connection, T bean) throws Exception
	{
		int returnValue = 0;
		
		List<Map<String, Object>> maps = parseMaps(bean);
		PreparedStatement stmt;
		
		if(maps.size() > 0)
		{
			String sql = "INSERT INTO " + getLeftEscape() + getTableName() + getRightEscape() + " " + getColumnsSequence(maps.get(0)) + " VALUES" + getTokensSequence(maps.get(0));
			Object[] args = maps.get(0).values().toArray();
			
			stmt = createPreparedStatement(connection, sql, args, Statement.RETURN_GENERATED_KEYS);
			returnValue += executeUpdate(stmt);
			
			mergeGeneratedKeys(bean, stmt.getGeneratedKeys());
			
			maps = parseMaps(bean);
			
			for(int i = 1; i < maps.size(); i++)
			{
				Map<String, Object> map = maps.get(i);
				
				sql = "INSERT INTO " + getLeftEscape() + getTableName() + getRightEscape() + " " + getColumnsSequence(map) + " VALUES" + getTokensSequence(map);
				args = map.values().toArray();
				
				stmt = createPreparedStatement(connection, sql, args, Statement.RETURN_GENERATED_KEYS);
				returnValue += executeUpdate(stmt);
			}
		}
		
		return returnValue;
	}
	
	@Override
	public int update(Connection connection, T bean) throws Exception
	{
		int returnValue = 0;
		
		List<Map<String, Object>> maps = parseMaps(bean);
		
		if(getGroupColumns() != null)
		{
			for(Map<String, Object> map : maps)
			{
				List<DAOFilter> daoFilters = new ArrayList<>();
				
				for(String groupColumn : getGroupColumns())
				{
					DAOFilter daoFilter = new DAOFilter();
					daoFilter.setName(groupColumn);
					daoFilter.setOperator("=");
					daoFilter.setValue(String.valueOf(map.get(groupColumn)));
					
					daoFilters.add(daoFilter);
				}
				
				List<T> selectedBeanAsList = select(connection, 0, 1, daoFilters);
				
				T selectedBean = null;
				
				if(selectedBeanAsList != null)
				{
					selectedBean = selectedBeanAsList.get(0);
				}
				
				T deleteBean = mergeBeanOnUpdateToDelete(bean, selectedBean);
				T insertBean = mergeBeanOnUpdateToInsert(bean, selectedBean);
				
				if(deleteBean != null)
				{
					delete(connection, deleteBean);
				}
				
				if(insertBean != null)
				{
					insert(connection, insertBean);
				}
			}
		}
		
		for(Map<String, Object> map : maps)
		{
			String sql = "UPDATE " + getLeftEscape() + getTableName() + getRightEscape() + " SET " + getColumnsTokensSequence(map) + " WHERE " + getIdentifiersColumnsTokensSequence();
			
			List<Object> args = new ArrayList<>();
			args.addAll(map.values());
			args.addAll(Arrays.asList(getValuesFromKeys(getIdentifiersColumns(), map)));
			
			returnValue += executeUpdate(connection, sql, args.toArray());
		}
		
		return returnValue;
	}
	
	@Override
	public int delete(Connection connection, T bean) throws Exception
	{
		int returnValue = 0;
		
		for(Map<String, Object> map : parseMaps(bean))
		{
			String sql = "DELETE FROM " + getLeftEscape() + getTableName() + getRightEscape() + " WHERE " + getIdentifiersColumnsTokensSequence();
			Object[] args = getValuesFromKeys(getIdentifiersColumns(), map);
			
			returnValue += executeUpdate(connection, sql, args);
		}
		
		return returnValue;
	}
	
	private String parseWhere(List<DAOFilter> daoFilters, List<Object> args)
	{
		String where = "";
		
		if(daoFilters != null)
		{
			for(int i = 0; i < daoFilters.size(); i++)
			{
				if(daoFilters.get(i).getName() != null && daoFilters.get(i).getOperator() != null)
				{
					if(i == 0)
					{
						where += " WHERE ";
					}
					
					where += getLeftEscape();
					where += daoFilters.get(i).getName().replaceAll("'", getInjectionEscape() + "'");
					where += getRightEscape();
					where += " ";
					where += daoFilters.get(i).getOperator().replaceAll("'", getInjectionEscape() + "'");
					where += " ";
					where += "?";
					
					String glue = null;
					
					if(daoFilters.get(i).getGlue() != null)
					{
						glue = daoFilters.get(i).getGlue().replaceAll("'", getInjectionEscape() + "'");
					}
					
					if(i < daoFilters.size() - 1)
					{
						where += " " + (glue != null ? glue : DSimpleDAO.getConfiguration().getFilter().getGlue()) + " ";
					}
					
					args.add(daoFilters.get(i).getValue());
				}
			}
		}
		
		where = where.replaceAll(" .+ $", "");
		
		return where;
	}
	
	private String parseOrder(List<DAOFilter> daoFilters)
	{
		String order = "";
		
		if(daoFilters != null)
		{
			for(int i = 0; i < daoFilters.size(); i++)
			{
				if(daoFilters.get(i).getName() != null && daoFilters.get(i).getOrder() != null)
				{
					if(i == 0)
					{
						order += " ORDER BY ";
					}
					
					order += getLeftEscape();
					order += daoFilters.get(i).getName().replaceAll("'", getInjectionEscape() + "'");
					order += getRightEscape();
					order += " ";
					order += daoFilters.get(i).getOrder().replaceAll("'", getInjectionEscape() + "'");
					order += ", ";
				}
			}
		}
		
		order = order.replaceAll("\\, $", "");
		
		return order;
	}
}
