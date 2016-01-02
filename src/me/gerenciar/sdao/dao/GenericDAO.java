package me.gerenciar.sdao.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;

import me.gerenciar.sdao.dao.filter.DAOFilter;

public interface GenericDAO<T extends Serializable>
{
	public abstract int insert(Connection connection, T bean) throws Exception;
	
	public abstract int update(Connection connection, T bean) throws Exception;
	
	public abstract int delete(Connection connection, T bean) throws Exception;
	
	public abstract List<T> selectAll(Connection connection, long starterIndex, long endIndex) throws Exception;
	
	public abstract List<T> select(Connection connection, long starterIndex, long endIndex, List<DAOFilter> daoFilters) throws Exception;
	
	public abstract long countAll(Connection connection) throws Exception;
	
	public abstract long count(Connection connection, List<DAOFilter> daoFilters) throws Exception;
}
