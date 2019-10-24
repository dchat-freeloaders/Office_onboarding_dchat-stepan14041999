package com.github.iauglov.service;

import com.github.iauglov.model.AnswerCreatingStage;
import com.github.iauglov.model.GuideCreatingStage;
import com.github.iauglov.model.QuestionCreatingStage;
import com.github.iauglov.persistence.Answer;
import com.github.iauglov.persistence.Guide;
import com.github.iauglov.persistence.Question;
import im.dlg.botsdk.domain.Message;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CrudProcessor {

    private final Map<Integer, GuideCreatingStage> guideCreatingMap = new HashMap<>();
    private final Map<Integer, Guide> guideMap = new HashMap<>();

    private final Map<Integer, QuestionCreatingStage> questionCreatingMap = new HashMap<>();
    private final Map<Integer, Question> questionMap = new HashMap<>();

    private final Map<Integer, AnswerCreatingStage> answerCreatingMap = new HashMap<>();
    private final Map<Integer, Answer> answerMap = new HashMap<>();

    public boolean processMessage(Message message) {
        int peerId = message.getPeer().getId();

        if (guideCreatingMap.containsKey(peerId)) {
            processGuideCreation(message);
        }

        if (questionCreatingMap.containsKey(peerId)) {
            processQuestionCreation(message);
        }

        if (answerCreatingMap.containsKey(peerId)) {
            processAnswerCreation(message);
        }

        return false;
    }

    private void processAnswerCreation(Message message) {
        AnswerCreatingStage stage = answerCreatingMap.get(message.getPeer().getId());
    }

    private void processQuestionCreation(Message message) {

    }

    private void processGuideCreation(Message message) {

    }

    public void startCreatingGuide(int peerId) {
        questionCreatingMap.remove(peerId);
        answerCreatingMap.remove(peerId);

        guideCreatingMap.put(peerId, GuideCreatingStage.INPUT_TITLE);
    }

    public void startCreatingQuestion(int peerId) {
        guideCreatingMap.remove(peerId);
        answerCreatingMap.remove(peerId);

        questionCreatingMap.put(peerId, QuestionCreatingStage.INPUT_TEXT);
    }

    public void startCreatingAnswer(int peerId) {
        questionCreatingMap.remove(peerId);
        guideCreatingMap.remove(peerId);

        answerCreatingMap.put(peerId, AnswerCreatingStage.INPUT_TEXT);
    }
}
