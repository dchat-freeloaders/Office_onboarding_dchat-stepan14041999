package com.github.iauglov.service;

import com.github.iauglov.model.Action;
import static com.github.iauglov.model.Action.ADMIN;
import static com.github.iauglov.model.Action.ANSWERS;
import static com.github.iauglov.model.Action.ANSWERS_CREATE;
import static com.github.iauglov.model.Action.ANSWERS_DELETE;
import static com.github.iauglov.model.Action.ANSWERS_EDIT;
import static com.github.iauglov.model.Action.ANSWERS_LINK_WITH_QUESTION;
import static com.github.iauglov.model.Action.GUIDES;
import static com.github.iauglov.model.Action.GUIDES_CREATE;
import static com.github.iauglov.model.Action.GUIDES_DELETE;
import static com.github.iauglov.model.Action.GUIDES_DELETE_CONFIRMATION;
import static com.github.iauglov.model.Action.GUIDES_EDIT;
import static com.github.iauglov.model.Action.QUESTIONS;
import static com.github.iauglov.model.Action.QUESTIONS_CREATE;
import static com.github.iauglov.model.Action.QUESTIONS_DELETE;
import static com.github.iauglov.model.Action.QUESTIONS_EDIT;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_ANSWER;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_GUIDE;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import static im.dlg.botsdk.domain.interactive.InteractiveAction.Style.DANGER;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import im.dlg.botsdk.domain.interactive.InteractiveSelect;
import im.dlg.botsdk.domain.interactive.InteractiveSelectOption;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InteractiveProcessor {

    private final Bot bot;
    private final GuideService guideService;
    private final CrudProcessor crudProcessor;

    public void process(InteractiveEvent event) {
        String id = event.getId().toUpperCase();
        if (Action.canProcess(id)) {
            switch (Action.valueOf(id)) {
                case ADMIN: {
                    processAdmin(event);
                    break;
                }
                case GUIDES: {
                    processGuides(event);
                    break;
                }
                case GUIDES_CREATE: {
                    processGuideCreating(event);
                    break;
                }
                case GUIDES_EDIT: {
                    processGuideEditing(event);
                    break;
                }
                case GUIDES_DELETE: {
                    processGuideDeleting(event);
                    break;
                }
                case ANSWERS: {
                    processAnswers(event);
                    break;
                }
                case QUESTIONS: {
                    processQuestions(event);
                    break;
                }
                default: {
                    unknownAction(event);
                }
            }
        } else {
            unknownAction(event);
        }
        System.out.println(event);
    }

    private void processGuideEditing(InteractiveEvent event) {

    }

    private void processGuideDeleting(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        guideService.getAllGuides().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getTitle()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Какой гайд вы хотите удалить?", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(GUIDES.asId(), new InteractiveButton(GUIDES.asId(), GUIDES.getLabel())));
        actions.add(new InteractiveAction(GUIDES_DELETE_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Quiz", "Do you want to answer a quiz?", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processGuideCreating(InteractiveEvent event) {
        crudProcessor.startCreatingGuide(event);
    }

    private void processAdmin(InteractiveEvent event) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(GUIDES.asId(), new InteractiveButton(GUIDES.asId(), GUIDES.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));

        InteractiveGroup group = new InteractiveGroup("Админ-панель", "Выберите группу действий.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processGuides(InteractiveEvent event) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ADMIN.asId(), new InteractiveButton(ADMIN.asId(), ADMIN.getLabel())));
        actions.add(new InteractiveAction(GUIDES_CREATE.asId(), new InteractiveButton(GUIDES_CREATE.asId(), GUIDES_CREATE.getLabel())));
        actions.add(new InteractiveAction(GUIDES_EDIT.asId(), new InteractiveButton(GUIDES_EDIT.asId(), GUIDES_EDIT.getLabel())));
        actions.add(new InteractiveAction(GUIDES_DELETE.asId(), DANGER, new InteractiveButton(GUIDES_DELETE.asId(), GUIDES_DELETE.getLabel()), null));

        InteractiveGroup group = new InteractiveGroup("Управление гайдами", "Выберите действие.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processAnswers(InteractiveEvent event) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ADMIN.asId(), new InteractiveButton(ADMIN.asId(), ADMIN.getLabel())));
        actions.add(new InteractiveAction(ANSWERS_CREATE.asId(), new InteractiveButton(ANSWERS_CREATE.asId(), ANSWERS_CREATE.getLabel())));
        actions.add(new InteractiveAction(ANSWERS_EDIT.asId(), new InteractiveButton(ANSWERS_EDIT.asId(), ANSWERS_EDIT.getLabel())));
        actions.add(new InteractiveAction(ANSWERS_LINK_WITH_QUESTION.asId(), new InteractiveButton(ANSWERS_LINK_WITH_QUESTION.asId(), ANSWERS_LINK_WITH_QUESTION.getLabel())));
        actions.add(new InteractiveAction(ANSWERS_DELETE.asId(), DANGER, new InteractiveButton(ANSWERS_DELETE.asId(), ANSWERS_DELETE.getLabel()), null));

        InteractiveGroup group = new InteractiveGroup("Управление ответами", "Выберите действие.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processQuestions(InteractiveEvent event) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ADMIN.asId(), new InteractiveButton(ADMIN.asId(), ADMIN.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_CREATE.asId(), new InteractiveButton(QUESTIONS_CREATE.asId(), QUESTIONS_CREATE.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_EDIT.asId(), new InteractiveButton(QUESTIONS_EDIT.asId(), QUESTIONS_EDIT.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_LINK_WITH_GUIDE.asId(), new InteractiveButton(QUESTIONS_LINK_WITH_GUIDE.asId(), QUESTIONS_LINK_WITH_GUIDE.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_LINK_WITH_ANSWER.asId(), new InteractiveButton(QUESTIONS_LINK_WITH_ANSWER.asId(), QUESTIONS_LINK_WITH_ANSWER.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_DELETE.asId(), DANGER, new InteractiveButton(QUESTIONS_DELETE.asId(), QUESTIONS_DELETE.getLabel()), null));

        InteractiveGroup group = new InteractiveGroup("Управление вопросами", "Выберите действие.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void unknownAction(InteractiveEvent interactiveEvent) {
        String text = "Действие в разработке или не зарегистрировано.\nВведите /help для отображения списка команд.";
        bot.messaging().sendText(interactiveEvent.getPeer(), text);
    }

}
