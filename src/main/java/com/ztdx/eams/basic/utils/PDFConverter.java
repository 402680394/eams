package com.ztdx.eams.basic.utils;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.ztdx.eams.basic.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.ConnectException;

/**
 * Created by li on 2018/6/7.
 */
@Component
public class PDFConverter {

    //端口号
    @Value("${openoffice.port}")
    private int OPENOFFICE_PORT;

    public void converterPDF(File inputFile, File outputFile) {

        OpenOfficeConnection connection = new SocketOpenOfficeConnection(OPENOFFICE_PORT);
        try {
            connection.connect();
        } catch (ConnectException e) {
            throw new BusinessException("未连接到openoffice服务");
        }

        DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
        converter.convert(inputFile, outputFile);

        connection.disconnect();
    }

}