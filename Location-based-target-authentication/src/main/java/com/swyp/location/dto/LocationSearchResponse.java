package com.swyp.location.dto;

import java.util.List;

public record LocationSearchResponse(
    List<LocationInfo> locations
) {
    public static LocationSearchResponse from(KakaoApiResponse response) {
        List<LocationInfo> locations = response.documents().stream()
            .map(doc -> new LocationInfo(
                doc.place_name(),
                doc.address_name(),
                doc.road_address_name(),
                Double.parseDouble(doc.y()),
                Double.parseDouble(doc.x())
            ))
            .toList();
        
        return new LocationSearchResponse(locations);
    }
} 