package com.github.iauglov.service;

import com.github.iauglov.persistence.Group;
import com.github.iauglov.persistence.GroupRepository;
import com.github.iauglov.persistence.InternalUser;
import com.github.iauglov.persistence.UserRepository;
import com.google.common.util.concurrent.ListenableFuture;
import dialog.GroupsGrpc;
import dialog.GroupsOuterClass;
import dialog.Peers;
import im.dlg.botsdk.Bot;
import io.grpc.Channel;
import io.grpc.stub.AbstractStub;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static java.time.Clock.systemUTC;
import static java.time.LocalDateTime.now;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

@Service
@AllArgsConstructor
@Slf4j
public class GroupService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final Bot bot;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public void registerGroupId(int groupId, long hash) {
        Group group = new Group();
        group.setId(groupId);
        group.setHash(hash);
        groupRepository.save(group);
    }

    private void registerMembers(int groupId, Long accessHash) throws NoSuchFieldException, NoSuchMethodException {
        Field internalBotApiField = bot.getClass().getDeclaredField("internalBotApi");
        ReflectionUtils.makeAccessible(internalBotApiField);
        Object internalBotApi = ReflectionUtils.getField(internalBotApiField, bot);

        Method withTokenMethod = internalBotApi.getClass().getDeclaredMethod("withToken", AbstractStub.class, Function.class);
        ReflectionUtils.makeAccessible(withTokenMethod);

        Field channelField = internalBotApi.getClass().getDeclaredField("channel");
        ReflectionUtils.makeAccessible(channelField);
        Object channel = ReflectionUtils.getField(channelField, internalBotApi);

        Method getChannelMethod = channel.getClass().getDeclaredMethod("getChannel");
        ReflectionUtils.makeAccessible(getChannelMethod);
        Object getChannel = ReflectionUtils.invokeMethod(getChannelMethod, channel);

        Peers.GroupOutPeer groupOutPeer = Peers.GroupOutPeer.newBuilder().setGroupId(groupId).setAccessHash(accessHash).build();
        GroupsOuterClass.RequestLoadMembers request = GroupsOuterClass.RequestLoadMembers.newBuilder()
                .setGroup(groupOutPeer).setLimit(10000).build();

        ((CompletableFuture<GroupsOuterClass.ResponseLoadMembers>) ReflectionUtils.invokeMethod(withTokenMethod, internalBotApi, GroupsGrpc.newFutureStub((Channel) getChannel),
                new MyFunc(request))).thenAcceptAsync(response -> {
            response.getMembersList().forEach(member -> {
                if (!userRepository.existsById(member.getUid())) {
                    bot.users().findUserPeer(member.getUid()).thenAccept(optionalPeer -> optionalPeer.ifPresent(peer -> {
                        bot.users().get(peer).thenAccept(optionalUser -> optionalUser.ifPresent(user -> {
                            userRepository.save(new InternalUser(user.getPeer().getId(), user.getName(), now(systemUTC())));
                        }));
                    }));
                }
            });
        }, executor);

    }

    public void processGroups() {
        groupRepository.findAll().forEach(group -> {
            try {
                registerMembers(group.getId(), group.getHash());
            } catch (Throwable t) {
            }
        });
    }

    @AllArgsConstructor
    private static class MyFunc implements Function<GroupsGrpc.GroupsFutureStub, ListenableFuture<GroupsOuterClass.ResponseLoadMembers>> {

        private final GroupsOuterClass.RequestLoadMembers requestLoadMembers;

        @Override
        public ListenableFuture<GroupsOuterClass.ResponseLoadMembers> apply(GroupsGrpc.GroupsFutureStub stub) {
            return stub.loadMembers(requestLoadMembers);
        }
    }

}
