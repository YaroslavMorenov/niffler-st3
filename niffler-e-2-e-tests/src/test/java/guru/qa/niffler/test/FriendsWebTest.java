package guru.qa.niffler.test;


import guru.qa.niffler.jupiter.annotation.*;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.components.HeaderComponent;
import guru.qa.niffler.test.web.BaseWebTest;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;
import static guru.qa.niffler.jupiter.annotation.GeneratedUser.Selector.NESTED;
import static guru.qa.niffler.jupiter.annotation.GeneratedUser.Selector.OUTER;

//@Execution(ExecutionMode.SAME_THREAD)
public class FriendsWebTest extends BaseWebTest {

    @ApiLogin(
            user = @GenerateUser(
                    incomeInvitations = @IncomeInvitation
            )
    )
    @Test
    @AllureId("12345")
    void incomeInvitationShouldBePresentInTable(@GeneratedUser(selector = NESTED) UserJson userForTest) {
        open(CFG.nifflerFrontUrl() + "/main");
        new HeaderComponent()
                .goToPeoplePage()
                .checkInvitationToFriendIncome(userForTest.getIncomeInvitations().get(0).getUsername());
    }

    @ApiLogin(
            user = @GenerateUser(
                    outcomeInvitations = @OutcomeInvitation
            )
    )
    @Test
    @AllureId("123456")
    void outComeInvitationShouldBePresentInTable(@GeneratedUser(selector = NESTED) UserJson userForTest) {
        open(CFG.nifflerFrontUrl() + "/main");
        new HeaderComponent()
                .goToPeoplePage()
                .checkInvitationToFriendSent(userForTest.getOutcomeInvitations().get(0).getUsername());
    }

    @ApiLogin(
            user = @GenerateUser(
                    friends = @Friend
            )
    )
    @Test
    @AllureId("1234567")
    void friendShouldBePresentInTable(@GeneratedUser(selector = NESTED) UserJson userForTest) {
        open(CFG.nifflerFrontUrl() + "/main");
        new HeaderComponent()
                .goToPeoplePage()
                .checkUserHaveFriend();
    }
}
