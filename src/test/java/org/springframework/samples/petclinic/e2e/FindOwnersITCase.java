package org.springframework.samples.petclinic.e2e;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class FindOwnersITCase {

    private static WebDriver driver;

    @BeforeClass
    public static void startDriver() {
        driver = Utils.getDriver();
    }

    @Test
    public void addAndFindOwner() {
        driver.get(Utils.getBaseUrl());

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

    @AfterClass
    public static void stopDriver() {
        driver.quit();
    }
}
