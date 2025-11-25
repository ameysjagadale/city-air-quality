package com.example.air_quality_service.controller;

import com.example.air_quality_service.model.AirQuality;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class AirQualityController {

    private final String API_TOKEN = "b8917c3a2ddf646b8d3107d41f41d7578fbc2d0b";
    private final String API_URL = "https://api.waqi.info/feed/";

    @GetMapping("/api/airquality")
    public AirQuality getAirQuality(@RequestParam String city) {
        String url = API_URL + city + "/?token=" + API_TOKEN;
        RestTemplate restTemplate = new RestTemplate();

        try {
            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !"ok".equals(response.get("status"))) return null;

            Map data = (Map) response.get("data");
            Map iaqi = (Map) data.get("iaqi");
            if (iaqi == null) iaqi = Map.of(); // empty map

            Map cityMap = (Map) data.get("city");

            AirQuality airQuality = new AirQuality();
            airQuality.setCityName(cityMap != null ? (String) cityMap.get("name") : city);
            Object aqiObj = data.get("aqi");
            airQuality.setAqi(aqiObj instanceof Number ? ((Number) aqiObj).intValue() : 0);

            airQuality.setPm25(getValue(iaqi, "pm25"));
            airQuality.setPm10(getValue(iaqi, "pm10"));
            airQuality.setCo(getValue(iaqi, "co"));
            airQuality.setNo2(getValue(iaqi, "no2"));
            airQuality.setSo2(getValue(iaqi, "so2"));
            airQuality.setO3(getValue(iaqi, "o3"));
            airQuality.setTemperature(getValue(iaqi, "t"));
            airQuality.setHumidity(getValue(iaqi, "h"));
            airQuality.setWind(getValue(iaqi, "w"));

            return airQuality;

        } catch (Exception e) {
            return new AirQuality(); // empty object in case of error
        }
    }

    private double getValue(Map<String, Map> iaqi, String key) {
        if (iaqi.get(key) != null && iaqi.get(key).get("v") instanceof Number) {
            return ((Number) iaqi.get(key).get("v")).doubleValue();
        }
        return 0;
    }
}
