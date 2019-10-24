package com.github.iauglov.service;

import com.github.iauglov.model.Action;
import static com.github.iauglov.model.Action.ADMIN;
import static com.github.iauglov.model.Action.ANSWERS;
import static com.github.iauglov.model.Action.ANSWERS_CREATE;
import static com.github.iauglov.model.Action.ANSWERS_DELETE;
import static com.github.iauglov.model.Action.ANSWERS_DELETE_CONFIRMATION;
import static com.github.iauglov.model.Action.ANSWERS_EDIT;
import static com.github.iauglov.model.Action.ANSWERS_EDIT_CONFIRMATION;
import static com.github.iauglov.model.Action.ANSWERS_LINK_WITH_QUESTION;
import static com.github.iauglov.model.Action.ANSWERS_LINK_WITH_QUESTION_FIRST_STEP;
import static com.github.iauglov.model.Action.GUIDES;
import static com.github.iauglov.model.Action.GUIDES_CREATE;
import static com.github.iauglov.model.Action.GUIDES_DELETE;
import static com.github.iauglov.model.Action.GUIDES_DELETE_CONFIRMATION;
import static com.github.iauglov.model.Action.GUIDES_EDIT;
import static com.github.iauglov.model.Action.GUIDES_EDIT_CONFIRMATION;
import static com.github.iauglov.model.Action.GUIDES_EDIT_DELAY;
import static com.github.iauglov.model.Action.GUIDES_EDIT_TEXT;
import static com.github.iauglov.model.Action.GUIDES_EDIT_TITLE;
import static com.github.iauglov.model.Action.QUESTIONS;
import static com.github.iauglov.model.Action.QUESTIONS_CREATE;
import static com.github.iauglov.model.Action.QUESTIONS_DELETE;
import static com.github.iauglov.model.Action.QUESTIONS_EDIT;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_ANSWER;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_GUIDE;
import com.github.iauglov.model.NotFoundException;
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
    private final QuestionAnswerService questionAnswerService;
    private final GuideCrudProcessor guideCrudProcessor;
    private final QuestionAnswerCrudProcessor questionAnswerCrudProcessor;

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
                    guideCrudProcessor.startCreatingGuide(event);
                    break;
                }
                case GUIDES_EDIT: {
                    processGuideEditing(event);
                    break;
                }
                case GUIDES_EDIT_CONFIRMATION: {
                    processGuideEditingConfirmation(event);
                    break;
                }
                case GUIDES_EDIT_TITLE: {
                    guideCrudProcessor.startEditingGuideTitle(event);
                    break;
                }
                case GUIDES_EDIT_TEXT: {
                    guideCrudProcessor.startEditingGuideText(event);
                    break;
                }
                case GUIDES_EDIT_DELAY: {
                    guideCrudProcessor.startEditingGuideDelay(event);
                    break;
                }
                case GUIDES_DELETE: {
                    processGuideDeleting(event);
                    break;
                }
                case GUIDES_DELETE_CONFIRMATION: {
                    processGuideDeletingConfirmation(event);
                    break;
                }
                case ANSWERS: {
                    processAnswers(event);
                    break;
                }
                case ANSWERS_CREATE: {
                    questionAnswerCrudProcessor.startCreatingAnswer(event);
                    break;
                }
                case ANSWERS_LINK_WITH_QUESTION: {
                    processAnswerLinking(event);
                    break;
                }
                case ANSWERS_LINK_WITH_QUESTION_FIRST_STEP: {
                    questionAnswerCrudProcessor.startProcessLinkingAnswerWithQuestion(event);
                    break;
                }
                case ANSWERS_LINK_WITH_QUESTION_SECOND_STEP: {
                    questionAnswerCrudProcessor.endProcessLinkingAnswerWithQuestion(event);
                    break;
                }
                case ANSWERS_EDIT: {
                    processAnswerEditing(event);
                    break;
                }
                case ANSWERS_EDIT_CONFIRMATION: {
                    questionAnswerCrudProcessor.startAnswerEditing(event);
                    break;
                }
                case ANSWERS_DELETE: {
                    processAnswerDeleting(event);
                    break;
                }
                case ANSWERS_DELETE_CONFIRMATION: {
                    processAnswerDeletingConfirmation(event);
                    break;
                }
                case QUESTIONS: {
                    processQuestions(event);
                    break;
                }
                case QUESTIONS_CREATE: {
                    questionAnswerCrudProcessor.startCreatingQuestion(event);
                    break;
                }
                case QUESTIONS_LINK_WITH_ANSWER: {
                    break;
                }
                case QUESTIONS_LINK_WITH_ANSWER_CONFIRMATION: {
                    break;
                }
                case QUESTIONS_LINK_WITH_GUIDE: {
                    break;
                }
                case QUESTIONS_LINK_WITH_GUIDE_CONFIRMATION: {
                    break;
                }
                case QUESTIONS_EDIT: {
                    break;
                }
                case QUESTIONS_EDIT_CONFIRMATION: {
                    break;
                }
                case QUESTIONS_DELETE: {
                    break;
                }
                case QUESTIONS_DELETE_CONFIRMATION: {
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

    private void processAnswerDeletingConfirmation(InteractiveEvent event) {
        String answerId = event.getValue();

        try {
            questionAnswerService.deleteAnswer(Integer.parseInt(answerId));
            bot.messaging().delete(event.getMid());
            bot.messaging().sendText(event.getPeer(), "Ответ успешно удалён");
        } catch (NotFoundException e) {
            bot.messaging().sendText(event.getPeer(), "Вы пытаетесь удалить уже удаленный ответ.");
        }
        guideCrudProcessor.openInteractiveAdmin(event.getPeer());
    }

    private void processAnswerDeleting(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        questionAnswerService.getAllAnswers().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getText()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));
        actions.add(new InteractiveAction(ANSWERS_DELETE_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Удаление ответов", null, actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processAnswerEditing(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        questionAnswerService.getAllAnswers().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getText()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));
        actions.add(new InteractiveAction(ANSWERS_EDIT_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Редактирование ответов", "Выберите ответ, в котором хотите отредактировать текст", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processAnswerLinking(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        questionAnswerService.getAllAnswers().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getText()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));
        actions.add(new InteractiveAction(ANSWERS_LINK_WITH_QUESTION_FIRST_STEP.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Привязка ответа к вопросу", "Выберите ответ.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processGuideEditingConfirmation(InteractiveEvent event) {
        guideCrudProcessor.saveCurrentEditingGuide(event.getPeer().getId(), Integer.parseInt(event.getValue()));

        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(GUIDES.asId(), new InteractiveButton(GUIDES.asId(), GUIDES.getLabel())));
        actions.add(new InteractiveAction(GUIDES_EDIT_TITLE.asId(), new InteractiveButton(GUIDES_EDIT_TITLE.asId(), GUIDES_EDIT_TITLE.getLabel())));
        actions.add(new InteractiveAction(GUIDES_EDIT_TEXT.asId(), new InteractiveButton(GUIDES_EDIT_TEXT.asId(), GUIDES_EDIT_TEXT.getLabel())));
        actions.add(new InteractiveAction(GUIDES_EDIT_DELAY.asId(), new InteractiveButton(GUIDES_EDIT_DELAY.asId(), GUIDES_EDIT_DELAY.getLabel())));

        InteractiveGroup group = new InteractiveGroup("Редактирование гайда", "Выберите действие.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processGuideEditing(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        guideService.getAllGuides().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getTitle()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(GUIDES.asId(), new InteractiveButton(GUIDES.asId(), GUIDES.getLabel())));
        actions.add(new InteractiveAction(GUIDES_EDIT_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Редактирование гайдов", null, actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processGuideDeleting(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        guideService.getAllGuides().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getTitle()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(GUIDES.asId(), new InteractiveButton(GUIDES.asId(), GUIDES.getLabel())));
        actions.add(new InteractiveAction(GUIDES_DELETE_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Удаление", null, actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processGuideDeletingConfirmation(InteractiveEvent event) {
        String guideId = event.getValue();

        try {
            guideService.deleteGuide(Integer.valueOf(guideId));
            bot.messaging().delete(event.getMid());
            bot.messaging().sendText(event.getPeer(), "Гайд успешно удалён");
        } catch (NotFoundException e) {
            bot.messaging().sendText(event.getPeer(), "Вы пытаетесь удалить уже удаленный гайд.");
        }
        guideCrudProcessor.openInteractiveAdmin(event.getPeer());
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
