package amoj.service;

import amoj.entity.ExecMsg;
import amoj.entity.Result;
import amoj.entity.Stdout;
import amoj.entity.Submit;
import amoj.util.ExecUtil;
import amoj.util.PropertiesUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.logging.Logger;

@Service
public class JudgeService {
    private static Logger log = Logger.getLogger("judge");

    public static final String REDIS_KEY_RESULT ="amoj_result:queue";

    @Autowired
    private RedisTemplate redisTemplate;

    @Async("asyncServiceExecutor")
    public void judge(Submit submit){
        log.info("judge submit " + submit);
        /*
          (submitId=1,
          problemId=1000,
          language=1,
          userId=223,
          submitTime=Thu Jan 01 08:00:00 GMT+08:00 1970,
          source=printf(a+b),
          time=1000,
          mem=1000,
          resultId=1)
         */
        Result result = new Result();
        result.setSubmitId(submit.getSubmitId());
        //创建存放代码的临时文件夹
        String path = PropertiesUtil.StringValue("source_temp")+"/"+submit.getSubmitId();
        File file = new File(path);
        file.mkdir();
        File sourceFile = new File(path + "/" + sourceFileName(submit.getLanguage()));
        try {
            sourceFile.createNewFile();
            OutputStream output = new FileOutputStream(sourceFile);
            PrintWriter writer = new PrintWriter(output);
            writer.print(submit.getSource());
            writer.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //安全检查
        if (!verify(submit.getSource())) {
            result.setResult(PropertiesUtil.RESULT[9]);
            ExecUtil.exec("rm -rf " + path);
            log.info("push result :" + result);
            redisTemplate.opsForList().leftPush(REDIS_KEY_RESULT, result);
            return;
        }

        //编译
        String errorMsg = ExecUtil.exec(compileCmd(submit.getLanguage(), path))
                .getError();
        if(errorMsg != null){
            result.setResult(PropertiesUtil.RESULT[7]);
            result.setResultMsg(errorMsg);
        }
        ExecUtil.exec("chmod -R 777" + path);

        //judge
        String process = process(submit.getLanguage(), path);
        String judge_data = PropertiesUtil.StringValue("judge_data") + "/" + submit.getProblemId();
        /*
        python脚本参数:
            sudo python
            脚本地址   /home/judge.py
            (编译命令)源文件地址 process    /home/source/1/main
            数据地址 /home/amoj_data/1000
            path  /home/source/1
            时间限制
            空间限制
         */
        String judgeCmd = "python " +
                PropertiesUtil.StringValue("python_script") + " " +
                process + " " +
                judge_data + " " +
                path + " " +
                submit.getTime() + " " +
                submit.getMem();
        log.info("cmd "+judgeCmd);
        ExecMsg judgeMsg = ExecUtil.exec(judgeCmd);
        log.info("judge msg : "+judgeMsg);
        if(judgeMsg.getError() !=null){
            result.setResult(PropertiesUtil.RESULT[5]);
            result.setResultMsg(judgeMsg.getError());
            log.info("=========" + result.getSubmitId() + ":" +result.getResultMsg());
        }
        else{
            Stdout out = JSON.parseObject(judgeMsg.getInput(), Stdout.class);
            log.info("=====stdout====" + out);
            result.setResult(PropertiesUtil.RESULT[out.getStatus()]);
            result.setUsedTime(out.getMax_time().longValue());
            result.setUsedMem(out.getMax_memory().longValue());
        }
        ExecUtil.exec("rm -rf " + path);
        log.info("push result :" + result);
        redisTemplate.opsForList().leftPush(REDIS_KEY_RESULT, result);

    /*
    1、创建临时文件夹，创建文件，存放源代码
    2、修改文件夹、文件权限
    3、验证代码安全性
    4、编译源代码
    5、lorm模块运行代码
     */
    }

    private static boolean verify(String source) {
        String[] keys = PropertiesUtil.StringValue("dangerousKeys").split(",");
        for (String key : keys) {
            if (source.contains(key)) {
                return false;
            }
        }
        return true;
    }

    public String compileCmd(int language, String path)
    {
        String cmd = "";
        switch (language) {
            case 1:
                cmd = "gcc " + path + "/main.c -o " + path + "/main -w -lm";
                break;
            case 2:
                cmd = "g++ " + path + "/main.cpp -o " + path + "/main -w -lm";
                break;
            case 3:
                cmd = "javac " + path + "/Main.java";
                break;
        }
        return cmd;
    }

    public String sourceFileName(int language)
    {
        String name ="";
        switch (language) {
            case 1:
                name = "main.c";
                break;
            case 2:
                name = "main.cpp";
                break;
            case 3:
                name = "Main.java";
                break;
        }
        return name;
    }

    private static String process(int language, String path) {
        switch (language) {
            case 1:
                return path + "/main";
            case 2:
                return path + "/main";
            case 3:
                return "javaseast-classpathseast" + path + "seastMain";        }
        return null;
    }
}
