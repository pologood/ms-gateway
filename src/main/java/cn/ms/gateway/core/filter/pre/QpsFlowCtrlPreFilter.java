package cn.ms.gateway.core.filter.pre;

import cn.ms.gateway.base.filter.FilterType;
import cn.ms.gateway.base.filter.MSFilter;
import cn.ms.gateway.common.annotation.Filter;
import cn.ms.gateway.entity.GatewayREQ;
import cn.ms.gateway.entity.GatewayRES;

/**
 * 请求头参数校验
 * 
 * @author lry
 */
@Filter(value = FilterType.PRE, order=140)
public class QpsFlowCtrlPreFilter extends MSFilter<GatewayREQ, GatewayRES> {

	@Override
	public boolean check(GatewayREQ req, GatewayRES res, Object... args) throws Exception {
		return true;
	}

	@Override
	public GatewayRES run(GatewayREQ req, GatewayRES res, Object... args) throws Exception {
		return null;
	}

}
