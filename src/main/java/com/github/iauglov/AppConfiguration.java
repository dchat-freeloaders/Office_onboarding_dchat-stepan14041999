package com.github.iauglov;

import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;
import java.util.concurrent.ExecutionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public Bot bot() throws ExecutionException, InterruptedException {
        BotConfig botConfig = BotConfig.Builder.aBotConfig()
                .withHost("hackathon-mob.transmit.im")
                .withPort(443)
                .withToken("750f7c780c6010203cbffc0b5ec2f060684d2d3c")
                .build();

        return Bot.start(botConfig).get();
    }

}
