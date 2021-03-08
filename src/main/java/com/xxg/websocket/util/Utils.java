/**
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
package com.xxg.websocket.util;



import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Misc. static helper methods.
 */
public final class Utils {

  /**
   * Send file.
   *
   * @param request the request
   * @param response the response
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void sendFile(HttpServletRequest request, HttpServletResponse response, File file)
      throws IOException {

    try (OutputStream out = response.getOutputStream();
        RandomAccessFile raf = new RandomAccessFile(file, "r")) {
      long fileSize = raf.length();
      long rangeStart = 0;
      long rangeFinish = fileSize - 1;

      // accept attempts to resume download (if any)
      String range = request.getHeader("Range");
      if (range != null && range.startsWith("bytes=")) {
        String pureRange = range.replace("bytes=", "");
        int rangeSep = pureRange.indexOf('-');

        try {
          rangeStart = Long.parseLong(pureRange.substring(0, rangeSep));
          if (rangeStart > fileSize || rangeStart < 0) {
            rangeStart = 0;
          }
        } catch (NumberFormatException e) {
          // keep rangeStart unchanged
          //logger.trace("", e);
          e.printStackTrace();
        }

        if (rangeSep < pureRange.length() - 1) {
          try {
            rangeFinish = Long.parseLong(pureRange.substring(rangeSep + 1));
            if (rangeFinish < 0 || rangeFinish >= fileSize) {
              rangeFinish = fileSize - 1;
            }
          } catch (NumberFormatException e) {
            //logger.trace("", e);
            e.printStackTrace();
          }
        }
      }

      // set some headers
      response.setContentType("application/x-download");
      response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
      response.setHeader("Accept-Ranges", "bytes");
      response.setHeader("Content-Length", Long.toString(rangeFinish - rangeStart + 1));
      response.setHeader("Content-Range",
          "bytes " + rangeStart + "-" + rangeFinish + "/" + fileSize);

      // seek to the requested offset
      raf.seek(rangeStart);

      // send the file
      byte[] buffer = new byte[4096];

      long len;
      int totalRead = 0;
      boolean nomore = false;
      while (true) {
        len = raf.read(buffer);
        if (len > 0 && totalRead + len > rangeFinish - rangeStart + 1) {
          // read more then required?
          // adjust the length
          len = rangeFinish - rangeStart + 1 - totalRead;
          nomore = true;
        }

        if (len > 0) {
          out.write(buffer, 0, (int) len);
          totalRead += len;
          if (nomore) {
            break;
          }
        } else {
          break;
        }
      }
    }
  }

  /**
   * Gets the thread by name.
   *
   * @param name the name
   * @return the thread by name
   */
  public static Thread getThreadByName(String name) {
    if (name != null) {
      // get top ThreadGroup
      ThreadGroup masterGroup = Thread.currentThread().getThreadGroup();
      while (masterGroup.getParent() != null) {
        masterGroup = masterGroup.getParent();
      }

      Thread[] threads = new Thread[masterGroup.activeCount()];
      int numThreads = masterGroup.enumerate(threads);

      for (int i = 0; i < numThreads; i++) {
        if (threads[i] != null && name.equals(threads[i].getName())) {
          return threads[i];
        }
      }
    }
    return null;
  }



  /**
   * Send compressed file.
   *
   * @param response the response
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void sendCompressedFile(HttpServletResponse response, File file)
      throws IOException {
    try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream());
        InputStream fileInput = new BufferedInputStream(Files.newInputStream(file.toPath()))) {

      String fileName = file.getName();

      // set some headers
      response.setContentType("application/zip");
      response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");

      zip.putNextEntry(new ZipEntry(fileName));

      // send the file
      byte[] buffer = new byte[4096];
      long len;

      while ((len = fileInput.read(buffer)) > 0) {
        zip.write(buffer, 0, (int) len);
      }
      zip.closeEntry();
    }
  }
}
