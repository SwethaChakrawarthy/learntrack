package com.learntrack.course.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntrack.course.dto.CourseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Cache-aside storage for course data in Redis.
 * Callers check the cache first, load from PostgreSQL on a miss, then store the result with a TTL.
 * Redis failures are logged and treated as cache misses, so the API keeps working if Redis is down.
 */
@Component
public class CourseCache {

    private static final Logger log = LoggerFactory.getLogger(CourseCache.class);
    static final String CATALOG_KEY = "learntrack:courses:catalog";
    static final String COURSE_KEY_PREFIX = "learntrack:courses:";
    private static final TypeReference<List<CourseResponse>> CATALOG_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final Duration ttl;

    public CourseCache(StringRedisTemplate redis, ObjectMapper mapper,
                       @Value("${cache.course-ttl-minutes}") long ttlMinutes) {
        this.redis = redis;
        this.mapper = mapper;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public Optional<List<CourseResponse>> getCatalog() {
        return read(CATALOG_KEY, json -> mapper.readValue(json, CATALOG_TYPE));
    }

    public void putCatalog(List<CourseResponse> catalog) {
        write(CATALOG_KEY, catalog);
    }

    public Optional<CourseResponse> getCourse(Long id) {
        return read(courseKey(id), json -> mapper.readValue(json, CourseResponse.class));
    }

    public void putCourse(CourseResponse course) {
        write(courseKey(course.id()), course);
    }

    /** Deletes the catalog entry only after the database transaction commits. */
    public void evictCatalogAfterCommit() {
        afterCommit(() -> delete(CATALOG_KEY));
    }

    public void evictCourseAfterCommit(Long id) {
        afterCommit(() -> delete(courseKey(id)));
    }

    private static String courseKey(Long id) {
        return COURSE_KEY_PREFIX + id;
    }

    @FunctionalInterface
    private interface JsonReader<T> {
        T read(String json) throws JsonProcessingException;
    }

    private <T> Optional<T> read(String key, JsonReader<T> reader) {
        try {
            String json = redis.opsForValue().get(key);
            return json == null ? Optional.empty() : Optional.of(reader.read(json));
        } catch (Exception e) {
            log.warn("Cache read failed for key {}; falling back to database", key, e);
            return Optional.empty();
        }
    }

    private void write(String key, Object value) {
        try {
            redis.opsForValue().set(key, mapper.writeValueAsString(value), ttl);
        } catch (Exception e) {
            log.warn("Cache write failed for key {}", key, e);
        }
    }

    private void delete(String key) {
        try {
            redis.delete(key);
        } catch (Exception e) {
            // The TTL still expires the stale entry, so this failure is bounded.
            log.warn("Cache eviction failed for key {}", key, e);
        }
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
