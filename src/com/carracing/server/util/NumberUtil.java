package com.carracing.server.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class NumberUtil {
	
	public static double round(double value, int places) {
		return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
	}
}
