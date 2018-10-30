package org.springframework.samples.petclinic.e2e;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static WebDriver getDriver() {
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

    public static String getBaseUrl() {
        return System.getProperty("selenium.petclinic.url", "http://localhost:8080/");
    }
}
