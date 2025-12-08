package com.yunju.stats_service.domain.stats.batch;

import com.yunju.stats_service.domain.stats.model.entity.*;
import com.yunju.stats_service.domain.stats.model.id.*;
import com.yunju.stats_service.domain.stats.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickAggregationBatch {

    private final ShortUrlClickLogRepository clickLogRepository;
    private final UrlDailyClicksRepository dailyClicksRepository;
    private final UrlDeviceClicksRepository deviceClicksRepository;
    private final UrlReferrerClicksRepository referrerClicksRepository;
    private final UrlDailyDeviceClicksRepository dailyDeviceClicksRepository;
    private final UrlDailyReferrerClicksRepository dailyReferrerClicksRepository;
    private final AggregationStateRepository aggregationStateRepository;

    @Scheduled(fixedDelayString = "${stats.batch.interval-ms}")
    public void aggregate() {

        AggregationState state = loadState();
        LocalDateTime lastTime = state.getLastAggregatedAt();
        LocalDateTime now = LocalDateTime.now();

        log.info("[BATCH] Start aggregation | lastAggregatedAt={}", lastTime);

        List<ShortUrlClickLog> logs = clickLogRepository.findByClickedAtAfter(lastTime);

        if (logs.isEmpty()) {
            log.info("[BATCH] No new logs. Update timestamp only.");
            state.update(now);
            aggregationStateRepository.save(state);
            return;
        }

        recordDailyClicks(logs);
        recordDeviceClicks(logs);
        recordReferrerClicks(logs);
        recordDailyDeviceClicks(logs);
        recordDailyReferrerClicks(logs);

        state.update(now);
        aggregationStateRepository.save(state);

        log.info("[BATCH] Finished | new lastAggregatedAt={}", now);
    }

    /** 배치 시작 시 로드 */
    private AggregationState loadState() {
        return aggregationStateRepository.findById(1)
                .orElseGet(() -> aggregationStateRepository.save(AggregationState.init()));
    }

    // ----------------------------
    // 1) Daily Clicks (shortKey + date)
    // ----------------------------
    private void recordDailyClicks(List<ShortUrlClickLog> logs) {

        logs.stream()
                .collect(Collectors.groupingBy(
                        log -> new UrlDailyClicksId(log.getShortKey(), log.getClickedAt().toLocalDate()),
                        Collectors.toList()
                ))
                .forEach((id, logList) -> {

                    // group 안의 아무 log 로 originalUrl/shortUrl 적용
                    ShortUrlClickLog any = logList.get(0);

                    int clickCount = logList.size();

                    UrlDailyClicks entity = dailyClicksRepository.findById(id)
                            .orElse(UrlDailyClicks.builder()
                                    .id(id)
                                    .totalClicks(0)
                                    .originalUrl(any.getOriginalUrl())
                                    .shortUrl(any.getShortUrl())
                                    .build());

                    entity.increase(clickCount);
                    dailyClicksRepository.save(entity);
                });
    }

    // ----------------------------
    // 2) Device Clicks (shortKey + deviceType)
    // ----------------------------
    private void recordDeviceClicks(List<ShortUrlClickLog> logs) {

        logs.stream()
                .collect(Collectors.groupingBy(
                        log -> new UrlDeviceClicksId(log.getShortKey(), log.getDeviceType().name()),
                        Collectors.toList()
                ))
                .forEach((id, logList) -> {

                    ShortUrlClickLog any = logList.get(0);
                    int clickCount = logList.size();

                    UrlDeviceClicks entity = deviceClicksRepository.findById(id)
                            .orElse(UrlDeviceClicks.builder()
                                    .id(id)
                                    .clicks(0)
                                    .originalUrl(any.getOriginalUrl())
                                    .shortUrl(any.getShortUrl())
                                    .build());

                    entity.increase(clickCount);
                    deviceClicksRepository.save(entity);
                });
    }

    // ----------------------------
    // 3) Referrer Clicks (shortKey + referrer)
    // ----------------------------
    private void recordReferrerClicks(List<ShortUrlClickLog> logs) {

        logs.stream()
                .collect(Collectors.groupingBy(
                        log -> new UrlReferrerClicksId(
                                log.getShortKey(),
                                log.getReferrer() == null ? "direct" : log.getReferrer()
                        ),
                        Collectors.toList()
                ))
                .forEach((id, logList) -> {

                    ShortUrlClickLog any = logList.get(0);
                    int clickCount = logList.size();

                    UrlReferrerClicks entity = referrerClicksRepository.findById(id)
                            .orElse(UrlReferrerClicks.builder()
                                    .id(id)
                                    .clicks(0)
                                    .originalUrl(any.getOriginalUrl())
                                    .shortUrl(any.getShortUrl())
                                    .build());

                    entity.increase(clickCount);
                    referrerClicksRepository.save(entity);
                });
    }

    // ----------------------------
    // 4) Daily Device Clicks (shortKey + date + deviceType)
    // ----------------------------
    private void recordDailyDeviceClicks(List<ShortUrlClickLog> logs) {

        logs.stream()
                .collect(Collectors.groupingBy(
                        log -> new UrlDailyDeviceClicksId(
                                log.getShortKey(),
                                log.getClickedAt().toLocalDate(),
                                log.getDeviceType().name()
                        ),
                        Collectors.toList()
                ))
                .forEach((id, logList) -> {

                    ShortUrlClickLog any = logList.get(0);
                    int clickCount = logList.size();

                    UrlDailyDeviceClicks entity = dailyDeviceClicksRepository.findById(id)
                            .orElse(UrlDailyDeviceClicks.builder()
                                    .id(id)
                                    .clicks(0)
                                    .originalUrl(any.getOriginalUrl())
                                    .shortUrl(any.getShortUrl())
                                    .build());

                    entity.increase(clickCount);
                    dailyDeviceClicksRepository.save(entity);
                });
    }

    // ----------------------------
    // 5) Daily Referrer Clicks (shortKey + date + referrer)
    // ----------------------------
    private void recordDailyReferrerClicks(List<ShortUrlClickLog> logs) {

        logs.stream()
                .collect(Collectors.groupingBy(
                        log -> new UrlDailyReferrerClicksId(
                                log.getShortKey(),
                                log.getClickedAt().toLocalDate(),
                                log.getReferrer() == null ? "direct" : log.getReferrer()
                        ),
                        Collectors.toList()
                ))
                .forEach((id, logList) -> {

                    ShortUrlClickLog any = logList.get(0);
                    int clickCount = logList.size();

                    UrlDailyReferrerClicks entity = dailyReferrerClicksRepository.findById(id)
                            .orElse(UrlDailyReferrerClicks.builder()
                                    .id(id)
                                    .clicks(0)
                                    .originalUrl(any.getOriginalUrl())
                                    .shortUrl(any.getShortUrl())
                                    .build());

                    entity.increase(clickCount);
                    dailyReferrerClicksRepository.save(entity);
                });
    }
}

