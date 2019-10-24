package com.github.iauglov.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Action {

    ADMIN("Админ-панель"),
    GUIDES("Гайды"),
    GUIDES_CREATE("Создать гайд"),
    GUIDES_EDIT("Редактировать гайд"),
    GUIDES_DELETE("Удалить гайд"),
    QUESTIONS("Вопросы"),
    QUESTIONS_CREATE("Создать вопрос"),
    QUESTIONS_EDIT("Редактировать вопрос"),
    QUESTIONS_LINK_WITH_GUIDE("Связать вопрос с гайдом"),
    QUESTIONS_LINK_WITH_ANSWER("Связать вопрос с ответом"),
    QUESTIONS_DELETE("Удалить вопрос"),
    ANSWERS("Ответы"),
    ANSWERS_CREATE("Добавить ответ"),
    ANSWERS_EDIT("Редактировать ответ"),
    ANSWERS_LINK_WITH_QUESTION("Связать ответ с вопросом"),
    ANSWERS_DELETE("Удалить ответ");

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
