package com.redis.service;

import com.redis.repository.TimestampRepositoryReactiveImpl;
import com.redis.repository.UpdatesRepositoryReactiveImpl;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@RequiredArgsConstructor
public class UpdatesService {

    private final UpdatesRepositoryReactiveImpl updatesRepository;

    private final TimestampRepositoryReactiveImpl timestampRepository;

    public Mono<Boolean> addNewUpdates(String id, Long timestamp, List<String> updates) {
        return Mono.zip(
                updatesRepository.addNewUpdates(id, timestamp, updates),
                timestampRepository.addNewTimestamp(id, timestamp)
        )
                .map(Tuple2::getT1);
    }

    public Mono<Boolean> deleteOldUpdates(String id) {
        return updatesRepository.getById(id)
                .flatMap(map -> {
                    Long max = map.keySet()
                            .stream()
                            .max(Comparator.comparingLong(l -> l))
                            .orElseThrow();
                    List<Long> timestamps = map.keySet()
                            .stream()
                            .filter(timestamp -> timestamp < max)
                            .collect(Collectors.toList());
                    return timestampRepository.overrideOldValue(id, max)
                            .zipWith(updatesRepository.deleteTimestamps(id, timestamps)
                                    .map(deleted -> timestamps.size() == deleted))
                            .map(tuple -> tuple.getT1() && tuple.getT2());
                });
    }

    public Mono<Long> getOldest() {
        return timestampRepository.getAll()
                .map(list -> list.stream().max(Long::compareTo).orElseThrow());
    }

    public Mono<Properties> getDbState() {
        return timestampRepository.info();
    }

}
