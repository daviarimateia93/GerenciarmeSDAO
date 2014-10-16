package com.daviarimateia.dsimpledao.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MapHelper
{
	public static <T, U> Map<T, U> filterMapExcluding(String[] keysFilters, Map<T, U> map)
	{
		return filterMap(true, keysFilters, map);
	}
	
	public static <T, U> Map<T, U> filterMapIncluding(String[] keysFilters, Map<T, U> map)
	{
		return filterMap(false, keysFilters, map);
	}
	
	private static <T, U> Map<T, U> filterMap(boolean excluding, String[] keysFilters, Map<T, U> map)
	{
		List<String> filters = Arrays.asList(keysFilters);
		String[] keys = map.keySet().toArray(new String[map.keySet().size()]);
		
		for(String key : keys)
		{
			if(filters.contains(key) && excluding)
			{
				map.remove(key);
			}
			else if(!filters.contains(key) && !excluding)
			{
				map.remove(key);
			}
		}
		
		return map;
	}
}
