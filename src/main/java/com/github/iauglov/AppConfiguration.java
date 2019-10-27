package com.github.iauglov;

import com.github.iauglov.service.GroupService;
import com.github.iauglov.service.GuideService;
import javax.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@DependsOn("botConfiguration")
public class AppConfiguration {

    private final ApplicationContext applicationContext;

    private GuideService guideService;
    private GroupService groupService;

    public AppConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void postInit() {
        guideService = applicationContext.getBean(GuideService.class);
        groupService = applicationContext.getBean(GroupService.class);
    }

    @Scheduled(fixedDelay = 10_000)
    public void processGuideSending() {
        guideService.processScheduledGuides();
    }

    @Scheduled(fixedDelay = 15_000)
    public void listenGroups() {
        groupService.processGroups();
    }

}
