package com.cm.sanchalak.service.impl;

import com.cm.sanchalak.dto.curriculum.*;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.repository.spec.*;
import com.cm.sanchalak.security.OwnershipValidator;
import com.cm.sanchalak.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final SubjectChapterRepository chapterRepository;
    private final ChapterContentRepository contentRepository;
    private final QuestionRepository questionRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final OwnershipValidator ownership;

    // --- Chapters ---
    @Override
    public ChapterDto createChapter(ChapterRequest request) {
        SchoolClass sClass = classRepository.findOne(SchoolClassSpecification.activeById(request.getClassId()))
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findOne(SubjectSpecification.activeById(request.getSubjectId()))
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        SubjectChapter chapter = new SubjectChapter();
        chapter.setSchoolClass(sClass);
        chapter.setSubject(subject);
        chapter.setName(request.getName());
        chapter.setDescription(request.getDescription());
        chapter.setSequenceOrder(request.getSequenceOrder());

        return mapToChapterDto(chapterRepository.save(chapter));
    }

    @Override
    public ChapterDto updateChapter(Long id, ChapterRequest request) {
        SubjectChapter chapter = chapterRepository.findOne(CurriculumSpecification.chapterById(id))
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        chapter.setName(request.getName());
        chapter.setDescription(request.getDescription());
        chapter.setSequenceOrder(request.getSequenceOrder());

        return mapToChapterDto(chapterRepository.save(chapter));
    }

    @Override
    public void deleteChapter(Long id) {
        SubjectChapter chapter = chapterRepository.findOne(CurriculumSpecification.chapterById(id))
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        chapterRepository.delete(chapter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterDto> getChaptersByClassAndSubject(Long classId, Long subjectId) {
        classRepository.findOne(SchoolClassSpecification.activeById(classId))
                .orElseThrow(() -> new RuntimeException("Class not found"));

        return chapterRepository.findAll(CurriculumSpecification.chapterScoped()
                .and((root, query, cb) -> cb.equal(root.get("schoolClass").get("id"), classId))
                .and((root, query, cb) -> cb.equal(root.get("subject").get("id"), subjectId))
                .and((root, query, cb) -> {
                    query.orderBy(cb.asc(root.get("sequenceOrder")));
                    return cb.conjunction();
                }))
                .stream().map(this::mapToChapterDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChapterDto getChapter(Long id) {
        return chapterRepository.findOne(CurriculumSpecification.chapterById(id))
                .map(this::mapToChapterDto)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
    }

    // --- Content ---
    @Override
    public ContentDto addContentToChapter(Long chapterId, ContentRequest request) {
        SubjectChapter chapter = chapterRepository.findOne(CurriculumSpecification.chapterById(chapterId))
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        ChapterContent content = new ChapterContent();
        content.setChapter(chapter);
        content.setClassId(request.getClassId());
        content.setSubjectId(request.getSubjectId());
        content.setTitle(request.getTitle());
        content.setTextContent(request.getTextContent());
        content.setVideoUrl(request.getVideoUrl());
        content.setPdfUrl(request.getPdfUrl());
        content.setLinkUrl(request.getLinkUrl());
        content.setSequenceOrder(request.getSequenceOrder() != null ? request.getSequenceOrder() : 0);

        return mapToContentDto(contentRepository.save(content));
    }

    @Override
    public ContentDto createContent(ContentRequest request) {
        ChapterContent content = new ChapterContent();
        content.setClassId(request.getClassId());
        content.setSubjectId(request.getSubjectId());
        content.setTitle(request.getTitle());
        content.setTextContent(request.getTextContent());
        content.setVideoUrl(request.getVideoUrl());
        content.setPdfUrl(request.getPdfUrl());
        content.setLinkUrl(request.getLinkUrl());
        content.setSequenceOrder(request.getSequenceOrder() != null ? request.getSequenceOrder() : 0);

        if (request.getChapterId() != null) {
            SubjectChapter chapter = chapterRepository
                    .findOne(CurriculumSpecification.chapterById(request.getChapterId()))
                    .orElseThrow(() -> new RuntimeException("Chapter not found"));
            content.setChapter(chapter);
        }

        return mapToContentDto(contentRepository.save(content));
    }

    @Override
    public void deleteContent(Long contentId) {
        ChapterContent content = contentRepository.findOne(CurriculumSpecification.contentById(contentId))
                .orElseThrow(() -> new RuntimeException("Content not found"));
        contentRepository.delete(content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentDto> getChapterContents(Long chapterId) {
        chapterRepository.findOne(CurriculumSpecification.chapterById(chapterId))
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        return contentRepository.findAll(CurriculumSpecification.contentScoped()
                .and((root, query, cb) -> cb.equal(root.get("chapter").get("id"), chapterId))
                .and((root, query, cb) -> {
                    query.orderBy(cb.asc(root.get("sequenceOrder")));
                    return cb.conjunction();
                }))
                .stream().map(this::mapToContentDto).collect(Collectors.toList());
    }

    // --- Questions ---
    @Override
    public QuestionDto addQuestionToChapter(Long chapterId, QuestionRequest request) {
        SubjectChapter chapter = chapterRepository.findOne(CurriculumSpecification.chapterById(chapterId))
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        Question question = new Question();
        question.setChapter(chapter);
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(Question.QuestionType.valueOf(request.getQuestionType()));
        question.setMarks(request.getMarks());

        List<QuestionOption> options = new ArrayList<>();
        if (request.getOptions() != null) {
            for (QuestionRequest.QuestionOptionRequest reqOpt : request.getOptions()) {
                QuestionOption opt = new QuestionOption();
                opt.setQuestion(question);
                opt.setOptionText(reqOpt.getOptionText());
                opt.setIsCorrect(reqOpt.getIsCorrect());
                options.add(opt);
            }
        }
        question.setOptions(options);

        return mapToQuestionDto(questionRepository.save(question));
    }

    @Override
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findOne(QuestionSpecification.activeById(questionId))
                .orElseThrow(() -> new RuntimeException("Question not found"));
        questionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDto> getChapterQuestions(Long chapterId) {
        chapterRepository.findOne(CurriculumSpecification.chapterById(chapterId))
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        return questionRepository.findAll(QuestionSpecification.activeScoped()
                .and((root, query, cb) -> cb.equal(root.get("chapter").get("id"), chapterId)))
                .stream().map(this::mapToQuestionDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentDto> getAllContent(Long classId, Long subjectId, Long chapterId, String contentType, int page,
            int size) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);

        Specification<ChapterContent> spec = CurriculumSpecification.contentScoped();
        if (classId != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("classId"), classId));
        if (subjectId != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("subjectId"), subjectId));
        if (chapterId != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("chapter").get("id"), chapterId));

        return contentRepository.findAll(spec, pageable).map(this::mapToContentDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionDto> getAllQuestions(Long classId, Long subjectId, Long chapterId, int page, int size) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);

        Specification<Question> spec = QuestionSpecification.activeScoped();
        if (classId != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("chapter").get("schoolClass").get("id"), classId));
        if (subjectId != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("chapter").get("subject").get("id"), subjectId));
        if (chapterId != null)
            spec = spec.and((root, query, cb) -> cb.equal(root.get("chapter").get("id"), chapterId));

        return questionRepository.findAll(spec, pageable).map(this::mapToQuestionDto);
    }

    // --- Mappers ---
    private ChapterDto mapToChapterDto(SubjectChapter chapter) {
        return ChapterDto.builder()
                .id(chapter.getId())
                .classId(chapter.getSchoolClass().getId())
                .subjectId(chapter.getSubject().getId())
                .name(chapter.getName())
                .description(chapter.getDescription())
                .sequenceOrder(chapter.getSequenceOrder())
                .build();
    }

    private ContentDto mapToContentDto(ChapterContent content) {
        return ContentDto.builder()
                .id(content.getId())
                .chapterId(content.getChapter() != null ? content.getChapter().getId() : null)
                .classId(content.getClassId())
                .subjectId(content.getSubjectId())
                .title(content.getTitle())
                .textContent(content.getTextContent())
                .videoUrl(content.getVideoUrl())
                .pdfUrl(content.getPdfUrl())
                .linkUrl(content.getLinkUrl())
                .sequenceOrder(content.getSequenceOrder())
                .build();
    }

    private QuestionDto mapToQuestionDto(Question q) {
        List<QuestionDto.QuestionOptionDto> optionDtos = new ArrayList<>();
        if (q.getOptions() != null) {
            optionDtos = q.getOptions().stream().map(o -> QuestionDto.QuestionOptionDto.builder()
                    .id(o.getId())
                    .optionText(o.getOptionText())
                    .isCorrect(o.getIsCorrect())
                    .build()).collect(Collectors.toList());
        }

        return QuestionDto.builder()
                .id(q.getId())
                .chapterId(q.getChapter().getId())
                .questionText(q.getQuestionText())
                .questionType(q.getQuestionType().name())
                .marks(q.getMarks())
                .options(optionDtos)
                .build();
    }
}
