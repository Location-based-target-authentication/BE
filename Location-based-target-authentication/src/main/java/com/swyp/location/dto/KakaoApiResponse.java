package com.swyp.location.dto;

import java.util.List;

public record KakaoApiResponse(
    List<Document> documents
) {
    public record Document(
        String place_name,
        String address_name,
        String road_address_name,
        String x,  // longitude
        String y   // latitude
    ) {}
} 