package com.github.iauglov.service;

import static com.github.iauglov.model.Action.ANSWERS;
import static com.github.iauglov.model.Action.ANSWERS_LINK_WITH_QUESTION_SECOND_STEP;
import static com.github.iauglov.model.Action.GUIDES;
import static com.github.iauglov.model.Action.QUESTIONS;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_ANSWER_SECOND_STEP;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_GUIDE_SECOND_STEP;
import com.github.iauglov.persistence.Answer;
import com.github.iauglov.persistence.AnswerRepository;
import com.github.iauglov.persistence.Guide;
import com.github.iauglov.persistence.GuideRepository;
import com.github.iauglov.persistence.Question;
import com.github.iauglov.persistence.QuestionRepository;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import im.dlg.botsdk.domain.interactive.InteractiveSelect;
import im.dlg.botsdk.domain.interactive.InteractiveSelectOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class QuestionAnswerCrudProcessor {

    private final CrudCache crudCache;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final Bot bot;
    private final QuestionAnswerService questionAnswerService;
    private final GuideService guideService;
    private final GuideRepository guideRepository;

    public boolean processMessage(Message message) {
        int peerId = message.getPeer().getId();

        if (crudCache.questionCreating.contains(peerId)) {
            processQuestionCreation(message);
            return true;
        }

        if (crudCache.answerCreating.contains(peerId)) {
            processAnswerCreation(message);
            return true;
        }

        if (crudCache.questionEditingMap.containsKey(peerId)) {
            processQuestionEditing(message);
            return true;
        }

        if (crudCache.answerEditingMap.containsKey(peerId)) {
            processAnswerEditing(message);
            return true;
        }

        return false;
    }

    public void startCreatingQuestion(InteractiveEvent event) {
        int peerId = event.getPeer().getId();

        removeFor(peerId);

        crudCache.questionCreating.add(peerId);

        bot.messaging().update(event.getMid(), "Введите текст вопроса.");
    }

    public void startCreatingAnswer(InteractiveEvent event) {
        int peerId = event.getPeer().getId();

        removeFor(peerId);

        crudCache.answerCreating.add(peerId);

        bot.messaging().update(event.getMid(), "Введите текст ответа.");
    }

    private void processAnswerCreation(Message message) {
        Answer answer = new Answer();
        answer.setText(message.getText());

        crudCache.answerCreating.remove(message.getPeer().getId());

        answerRepository.save(answer);

        bot.messaging().sendText(message.getPeer(), "Ответ успешно создан. Не забудьте его привязать к вопросу.").thenAccept(uuid -> {
            openInteractiveAdmin(message.getPeer());
        });
    }

    private void processQuestionCreation(Message message) {
        Question question = new Question();
        question.setText(message.getText());

        crudCache.questionCreating.remove(message.getPeer().getId());
        questionRepository.save(question);

        bot.messaging().sendText(message.getPeer(), "Вопрос успешно создан. Не забудьте его привязать к гайду или ответу.").thenAccept(uuid -> {
            openInteractiveAdmin(message.getPeer());
        });
    }

    private void removeFor(int peerId) {
        crudCache.questionCreating.remove(peerId);
        crudCache.questionCachedToLinkWithAnswer.remove(peerId);
        crudCache.questionCachedToLinkWithGuide.remove(peerId);

        crudCache.answerCreating.remove(peerId);
        crudCache.answerCachedToLink.remove(peerId);
    }

    public void startProcessLinkingAnswerWithQuestion(InteractiveEvent event) {
        Integer answerId = Integer.valueOf(event.getValue());

        crudCache.answerCachedToLink.put(event.getPeer().getId(), answerId);

        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        questionAnswerService.getAllQuestions().forEach(question -> {
            selectOptions.add(new InteractiveSelectOption(question.getId().toString(), question.getText()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));
        actions.add(new InteractiveAction(ANSWERS_LINK_WITH_QUESTION_SECOND_STEP.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Привязка ответа к вопросу", "Выберите вопрос.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    public void openInteractiveAdmin(Peer peer) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(GUIDES.asId(), new InteractiveButton(GUIDES.asId(), GUIDES.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));

        InteractiveGroup group = new InteractiveGroup("Админ-панель", "Выберите группу действий.", actions);

        bot.interactiveApi().send(peer, group);
    }

    public void endProcessLinkingAnswerWithQuestion(InteractiveEvent event) {
        Integer answerId = crudCache.answerCachedToLink.remove(event.getPeer().getId());
        Integer questionId = Integer.valueOf(event.getValue());

        Optional<Answer> optionalAnswer = answerRepository.findById(answerId);

        if (!optionalAnswer.isPresent()) {
            bot.messaging().sendText(event.getPeer(), "Ответ не найден. Попробуйте снова").thenAccept(uuid -> {
                openInteractiveAdmin(event.getPeer());
            });
        }

        Optional<Question> optionalQuestion = questionRepository.findById(questionId);

        if (!optionalQuestion.isPresent()) {
            bot.messaging().sendText(event.getPeer(), "Вопрос не найден. Попробуйте снова").thenAccept(uuid -> {
                openInteractiveAdmin(event.getPeer());
            });
        }

        Answer answer = optionalAnswer.get();
        Question question = optionalQuestion.get();
        answer.setQuestion(question);
        
        answerRepository.save(answer);

        bot.messaging().sendText(event.getPeer(), "Ответ успешно привязан к вопросу.").thenAccept(uuid -> {
            openInteractiveAdmin(event.getPeer());
        });
    }

    public void startAnswerEditing(InteractiveEvent event) {
        int peerId = event.getPeer().getId();
        Integer answerId = Integer.valueOf(event.getValue());

        removeFor(peerId);

        crudCache.answerEditingMap.put(peerId, answerId);

        bot.messaging().sendText(event.getPeer(), "Введите новый текст для ответа.");
    }

    public void processAnswerEditing(Message message) {
        Integer answerId = crudCache.answerEditingMap.remove(message.getPeer().getId());

        Optional<Answer> optionalAnswer = answerRepository.findById(answerId);

        if (!optionalAnswer.isPresent()) {
            bot.messaging().sendText(message.getPeer(), "Ответ не найден. Попробуйте снова").thenAccept(uuid -> {
                openInteractiveAdmin(message.getPeer());
            });
        }

        Answer answer = optionalAnswer.get();
        answer.setText(message.getText());

        answerRepository.save(answer);

        bot.messaging().sendText(message.getPeer(), "Ответ успешно отредактирован.").thenAccept(uuid -> {
            openInteractiveAdmin(message.getPeer());
        });
    }

    public void startQuestionEditing(InteractiveEvent event) {
        int peerId = event.getPeer().getId();
        Integer questionId = Integer.valueOf(event.getValue());

        removeFor(peerId);

        crudCache.questionEditingMap.put(peerId, questionId);

        bot.messaging().sendText(event.getPeer(), "Введите новый текст для вопроса.");
    }

    private void processQuestionEditing(Message message) {
        Integer questionId = crudCache.questionEditingMap.remove(message.getPeer().getId());

        Optional<Question> optionalQuestion = questionRepository.findById(questionId);

        if (!optionalQuestion.isPresent()) {
            bot.messaging().sendText(message.getPeer(), "Вопрос не найден. Попробуйте снова").thenAccept(uuid -> {
                openInteractiveAdmin(message.getPeer());
            });
        }

        Question question = optionalQuestion.get();
        question.setText(message.getText());

        questionRepository.save(question);

        bot.messaging().sendText(message.getPeer(), "Вопрос успешно отредактирован.").thenAccept(uuid -> {
            openInteractiveAdmin(message.getPeer());
        });
    }

    public void startQuestionLinkingWithAnswer(InteractiveEvent event) {
        Integer questionId = Integer.valueOf(event.getValue());

        crudCache.questionCachedToLinkWithAnswer.put(event.getPeer().getId(), questionId);

        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        questionAnswerService.getAllAnswers().forEach(answer -> {
            selectOptions.add(new InteractiveSelectOption(answer.getId().toString(), answer.getText()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_LINK_WITH_ANSWER_SECOND_STEP.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Привязка вопроса к ответу", "Выберите ответ.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    public void endQuestionLinkingWithAnswer(InteractiveEvent event) {
        Integer questionId = crudCache.questionCachedToLinkWithAnswer.remove(event.getPeer().getId());
        Integer answerId = Integer.valueOf(event.getValue());

        Optional<Question> optionalQuestion = questionRepository.findById(questionId);

        if (!optionalQuestion.isPresent()) {
            bot.messaging().sendText(event.getPeer(), "Вопрос не найден. Попробуйте снова").thenAccept(uuid -> {
                openInteractiveAdmin(event.getPeer());
            });
        }

        Optional<Answer> optionalAnswer = answerRepository.findById(answerId);

        if (!optionalAnswer.isPresent()) {
            bot.messaging().sendText(event.getPeer(), "Ответ не найден. Попробуйте снова").thenAccept(uuid -> {
                openInteractiveAdmin(event.getPeer());
            });
        }

        Answer answer = optionalAnswer.get();
        Question question = optionalQuestion.get();
        question.setAnswer(answer);
        questionRepository.save(question);

        bot.messaging().sendText(event.getPeer(), "Вопрос успешно привязан к ответу.").thenAccept(uuid -> {
            openInteractiveAdmin(event.getPeer());
        });
    }

    public void startQuestionLinkingWithGuide(InteractiveEvent event) {
        Integer questionId = Integer.valueOf(event.getValue());

        crudCache.questionCachedToLinkWithGuide.put(event.getPeer().getId(), questionId);

        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        guideService.getAllGuides().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getTitle()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_LINK_WITH_GUIDE_SECOND_STEP.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Привязка вопроса к гайду", "Выберите гайд.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    public void endQuestionLinkingWithGuide(InteractiveEvent event) {
        Integer questionId = crudCache.questionCachedToLinkWithGuide.remove(event.getPeer().getId());
        Integer guideId = Integer.valueOf(event.getValue());

        Optional<Question> optionalQuestion = questionRepository.findById(questionId);

        if (!optionalQuestion.isPresent()) {
            bot.messaging().sendText(event.getPeer(), "Вопрос не найден. Попробуйте снова").thenAccept(uuid -> {
                openInteractiveAdmin(event.getPeer());
            });
        }

        Optional<Guide> optionalGuide = guideRepository.findById(guideId);

        if (!optionalGuide.isPresent()) {
            bot.messaging().sendText(event.getPeer(), "Гайд не найден. Попробуйте снова").thenAccept(uuid -> {
                openInteractiveAdmin(event.getPeer());
            });
        }

        Guide guide = optionalGuide.get();
        Question question = optionalQuestion.get();
        question.setGuide(guide);
        questionRepository.save(question);

        bot.messaging().sendText(event.getPeer(), "Вопрос успешно привязан к гайду.").thenAccept(uuid -> {
            openInteractiveAdmin(event.getPeer());
        });
    }
}
