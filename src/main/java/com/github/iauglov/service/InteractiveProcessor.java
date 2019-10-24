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
import static com.github.iauglov.model.Action.GET_ANSWERS;
import static com.github.iauglov.model.Action.GUIDES;
import static com.github.iauglov.model.Action.GUIDES_CREATE;
import static com.github.iauglov.model.Action.GUIDES_DELETE;
import static com.github.iauglov.model.Action.GUIDES_DELETE_CONFIRMATION;
import static com.github.iauglov.model.Action.GUIDES_EDIT;
import static com.github.iauglov.model.Action.GUIDES_EDIT_CONFIRMATION;
import static com.github.iauglov.model.Action.GUIDES_EDIT_DELAY;
import static com.github.iauglov.model.Action.GUIDES_EDIT_TEXT;
import static com.github.iauglov.model.Action.GUIDES_EDIT_TITLE;
import static com.github.iauglov.model.Action.GUIDES_LIST;
import static com.github.iauglov.model.Action.QUESTIONS;
import static com.github.iauglov.model.Action.QUESTIONS_CREATE;
import static com.github.iauglov.model.Action.QUESTIONS_DELETE;
import static com.github.iauglov.model.Action.QUESTIONS_DELETE_CONFIRMATION;
import static com.github.iauglov.model.Action.QUESTIONS_EDIT;
import static com.github.iauglov.model.Action.QUESTIONS_EDIT_CONFIRMATION;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_ANSWER;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_ANSWER_FIRST_STEP;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_GUIDE;
import static com.github.iauglov.model.Action.QUESTIONS_LINK_WITH_GUIDE_FIRST_STEP;
import com.github.iauglov.model.NotFoundException;
import com.github.iauglov.persistence.Answer;
import com.github.iauglov.persistence.Guide;
import com.github.iauglov.persistence.Question;
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
                case GET_ANSWERS: {
                    sentAnswersToPeer(event);
                    break;
                }
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
                case GUIDES_LIST: {
                    processGuideList(event);
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
                    processQuestionLinkingWithAnswer(event);
                    break;
                }
                case QUESTIONS_LINK_WITH_ANSWER_FIRST_STEP: {
                    questionAnswerCrudProcessor.startQuestionLinkingWithAnswer(event);
                    break;
                }
                case QUESTIONS_LINK_WITH_ANSWER_SECOND_STEP: {
                    questionAnswerCrudProcessor.endQuestionLinkingWithAnswer(event);
                    break;
                }
                case QUESTIONS_LINK_WITH_GUIDE: {
                    processQuestionLinkingWithGuide(event);
                    break;
                }
                case QUESTIONS_LINK_WITH_GUIDE_FIRST_STEP: {
                    questionAnswerCrudProcessor.startQuestionLinkingWithGuide(event);
                    break;
                }
                case QUESTIONS_LINK_WITH_GUIDE_SECOND_STEP: {
                    questionAnswerCrudProcessor.endQuestionLinkingWithGuide(event);
                    break;
                }
                case QUESTIONS_EDIT: {
                    processQuestionEditing(event);
                    break;
                }
                case QUESTIONS_EDIT_CONFIRMATION: {
                    questionAnswerCrudProcessor.startQuestionEditing(event);
                    break;
                }
                case QUESTIONS_DELETE: {
                    processQuestionDeleting(event);
                    break;
                }
                case QUESTIONS_DELETE_CONFIRMATION: {
                    processQuestionDeletingConfirmation(event);
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

    private void sentAnswersToPeer(InteractiveEvent event) {
        Integer questionId = Integer.valueOf(event.getValue());

        try {
            Answer answer = questionAnswerService.getAnswerForQuestion(questionId);
            bot.messaging().sendText(event.getPeer(), answer.getText());

            List<Question> questions = questionAnswerService.getAllQuestionsForAnswer(answer.getId());
            List<InteractiveAction> actions = new ArrayList<>();

            questions.forEach(question -> {
                actions.add(new InteractiveAction(GET_ANSWERS.asId(), new InteractiveButton(question.getId().toString(), question.getText())));
            });

            InteractiveGroup group = new InteractiveGroup("Популярные вопросы", "Выберите интересующий вас вопрос.", actions);

            bot.interactiveApi().send(event.getPeer(), group);
        } catch (NotFoundException e) {
            bot.messaging().sendText(event.getPeer(), "К сожалению, на данный момент вопрос нет ответа. Обратитесь к службе поддержки");
            e.printStackTrace();
        }
    }

    private void processGuideList(InteractiveEvent event) {
        List<Guide> guides = guideService.getAllGuides();

        if (guides.size() == 0) {
            bot.messaging().sendText(event.getPeer(), "Гайды еще не созадвались");
        }

        StringBuilder stringBuilder = new StringBuilder();

        guides.forEach(guide -> {
            stringBuilder
                    .append("ID: ").append(guide.getId())
                    .append(", Задержка: ").append(guide.getDelay())
                    .append(" секунд, Заголовок: ").append(guide.getTitle())
                    .append(", Текст: ").append(guide.getText())
                    .append("\n\n");
        });

        stringBuilder.deleteCharAt(stringBuilder.length() - 1).deleteCharAt(stringBuilder.length() - 1);

        bot.messaging().sendText(event.getPeer(), stringBuilder.toString());
        guideCrudProcessor.openInteractiveAdmin(event.getPeer());
    }

    private void processQuestionLinkingWithGuide(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        questionAnswerService.getAllQuestions().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getText()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_LINK_WITH_ANSWER_FIRST_STEP.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Привязка вопроса к гайду", "Тут вы можете связать вопрос с ответом.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processQuestionLinkingWithAnswer(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        questionAnswerService.getAllQuestions().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getText()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_LINK_WITH_GUIDE_FIRST_STEP.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Привязка вопроса к гайду", "Тут вы можете связать вопрос с гайдом.", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processQuestionDeletingConfirmation(InteractiveEvent event) {
        String questionId = event.getValue();

        try {
            questionAnswerService.deleteQuestion(Integer.parseInt(questionId));
            bot.messaging().delete(event.getMid());
            bot.messaging().sendText(event.getPeer(), "Вопрос успешно удалён.").thenAccept(uuid -> {
                guideCrudProcessor.openInteractiveAdmin(event.getPeer());
            });
        } catch (NotFoundException e) {
            bot.messaging().sendText(event.getPeer(), "Вы пытаетесь удалить уже удаленный вопрос.").thenAccept(uuid -> {
                guideCrudProcessor.openInteractiveAdmin(event.getPeer());
            });
        }
    }

    private void processQuestionDeleting(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        questionAnswerService.getAllQuestions().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getText()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_DELETE_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Удаление вопросов", null, actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processQuestionEditing(InteractiveEvent event) {
        List<InteractiveSelectOption> selectOptions = new ArrayList<>();

        questionAnswerService.getAllQuestions().forEach(guide -> {
            selectOptions.add(new InteractiveSelectOption(guide.getId().toString(), guide.getText()));
        });

        InteractiveSelect interactiveSelect = new InteractiveSelect("Выбрать...", "Выбрать...", selectOptions);

        ArrayList<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS_EDIT_CONFIRMATION.asId(), interactiveSelect));

        InteractiveGroup group = new InteractiveGroup("Редактирование вопросов", "Выберите вопрос, в котором хотите отредактировать текст", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processAnswerDeletingConfirmation(InteractiveEvent event) {
        String answerId = event.getValue();

        try {
            questionAnswerService.deleteAnswer(Integer.parseInt(answerId));
            bot.messaging().delete(event.getMid());
            bot.messaging().sendText(event.getPeer(), "Ответ успешно удалён.").thenAccept(uuid -> {
                guideCrudProcessor.openInteractiveAdmin(event.getPeer());
            });
        } catch (NotFoundException e) {
            bot.messaging().sendText(event.getPeer(), "Вы пытаетесь удалить уже удаленный ответ.").thenAccept(uuid -> {
                guideCrudProcessor.openInteractiveAdmin(event.getPeer());
            });
        }
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

        InteractiveGroup group = new InteractiveGroup("Удаление ответов", "При удалении ответа все вопросы к этому ответу будут удалены!", actions);

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

        InteractiveGroup group = new InteractiveGroup("Удаление гайдов", "При удалении гайда все вопросы к этому гайду будут удалены!", actions);

        bot.interactiveApi().update(event.getMid(), group);
    }

    private void processGuideDeletingConfirmation(InteractiveEvent event) {
        String guideId = event.getValue();

        try {
            guideService.deleteGuide(Integer.valueOf(guideId));
            bot.messaging().delete(event.getMid());
            bot.messaging().sendText(event.getPeer(), "Гайд успешно удалён").thenAccept(uuid -> {
                guideCrudProcessor.openInteractiveAdmin(event.getPeer());
            });
        } catch (NotFoundException e) {
            bot.messaging().sendText(event.getPeer(), "Вы пытаетесь удалить уже удаленный гайд.").thenAccept(uuid -> {
                guideCrudProcessor.openInteractiveAdmin(event.getPeer());
            });
        }
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
        actions.add(new InteractiveAction(GUIDES_LIST.asId(), new InteractiveButton(GUIDES_LIST.asId(), GUIDES_LIST.getLabel())));
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
