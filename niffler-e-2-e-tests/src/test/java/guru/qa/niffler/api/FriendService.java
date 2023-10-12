package guru.qa.niffler.api;

import guru.qa.niffler.model.UserJson;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FriendService {

    @POST("/addFriend")
    Call<Void> addFriend(
            @Header("Authorization") String token,
            @Body UserJson userJson);

    @POST("/acceptInvitation")
    Call<Void> acceptInvitation(
            @Header("Authorization") String token,
            @Body UserJson userJson);
}
