package stepdefinitions;

package stepdefinitions;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import io.cucumber.*;
//import io.cucumber.java.Before;
//import io.cucumber.java.After;
//import io.cucumber.java.en.Given;
//import io.cucumber.java.en.When;
//import io.cucumber.java.en.Then;
//import io.cucumber.java.en.And;
import java.time.Duration;

public class LoginSteps {

    WebDriver driver;
    WebDriverWait wait;

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--incognito");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        driver.get("https://the-internet.herokuapp.com/login");
        Allure.step("Navigated to login page");
    }

    @When("I enter username {string} and password {string}")
    public void iEnterCredentials(String username, String password) {
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("username"))
        );
        usernameField.clear();
        usernameField.sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        Allure.step("Entered username: " + username);
    }

    @And("I click the login button")
    public void iClickTheLoginButton() {
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        Allure.step("Clicked login button");
    }

    @Then("I should see the message {string}")
    public void iShouldSeeTheMessage(String expectedMessage) {
        WebElement message = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("flash"))
        );
        System.out.println("Actual message: " + message.getText());
        Assert.assertTrue(message.getText().contains(expectedMessage),
                "Expected: " + expectedMessage + " but got: " + message.getText());
        Allure.step("Verified message: " + expectedMessage);
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}