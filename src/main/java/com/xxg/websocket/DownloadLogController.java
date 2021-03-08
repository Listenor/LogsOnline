package com.xxg.websocket;

import com.xxg.websocket.util.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

@WebServlet("/downloadLogFile")
public class DownloadLogController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        //中文名可能有问题。以后再看了
        String fileName = req.getParameter("fileName");
        String compressedParam = req.getParameter("compressed");
        boolean compressed = "true".equals(compressedParam);

        String logsDir = (String) session.getAttribute(Const.logsDir);
        String AllFileNames = (String)session.getAttribute(Const.AllFilesNameStr);

        if(null == logsDir || null == AllFileNames){
            PrintStream out = new PrintStream(resp.getOutputStream());
            resp.setCharacterEncoding("utf-8");
            resp.setContentType("application/json;charSet=utf-8");
            out.write("非法路径".getBytes("utf-8"));
            out.close();
        }else if (null == fileName || "".equals(fileName) || AllFileNames.indexOf(fileName)==-1){
            PrintStream out = new PrintStream(resp.getOutputStream());
            resp.setCharacterEncoding("utf-8");
            resp.setContentType("application/json;charSet=utf-8");
            out.write("文件名称不对，找不到此文件".getBytes("utf-8"));
            out.close();
        }else{

            File file = new File(logsDir+File.separator+fileName);

            if (compressed) {
                Utils.sendCompressedFile(resp, file);
            } else {
                Utils.sendFile(req, resp, file);
            }
        }



    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }
}
