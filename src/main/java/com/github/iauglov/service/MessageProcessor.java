package com.github.iauglov.service;

import com.github.iauglov.model.Command;
import com.github.iauglov.model.NotFoundException;
import com.github.iauglov.persistence.Guide;
import com.github.iauglov.persistence.InternalUser;
import com.github.iauglov.persistence.UserRepository;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.Message;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageProcessor {

    private final UserRepository userRepository;
    private final GuideService guideService;
    private final Bot bot;

    public void process(Message message) {
        defaultProcessor(message);

        String messageText = message.getText();

        if (messageText.startsWith("/") && messageText.length() > 1) {
            String truncatedMessage = messageText.substring(1);

            String[] commandAndArgs = truncatedMessage.split(" ");
            String command = commandAndArgs[0].toUpperCase();
            String[] args = Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length);

            if (Command.canProcess(command)) {
                switch (Command.valueOf(command)) {
                    case START: {
                        processStart(message);
                    }
                    case HELP: {
                        processHelp(message);
                        break;
                    }
                    case GUIDE: {
                        processGuide(message, args);
                        break;
                    }
                    case QUESTION: {
                        processQuestion(message, args);
                        break;
                    }
                    case ANSWER: {
                        processAnswer(message, args);
                        break;
                    }
                    default: {
                        String text = "Команда в разработке.\nВведите /help для отображения списка команд";
                        bot.messaging().sendText(message.getPeer(), text);
                        break;
                    }
                }
            } else {
                unknownCommand(message);
            }
        } else {
            unknownCommand(message);
        }
    }

    private void processHelp(Message message) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Помощь по органайзеру гайдов.").append("\n");
        stringBuilder.append("/help - Вывод списка команд органайзера.").append("\n");
        stringBuilder.append("/guide create <time> <text> - Создать гайд с текстом <text>, который отобразится новичку через <time> времени. Время указывается в формате Dt, где D - время, а t - тип времени (Например 10h).").append("\n");
        stringBuilder.append("/guide count - Отобразить количество созданных гайдов.").append("\n");
        stringBuilder.append("/guide list - Отобразить гайды в формате ID - Задержка - Текст.").append("\n");
        stringBuilder.append("/guide delete <id> - Удалить гайд по ID.").append("\n");

        bot.messaging().sendText(message.getPeer(), stringBuilder.toString());
    }

    private void processGuide(Message message, String[] args) {
        if (args.length > 0) {
            String subCommand = args[0];

            switch (subCommand) {
                case "create": {
                    if (args.length > 2) {
                        String delay = args[1];
                        String text = StringUtils.join(Arrays.copyOfRange(args, 2, args.length));

                        try {
                            guideService.registerNewGuide(delay, text);
                            bot.messaging().sendText(message.getPeer(), "Гайд успешно создан.");
                        } catch (DateTimeParseException exc) {
                            bot.messaging().sendText(message.getPeer(), "Неизвестный формат времени.");
                        }
                    } else {
                        unknownCommand(message);
                    }
                    break;
                }
                case "count": {
                    bot.messaging().sendText(message.getPeer(), String.format("Количество гайдов: %d.", guideService.getCountOfGuides()));
                    break;
                }
                case "list": {
                    List<Guide> guides = guideService.getAllGuides();

                    if (guides.size() == 0) {
                        bot.messaging().sendText(message.getPeer(), "Гайды еще не созадвались");
                    }

                    StringBuilder stringBuilder = new StringBuilder();

                    guides.forEach(guide -> {
                        stringBuilder
                                .append("ID: ").append(guide.getId())
                                .append(", Задержка: ").append(guide.getDelay())
                                .append(" секунд, Текст: ").append(guide.getText())
                                .append("\n\n");
                    });

                    stringBuilder.deleteCharAt(stringBuilder.length() - 1).deleteCharAt(stringBuilder.length() - 1);

                    bot.messaging().sendText(message.getPeer(), stringBuilder.toString());
                    break;
                }
                case "delete": {
                    if (args.length > 1) {
                        try {
                            guideService.deleteGuide(Integer.valueOf(args[1]));
                            bot.messaging().sendText(message.getPeer(), "Гайд успешно удалён.");
                        } catch (NumberFormatException exc) {
                            unknownCommand(message);
                        } catch (NotFoundException e) {
                            bot.messaging().sendText(message.getPeer(), "Неверный ID гайда. Попробуйте снова.");
                        }
                    }
                    break;
                }
                default: {
                    unknownCommand(message);
                    break;
                }
            }
        } else {
            unknownCommand(message);
        }
    }

    private void processQuestion(Message message, String[] args) {
        if (args.length > 0) {
            String subCommand = args[0];

            switch (subCommand) {

            }
        }
    }

    private void processAnswer(Message message, String[] args) {
        if (args.length > 0) {
            String subCommand = args[0];

            switch (subCommand) {

            }
        }
    }

    private void processStart(Message message) {
        Integer userId = message.getPeer().getId();

        if (!userRepository.existsById(userId)) {
            InternalUser internalUser = new InternalUser();
            internalUser.setId(userId);
            userRepository.save(internalUser);
        }
    }

    private void unknownCommand(Message message) {
        String text = "Неизвестная команда или неверный синтаксис.\nВведите /help для отображения списка команд.";
        bot.messaging().sendText(message.getPeer(), text);
    }

    private void defaultProcessor(Message message) {
//        bot.users().get(message.getSender()).thenAccept(userOpt -> userOpt.ifPresent(user -> {
//                    System.out.println("Got a message: " + message.getText() + " from user id: " + user.getPeer().getId());
//                })
//        ).thenCompose(aVoid -> bot.messaging().sendText(message.getPeer(), "pong")
//        ).exceptionally(ex -> {
//            ex.printStackTrace();
//            return null;
//        }).thenAccept(uuid ->
//                System.out.println("Sent a message with UUID: " + uuid));
    }

}
