package com.github.iauglov.service;

import com.github.iauglov.model.NotFoundException;
import com.github.iauglov.persistence.Answer;
import com.github.iauglov.persistence.AnswerRepository;
import com.github.iauglov.persistence.Guide;
import com.github.iauglov.persistence.GuideRepository;
import com.github.iauglov.persistence.Question;
import com.github.iauglov.persistence.QuestionRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class QuestionAnswerService {

    private final QuestionRepository questionRepository;
    private final GuideRepository guideRepository;
    private final AnswerRepository answerRepository;

    public void registerNewQuestion(String text) {
        Question question = new Question();
        question.setText(text);
        questionRepository.save(question);
    }

    public void linkQuestionWithGuide(Integer guideId, Integer questionId) throws NotFoundException {
        Optional<Guide> optionalGuide = guideRepository.findById(guideId);

        if (optionalGuide.isPresent()) {
            Optional<Question> optionalQuestion = questionRepository.findById(questionId);

            if (optionalQuestion.isPresent()) {
                Question question = optionalQuestion.get();
                Guide guide = optionalGuide.get();

                question.setGuide(guide);
                questionRepository.save(question);
            } else {
                throw new NotFoundException(String.format("Can't find question with id '%d'", questionId));
            }
        } else {
            throw new NotFoundException(String.format("Can't find guide with id '%d'", guideId));
        }
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

}
