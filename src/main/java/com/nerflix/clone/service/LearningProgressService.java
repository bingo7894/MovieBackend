package com.nerflix.clone.service;

import com.nerflix.clone.dto.request.LessonNoteRequest;
import com.nerflix.clone.dto.request.ProgressUpdateRequest;
import com.nerflix.clone.dto.response.*;

import java.util.List;

public interface LearningProgressService {
    LessonProgressResponse updateProgress(Long episodeId, ProgressUpdateRequest request, String email);

    MessageResponse enrollCourse(Long courseId, String email);

    MessageResponse unenrollCourse(Long courseId, String email);

    PageResponse<CourseSummaryResponse> getEnrolledCourses(String email, int page, int size, String search);

    List<LessonNoteResponse> getNotes(Long episodeId, String email);

    LessonNoteResponse createNote(Long episodeId, LessonNoteRequest request, String email);

    MessageResponse deleteNote(Long noteId, String email);
}
