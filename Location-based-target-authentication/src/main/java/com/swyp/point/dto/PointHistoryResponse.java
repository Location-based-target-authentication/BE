package com.swyp.point.dto;


import com.swyp.point.entity.PointHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PointHistoryResponse {
    private Long userId;
    private String socialId;
    private List<PointHistory> pointHistory;
}
