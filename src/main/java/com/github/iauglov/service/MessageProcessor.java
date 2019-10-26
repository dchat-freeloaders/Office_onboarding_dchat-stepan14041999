package com.github.iauglov.service;

import static com.github.iauglov.model.Action.ADMIN;
import static com.github.iauglov.model.Action.ANSWERS;
import static com.github.iauglov.model.Action.GUIDES;
import static com.github.iauglov.model.Action.QUESTIONS;
import com.github.iauglov.model.Command;
import com.github.iauglov.persistence.InternalUser;
import com.github.iauglov.persistence.UserRepository;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageProcessor {

    private final UserRepository userRepository;
    private final GuideService guideService;
    private final GuideCrudProcessor guideCrudProcessor;
    private final QuestionAnswerCrudProcessor questionAnswerCrudProcessor;
    private final Bot bot;

    public void process(Message message) {
        // Возможность работать с ботом только через приватный чат
        if (message.getPeer().getId() != message.getSender().getId()) {
            return;
        }

        if (message.getText().isEmpty()) {
            return;
        }

        if (guideCrudProcessor.processMessage(message) || questionAnswerCrudProcessor.processMessage(message)) {
            return;
        }

        String messageText = message.getText();

        if (messageText.startsWith("/")) {
            if (messageText.length() > 1) {
                String truncatedMessage = messageText.substring(1);

                String[] commandAndArgs = truncatedMessage.split(" ");
                String command = commandAndArgs[0].toUpperCase();
                String[] args = Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length);

                if (Command.canProcess(command)) {
                    switch (Command.valueOf(command)) {
                        case START: {
                            processStart(message);
                            break;
                        }
                        case HELP: {
                            processHelp(message);
                            break;
                        }
                        case ADMIN: {
                            interactiveAdmin(message);
                            break;
                        }
                        default: {
                            String text = "Команда в разработке.\nВведите /help для отображения списка команд";
                            bot.messaging().sendText(message.getSender(), text);
                            break;
                        }
                    }
                } else {
                    unknownCommand(message);
                }
            } else {
                unknownCommand(message);
            }
        } else {
            processStart(message);
        }
    }

    private void interactiveAdmin(Message message) {
        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(GUIDES.asId(), new InteractiveButton(GUIDES.asId(), GUIDES.getLabel())));
        actions.add(new InteractiveAction(QUESTIONS.asId(), new InteractiveButton(QUESTIONS.asId(), QUESTIONS.getLabel())));
        actions.add(new InteractiveAction(ANSWERS.asId(), new InteractiveButton(ANSWERS.asId(), ANSWERS.getLabel())));

        InteractiveGroup group = new InteractiveGroup("Админ-панель", "Выберите группу действий.", actions);

        bot.interactiveApi().send(message.getSender(), group);
    }

    private void processHelp(Message message) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Помощь по органайзеру гайдов.").append("\n\n");
        stringBuilder.append("/help - Вывод списка команд органайзера.").append("\n");
        stringBuilder.append("/admin - Вывод панели администратора.").append("\n");

        bot.messaging().sendText(message.getSender(), stringBuilder.toString());
    }

    private void processStart(Message message) {
        Integer userId = message.getSender().getId();

        CompletableFuture<Void> completableFuture;
        InternalUser internalUser;

        if (!userRepository.existsById(userId)) {
            internalUser = new InternalUser();
            internalUser.setId(userId);

            completableFuture = bot.users().get(message.getPeer()).thenAccept(optionalUser -> optionalUser.ifPresent(user -> {
                internalUser.setName(user.getName());
                userRepository.save(internalUser);
            }));
        } else {
            internalUser = userRepository.findById(userId).get();
            completableFuture = null;
        }

        if (completableFuture != null) {
            try {
                completableFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        List<InteractiveAction> actions = new ArrayList<>();

        actions.add(new InteractiveAction(ADMIN.asId(), new InteractiveButton(ADMIN.asId(), ADMIN.getLabel())));

        InteractiveGroup group = new InteractiveGroup("Onboarding bot", String.format("Здравствуйте, %s. Вам доступна админ-панель.", internalUser.getName()), actions);

        bot.interactiveApi().send(message.getPeer(), group);
    }

    private void unknownCommand(Message message) {
        String text = "Неизвестная команда или неверный синтаксис.\nВведите /help для отображения списка команд.";
        bot.messaging().sendText(message.getSender(), text);
    }

}
