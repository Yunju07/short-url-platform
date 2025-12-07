package com.yunju.url_service.infra.aggregation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ClickAggregationStore {

    private final Map<String, ClickStat> store = new ConcurrentHashMap<>();

    public void accumulate(String shortKey, LocalDateTime clickedAt) {
        store.compute(shortKey, (key, stat) -> {
            if (stat == null) {
                log.debug("[STORE INIT] key={} firstClick={}", shortKey, clickedAt);
                return new ClickStat(1L, clickedAt);
            }
            stat.totalClicks++;
            stat.lastClickedAt = clickedAt;
            log.debug("[STORE ADD] key={} total={}", shortKey, stat.getTotalClicks());
            return stat;
        });
    }

    public Map<String, ClickStat> snapshotAndClear() {
        Map<String, ClickStat> copy = new HashMap<>(store);
        store.clear();
        return copy;
    }

    @Getter
    @AllArgsConstructor
    public static class ClickStat {
        private Long totalClicks;
        private LocalDateTime lastClickedAt;
    }
}
