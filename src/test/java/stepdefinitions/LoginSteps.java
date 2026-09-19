package stepdefinitions;

import factory.DriverFactory;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import pages.LoginPage;

public class LoginSteps {

    private static final Logger log = LoggerFactory.getLogger(LoginSteps.class);
    private final LoginPage loginPage;

    public LoginSteps() {
        // Hooks.setUp() (a separate @Before) always runs before any step, so the
        // driver is already initialized by the time this constructor runs.
        this.loginPage = new LoginPage(DriverFactory.getDriver());
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        loginPage.open();
        Allure.step("Navigated to login page");
    }

    @When("I enter username {string} and password {string}")
    public void iEnterCredentials(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        Allure.step("Entered username: " + username);
    }

    @And("I click the login button")
    public void iClickTheLoginButton() {
        loginPage.clickLoginButton();
        Allure.step("Clicked login button");
    }

    @Then("I should see the message {string}")
    public void iShouldSeeTheMessage(String expectedMessage) {
        String actualMessage = loginPage.getFlashMessageText();
        log.info("Actual message: {}", actualMessage);
        Assert.assertTrue(actualMessage.contains(expectedMessage),
                "Expected: " + expectedMessage + " but got: " + actualMessage);
        Allure.step("Verified message: " + expectedMessage);
    }
}