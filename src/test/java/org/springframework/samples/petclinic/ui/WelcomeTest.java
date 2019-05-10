package org.springframework.samples.petclinic.ui;

import com.google.common.base.Function;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class WelcomeTest {
    private URL seleniumHubUrl;
    private String targetUrl;
    private RemoteWebDriver driver;

    @Before
    public void setUp() throws Exception {
        String seleniumHubHost = "localhost";
        String seleniumHubPort = "4444";
        String seleniumHubEndpoint = "/wd/hub";
        seleniumHubUrl = new URL("http://" + seleniumHubHost + ":" + seleniumHubPort + seleniumHubEndpoint);

        String targetHost = "localhost";
        String targetPort = "1234";
        targetUrl = "http://" + targetHost + ":" + targetPort;
    }

    @Test
    public void executeFirefoxDriver()  {
        this.execute(DesiredCapabilities.firefox());
    }

    @Test
    public void executeChrome()  {
        DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--lang=en");
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);

        this.execute(desiredCapabilities);
    }

    private void execute(final DesiredCapabilities capability) {
        driver = new RemoteWebDriver(seleniumHubUrl, capability);
        driver.manage().window().maximize();

        goToHomepage();
        navigateByMenu();

        driver.close();
    }

    private void goToHomepage() {
        driver.navigate().to(targetUrl + "/");
        waitUntilDomReady(driver);

        WebElement homeHeader = findByCss("body div.container h2");

        assertThat(homeHeader.getText()).isEqualTo("Willkommen");
    }

    private void navigateByMenu() {
        WebElement naviItem = findByCss("body nav div#main-navbar ul.nav li a[href='/vets.html']");
        assertThat(naviItem.getText().trim()).isEqualToIgnoringCase("Veterinarians");

        naviItem.click();
        waitUntilDomReady(driver);

        assertThat(driver.getCurrentUrl()).endsWith("vets.html");
        WebElement vetHeader = findByCss("body div.container h2");
        assertThat(vetHeader.getText()).isEqualToIgnoringCase("veterinarians");
    }

    private WebElement findByCss(String selector) {
        return driver.findElement(By.cssSelector(selector));
    }

    private void waitUntilDomReady(RemoteWebDriver driver) {
        // see https://stackoverflow.com/questions/5868439/wait-for-page-load-in-selenium
        int timeOutInSeconds = 30;
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        wait.until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        });
    }
}
