package com.learntrack.course.service;

import com.learntrack.course.cache.CourseCache;
import com.learntrack.course.dto.CourseRequest;
import com.learntrack.course.dto.CourseResponse;
import com.learntrack.course.exception.CourseNotFoundException;
import com.learntrack.course.exception.NotCourseOwnerException;
import com.learntrack.course.model.Course;
import com.learntrack.course.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseCache cache;

    public CourseService(CourseRepository courseRepository, CourseCache cache) {
        this.courseRepository = courseRepository;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCatalog() {
        return cache.getCatalog().orElseGet(() -> {
            List<CourseResponse> catalog = courseRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(CourseResponse::from)
                    .toList();
            cache.putCatalog(catalog);
            return catalog;
        });
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long id) {
        return cache.getCourse(id).orElseGet(() -> {
            CourseResponse course = CourseResponse.from(findCourse(id));
            cache.putCourse(course);
            return course;
        });
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request, String instructorEmail) {
        Course saved = courseRepository.save(
                new Course(request.title().trim(), request.description().trim(), instructorEmail));
        cache.evictCatalogAfterCommit();
        return CourseResponse.from(saved);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request, String instructorEmail) {
        Course course = findCourse(id);
        if (!course.getInstructorEmail().equals(instructorEmail)) {
            throw new NotCourseOwnerException();
        }
        course.update(request.title().trim(), request.description().trim());
        cache.evictCatalogAfterCommit();
        cache.evictCourseAfterCommit(id);
        return CourseResponse.from(course);
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
    }
}
