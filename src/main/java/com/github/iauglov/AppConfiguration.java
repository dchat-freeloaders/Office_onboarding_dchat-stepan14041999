package com.github.iauglov;

import com.github.iauglov.service.GuideService;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class AppConfiguration {

    private final ApplicationContext applicationContext;

    @Value("${dchat.bot.token}")
    private String botToken;
    @Value("${dchat.bot.host}")
    private String botHost;
    @Value("${dchat.bot.port}")
    private Integer botPort;
    private GuideService guideService;

    public AppConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void postInit() {
        guideService = applicationContext.getBean(GuideService.class);
    }

    @Bean
    public Bot bot() throws ExecutionException, InterruptedException {
        BotConfig botConfig = BotConfig.Builder.aBotConfig()
                .withHost(botHost)
                .withPort(botPort)
                .withToken(botToken)
                .build();

        return Bot.start(botConfig).get();
    }

    @Scheduled(fixedDelay = 10_000)
    public void processGuideSending() {
        guideService.processScheduledGuides();
    }

}
