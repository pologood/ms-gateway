package cn.ms.gateway.base.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import cn.ms.gateway.base.IFilter;
import cn.ms.gateway.base.IGateway;
import cn.ms.gateway.base.annotation.Filter;
import cn.ms.gateway.base.annotation.FilterEnable;
import cn.ms.gateway.base.type.FilterType;

/**
 * 微服务网关核心
 * 
 * @author lry
 *
 * @param <REQ>
 * @param <RES>
 */
public class Gateway<REQ, RES> implements IGateway<REQ, RES> {

	/**在线过滤器**/
	private Map<String, Map<String, IFilter<REQ, RES>>> serviceFilterOnLineMap = new LinkedHashMap<String, Map<String, IFilter<REQ, RES>>>();
	/**离线过滤器**/
	private Map<String, Map<String, IFilter<REQ, RES>>> serviceFilterOffLineMap = new LinkedHashMap<String, Map<String, IFilter<REQ, RES>>>();

	@Override
	public void addFilter(IFilter<REQ, RES> filter) {
		String filterName = filter.filterName();
		if (filterName == null || filterName.length() < 1) {
			filterName = filter.getClass().getName();
		}
		
		FilterEnable filterEnable = filter.getClass().getAnnotation(FilterEnable.class);
		if(filterEnable==null||filterEnable.value()){//在线过滤器
			//过滤器注解
			Filter filterAnnotation = filter.getClass().getAnnotation(Filter.class);
			if(filterAnnotation!=null){
				String code=filterAnnotation.value().getCode();
				Map<String, IFilter<REQ, RES>> filterMap= serviceFilterOnLineMap.get(code);
				if(filterMap==null){
					filterMap=new LinkedHashMap<String, IFilter<REQ,RES>>();
				}
				filterMap.put(filterName, filter);
				serviceFilterOnLineMap.put(code, filterMap);
			}
		}else{//离线过滤器
			//过滤器注解
			Filter filterAnnotation = filter.getClass().getAnnotation(Filter.class);
			if(filterAnnotation!=null){
				String code=filterAnnotation.value().getCode();
				Map<String, IFilter<REQ, RES>> filterMap= serviceFilterOffLineMap.get(code);
				if(filterMap==null){
					filterMap=new LinkedHashMap<String, IFilter<REQ,RES>>();
				}
				filterMap.put(filterName, filter);
				serviceFilterOffLineMap.put(code, filterMap);
			}
		}
	}
	
	@Override
	public void addFilters(List<IFilter<REQ, RES>> filters) {
		for (IFilter<REQ, RES> filter:filters) {
			this.addFilter(filter);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		//$NON-NLS-初始化所有处理器$
		Map<String, List<IFilter<REQ, RES>>> filterMap=new LinkedHashMap<String, List<IFilter<REQ,RES>>>();
		@SuppressWarnings("rawtypes")
		ServiceLoader<IFilter> serviceloader = ServiceLoader.load(IFilter.class);
		for (IFilter<REQ, RES> filter : serviceloader) {
			Filter filterAnnotation=filter.getClass().getAnnotation(Filter.class);
			if (filterAnnotation!=null) {
				//$NON-NLS-根据过滤器类型分组收集$
				String filterType=filterAnnotation.value().getCode();
				List<IFilter<REQ, RES>> filterList=filterMap.get(filterType);
				if(filterList==null){
					filterList=new ArrayList<IFilter<REQ,RES>>();
				}
				filterList.add(filter);
				filterMap.put(filterType, filterList);
			}
		}

		//$NON-NLS-排序$
		for (FilterType filterType:FilterType.values()) {
			List<IFilter<REQ, RES>> filterList=filterMap.get(filterType.getCode());
			//分组进行组内排序过滤器
			Collections.sort(filterList, new Comparator<IFilter<REQ, RES>>() {
				@Override
				public int compare(IFilter<REQ, RES> o1, IFilter<REQ, RES> o2) {
					return o1.getClass().getAnnotation(Filter.class).order() - o2.getClass().getAnnotation(Filter.class).order();
				}
			});
			
			//$NON-NLS-收集有序服务$
			this.addFilters(filterList);
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public RES handler(REQ req, Object... args) throws Throwable {
		for (Map.Entry<String, Map<String, IFilter<REQ, RES>>> entryOnLine:serviceFilterOnLineMap.entrySet()) {
			String key = entryOnLine.getKey();
			Map<String, IFilter<REQ, RES>> valueOnLine = entryOnLine.getValue();
			for (Map.Entry<String, IFilter<REQ, RES>> tempEntryOnLine:valueOnLine.entrySet()) {
				System.out.println(key+"-->"+tempEntryOnLine.getValue().getClass().getAnnotation(Filter.class).order()+"-->"+tempEntryOnLine);				
			}
		}
		return null;
	}

	@Override
	public void destory() throws Exception {

	}

}