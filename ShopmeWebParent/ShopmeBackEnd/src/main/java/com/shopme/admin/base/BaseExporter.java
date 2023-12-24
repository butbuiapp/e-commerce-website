package com.shopme.admin.base;

import jakarta.servlet.http.HttpServletResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseExporter {

    public void setResponseHeader(HttpServletResponse response,
                                  String contentType,
                                  String prefix,
                                  String fileExtension) {
        // create file name
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentTime = dateFormat.format(new Date());
        String fileName = prefix + currentTime + fileExtension;

        response.setContentType(contentType);
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + fileName;
        response.setHeader(headerKey, headerValue);
    }
}
