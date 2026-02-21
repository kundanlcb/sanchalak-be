package com.cm.sanchalak.service;

import com.cm.sanchalak.dto.curriculum.*;
import org.springframework.data.domain.Page;
import java.util.List;

public interface CurriculumService {
    // Chapters
    ChapterDto createChapter(ChapterRequest request);

    ChapterDto updateChapter(Long id, ChapterRequest request);

    void deleteChapter(Long id);

    List<ChapterDto> getChaptersByClassAndSubject(Long classId, Long subjectId);

    ChapterDto getChapter(Long id);

    // Content
    ContentDto addContentToChapter(Long chapterId, ContentRequest request);

    void deleteContent(Long contentId);

    List<ContentDto> getChapterContents(Long chapterId);

    Page<ContentDto> getAllContent(Long classId, Long subjectId, Long chapterId, String contentType, int page,
            int size);

    // Questions
    QuestionDto addQuestionToChapter(Long chapterId, QuestionRequest request);

    void deleteQuestion(Long questionId);

    List<QuestionDto> getChapterQuestions(Long chapterId);

    Page<QuestionDto> getAllQuestions(Long classId, Long subjectId, Long chapterId, int page, int size);
}
