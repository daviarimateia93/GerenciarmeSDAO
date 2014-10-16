package com.daviarimateia.dsimpledao.dao.jdbc.mysql;

import java.io.Serializable;

public abstract class MySQLGenericSimpleDAO<T extends Serializable> extends MySQLGenericDAO<T>
{
	protected MySQLGenericSimpleDAO()
	{
		super();
	}
	
	@Override
	protected T mergeBeanOnUpdateToDelete(T updateBean, T selectedBean)
	{
		return null;
	}
	
	@Override
	protected T mergeBeanOnUpdateToInsert(T updateBean, T selectedBean)
	{
		return null;
	}
	
	@Override
	protected T mergeBean(T lastBean, T bean)
	{
		return null;
	}
}
