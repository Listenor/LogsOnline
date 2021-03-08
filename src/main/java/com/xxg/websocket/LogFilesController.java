package com.xxg.websocket;

import com.xxg.websocket.util.SizeExpression;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "logFiles",urlPatterns = {"/logFiles"})
public class LogFilesController extends HttpServlet {




    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        boolean readResult = readAndSaveLogsInfoToSession(session);
        int code = 500;
        String msg = "";

        if(readResult){
            File[] logFiles = getLogFiles(session);
            if(null != logFiles && logFiles.length>0){
                code = 200;
                msg = getLogFilesJson(logFiles,session);
            }else{
                msg = "目录："+(String) session.getAttribute(Const.logsDir)+"没有日志文件";
                //重置目录
                //session.setAttribute(Const.logsDir,null);
            }
        }else{
            msg = "日志文件目录错误：请检查：系统是否就Linux,配置文件配置的路径是否有误";
        }
        PrintStream out = new PrintStream(resp.getOutputStream());
        resp.setCharacterEncoding("utf-8");
        //resp.setHeader("Content-Type", "text/html;charset=utf-8");
        resp.setContentType("application/json;charSet=utf-8");
        out.write(returnJson(code,msg).getBytes("UTF-8"));

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        PrintStream out = new PrintStream(resp.getOutputStream());
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charSet=utf-8");
        //resp.setHeader("Content-Type", "text/html;charset=utf-8");
        out.write(returnJson(500,"请使用get请求").getBytes("UTF-8"));
    }
    //缓存logs目录路径
    public boolean readAndSaveLogsInfoToSession(HttpSession session){
        boolean result = false;

        if(null != session.getAttribute(Const.logsDir)){
            result = true;
        }else{
            String path = LogFilesController.class.getResource("/").getPath();
            System.out.println("path:"+path);
            String customFilePath = (path.replace("/build/classes", "").replace("%20"," ").replace("classes/", "") + "custom.properties").replaceFirst("/", "");
            System.out.println ("websiteURL:"+customFilePath);

            File customFile = new File(customFilePath);
            if(customFile.exists()){
               try {
                   Properties ps = new Properties();
                   ps.load(new FileInputStream(customFile));
                   String customLogsDir = ps.getProperty(Const.logsDir);
                   if(null!= customLogsDir && !"".equals(customLogsDir)){
                       File logsDir = new File(customLogsDir);
                       if(logsDir.exists() && logsDir.isDirectory()){
                           session.setAttribute(Const.logsDir,customLogsDir);
                           result = true;
                       }
                   }
               }catch (Exception e){
                   e.printStackTrace();
               }

            }
            //如果配置文件没有需要的值，指定默认tomcat的值
            if(!result){
                String tomcatHome = System.getProperty("catalina.home");
                if(null != tomcatHome){
                    String tomcatLogsDir = tomcatHome  + File.separator+"logs";
                    System.out.println(tomcatLogsDir);
                    session.setAttribute(Const.logsDir,tomcatLogsDir);
                    result = true;

                }

            }
        }
        return result;
    }

    public File[] getLogFiles(HttpSession session){

        String logsDirPath = (String) session.getAttribute(Const.logsDir);
        File logsDir = new File(logsDirPath);
        File[] logFiles = logsDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile() && isNotBakFile(pathname.getName());
            }
        });

        if(null != logFiles){
            Arrays.sort(logFiles, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    if(o1.lastModified()>o2.lastModified()){
                        return -1;
                    }else if(o1.lastModified()<o2.lastModified()){
                        return 1;
                    }else{
                        return 0;
                    }
                }
            });
        }

        return logFiles;

    }

    public boolean isNotBakFile(String fileName){

        Pattern pattern = Pattern.compile("[0-9]{4}[-][0-9]{1,2}[-][0-9]{1,2}");
        Matcher matcher = pattern.matcher(fileName);
        if(matcher.find()){
            return false;
        }else{
            return true;
        }

    }

    public String getLogFilesJson(File[] files ,HttpSession session){

        StringBuffer AllFileNamesStr = new StringBuffer();

        StringBuffer json = new StringBuffer();
        json.append("[");

        for (File file:files ) {
            json.append("{");
            json.append("\"fileName\":").append("\"").append(file.getName()).append("\"").append(",")
                    .append("\"size\":").append(file.length()).append(",")
                    .append("\"sizeText\":").append("\"").append(SizeExpression.format(file.length(),0,true)).append("\"").append(",")
                    .append("\"lastModified\":").append("\"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()))).append("\"");
                    /*.append("'':").append("'").append().append("'")
                    .append("'':").append("'").append().append("'") */

            json.append("},");

            AllFileNamesStr.append(file.getName()+",");

        }
        //删除最后的逗号
        json.deleteCharAt(json.length() - 1);
        json.append("]");
        session.setAttribute(Const.AllFilesNameStr,AllFileNamesStr.toString());
        return json.toString();
    }


    public String returnJson(int code,String data){
        if(data.indexOf("[")!=1 && data.indexOf("{")!=1){
            data = "\""+data+"\"";
        }
        String str = "{\"code\":"+code+",\"data\":"+data+"}";
        System.out.println(str);
         return str;
    }


}
