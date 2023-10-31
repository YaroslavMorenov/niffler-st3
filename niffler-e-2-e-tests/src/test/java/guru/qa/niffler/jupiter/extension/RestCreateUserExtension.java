package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.api.AuthServiceClient;
import guru.qa.niffler.api.FriendServiceClient;
import guru.qa.niffler.api.RegisterServiceClient;
import guru.qa.niffler.api.context.SessionStorageContext;
import guru.qa.niffler.jupiter.annotation.Friend;
import guru.qa.niffler.jupiter.annotation.GenerateUser;
import guru.qa.niffler.jupiter.annotation.IncomeInvitation;
import guru.qa.niffler.jupiter.annotation.OutcomeInvitation;
import guru.qa.niffler.model.UserJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static guru.qa.niffler.util.FakerUtils.generateRandomUsername;

public class RestCreateUserExtension extends CreateUserExtension {

    private final RegisterServiceClient registerServiceClient = new RegisterServiceClient();
    private final AuthServiceClient authServiceClient = new AuthServiceClient();
    private final FriendServiceClient friendServiceClient = new FriendServiceClient();

    @Override
    protected UserJson createUserForTest(GenerateUser annotation) throws IOException {
        UserJson userJson = new UserJson();
        String username = generateRandomUsername();
        userJson.setUsername(username);
        userJson.setPassword(DEFAULT_PASSWORD);
        registerServiceClient.registerUser(username, DEFAULT_PASSWORD);
        return userJson;
    }

    @Override
    protected List<UserJson> createFriendsIfPresent(GenerateUser annotation, UserJson user) throws IOException {
        Friend friends = annotation.friends();
        List<UserJson> userList = new ArrayList<>();
        if (friends.handleAnnotation()) {
            for (int i = 0; i < friends.count(); i++) {
                UserJson friend = createUserForTest(annotation);
                addInvitation(user, friend);
                acceptInvitation(user, friend);
                userList.add(friend);
            }
        }
        return userList;
    }

    @Override
    protected List<UserJson> createIncomeInvitationsIfPresent(GenerateUser annotation, UserJson user) throws IOException {
        IncomeInvitation incomeInvitation = annotation.incomeInvitations();
        List<UserJson> userList = new ArrayList<>();
        if (incomeInvitation.handleAnnotation()) {
            for (int i = 0; i < incomeInvitation.count(); i++) {
                UserJson friend = createUserForTest(annotation);
                addInvitation(friend, user);
                userList.add(friend);
            }
        }
        return userList;
    }

    @Override
    protected List<UserJson> createOutcomeInvitationsIfPresent(GenerateUser annotation, UserJson user) throws IOException {
        OutcomeInvitation outcomeInvitation = annotation.outcomeInvitations();
        List<UserJson> userList = new ArrayList<>();
        if (outcomeInvitation.handleAnnotation()) {
            for (int i = 0; i < outcomeInvitation.count(); i++) {
                UserJson friend = createUserForTest(annotation);
                addInvitation(user, friend);
                userList.add(friend);
            }
        }
        return userList;
    }

    private void addInvitation(UserJson userSent, UserJson userReceived) throws IOException {
        authServiceClient.doLogin(userSent.getUsername(), DEFAULT_PASSWORD);
        String token = SessionStorageContext.getInstance().getToken();
        friendServiceClient.addFriend(token, userReceived);
        SessionStorageContext.getInstance().clearContext();
    }

    private void acceptInvitation(UserJson userReceived, UserJson userSent) throws IOException {
        authServiceClient.doLogin(userSent.getUsername(), DEFAULT_PASSWORD);
        String token = SessionStorageContext.getInstance().getToken();
        friendServiceClient.acceptInvitation(token, userReceived);
        SessionStorageContext.getInstance().clearContext();
    }
}
