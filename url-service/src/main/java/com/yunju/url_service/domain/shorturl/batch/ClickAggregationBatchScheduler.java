package com.yunju.url_service.domain.shorturl.batch;

import com.yunju.url_service.domain.shorturl.repository.ShortUrlRepository;
import com.yunju.url_service.infra.aggregation.ClickAggregationStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickAggregationBatchScheduler {

    private final ClickAggregationStore aggregationStore;
    private final ShortUrlRepository shortUrlRepository;

    @Transactional
    @Scheduled(fixedRate = 5000)
    public void flushAggregation() {

        Map<String, ClickAggregationStore.ClickStat> snapshot = aggregationStore.snapshotAndClear();

        if (snapshot.isEmpty()) {
            log.debug("[AGGREGATION FLUSH] Snapshot is empty — skip update");
            return;
        }

        log.debug("[AGGREGATION FLUSH] 시작 — entries={}", snapshot.size());

        snapshot.forEach((shortKey, stat) -> {

            log.debug(
                    "[AGG UPDATE] shortKey={}, incrementClicks={}, lastClickAt={}",
                    shortKey,
                    stat.getTotalClicks(),
                    stat.getLastClickedAt()
            );

            shortUrlRepository.updateClickInfo(
                    shortKey,
                    stat.getTotalClicks(),
                    stat.getLastClickedAt()
            );
        });

        log.debug("[AGGREGATION FLUSH] 완료 — updatedRows={}", snapshot.size());
    }
}
