package com.eventshop.eventshoplinux.webcrawl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aravindh on 6/16/15.
 */
public class WebCrawlAQI {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebCrawlAQI.class);
    public static void main(String args[]){
        WebDriver driver = new FirefoxDriver();
        driver.manage().window().maximize();
        driver.get("http://feeds.enviroflash.info/");

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver.findElement(By.xpath("//*[@id='sStateID']/option[@value='CA']")).click();
        //driver.findElement(By.linkText("CA")).click();
        try{
            Thread.sleep(5000);
        }catch (InterruptedException e){

        }

        driver.findElement(By.xpath("//*[@id='content']/div[4]/table/tbody/tr/td[3]/input")).click();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(int i=3;i<104;i++){
            String xp= "//*[@id='cityList']/table/tbody/tr["+i+"]/td[4]/a";
            String val =  driver.findElement(By.xpath(xp)).getAttribute("href");
            //String val =  driver.findElement(By.xpath("//*[@id='rss']/a")).getText();
            LOGGER.info(val);
        }


    }//*[@id='sStateID']
}
