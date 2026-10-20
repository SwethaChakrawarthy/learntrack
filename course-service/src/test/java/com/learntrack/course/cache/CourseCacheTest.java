package com.learntrack.course.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntrack.course.dto.CourseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseCacheTest {

    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> ops;

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private CourseCache cache;

    @BeforeEach
    void setUp() {
        cache = new CourseCache(redis, mapper, 10);
    }

    @Test
    void cachedCatalog_isDeserialized() throws Exception {
        List<CourseResponse> catalog = List.of(
                new CourseResponse(1L, "Java", "Intro", "t@x.com", Instant.parse("2026-01-01T00:00:00Z")));
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(CourseCache.CATALOG_KEY)).thenReturn(mapper.writeValueAsString(catalog));

        assertThat(cache.getCatalog()).contains(catalog);
    }

    @Test
    void redisDown_isTreatedAsCacheMiss() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenThrow(new RedisConnectionFailureException("Redis is down"));

        assertThat(cache.getCatalog()).isEmpty();
    }

    @Test
    void eviction_outsideTransaction_deletesImmediately() {
        cache.evictCatalogAfterCommit();

        verify(redis).delete(CourseCache.CATALOG_KEY);
    }
}
