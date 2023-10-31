package guru.qa.niffler.util;

import io.qameta.allure.AllureId;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ExtensionUtil {

    public static String getAllureId(ExtensionContext context) {
        AllureId allureId = context.getRequiredTestMethod().getAnnotation(AllureId.class);
        if (allureId == null) {
            throw new IllegalStateException("Annotation @AllureId must be present!");
        }
        return allureId.value();
    }
}
