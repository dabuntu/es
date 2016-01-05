package com.eventshop.eventshoplinux.camel.queryProcessor;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.domain.datasource.emage.Message;
import com.eventshop.eventshoplinux.model.Emage;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by nandhiniv on 5/20/15.
 */
public class CommonQueryUtil {


    protected Log log = LogFactory.getLog(this.getClass().getName());

    public static int checkOS() {
        String currentOs = System.getProperty("os.name").toLowerCase();
        int os;
        if (currentOs.contains("nix") || currentOs.contains("nux")
                || currentOs.contains("aix")) {
            os = 0;
        } else if (currentOs.contains("win")) {
            os = 1;
        } else {
            os = -1;
        }
        return os;
    }

    public List<String> getQueriedSources(JsonObject query) {
        List<String> queriedSources = new ArrayList<String>();

        if (query.get("dataSources") != null) {
            JsonArray dataSources = query.get("dataSources").getAsJsonArray();
            ArrayList<String> tempList = new ArrayList<String>();
            if (dataSources != null) {
                for (int j = 0; j < dataSources.size(); j++) {
                    tempList.add(dataSources.get(j).getAsString().toString());
                }
            }
            for (String s : tempList) {
                if (!queriedSources.contains(s)) {
                    queriedSources.add(s);
                }
            }
        }
        return queriedSources;
    }

    public String createColourString(JsonObject query) {
        String colorStr = "";
        if (query.get("colorCodes") != null) {
            JsonArray colors = query.get("colorCodes").getAsJsonArray();
            for (int j = 0; j < colors.size(); j++) {
                String color = colors.get(j).getAsString().trim().toLowerCase();
                colorStr += "\\\"" + color + "\\\",";
            }
            if (!colorStr.isEmpty())
                colorStr = colorStr.substring(0, colorStr.length() - 1);
        }
        return colorStr;
    }

    public void compileCppCode(String masterQueryID, String subQueryID, List<String> queriedSources, List<String> queryCC, String colorStr) {

        String[] compile = null;
        String outputQueryID = "Q" + String.valueOf(masterQueryID) + "_" + subQueryID;
        String run = "";
        int OS = checkOS();


        if (OS == 0) {
            createCCProcessLinux(queriedSources, queryCC, outputQueryID, masterQueryID, colorStr);

            String[] tcompile = {"sh", Config.getProperty("context") + "proc/Debug/compile.sh",
                    outputQueryID, "EmageOperators_" + outputQueryID};
            // String[] tcompile = {"/bin/sh", "cd",
            // "/usr/share/tomcat7/webapps/eventshoplinux/proc/Debug"};
            compile = tcompile;
            log.info("Execution file: " + run);
        } else if (OS == 1) {
            createCCProcessWindows(queriedSources, queryCC, outputQueryID, masterQueryID, colorStr);

            String[] tcompile = {
                    "\"" + Config.getProperty("context").replace("\\", "\\\\") + "proc\\Debug\\compile.bat\"", outputQueryID,
                    "EmageOperators_" + outputQueryID};
            compile = tcompile;
            run = "\"" + Config.getProperty("context").replace("\\", "\\\\") + "proc\\Debug\\EmageOperators_" + outputQueryID
                    + ".exe\"";
        }

        try {
            if (compile != null) {
                compileQuery(compile);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private void compileQuery(String[] compile) throws IOException, InterruptedException {
        // Compile the query

        Process proc = Runtime.getRuntime().exec(compile);
        BufferedReader bri = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader bre = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        String errLine;
        log.info("Compiling........");
        while ((errLine = bre.readLine()) != null) {
            log.info(errLine);
        }
        String line;
        while ((line = bri.readLine()) != null) {
            log.info(line);
        }
        proc.destroy();
        proc.waitFor();
        log.info("Compiled!");
    }

    public void runQuery(String run) throws IOException, InterruptedException {
        // Run the query
        log.info("Trying to RUN: " + run);
        Process proc = Runtime.getRuntime().exec(run);


        BufferedReader bri = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader bre = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        String line;
        while ((line = bri.readLine()) != null) {
            log.info(line);
        }
        bri.close();
        while ((line = bre.readLine()) != null) {
            log.info(line);
        }
        bre.close();
        proc.destroy();
        proc.waitFor();
        log.info("Finished Running!");
    }

    public void createCCProcessLinux(List<String> queriedSources, List<String> queryCC, String outputQueryName, String masterQueryID,
                                     String colors) {

        DataSourceManagementDAO dataSourceManagementDAO = new DataSourceManagementDAO();
        List<DataSource> queriedDSSources = new ArrayList<DataSource>();
        List<String> querySources = new ArrayList<String>();

        for (String id : queriedSources) {
            if (id.startsWith("ds")) {
                queriedDSSources.add(dataSourceManagementDAO.getDataSource(Integer.parseInt(id.substring(2))));
            } else if (id.startsWith("q") || id.startsWith("Q")) {
                querySources.add(id);
            }
        }

        // Create C++ code and process to start
        String cc = "#include \"spatial_analysis.h\"\n"
                + "#include \"temporal_analysis.h\"\n\n"
                + "#include <ctime>\n\n" + "#include <unistd.h>\n\n" + "#include <cv.h>\n\n"
                + "using namespace cv;\n\n" + "int main()\n" + "{\n";
        for (int i = 0; i < queriedDSSources.size(); i++) {
            DataSource src = queriedDSSources.get(i);

            // String filename = "\""+ dataFolderPath + "ds" + src.srcID + "_" +
            // src.srcName + "\"";
            String filename = "\"" + Config.getProperty("tempDir") + "ds" + src.srcID + "\"";

            if (src.srcVarName == null)
                cc += ("\tEmageIngestor ds" + src.srcID + "(" + filename + ");\n");
            else
                cc += ("\tEmageIngestor " + src.srcVarName + "(" + filename + ");\n");
        }

        for (String querySrc : querySources) {
            ;
            String filename = "\"" + Config.getProperty("tempDir") + "queries/" + "Q" + masterQueryID + "_" + querySrc.substring(1) + "\"";

            cc += ("\tEmageIngestor " + "Q" + masterQueryID + "_" + querySrc + "(" + filename + ");\n");
        }

        for (int i = 0; i < queryCC.size(); ++i) {
            log.info("last sub query -> outputQueryName " + outputQueryName + "_"
                    + (queryCC.size() - 1));
            if (i == queryCC.size() - 1) {// root op
                String qCode = queryCC.get(i);
                qCode = qCode.replace(outputQueryName + "_" + (i + 1), outputQueryName);
                cc += qCode;
                log.info("replace id of last subquery " + outputQueryName + "_"
                        + (i + 1) + " with masterqid ");
            } else {

                cc += queryCC.get(i);
            }
        }

        cc += (
//                "\twhile(true)\n" + "\t{\n" +
                "\t\twhile(" + outputQueryName
                        + ".has_next())\n" + "\t\t{\n" + "\t\t\tEmage e = " + outputQueryName + ".next();\n");

        // Decide how to show outputs
//        String outputName = outputQueryName;// + "_" + queryType;
        cc += ("\t\t\tcreate_output(e, \"" + Config.getProperty("context").replace("\\", "/")
                + Constant.RESULT_Q + outputQueryName + "\",\"" + colors + "\");\n");

//        cc += ("\t\t}\n" + "\t\tsleep(100);\n" + // (CAP "S") Sleep(100) for
//                // windows, but sleep(100)
//                // for linux
        cc += ("\t\t}\n" + "\treturn 0;\n" + "}\n");
        String ccPath = Config.getProperty("context") + "proc/src/" + outputQueryName + ".cc";
        log.info("ccPath " + ccPath);
        try {
            FileOutputStream ous = new FileOutputStream(ccPath);
            ous.write(cc.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createCCProcessWindows(List<String> queriedSources, List<String> queryCC, String outputQueryName, String masterQueryID,
                                       String colors) {

        DataSourceManagementDAO dataSourceManagementDAO = new DataSourceManagementDAO();
        List<DataSource> queriedDSSources = new ArrayList<DataSource>();
        List<String> querySources = new ArrayList<String>();

        for (String id : queriedSources) {
            if (id.startsWith("ds")) {
                queriedDSSources.add(dataSourceManagementDAO.getDataSource(Integer.parseInt(id.substring(2))));
            } else if (id.startsWith("q") || id.startsWith("Q")) {
                querySources.add(id);
            }
        }
        // Create C++ code and process to start
        String cc = "#include \"spatial_analysis.h\"\n"
                + "#include \"temporal_analysis.h\"\n\n"
                + "#include <ctime>\n\n" + "#include <unistd.h>\n\n"
                + "using namespace cv;\n\n" + "int main()\n" + "{\n";
        for (int i = 0; i < queriedDSSources.size(); i++) {
            DataSource src = queriedDSSources.get(i);

            // String filename = "\""+ dataFolderPath + "ds" + src.srcID + "_" +
            // src.srcName + "\"";
            String filename = "\"" + Config.getProperty("tempDir") + "ds" + src.srcID + "\"";

            if (src.srcVarName == null)
                cc += ("\tEmageIngestor ds" + src.srcID + "(" + filename + ");\n");
            else
                cc += ("\tEmageIngestor " + src.srcVarName + "(" + filename + ");\n");
        }

        for (int i = 0; i < queryCC.size(); ++i) {
            cc += queryCC.get(i);
        }

        cc += (
//                "\twhile(true)\n" + "\t{\n" +
                "\t\twhile(" + outputQueryName
                        + ".has_next())\n" + "\t\t{\n" + "\t\t\tEmage e = " + outputQueryName + ".next();\n");

        // Decide how to show outputs
//        outputName = queryID;// + "_" + queryType;
        cc += ("\t\t\tcreate_output(e, \"" + Config.getProperty("context").replace("\\", "\\\\")
                + Constant.RESULT_Q.replace("\\", "\\\\") + outputQueryName + "\",\"" + colors + "\");\n");

//        cc += ("\t\t}\n" + "\t\tSleep(100);\n" + "\t}\n" + "\treturn 0;\n"
//                + "}\n");
        cc += ("\t\t}\n" + "\treturn 0;\n" + "}\n");
        String ccPath = Config.getProperty("context").replace("\\", "\\\\") + "proc\\src\\" + outputQueryName + ".cc";
        try {
            FileOutputStream ous = new FileOutputStream(ccPath);
            ous.write(cc.getBytes());
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void createBinAndFinalEmage(String masterQueryID, String queryID) {

        try {
            String queryResultFileName = Config.getProperty("context") + "temp/queries/Q" + masterQueryID + "_" + queryID;
            File source = new File(queryResultFileName + ".json");
            readEmageFileAndCreateBin(source, queryResultFileName);
            source.renameTo(new File(Config.getProperty("context") + "temp/queries/Q" + masterQueryID + ".json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void readEmageFileAndCreateBin(File source, String queryResultFileName) throws IOException {
        String content = null;
        content = new Scanner(source).useDelimiter("\\Z").next();
        ObjectMapper mapper = new ObjectMapper();
        Emage emage = mapper.readValue(content, Emage.class);
        createBinFile(emage, queryResultFileName);
    }

    public void createBinFile(Emage emage, String destFilename) {
        Message.EmageMsg.Builder builder = Message.EmageMsg
                .newBuilder().setTheme(emage.getTheme())
                .setStartTime(emage.getStartTime())
                .setEndTime(emage.getEndTime())
                .setLatUnit(emage.getLatUnit())
                .setLongUnit(emage.getLongUnit())
                .setSwLat(emage.getSwLat())
                .setSwLong(emage.getSwLong())
                .setNeLat(emage.getNeLat())
                .setNeLong(emage.getNeLong())
                .setNumRows(emage.getRow())
                .setNumCols(emage.getCol());
        // Get the cell values and add it to emage
        double[] cells = emage.getImage();
        int cnt = 0;
        for (int i = 0; i < emage.getRow(); i++) {
            for (int j = 0; j < emage.getCol(); j++) {
//                                        if (cells[cnt] == 0.0) {
//                                            builder.addCell(Double.NaN);
//                                        } else {
                builder.addCell(cells[cnt]);
//                                        }
                cnt++;
            }

        }

        // Build the Message
        Message.EmageMsg msg = builder.build();

        // Output the data
        byte[] data = msg.toByteArray();
        byte[] size = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN).putInt(data.length)
                .array();
        ByteArrayOutputStream result = new ByteArrayOutputStream(
                data.length + 4);
        try {
            result.write(size);
            result.write(data);
        } catch (IOException e1) {
            log.error(e1.getMessage());
        }
        String filepath = destFilename;
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(filepath);
        } catch (FileNotFoundException e1) {
            log.error(e1.getMessage());
        }

        // Lock the file for writing
        FileLock lock = null;
        try {
            while (lock == null)
                lock = output.getChannel().tryLock();
            output.write(result.toByteArray());
            output.flush();
            lock.release();

            // 08/19/2011 Mingyan
            output.close();
        } catch (IOException e1) {
            log.error(e1.getMessage());
            // add by Siripen, to solve runnning method calling twice
            // from the front UI, cause this locking throw an error
            // will need to resolve this issue later
        }
    }

}

