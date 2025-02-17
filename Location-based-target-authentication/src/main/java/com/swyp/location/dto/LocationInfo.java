package com.swyp.location.dto;

public record LocationInfo(
    String placeName,
    String address,
    String roadAddress,
    Double latitude,
    Double longitude
) {} 