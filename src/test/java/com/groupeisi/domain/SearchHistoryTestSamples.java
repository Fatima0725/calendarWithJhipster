package com.groupeisi.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SearchHistoryTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static SearchHistory getSearchHistorySample1() {
        return new SearchHistory().id(1L).dayOfWeek("dayOfWeek1");
    }

    public static SearchHistory getSearchHistorySample2() {
        return new SearchHistory().id(2L).dayOfWeek("dayOfWeek2");
    }

    public static SearchHistory getSearchHistoryRandomSampleGenerator() {
        return new SearchHistory().id(longCount.incrementAndGet()).dayOfWeek(UUID.randomUUID().toString());
    }
}
