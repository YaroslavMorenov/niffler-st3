package guru.qa.niffler.service;

import guru.qa.niffler.data.CurrencyValues;
import guru.qa.niffler.data.UserEntity;
import guru.qa.niffler.data.repository.UserRepository;
import guru.qa.niffler.ex.NotFoundException;
import guru.qa.niffler.model.FriendJson;
import guru.qa.niffler.model.FriendState;
import guru.qa.niffler.model.UserJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static guru.qa.niffler.model.FriendState.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDataServiceTest {

    private UserDataService testedObject;

    private final UUID mainTestUserUuid = UUID.randomUUID();
    private final String mainTestUserName = "dima";
    private UserEntity mainTestUser;

    private final UUID secondTestUserUuid = UUID.randomUUID();
    private final String secondTestUserName = "barsik";
    private UserEntity secondTestUser;

    private final UUID thirdTestUserUuid = UUID.randomUUID();
    private final String thirdTestUserName = "emma";
    private UserEntity thirdTestUser;


    private final String notExistingUser = "not_existing_user";


    @BeforeEach
    void init() {
        mainTestUser = new UserEntity();
        mainTestUser.setId(mainTestUserUuid);
        mainTestUser.setUsername(mainTestUserName);
        mainTestUser.setCurrency(CurrencyValues.RUB);

        secondTestUser = new UserEntity();
        secondTestUser.setId(secondTestUserUuid);
        secondTestUser.setUsername(secondTestUserName);
        secondTestUser.setCurrency(CurrencyValues.RUB);

        thirdTestUser = new UserEntity();
        thirdTestUser.setId(thirdTestUserUuid);
        thirdTestUser.setUsername(thirdTestUserName);
        thirdTestUser.setCurrency(CurrencyValues.RUB);
    }


    @ValueSource(strings = {"photo", ""})
    @ParameterizedTest
    void userShouldBeUpdated(String photo, @Mock UserRepository userRepository) {
        when(userRepository.findByUsername(eq(mainTestUserName)))
                .thenReturn(mainTestUser);

        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(answer -> answer.getArguments()[0]);

        testedObject = new UserDataService(userRepository);

        final String photoForTest = photo.equals("") ? null : photo;

        final UserJson toBeUpdated = new UserJson();
        toBeUpdated.setUsername(mainTestUserName);
        toBeUpdated.setFirstname("Test");
        toBeUpdated.setSurname("TestSurname");
        toBeUpdated.setCurrency(CurrencyValues.USD);
        toBeUpdated.setPhoto(photoForTest);
        final UserJson result = testedObject.update(toBeUpdated);
        assertEquals(mainTestUserUuid, result.getId());
        assertEquals("Test", result.getFirstname());
        assertEquals("TestSurname", result.getSurname());
        assertEquals(CurrencyValues.USD, result.getCurrency());
        assertEquals(photoForTest, result.getPhoto());

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void getRequiredUserShouldThrowNotFoundExceptionIfUserNotFound(@Mock UserRepository userRepository) {
        when(userRepository.findByUsername(eq(notExistingUser)))
                .thenReturn(null);

        testedObject = new UserDataService(userRepository);

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> testedObject.getRequiredUser(notExistingUser));
        assertEquals(
                "Can`t find user by username: " + notExistingUser,
                exception.getMessage()
        );
    }

    @Test
    void allUsersShouldReturnCorrectUsersList(@Mock UserRepository userRepository) {
        when(userRepository.findByUsernameNot(eq(mainTestUserName)))
                .thenReturn(getMockUsersMappingFromDb());

        testedObject = new UserDataService(userRepository);

        List<UserJson> users = testedObject.allUsers(mainTestUserName);
        assertEquals(2, users.size());
        final UserJson invitation = users.stream()
                .filter(u -> u.getFriendState() == INVITE_SENT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Friend with state INVITE_SENT not found"));

        final UserJson friend = users.stream()
                .filter(u -> u.getFriendState() == FRIEND)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Friend with state FRIEND not found"));


        assertEquals(secondTestUserName, invitation.getUsername());
        assertEquals(thirdTestUserName, friend.getUsername());
    }


    static Stream<Arguments> friendsShouldReturnDifferentListsBasedOnIncludePendingParam() {
        return Stream.of(
                Arguments.of(true, List.of(INVITE_SENT, FRIEND)),
                Arguments.of(false, List.of(FRIEND))
        );
    }

    @MethodSource
    @ParameterizedTest
    void friendsShouldReturnDifferentListsBasedOnIncludePendingParam(boolean includePending,
                                                                     List<FriendState> expectedStates,
                                                                     @Mock UserRepository userRepository) {
        when(userRepository.findByUsername(eq(mainTestUserName)))
                .thenReturn(enrichTestUser());

        testedObject = new UserDataService(userRepository);
        final List<UserJson> result = testedObject.friends(mainTestUserName, includePending);
        assertEquals(expectedStates.size(), result.size());

        assertTrue(result.stream()
                .map(UserJson::getFriendState)
                .toList()
                .containsAll(expectedStates));
    }

    @Test
    void invitationsShouldBeInviteReceived(@Mock UserRepository userRepository) {
        thirdTestUser.addFriends(true, secondTestUser);
        secondTestUser.addInvites(thirdTestUser);

        testedObject = new UserDataService(userRepository);

        when(userRepository.findByUsername(eq(secondTestUserName)))
                .thenReturn(secondTestUser);

        final List<UserJson> invitations = testedObject.invitations(secondTestUserName);
        assertEquals(1, invitations.size());
        assertEquals(INVITE_RECEIVED, invitations.get(0).getFriendState());
    }

    @Test
    void addFriendShouldBeInviteSent(@Mock UserRepository userRepository) {
        FriendJson friendJson = new FriendJson();
        friendJson.setUsername(secondTestUserName);

        when(userRepository.findByUsername(eq(mainTestUserName)))
                .thenReturn(mainTestUser);
        when(userRepository.findByUsername(eq(secondTestUserName)))
                .thenReturn(secondTestUser);

        testedObject = new UserDataService(userRepository);
        final UserJson addFriend = testedObject.addFriend(mainTestUserName, friendJson);
        final UserEntity main = testedObject.getRequiredUser(mainTestUserName);
        assertEquals(secondTestUserName, main.getFriends().get(0).getFriend().getUsername(), "user " + mainTestUserName + "should have friend " + secondTestUserName);
        assertEquals(INVITE_SENT, addFriend.getFriendState(), "user " + secondTestUserName + "should state INVITE SENT");
    }

    @Test
    void acceptInvitationShouldBeUpdatedAfterAddFriend(@Mock UserRepository userRepository) {
        FriendJson friendJson = new FriendJson();
        friendJson.setUsername(secondTestUserName);
        secondTestUser.addFriends(true, mainTestUser);
        mainTestUser.addInvites(secondTestUser);

        when(userRepository.findByUsername(eq(mainTestUserName)))
                .thenReturn(mainTestUser);
        when(userRepository.findByUsername(eq(secondTestUserName)))
                .thenReturn(secondTestUser);

        testedObject = new UserDataService(userRepository);
        final List<UserJson> result = testedObject.acceptInvitation(mainTestUserName, friendJson);
        assertEquals(1, result.size());
        assertEquals(FRIEND, result.get(0).getFriendState(), "check friend state");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void declineInvitationShouldBeUpdatedAfterDecline(@Mock UserRepository userRepository) {
        FriendJson friendJson = new FriendJson();
        friendJson.setUsername(secondTestUserName);
        secondTestUser.addFriends(true, mainTestUser);
        mainTestUser.addInvites(secondTestUser);

        when(userRepository.findByUsername(eq(mainTestUserName)))
                .thenReturn(mainTestUser);
        when(userRepository.findByUsername(eq(secondTestUserName)))
                .thenReturn(secondTestUser);

        testedObject = new UserDataService(userRepository);
        final List<UserJson> result = testedObject.declineInvitation(mainTestUserName, friendJson);
        final UserEntity main = testedObject.getRequiredUser(mainTestUserName);
        final UserEntity second = testedObject.getRequiredUser(secondTestUserName);
        assertEquals(0, result.size());
        assertEquals(0, main.getInvites().size());
        assertEquals(0, main.getFriends().size());
        assertEquals(0, second.getInvites().size());
        assertEquals(0, second.getFriends().size());
        verify(userRepository, times(2)).save(any(UserEntity.class));
    }

    @Test
    void removeFriendShouldBeUpdatedAfterRemove(@Mock UserRepository userRepository) {
        mainTestUser.addInvites(secondTestUser);
        mainTestUser.addFriends(false, secondTestUser);
        secondTestUser.addInvites(mainTestUser);
        secondTestUser.addFriends(false, mainTestUser);

        when(userRepository.findByUsername(eq(mainTestUserName)))
                .thenReturn(mainTestUser);
        when(userRepository.findByUsername(eq(secondTestUserName)))
                .thenReturn(secondTestUser);

        testedObject = new UserDataService(userRepository);
        final List<UserJson> result = testedObject.removeFriend(mainTestUserName, secondTestUserName);
        final UserEntity main = testedObject.getRequiredUser(mainTestUserName);
        final UserEntity second = testedObject.getRequiredUser(secondTestUserName);
        assertEquals(0, result.size());
        assertEquals(0, main.getFriends().size());
        assertEquals(0, main.getInvites().size());
        assertEquals(0, second.getInvites().size());
        assertEquals(0, second.getFriends().size());
        verify(userRepository, times(2)).save(any(UserEntity.class));
    }

    private UserEntity enrichTestUser() {
        mainTestUser.addFriends(true, secondTestUser);
        secondTestUser.addInvites(mainTestUser);

        mainTestUser.addFriends(false, thirdTestUser);
        thirdTestUser.addFriends(false, mainTestUser);
        return mainTestUser;
    }


    private List<UserEntity> getMockUsersMappingFromDb() {
        mainTestUser.addFriends(true, secondTestUser);
        secondTestUser.addInvites(mainTestUser);

        mainTestUser.addFriends(false, thirdTestUser);
        thirdTestUser.addFriends(false, mainTestUser);

        return List.of(secondTestUser, thirdTestUser);
    }
}