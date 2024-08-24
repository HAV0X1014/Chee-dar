package Cheedar;

import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public void logSpeed(float speed) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(new Date());
        File output = new File("logs/" + formattedDate + ".json");
        File logDir = new File("logs/");

        JSONObject newData = new JSONObject();
        newData.put("Speed",speed);
        newData.put("Date",formattedDate);
        newData.put("Time",new SimpleDateFormat("hh:mm a").format(new Date()));

        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        //create the log dir if it doesn't exist already - safety check.
        if (output.exists()) {
            StringBuilder fullFile = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(output))) {
                String line;
                while ((line = br.readLine()) != null) {
                    fullFile.append(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //read the existing file, cast it to a json object, then add our new data to the end.
            JSONObject fileJson = new JSONObject(fullFile.toString());
            int runNumber = fileJson.length() + 1;
            fileJson.put(String.valueOf(runNumber),newData);
            try {
                FileWriter fw = new FileWriter(output);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(fileJson.toString(1));
                bw.flush();
                bw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                JSONObject newFileData = new JSONObject();
                newFileData.put("1",newData);
                //the newData needs to be added to a "wrapper" JSONObject so it is enumerable.
                FileWriter fw = new FileWriter(output);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(newFileData.toString(1));
                bw.flush();
                bw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
