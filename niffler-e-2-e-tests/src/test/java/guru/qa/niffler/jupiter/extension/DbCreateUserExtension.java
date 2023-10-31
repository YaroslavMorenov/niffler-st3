package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.db.model.auth.AuthUserEntity;
import guru.qa.niffler.db.model.auth.Authority;
import guru.qa.niffler.db.model.auth.AuthorityEntity;
import guru.qa.niffler.db.model.userdata.UserDataUserEntity;
import guru.qa.niffler.db.repository.UserRepository;
import guru.qa.niffler.db.repository.UserRepositoryHibernate;
import guru.qa.niffler.jupiter.annotation.*;
import guru.qa.niffler.model.UserJson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static guru.qa.niffler.util.FakerUtils.generateRandomName;

public class DbCreateUserExtension extends CreateUserExtension {

    private static final String DEFAULT_PASSWORD = "12345";
    private final UserRepository userRepository = new UserRepositoryHibernate();

    @Override
    protected UserJson createUserForTest(GenerateUser annotation) {
        AuthUserEntity authUser = createAuthUserEntity();
        userRepository.createUserForTest(authUser);
        UserJson result = UserJson.fromEntity(authUser);
        result.setPassword(DEFAULT_PASSWORD);
        return result;
    }

    @Override
    protected List<UserJson> createFriendsIfPresent(GenerateUser annotation, UserJson user) {
        Friend friends = annotation.friends();
        List<UserJson> usersList = new ArrayList<>();
        if (friends.handleAnnotation()) {
            UserDataUserEntity currentUserEntity = userRepository.getUserFromData(user.getUsername());
            for (int i = 0; i < friends.count(); i++) {
                AuthUserEntity userEntity = createAuthUserEntity();
                userRepository.createUserForTest(userEntity);
                UserDataUserEntity friendUserEntity = userRepository.getUserFromData(userEntity.getUsername());
                currentUserEntity.addFriends(false, friendUserEntity);
                friendUserEntity.addFriends(false, currentUserEntity);
                userRepository.setFriendsForUser(friendUserEntity);
                usersList.add(UserJson.fromEntity(friendUserEntity));
            }
            userRepository.setFriendsForUser(currentUserEntity);
        }
        return usersList;
    }

    @Override
    protected List<UserJson> createIncomeInvitationsIfPresent(GenerateUser annotation, UserJson user) {
        IncomeInvitation incomeInvitation = annotation.incomeInvitations();
        List<UserJson> usersList = new ArrayList<>();
        if (incomeInvitation.handleAnnotation()) {
            UserDataUserEntity currentUserEntity = userRepository.getUserFromData(user.getUsername());
            for (int i = 0; i < incomeInvitation.count(); i++) {
                AuthUserEntity userEntity = createAuthUserEntity();
                userRepository.createUserForTest(userEntity);
                UserDataUserEntity friendUserEntity = userRepository.getUserFromData(userEntity.getUsername());
                friendUserEntity.addFriends(true, currentUserEntity);
                userRepository.setFriendsForUser(friendUserEntity);
                usersList.add(UserJson.fromEntity(friendUserEntity));
            }
            userRepository.setFriendsForUser(currentUserEntity);
        }
        return usersList;
    }

    @Override
    protected List<UserJson> createOutcomeInvitationsIfPresent(GenerateUser annotation, UserJson user) {
        OutcomeInvitation outcomeInvitation = annotation.outcomeInvitations();
        List<UserJson> userList = new ArrayList<>();
        if (outcomeInvitation.handleAnnotation()) {
            UserDataUserEntity currentUserEntity = userRepository.getUserFromData(user.getUsername());
            for (int i = 0; i < outcomeInvitation.count(); i++) {
                AuthUserEntity userEntity = createAuthUserEntity();
                userRepository.createUserForTest(userEntity);
                UserDataUserEntity friendUserEntity = userRepository.getUserFromData(userEntity.getUsername());
                currentUserEntity.addFriends(true, friendUserEntity);
                userRepository.setFriendsForUser(currentUserEntity);
                userList.add(UserJson.fromEntity(friendUserEntity));
            }
        }
        return userList;
    }

    private AuthUserEntity createAuthUserEntity() {
        AuthUserEntity authUser = new AuthUserEntity();
        authUser.setUsername(generateRandomName());
        authUser.setPassword(DEFAULT_PASSWORD);
        authUser.setEnabled(true);
        authUser.setAccountNonExpired(true);
        authUser.setAccountNonLocked(true);
        authUser.setCredentialsNonExpired(true);
        authUser.setAuthorities(new ArrayList<>(Arrays.stream(Authority.values())
                .map(a -> {
                    AuthorityEntity ae = new AuthorityEntity();
                    ae.setAuthority(a);
                    ae.setUser(authUser);
                    return ae;
                }).toList()));
        return authUser;
    }
}
