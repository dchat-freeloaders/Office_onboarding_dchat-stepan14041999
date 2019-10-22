package com.github.iauglov.model;

public enum Command {

    START,
    HELP,
    GUIDE,
    QUESTION,
    ANSWER;

    public static boolean canProcess(String truncatedMessage) {
        try {
            valueOf(truncatedMessage);
            return true;
        } catch (IllegalArgumentException exc) {
            return false;
        }
    }
}
