package guru.qa.niffler.api;

import guru.qa.niffler.model.UserJson;

import java.io.IOException;

public class FriendServiceClient extends RestService {

    private final FriendService friendService = retrofit.create(FriendService.class);

    public FriendServiceClient() {
        super(CFG.nifflerUserdataUrl());
    }

    public void addFriend(String token, UserJson userJson) throws IOException {
        friendService.addFriend("Bearer " + token, userJson).execute();
    }

    public void acceptInvitation(String token, UserJson userJson) throws IOException {
        friendService.acceptInvitation("Bearer " + token, userJson).execute();
    }
}
