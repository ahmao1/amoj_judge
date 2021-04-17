package amoj.util;

import amoj.entity.ExecMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecUtil {

    public static ExecMsg exec(String cmd) {
        Runtime runtime = Runtime.getRuntime();
        Process exec = null;
        try {
            exec = runtime.exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
            return new ExecMsg(e.getMessage(), null,null);
        }
        ExecMsg msg = new ExecMsg();
        msg.setError(message(exec.getErrorStream()));
        msg.setInput(message(exec.getInputStream()));
        return msg;
    }

    private static String message(InputStream inputStream) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            StringBuilder message = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                message.append(str);
            }
            String result = message.toString();
            if (result.equals("")) {
                return null;
            }
            return result;
        } catch (IOException e) {
            return e.getMessage();
        } finally {
            try {
                inputStream.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
