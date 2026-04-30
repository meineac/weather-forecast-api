package com.forecast.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
public class Coordinate {
    private BigDecimal lat;
    private BigDecimal lon;
}