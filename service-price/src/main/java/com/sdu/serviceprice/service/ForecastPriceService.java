package com.sdu.serviceprice.service;

import com.sdu.internalcommon.constant.CommonStatusEnum;
import com.sdu.internalcommon.dto.PriceRule;
import com.sdu.internalcommon.dto.ResponseResult;
import com.sdu.internalcommon.request.ForecastPriceDTO;
import com.sdu.internalcommon.response.DirectionResponse;
import com.sdu.internalcommon.response.ForecastPriceResponse;
import com.sdu.serviceprice.mapper.PriceRuleMapper;
import com.sdu.serviceprice.remote.ServiceMapClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author LHP
 * @date 2023-07-12 0:46
 * @description
 */

@Service
@Slf4j
public class ForecastPriceService {

    @Autowired
    private ServiceMapClient serviceMapClient;

    @Autowired
    private PriceRuleMapper priceRuleMapper;

    /**
     * 根据出发地和目的地经纬度，预估价格
     * @param depLongitude
     * @param depLatitude
     * @param destLongitude
     * @param destLatitude
     * @return
     */
    public ResponseResult forecastPrice(String depLongitude, String depLatitude, String destLongitude, String destLatitude) {

        log.info("出发地经度：" + depLongitude);
        log.info("出发地纬度：" + depLatitude);
        log.info("目的地经度：" + destLongitude);
        log.info("目的地纬度：" + destLatitude);

        log.info("调用地图服务，查询距离和时长");
        ForecastPriceDTO forecastPriceDTO = new ForecastPriceDTO();
        forecastPriceDTO.setDepLongitude(depLongitude);
        forecastPriceDTO.setDepLatitude(depLatitude);
        forecastPriceDTO.setDestLongitude(destLongitude);
        forecastPriceDTO.setDestLatitude(destLatitude);
        ResponseResult<DirectionResponse> direction = serviceMapClient.direction(forecastPriceDTO);
        Integer distance = direction.getData().getDistance(); // 距离
        Integer duration = direction.getData().getDuration(); // 时长
        log.info("距离：" + distance + "，时长：" + duration);

        log.info("读取计价规则");
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("city_code", "110000");
        queryMap.put("vehicle_type", "1");
        List<PriceRule> priceRules = priceRuleMapper.selectByMap(queryMap);
        if (priceRules.size() == 0) {
            return ResponseResult.fail(CommonStatusEnum.PRICE_NOT_EXISTS.getCode(), CommonStatusEnum.PRICE_NOT_EXISTS.getValue());
        }
        PriceRule priceRule = priceRules.get(0); // 计价规则

        log.info("根据距离、时长和计价规则，计算价格");
        double price = getPrice(distance, duration, priceRule);

        ForecastPriceResponse forecastPriceResponse = new ForecastPriceResponse();
        forecastPriceResponse.setPrice(price);
        return ResponseResult.success(forecastPriceResponse);
    }

    /**
     * 根据距离、时长和计价规则，计算价格
     * @param distance 距离
     * @param duration 时长
     * @param priceRule 计价规则
     * @return
     */
    private double getPrice(Integer distance, Integer duration, PriceRule priceRule) {
        BigDecimal price = new BigDecimal(0);

        // 起步价
        Double startFare = priceRule.getStartFare();
        BigDecimal startFareBigDecimal = new BigDecimal(startFare);
        price = price.add(startFareBigDecimal);

        // 里程费
        // 超出起步里程（总里程 - 起步里程）
        // 总里程 m
        BigDecimal distanceBigDecimal = new BigDecimal(distance);
        // 总里程 km
        BigDecimal distanceKmBigDecimal = distanceBigDecimal.divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP);
        // 起步里程 km
        Integer startMile = priceRule.getStartMile();
        BigDecimal startKmBigDecimal = new BigDecimal(startMile);
        // 总里程 - 起步里程
        double distanceSubtract = distanceKmBigDecimal.subtract(startKmBigDecimal).doubleValue();
        // 最终收费的里程 km
        Double mile = distanceSubtract > 0 ? distanceSubtract : 0;
        BigDecimal mileBigDecimal = new BigDecimal(mile);
        // 计程单价 元/km
        Double unitPricePerMile = priceRule.getUnitPricePerMile();
        BigDecimal unitPricePerMileBigDecimal = new BigDecimal(unitPricePerMile);
        // 里程费（最终收费的里程 * 计程单价）
        BigDecimal mileFare = mileBigDecimal.multiply(unitPricePerMileBigDecimal).setScale(2, BigDecimal.ROUND_HALF_UP);
        price = price.add(mileFare);

        // 时长费
        // 时长 s
        BigDecimal time = new BigDecimal(duration);
        // 时长 min
        BigDecimal timeBigDecimal = time.divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP);
        // 计时单价 元/min
        Double unitPricePerMinute = priceRule.getUnitPricePerMinute();
        BigDecimal unitPricePerMinuteBigDecimal = new BigDecimal(unitPricePerMinute);
        // 时长费（时长 * 计时单价）
        BigDecimal timeFare = timeBigDecimal.multiply(unitPricePerMinuteBigDecimal);
        price = price.add(timeFare).setScale(2, BigDecimal.ROUND_HALF_UP);

        return price.doubleValue();
    }

/*    public static void main(String[] args) {
        PriceRule priceRule = new PriceRule();
        priceRule.setStartFare(10.00);
        priceRule.setStartMile(3);
        priceRule.setUnitPricePerMile(1.80);
        priceRule.setUnitPricePerMinute(0.50);
        System.out.println(getPrice(6500, 1800, priceRule));
    }*/

}
