package me.gerenciar.sdao.dao.filter;

public class DAOFilter
{
	private String name;
	private String value;
	private String operator;
	private String order;
	private String glue;
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public String getOperator()
	{
		return operator;
	}
	
	public void setOperator(String operator)
	{
		this.operator = operator;
	}
	
	public String getOrder()
	{
		return order;
	}
	
	public void setOrder(String order)
	{
		this.order = order;
	}
	
	public String getGlue()
	{
		return glue;
	}
	
	public void setGlue(String glue)
	{
		this.glue = glue;
	}
}
