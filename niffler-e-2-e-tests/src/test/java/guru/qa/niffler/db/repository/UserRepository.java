package guru.qa.niffler.db.repository;

import guru.qa.niffler.db.model.auth.AuthUserEntity;
import guru.qa.niffler.db.model.userdata.UserDataUserEntity;

public interface UserRepository {
    void createUserForTest(AuthUserEntity user);

    void removeAfterTest(AuthUserEntity user);

    void setFriendsForUser(UserDataUserEntity userDataUserEntity);

    UserDataUserEntity getUserFromData(String userName);
}
