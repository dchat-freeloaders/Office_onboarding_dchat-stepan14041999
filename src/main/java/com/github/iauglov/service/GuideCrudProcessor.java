package com.github.iauglov.service;

import static com.github.iauglov.model.Action.ANSWERS;
import static com.github.iauglov.model.Action.GUIDES;
import static com.github.iauglov.model.Action.QUESTIONS;
import com.github.iauglov.model.GuideCreatingStage;
import com.github.iauglov.persistence.Guide;
import com.github.iauglov.persistence.GuideRepository;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GuideCrudProcessor {

    private final Bot bot;
    private final GuideRepository guideRepository;
    private final CrudCache crudCache;

    public boolean processMessage(Message message) {
        int peerId = message.getPeer().getId();

        if (crudCache.guideCreatingMap.containsKey(peerId)) {
            processGuideCreation(message);
            return true;
        }

        if (crudCache.guideTitleEditing.contains(peerId)) {
            processGuideTitleEdition(message);
            return true;
        }

        if (crudCache.guideTextEditing.contains(peerId)) {
            processGuideTextEdition(message);
            return true;
        }

        if (crudCache.guideDelayEditing.contains(peerId)) {
            processGuideDelayEdition(message);
            return true;
        }

        return false;
    }

    private void processGuideDelayEdition(Message message) {
        Integer guideId = crudCache.guideEditingMap.get(message.getPeer().getId());
        Optional<Guide> optionalGuide = guideRepository.findById(guideId);

        if (!optionalGuide.isPresent()) {
            bot.messaging().sendText(message.getPeer(), "Гайд не найден, попробуйте снова.").thenAccept(uuid -> {
                openInteractiveAdmin(message.getPeer());
                crudCache.guideEditingMap.remove(message.getPeer().getId());
                crudCache.guideDelayEditing.remove(message.getPeer().getId());
            });
            return;
        }

        Guide guide = optionalGuide.get();

        long initDelayInSeconds;

        try {
            initDelayInSeconds = Duration.parse("PT" + message.getText().toUpperCase()).getSeconds();
        } catch (DateTimeParseException exc) {
            try {
                initDelayInSeconds = Duration.parse("P" + message.getText().toUpperCase()).getSeconds();
            } catch (DateTimeParseException exc2) {
                bot.messaging().sendText(message.getPeer(), "Неверный формат задержки. Попробуйте снова.");
                return;
            }
        }

        crudCache.guideEditingMap.remove(message.getPeer().getId());
        crudCache.guideDelayEditing.remove(message.getPeer().getId());
        guide.setDelay(initDelayInSeconds);

        guideRepository.save(guide);
        bot.messaging().sendText(message.getPeer(), "Задержка гайда успешно отредактирована.").thenAccept(uuid -> {
            openInteractiveAdmin(message.getPeer());
        });
    }

    private void processGuideTextEdition(Message message) {
        Integer guideId = crudCache.guideEditingMap.remove(message.getPeer().getId());
        crudCache.guideTextEditing.remove(message.getPeer().getId());
        Optional<Guide> optionalGuide = guideRepository.findById(guideId);

        if (!optionalGuide.isPresent()) {
            bot.messaging().sendText(message.getPeer(), "Гайд не найден, попробуйте снова.").thenAccept(uuid -> {
                openInteractiveAdmin(message.getPeer());
            });
            return;
        }

        Guide guide = optionalGuide.get();
        guide.setText(message.getText());

        guideRepository.save(guide);
        bot.messaging().sendText(message.getPeer(), "Текст гайда успешно отредактирован.").thenAccept(uuid -> {
            openInteractiveAdmin(message.getPeer());
        });
    }

    private void processGuideTitleEdition(Message message) {
        Integer guideId = crudCache.guideEditingMap.remove(message.getPeer().getId());
        crudCache.guideTitleEditing.remove(message.getPeer().getId());
        Optional<Guide> optionalGuide = guideRepository.findById(guideId);

        if (!optionalGuide.isPresent()) {
            bot.messaging().sendText(message.getPeer(), "Гайд не найден, попробуйте снова.").thenAccept(uuid -> {
                openInteractiveAdmin(message.getPeer());
            });
            return;
        }

        Guide guide = optionalGuide.get();
        guide.setTitle(message.getText());

        guideRepository.save(guide);
        bot.messaging().sendText(message.getPeer(), "Заголовок гайда успешно отредактирован.").thenAccept(uuid -> {
            openInteractiveAdmin(message.getPeer());
        });
    }

    private void processGuideCreation(Message message) {
        int peerId = message.getPeer().getId();

        GuideCreatingStage stage = crudCache.guideCreatingMap.get(peerId);

        if (stage == GuideCreatingStage.INPUT_TITLE) {
            Guide guide = new Guide();
            guide.setTitle(message.getText());

            crudCache.guideMap.put(peerId, guide);

            crudCache.guideCreatingMap.put(peerId, GuideCreatingStage.INPUT_TEXT);
            bot.messaging().sendText(message.getPeer(), "Введите текст гайда.");
        }

        if (stage == GuideCreatingStage.INPUT_TEXT) {
            crudCache.guideMap.get(peerId).setText(message.getText());

            crudCache.guideCreatingMap.put(peerId, GuideCreatingStage.INPUT_DELAY);
            bot.messaging().sendText(message.getPeer(), "Введите задержку гайда в формате длительности, например 10h, 1d, 5m.");
        }

        if (stage == GuideCreatingStage.INPUT_DELAY) {
            long initDelayInSeconds;

            try {
                initDelayInSeconds = Duration.parse("PT" + message.getText().toUpperCase()).getSeconds();
            } catch (DateTimeParseException exc) {
                try {
                    initDelayInSeconds = Duration.parse("P" + message.getText().toUpperCase()).getSeconds();
                } catch (DateTimeParseException exc2) {
                    bot.messaging().sendText(message.getPeer(), "Неверный формат задержки. Попробуйте снова.");
                    return;
                }
            }

            crudCache.guideCreatingMap.remove(peerId);
            Guide guide = crudCache.guideMap.remove(peerId);

            guide.setDelay(initDelayInSeconds);

            guideRepository.save(guide);
            bot.messaging().sendText(message.getPeer(), "Гайд успешно создан").thenAccept(uuid -> {
                openInteractiveAdmin(message.getPeer());
            });
        }
    }

    public void startCreatingGuide(InteractiveEvent event) {
        int peerId = event.getPeer().getId();

        removeFor(peerId);

        crudCache.guideCreatingMap.put(peerId, GuideCreatingStage.INPUT_TITLE);

        bot.messaging().update(event.getMid(), "Введите заголовок гайда в чат.");
    }

    public void startEditingGuideText(InteractiveEvent event) {
        int peerId = event.getPeer().getId();

        removeFor(peerId);

        crudCache.guideTextEditing.add(peerId);

        bot.messaging().update(event.getMid(), "Введите новый текст гайда в чат.");
    }

    public void startEditingGuideTitle(InteractiveEvent event) {
        int peerId = event.getPeer().getId();

        removeFor(peerId);

        crudCache.guideTitleEditing.add(peerId);

        bot.messaging().update(event.getMid(), "Введите новый заголовок гайда в чат.");
    }

    public void startEditingGuideDelay(InteractiveEvent event) {
        int peerId = event.getPeer().getId();

        removeFor(peerId);

        crudCache.guideDelayEditing.add(peerId);

        bot.messaging().update(event.getMid(), "Введите новую задержку гайда в чат, например 10h, 1d, 5m.");
    }

    public void openInteractiveAdmin(Peer peer) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(GUIDES.asId(), new InteractiveButton(GUIDES.asId(), GUIDES.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));

        InteractiveGroup group = new InteractiveGroup("Админ-панель", "Выберите группу действий.", actions);

        bot.interactiveApi().send(peer, group);
    }

    private void removeFor(int peerId) {
        crudCache.guideCreatingMap.remove(peerId);
        crudCache.guideTitleEditing.remove(peerId);
        crudCache.guideTextEditing.remove(peerId);
        crudCache.guideDelayEditing.remove(peerId);
        crudCache.guideMap.remove(peerId);
    }

    public void saveCurrentEditingGuide(int peerId, int guideId) {
        crudCache.guideEditingMap.put(peerId, guideId);
    }
}
