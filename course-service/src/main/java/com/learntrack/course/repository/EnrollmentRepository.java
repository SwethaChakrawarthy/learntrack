package com.learntrack.course.repository;

import com.learntrack.course.model.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByCourseIdAndStudentEmail(Long courseId, String studentEmail);

    // Load each enrollment's course in the same query to avoid the N+1 problem.
    @EntityGraph(attributePaths = "course")
    List<Enrollment> findByStudentEmailOrderByEnrolledAtDesc(String studentEmail);

    @EntityGraph(attributePaths = "course")
    Optional<Enrollment> findByIdAndStudentEmail(Long id, String studentEmail);
}
