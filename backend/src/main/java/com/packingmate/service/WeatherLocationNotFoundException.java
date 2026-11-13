package com.packingmate.service;

/** 국가 코드에 맞는 도시를 지오코딩할 수 없을 때 발생한다. */
public class WeatherLocationNotFoundException extends InvalidChecklistRequestException {
    public WeatherLocationNotFoundException() {
        super("도시를 찾지 못했습니다. 도시 이름과 선택한 국가를 확인해 주세요.");
    }
}
