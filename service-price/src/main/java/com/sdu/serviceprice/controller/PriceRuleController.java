package com.sdu.serviceprice.controller;

import com.sdu.internalcommon.dto.PriceRule;
import com.sdu.internalcommon.dto.ResponseResult;
import com.sdu.internalcommon.request.PriceRuleIsNewRequest;
import com.sdu.serviceprice.service.PriceRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author LHP
 * @date 2023-07-14 10:55
 * @description
 */

@RestController
@RequestMapping("/price-rule")
public class PriceRuleController {

    @Autowired
    PriceRuleService priceRuleService;

    @PostMapping("/add")
    public ResponseResult add(@RequestBody PriceRule priceRule){
        return priceRuleService.add(priceRule);
    }

    @PostMapping("/edit")
    public ResponseResult edit(@RequestBody PriceRule priceRule){
        return priceRuleService.edit(priceRule);
    }

    /**
     * 查询最新的计价规则
     * @param fareType
     * @return
     */
    @GetMapping("/get-newest-version")
    public ResponseResult<PriceRule> getNewestVersion(@RequestParam String fareType){
        return priceRuleService.getNewestVersion(fareType);
    }

    /**
     * 判断规则是否是最新
     * @param priceRuleIsNewRequest
     * @return
     */
    @PostMapping("/is-new")
    public ResponseResult<Boolean> isNew(@RequestBody PriceRuleIsNewRequest priceRuleIsNewRequest){

        return priceRuleService.isNew(priceRuleIsNewRequest.getFareType(),priceRuleIsNewRequest.getFareVersion());
    }
//
//    /**
//     * 判断该城市和对应车型的计价规则是否存在
//     * @param priceRule
//     * @return
//     */
//    @PostMapping("/if-exists")
//    public ResponseResult<Boolean> ifExists(@RequestBody PriceRule priceRule){
//        return priceRuleService.ifExists(priceRule);
//    }
}
