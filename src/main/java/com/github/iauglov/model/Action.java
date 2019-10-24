package com.github.iauglov.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Action {

    // ADMIN
    ADMIN("Админ-панель"),
    GUIDES("Гайды"),
    GUIDES_CREATE("Создать гайд"),
    GUIDES_LIST("Вывести список гайдов"),
    GUIDES_EDIT("Редактировать гайд"),
    GUIDES_EDIT_CONFIRMATION(""),
    GUIDES_EDIT_TITLE("Редактировать заголовок"),
    GUIDES_EDIT_TEXT("Редактировать текст"),
    GUIDES_EDIT_DELAY("Редактировать задержку"),
    GUIDES_DELETE("Удалить гайд"),
    GUIDES_DELETE_CONFIRMATION(""),
    QUESTIONS("Вопросы"),
    QUESTIONS_CREATE("Создать вопрос"),
    QUESTIONS_EDIT("Редактировать вопрос"),
    QUESTIONS_EDIT_CONFIRMATION(""),
    QUESTIONS_LINK_WITH_GUIDE("Связать вопрос с гайдом"),
    QUESTIONS_LINK_WITH_GUIDE_FIRST_STEP(""),
    QUESTIONS_LINK_WITH_GUIDE_SECOND_STEP(""),
    QUESTIONS_LINK_WITH_ANSWER("Связать вопрос с ответом"),
    QUESTIONS_LINK_WITH_ANSWER_FIRST_STEP(""),
    QUESTIONS_LINK_WITH_ANSWER_SECOND_STEP(""),
    QUESTIONS_DELETE("Удалить вопрос"),
    QUESTIONS_DELETE_CONFIRMATION(""),
    ANSWERS("Ответы"),
    ANSWERS_CREATE("Добавить ответ"),
    ANSWERS_EDIT("Редактировать ответ"),
    ANSWERS_EDIT_CONFIRMATION(""),
    ANSWERS_LINK_WITH_QUESTION("Связать ответ с вопросом"),
    ANSWERS_LINK_WITH_QUESTION_FIRST_STEP(""),
    ANSWERS_LINK_WITH_QUESTION_SECOND_STEP(""),
    ANSWERS_DELETE("Удалить ответ"),
    ANSWERS_DELETE_CONFIRMATION(""),

    //USER
    GET_ANSWERS("");

    private final String label;

    public static boolean canProcess(String id) {
        try {
            valueOf(id.toUpperCase());
            return true;
        } catch (IllegalArgumentException exc) {
            return false;
        }
    }

    public String asId() {
        return this.name().toLowerCase();
    }
}
