package org.springframework.samples.petclinic.e2e;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FindOwnersITCase {

    private static final Logger LOG = LoggerFactory.getLogger(FindOwnersITCase.class);

    @Autowired
    private Environment environment;

    private static WebDriver driver;

    @BeforeClass
    public static void startDriver() {
        driver = getDriver();
    }

    @Test
    public void addAndFindOwner() {
        driver.get(getPetclinicHost());

        driver.findElement(By.cssSelector("[title^='find owners']")).click();
        driver.findElement(By.linkText("Add Owner")).click();

        driver.findElement(By.id("firstName")).sendKeys("Arthur");
        driver.findElement(By.id("lastName")).sendKeys("Dent");
        driver.findElement(By.id("address")).sendKeys("Planet");
        driver.findElement(By.id("city")).sendKeys("Earth");
        driver.findElement(By.id("telephone")).sendKeys("42");
        driver.findElement(By.xpath("//button[contains(text(),'Add')]")).click();

        driver.findElement(By.cssSelector("[title^='find owners']")).click();
        driver.findElement(By.xpath("//input[@id='lastName']")).sendKeys("Dent");
        driver.findElement(By.xpath("//button[contains(text(),'Find')]")).click();
    }

    private static WebDriver getDriver() {
        String remoteUrl = System.getProperty("selenium.remote.url", "http://localhost:4444/wd/hub").toLowerCase();
        String browser = System.getProperty(" selenium.browser", "chrome").toLowerCase();
        try {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setBrowserName(browser);
            return new RemoteWebDriver(new URL(remoteUrl), capabilities);
        } catch (MalformedURLException e) {
            LOG.error("Unable to create URL for selenium remote webdriver" + remoteUrl, e);
            throw new RuntimeException(e);
        }
    }

    private String getPetclinicHost() {
        // When using selenium grid, this might be different to "localhost"
        return System.getProperty("selenium.petclinic.url",
                                  "http://localhost:" + environment.getProperty("local.server.port"));
    }

    @AfterClass
    public static void stopDriver() {
        driver.quit();
    }
}
