package com.cm.sanchalak.service.impl;

import com.cm.sanchalak.dto.curriculum.*;
import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.repository.*;
import com.cm.sanchalak.service.CurriculumService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Transactional
public class CurriculumServiceImpl implements CurriculumService {

    private final SubjectChapterRepository chapterRepository;
    private final ChapterContentRepository contentRepository;
    private final QuestionRepository questionRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;

    public CurriculumServiceImpl(SubjectChapterRepository chapterRepository,
            ChapterContentRepository contentRepository,
            QuestionRepository questionRepository,
            SchoolClassRepository classRepository,
            SubjectRepository subjectRepository) {
        this.chapterRepository = chapterRepository;
        this.contentRepository = contentRepository;
        this.questionRepository = questionRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
    }

    // --- Chapters ---
    @Override
    public ChapterDto createChapter(ChapterRequest request) {
        SchoolClass sClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
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
        SubjectChapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        chapter.setName(request.getName());
        chapter.setDescription(request.getDescription());
        chapter.setSequenceOrder(request.getSequenceOrder());

        return mapToChapterDto(chapterRepository.save(chapter));
    }

    @Override
    public void deleteChapter(Long id) {
        chapterRepository.deleteById(id);
    }

    @Override
    public List<ChapterDto> getChaptersByClassAndSubject(Long classId, Long subjectId) {
        return chapterRepository.findBySchoolClass_IdAndSubject_IdOrderBySequenceOrderAsc(classId, subjectId)
                .stream().map(this::mapToChapterDto).collect(Collectors.toList());
    }

    @Override
    public ChapterDto getChapter(Long id) {
        return chapterRepository.findById(id)
                .map(this::mapToChapterDto)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
    }

    // --- Content ---
    @Override
    public ContentDto addContentToChapter(Long chapterId, ContentRequest request) {
        SubjectChapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        ChapterContent content = new ChapterContent();
        content.setChapter(chapter);
        content.setTitle(request.getTitle());
        content.setContentType(ChapterContent.ContentType.valueOf(request.getContentType()));
        content.setContentData(request.getContentData());
        content.setSequenceOrder(request.getSequenceOrder());

        return mapToContentDto(contentRepository.save(content));
    }

    @Override
    public void deleteContent(Long contentId) {
        contentRepository.deleteById(contentId);
    }

    @Override
    public List<ContentDto> getChapterContents(Long chapterId) {
        return contentRepository.findByChapter_IdOrderBySequenceOrderAsc(chapterId)
                .stream().map(this::mapToContentDto).collect(Collectors.toList());
    }

    // --- Questions ---
    @Override
    public QuestionDto addQuestionToChapter(Long chapterId, QuestionRequest request) {
        SubjectChapter chapter = chapterRepository.findById(chapterId)
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

        // Save implicitly saves options due to CascadeType.ALL
        return mapToQuestionDto(questionRepository.save(question));
    }

    @Override
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    @Override
    public List<QuestionDto> getChapterQuestions(Long chapterId) {
        return questionRepository.findByChapter_Id(chapterId)
                .stream().map(this::mapToQuestionDto).collect(Collectors.toList());
    }

    @Override
    public Page<ContentDto> getAllContent(Long classId, Long subjectId, Long chapterId, String contentType, int page,
            int size) {
        ChapterContent.ContentType type = null;
        if (contentType != null && !contentType.isBlank()) {
            try {
                type = ChapterContent.ContentType.valueOf(contentType);
            } catch (IllegalArgumentException ignored) {
                // invalid type → return all
            }
        }
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);
        return contentRepository.findAllWithFilters(classId, subjectId, chapterId, type, pageable)
                .map(this::mapToContentDto);
    }

    @Override
    public Page<QuestionDto> getAllQuestions(Long classId, Long subjectId, Long chapterId, int page, int size) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);
        return questionRepository.findAllWithFilters(classId, subjectId, chapterId, pageable)
                .map(this::mapToQuestionDto);
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
                .chapterId(content.getChapter().getId())
                .title(content.getTitle())
                .contentType(content.getContentType().name())
                .contentData(content.getContentData())
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
