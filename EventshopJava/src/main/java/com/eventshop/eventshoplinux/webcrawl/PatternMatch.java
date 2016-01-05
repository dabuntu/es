package com.eventshop.eventshoplinux.webcrawl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aravindh on 6/16/15.
 */
public class PatternMatch {
    public static void main(String args[]){
        String patternString ="AQI - Particle Pollution \\(2.5 microns\\)";
        String matchString="Location: Atascadero, CA Current Air Quality: 06/15/15 11:00 PM PDT Good - 26 AQI - Particle Pollution (10 microns) Good - 17 AQI - Ozone Good - 25 AQI - Particle Pollution (2.5 microns) Agency: San Luis Obispo County APCD Last Update: Mon, 15 Jun 2015 11:45:02 PDT";
        String patternLocation="Location:";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(matchString);

        Pattern pattern3 = Pattern.compile(patternLocation);
        Matcher matcher3= pattern3.matcher(matchString);

        Pattern patternCA = Pattern.compile(" CA");
        Matcher matcherCA = patternCA.matcher(matchString);

        int locationBeginIndex=0, locationEndIndex=0;
        String finalLocation;

        int foundIndex=0;
        if (matcher.find()) {
            String subString = matchString.substring(0,matcher.start());
            Pattern pattern1 = Pattern.compile("-");
            Matcher matcher1 = pattern1.matcher(subString);
            while(matcher1.find()){
                foundIndex=matcher1.start();
            }
            String finalString = subString.substring(foundIndex+1,matcher.start());
            finalString=finalString.replace(" ","");
            System.out.println("Final Extracted output: "+ finalString );
            if(matcher3.find()){
                locationBeginIndex=matcher3.end();
              }
            if(matcherCA.find()){
                locationEndIndex = matcherCA.start();
            }
            finalLocation = matchString.substring(locationBeginIndex + 1, locationEndIndex - 1);
            System.out.println(finalLocation);

        }
    }

}
