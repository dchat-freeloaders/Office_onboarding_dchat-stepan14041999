package com.github.iauglov.service;

import static com.github.iauglov.model.Action.ANSWERS;
import static com.github.iauglov.model.Action.GUIDES;
import static com.github.iauglov.model.Action.QUESTIONS;
import com.github.iauglov.model.AnswerCreatingStage;
import com.github.iauglov.model.GuideCreatingStage;
import com.github.iauglov.model.QuestionCreatingStage;
import com.github.iauglov.persistence.Answer;
import com.github.iauglov.persistence.Guide;
import com.github.iauglov.persistence.GuideRepository;
import com.github.iauglov.persistence.Question;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CrudProcessor {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    private final Map<Integer, GuideCreatingStage> guideCreatingMap = new HashMap<>();
    private final Map<Integer, Guide> guideMap = new HashMap<>();

    private final Map<Integer, QuestionCreatingStage> questionCreatingMap = new HashMap<>();
    private final Map<Integer, Question> questionMap = new HashMap<>();

    private final Map<Integer, AnswerCreatingStage> answerCreatingMap = new HashMap<>();
    private final Map<Integer, Answer> answerMap = new HashMap<>();

    private final Map<Integer, UUID> messageMap = new HashMap<>();

    private final Bot bot;
    private final GuideRepository guideRepository;

    public boolean processMessage(Message message) {
        int peerId = message.getPeer().getId();

        if (guideCreatingMap.containsKey(peerId)) {
            processGuideCreation(message);
            return true;
        }

        if (questionCreatingMap.containsKey(peerId)) {
            processQuestionCreation(message);
            return true;
        }

        if (answerCreatingMap.containsKey(peerId)) {
            processAnswerCreation(message);
            return true;
        }

        return false;
    }

    private void processAnswerCreation(Message message) {
        AnswerCreatingStage stage = answerCreatingMap.get(message.getPeer().getId());
    }

    private void processQuestionCreation(Message message) {

    }

    private void processGuideCreation(Message message) {
        int peerId = message.getPeer().getId();

        GuideCreatingStage stage = guideCreatingMap.get(peerId);

        if (stage == GuideCreatingStage.INPUT_TITLE) {
            Guide guide = new Guide();
            guide.setTitle(message.getText());

            guideMap.put(peerId, guide);

            guideCreatingMap.put(peerId, GuideCreatingStage.INPUT_TEXT);
            bot.messaging().delete(message.getMessageId()).thenAccept(uuid -> {
                bot.messaging().update(messageMap.get(peerId), "Введите текст гайда.");
            });
        }

        if (stage == GuideCreatingStage.INPUT_TEXT) {
            guideMap.get(peerId).setText(message.getText());

            guideCreatingMap.put(peerId, GuideCreatingStage.INPUT_DELAY);
            bot.messaging().delete(message.getMessageId()).thenAccept(uuid -> {
                bot.messaging().update(messageMap.get(peerId), "Введите задержку гайда в формате длительности, например 10h.");
            });
        }

        if (stage == GuideCreatingStage.INPUT_DELAY) {
            guideCreatingMap.remove(peerId);
            Guide guide = guideMap.remove(peerId);

            long initDelayInSeconds = Duration.parse("PT" + message.getText().toUpperCase()).getSeconds();
            guide.setDelay(initDelayInSeconds);

            guideRepository.save(guide);
            bot.messaging().delete(message.getMessageId()).thenAccept(uuid -> {
                bot.messaging().update(messageMap.get(peerId), "Введите текст гайда. Через 2 секунды откроется админ-панель");
                executorService.schedule(() -> interactiveAdmin(peerId), 2, TimeUnit.SECONDS);
            });
        }
    }

    public void startCreatingGuide(InteractiveEvent event) {
        int peerId = event.getPeer().getId();

        questionCreatingMap.remove(peerId);
        answerCreatingMap.remove(peerId);

        guideCreatingMap.put(peerId, GuideCreatingStage.INPUT_TITLE);

        bot.messaging().update(event.getMid(), "Введите заголовок гайда в чат.");
        messageMap.put(peerId, event.getMid());
    }

    public void startCreatingQuestion(InteractiveEvent event) {
        int peerId = event.getPeer().getId();

        guideCreatingMap.remove(peerId);
        answerCreatingMap.remove(peerId);

        questionCreatingMap.put(peerId, QuestionCreatingStage.INPUT_TEXT);
    }

    public void startCreatingAnswer(InteractiveEvent event) {
        int peerId = event.getPeer().getId();

        questionCreatingMap.remove(peerId);
        guideCreatingMap.remove(peerId);

        answerCreatingMap.put(peerId, AnswerCreatingStage.INPUT_TEXT);
    }

    private void interactiveAdmin(int peerId) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(GUIDES.asId(), new InteractiveButton(GUIDES.asId(), GUIDES.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));

        InteractiveGroup group = new InteractiveGroup("Админ-панель", "Выберите группу действий.", actions);

        bot.interactiveApi().update(messageMap.remove(peerId), group);
    }
}
