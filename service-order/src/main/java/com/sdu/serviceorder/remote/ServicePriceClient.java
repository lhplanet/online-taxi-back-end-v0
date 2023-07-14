package com.sdu.serviceorder.remote;

import com.sdu.internalcommon.dto.ResponseResult;
import com.sdu.internalcommon.request.PriceRuleIsNewRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author LHP
 * @date 2023-07-14 17:46
 * @description
 */

@FeignClient("service-price")
public interface ServicePriceClient {

//    @PostMapping("/price-rule/is-new")
//    public ResponseResult<Boolean> isNew(@RequestParam String fareType, @RequestParam Integer fareVersion);

    @PostMapping("/price-rule/is-new")
    public ResponseResult<Boolean> isNew(@RequestBody PriceRuleIsNewRequest priceRuleIsNewRequest);

//    @RequestMapping(method = RequestMethod.GET,value = "/price-rule/if-exists")
//    public ResponseResult<Boolean> ifPriceExists(@RequestBody PriceRule priceRule);
//
//    @RequestMapping(method = RequestMethod.POST, value = "/calculate-price")
//    public ResponseResult<Double> calculatePrice(@RequestParam Integer distance , @RequestParam Integer duration, @RequestParam String cityCode, @RequestParam String vehicleType);


}
