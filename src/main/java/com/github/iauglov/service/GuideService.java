package com.github.iauglov.service;

import static com.github.iauglov.model.Action.GET_ANSWERS;
import com.github.iauglov.model.NotFoundException;
import com.github.iauglov.persistence.Guide;
import com.github.iauglov.persistence.GuideRepository;
import com.github.iauglov.persistence.InternalUser;
import com.github.iauglov.persistence.Question;
import com.github.iauglov.persistence.UserGuide;
import com.github.iauglov.persistence.UserGuide.PK;
import com.github.iauglov.persistence.UserGuideRepository;
import com.github.iauglov.persistence.UserRepository;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import static java.time.Clock.systemUTC;
import java.time.Duration;
import java.time.LocalDateTime;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GuideService {

    private static final String GUIDE_PATTERN = "%s\n\n%s";

    private final GuideRepository guideRepository;
    private final UserRepository userRepository;
    private final UserGuideRepository userGuideRepository;
    private final QuestionAnswerService questionAnswerService;
    private final Bot bot;

    public void registerNewGuide(String delay, String title, String text) {
        long initDelayInSeconds = Duration.parse("PT" + delay.toUpperCase()).getSeconds();

        Guide guide = new Guide();
        guide.setDelay(initDelayInSeconds);
        guide.setTitle(title);
        guide.setText(text);
        guideRepository.save(guide);
    }

    public void processScheduledGuides() {
        List<UserGuide> sentGuides = userGuideRepository.findAll();

        guideRepository.findAll().forEach(guide -> {
            List<InternalUser> internalUsers = userRepository.findAll();

            internalUsers.forEach(internalUser -> {
                LocalDateTime userRegisteredAt = internalUser.getRegisteredAt();

                UserGuide userGuide = new UserGuide();
                PK pk = new PK();
                pk.setGuide(guide);
                pk.setUser(internalUser);
                userGuide.setPk(pk);

                if (userRegisteredAt.plus(guide.getDelay(), SECONDS).isBefore(now(systemUTC())) && !sentGuides.contains(userGuide)) {
                    bot.users().findUserPeer(internalUser.getId()).thenAccept(optionalPeer -> {
                        optionalPeer.ifPresent(peer -> {
                            bot.messaging().sendText(peer, String.format(GUIDE_PATTERN, guide.getTitle(), guide.getText())).thenAccept(uuid -> {
                                userGuideRepository.save(userGuide);

                                List<Question> questions = questionAnswerService.getAllQuestionsForGuide(guide.getId());

                                if (questions.size() == 0) {
                                    return;
                                }

                                List<InteractiveAction> actions = new ArrayList<>();

                                questions.forEach(question -> {
                                    actions.add(new InteractiveAction(GET_ANSWERS.asId(), new InteractiveButton(question.getId().toString(), question.getText())));
                                });

                                InteractiveGroup group = new InteractiveGroup("Популярные вопросы", "Выберите интересующий вас вопрос.", actions);

                                bot.interactiveApi().send(peer, group);
                            });
                        });
                    });
                }
            });
        });
    }

    public List<Guide> getAllGuides() {
        return guideRepository.findAll();
    }

    public void deleteGuide(Integer guideId) throws NotFoundException {
        if (guideRepository.existsById(guideId)) {
            guideRepository.deleteById(guideId);
        } else {
            throw new NotFoundException(String.format("Guide with id '%d' not found", guideId));
        }
    }
}
