package com.github.iauglov.service;

import com.github.iauglov.model.NotFoundException;
import com.github.iauglov.persistence.Answer;
import com.github.iauglov.persistence.AnswerRepository;
import com.github.iauglov.persistence.Question;
import com.github.iauglov.persistence.QuestionRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class QuestionAnswerService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    public List<Question> getAllQuestionsForGuide(Integer guideId) {
        return questionRepository.findAllByGuideId(guideId);
    }

    public List<Question> getAllQuestionsForAnswer(Integer answerId) {
        return questionRepository.findAllByAnswerId(answerId);
    }

    public Answer getAnswerForQuestion(Integer questionId) throws NotFoundException {
        return answerRepository.findByQuestionId(questionId).orElseThrow(() -> new NotFoundException(String.format("Can't find answer with question id '%d'", questionId)));
    }

    public void deleteAnswer(int parseInt) throws NotFoundException {
        if (answerRepository.existsById(parseInt)) {
            answerRepository.deleteById(parseInt);
        } else {
            throw new NotFoundException(String.format("Answer with id '%d' not found", parseInt));
        }
    }

    public void deleteQuestion(int parseInt) throws NotFoundException {
        if (questionRepository.existsById(parseInt)) {
            questionRepository.deleteById(parseInt);
        } else {
            throw new NotFoundException(String.format("Question with id '%d' not found", parseInt));
        }
    }
}
