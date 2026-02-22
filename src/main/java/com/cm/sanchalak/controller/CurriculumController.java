package com.cm.sanchalak.controller;

import com.cm.sanchalak.dto.curriculum.*;
import com.cm.sanchalak.service.CurriculumService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/curriculum")
public class CurriculumController {

    private final CurriculumService curriculumService;

    public CurriculumController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    // === Chapters ===
    @PostMapping("/chapters")
    public ResponseEntity<ChapterDto> createChapter(@Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(curriculumService.createChapter(request));
    }

    @PutMapping("/chapters/{id}")
    public ResponseEntity<ChapterDto> updateChapter(@PathVariable Long id, @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(curriculumService.updateChapter(id, request));
    }

    @GetMapping("/chapters")
    public ResponseEntity<List<ChapterDto>> getChapters(
            @RequestParam Long classId,
            @RequestParam Long subjectId) {
        return ResponseEntity.ok(curriculumService.getChaptersByClassAndSubject(classId, subjectId));
    }

    @GetMapping("/chapters/{id}")
    public ResponseEntity<ChapterDto> getChapter(@PathVariable Long id) {
        return ResponseEntity.ok(curriculumService.getChapter(id));
    }

    @DeleteMapping("/chapters/{id}")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long id) {
        curriculumService.deleteChapter(id);
        return ResponseEntity.ok().build();
    }

    // === Content ===
    @PostMapping("/chapters/{chapterId}/content")
    public ResponseEntity<ContentDto> addContent(@PathVariable Long chapterId,
            @Valid @RequestBody ContentRequest request) {
        return ResponseEntity.ok(curriculumService.addContentToChapter(chapterId, request));
    }

    @GetMapping("/chapters/{chapterId}/content")
    public ResponseEntity<List<ContentDto>> getContents(@PathVariable Long chapterId) {
        return ResponseEntity.ok(curriculumService.getChapterContents(chapterId));
    }

    @PostMapping("/content")
    public ResponseEntity<ContentDto> createContent(@Valid @RequestBody ContentRequest request) {
        return ResponseEntity.ok(curriculumService.createContent(request));
    }

    @GetMapping("/content")
    public ResponseEntity<Page<ContentDto>> getAllContent(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) String contentType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity
                .ok(curriculumService.getAllContent(classId, subjectId, chapterId, contentType, page, size));
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        curriculumService.deleteContent(id);
        return ResponseEntity.ok().build();
    }

    // === Questions ===
    @PostMapping("/chapters/{chapterId}/questions")
    public ResponseEntity<QuestionDto> addQuestion(@PathVariable Long chapterId,
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(curriculumService.addQuestionToChapter(chapterId, request));
    }

    @GetMapping("/chapters/{chapterId}/questions")
    public ResponseEntity<List<QuestionDto>> getQuestions(@PathVariable Long chapterId) {
        return ResponseEntity.ok(curriculumService.getChapterQuestions(chapterId));
    }

    @GetMapping("/questions")
    public ResponseEntity<Page<QuestionDto>> getAllQuestions(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(curriculumService.getAllQuestions(classId, subjectId, chapterId, page, size));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        curriculumService.deleteQuestion(id);
        return ResponseEntity.ok().build();
    }
}
