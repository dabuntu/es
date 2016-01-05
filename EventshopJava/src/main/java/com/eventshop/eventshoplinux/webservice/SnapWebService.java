package com.eventshop.eventshoplinux.webservice;

import com.eventshop.eventshoplinux.model.MapSnapshot;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by abhisekmohanty on 3/6/15.
 */
@Path("/snapWebService")
public class SnapWebService {




    private final static Logger LOGGER = LoggerFactory.getLogger(SnapWebService.class);
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getSnapshot")

    public String GetSnapshot(MapSnapshot query) {
        String message;
        try{
            LOGGER.debug("inside getSnap");
            int id = query.getId();
            String type = query.getType();
            String endPoint = query.getEndPoint();
            LOGGER.debug("endpoint is "+endPoint);
            File image = TakeSnapshot(id, type);
            InputStream input = new FileInputStream(image);
            byte[] imageBytes = IOUtils.toByteArray(input);
            String encodedImage = Base64.encodeBase64String(imageBytes);
            String obj = "{\"image\" : \"" + encodedImage + "\"}";




//            System.out.println(encodedImage);

//            byte[] data = Base64.decodeBase64(encodedImage);
//            OutputStream stream = new FileOutputStream(new File("test.png"));
//            stream.write(data);
//            stream.close();

            if (endPoint != null) {
                final ClientConfig config = new DefaultClientConfig();
                final Client client = Client.create(config);
                final WebResource resource = client.resource(endPoint);
                final ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                        .post(ClientResponse.class, obj);

                // check response status code
                if (response.getStatus() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatus());
                }

                // display response
                String output = response.getEntity(String.class);
                LOGGER.debug("Output from Server .... ");
                LOGGER.debug(output + "\n");
                message = "{\"message\" : \" Query snapshot sent to " + endPoint + "\"}";
                return message;
            } else {
                LOGGER.debug("Query snapshot sent in response");
                LOGGER.debug("Q Emage in base64 encoded string is :- " + encodedImage);
                return obj;
            }

        }catch(Exception ex){
            ex.printStackTrace();
            message = "{\"message\" : \" Exception occured.. " + ex.toString() + "\"}";
            return message;
        }


    }


    public File TakeSnapshot(int id, String type) {

//        System.setProperty("webdriver.chrome.driver", "/home/abhisekmohanty/Desktop/chromedriver");

        WebDriver driver = new FirefoxDriver();

        driver.get(Config.getProperty("snapshotURL") + id);

        WebElement element = driver.findElement(By.id("map"));

        File screen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        org.openqa.selenium.Point p = element.getLocation();

        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();

        try {
            BufferedImage img = ImageIO.read(screen);

            BufferedImage dest = img.getSubimage(p.getX(), p.getY(), width,
                    height);

            ImageIO.write(dest, "png", screen);

            long currentTime = System.currentTimeMillis();

            File f = new File(Config.getProperty("tempDir")+"/query_images/"+type+id+"_"+currentTime+".png");

            FileUtils.copyFile(screen, f);

        } catch (IOException e) {
            e.printStackTrace();
        }
        driver.quit();
        return screen;
    }

//    public static void main(String[] args) {
//        SnapWebService snapWebService = new SnapWebService();
//        snapWebService.GetSnapshot();
//    }
}
