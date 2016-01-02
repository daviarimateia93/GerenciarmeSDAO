package me.gerenciar.sdao.dao.jdbc.hsqldb;

import java.io.Serializable;

public abstract class HSQLDBGenericSimpleDAO<T extends Serializable> extends HSQLDBGenericDAO<T>
{
	protected HSQLDBGenericSimpleDAO()
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
