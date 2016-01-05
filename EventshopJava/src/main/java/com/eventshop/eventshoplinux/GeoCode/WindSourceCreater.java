package com.eventshop.eventshoplinux.GeoCode;

import java.io.*;

/**
 * Created by aravindh on 6/11/15.
 */
public class WindSourceCreater {

    public static void main(String args[]){

       try{
           BufferedReader br = new BufferedReader(new FileReader("/home/aravindh/new.txt"));
           String line = "";
           int cnt=0;
           String[] lineSplit = new String[4];
           BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                   "/home/aravindh/WindSpeed/northwest.file"), true));
            float windDirDeg;
           while ((line = br.readLine()) != null) {
                lineSplit=line.split(",");
               if(lineSplit.length  == 4){
                   windDirDeg=Float.parseFloat(lineSplit[3]);
                   if (windDirDeg > (315-22.5) && windDirDeg < (315+22.5)){
                       bw.write(lineSplit[0]+","+lineSplit[1]+","+lineSplit[2]+","+lineSplit[3]);
                       bw.newLine();
                  //     System.out.println(line);
                   cnt++;
                   }
               }
               else{
                   //System.out.println("Not enough fields..");
               }
           }
           bw.close();
           br.close();
          // System.out.println("cnt:"+cnt);
       }catch(Exception ex){

       }

    }
}
