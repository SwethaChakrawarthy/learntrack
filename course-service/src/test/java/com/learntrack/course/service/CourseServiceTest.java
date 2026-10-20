package com.learntrack.course.service;

import com.learntrack.course.cache.CourseCache;
import com.learntrack.course.dto.CourseRequest;
import com.learntrack.course.dto.CourseResponse;
import com.learntrack.course.exception.CourseNotFoundException;
import com.learntrack.course.exception.NotCourseOwnerException;
import com.learntrack.course.model.Course;
import com.learntrack.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock CourseRepository courseRepository;
    @Mock CourseCache cache;
    @InjectMocks CourseService courseService;

    @Test
    void getCatalog_onCacheHit_doesNotQueryDatabase() {
        List<CourseResponse> cached = List.of(new CourseResponse(1L, "Java", "Intro", "t@x.com", null));
        when(cache.getCatalog()).thenReturn(Optional.of(cached));

        assertThat(courseService.getCatalog()).isEqualTo(cached);
        verifyNoInteractions(courseRepository);
    }

    @Test
    void getCatalog_onCacheMiss_loadsFromDatabase_andCachesResult() {
        when(cache.getCatalog()).thenReturn(Optional.empty());
        when(courseRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(new Course("Java", "Intro", "t@x.com")));

        List<CourseResponse> result = courseService.getCatalog();

        assertThat(result).extracting(CourseResponse::title).containsExactly("Java");
        verify(cache).putCatalog(anyList());
    }

    @Test
    void getCourse_missing_throwsNotFound() {
        when(cache.getCourse(99L)).thenReturn(Optional.empty());
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourse(99L))
                .isInstanceOf(CourseNotFoundException.class);
    }

    @Test
    void createCourse_evictsCatalog() {
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse created = courseService.createCourse(new CourseRequest(" Java ", "Intro"), "t@x.com");

        assertThat(created.title()).isEqualTo("Java");
        assertThat(created.instructorEmail()).isEqualTo("t@x.com");
        verify(cache).evictCatalogAfterCommit();
    }

    @Test
    void updateCourse_byAnotherInstructor_isForbidden_andCacheUntouched() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(new Course("Java", "Intro", "owner@x.com")));

        assertThatThrownBy(() -> courseService.updateCourse(1L, new CourseRequest("New", "New"), "other@x.com"))
                .isInstanceOf(NotCourseOwnerException.class);
        verify(cache, never()).evictCatalogAfterCommit();
    }
}
