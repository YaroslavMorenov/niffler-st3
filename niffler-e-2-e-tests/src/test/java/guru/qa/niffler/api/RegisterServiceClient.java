package guru.qa.niffler.api;

import java.io.IOException;
import java.util.UUID;

public class RegisterServiceClient extends RestService {

    private final RegisterService registerService = retrofit.create(RegisterService.class);

    public RegisterServiceClient() {
        super(CFG.nifflerAuthUrl() + "/register/");
    }

    public void registerUser(String username, String password) throws IOException {
        String csrfToken = UUID.randomUUID().toString();
        registerService.registerUser("XSRF-TOKEN=" + csrfToken, csrfToken, username, password, password)
                .execute();
    }
}
