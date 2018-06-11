package com.ztdx.eams.basic.utils;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

import java.io.File;
import java.net.ConnectException;

/**
 * Created by li on 2018/6/7.
 */
public class PDFConverterUtils {

    public void createPdf() {
        String docpath = "C:\\Users\\li\\Desktop\\P9.5.2-TRN-档案中心-20170531-V1.0.doc";
        String pdfpath = "C:\\Users\\li\\Desktop\\P9.5.2-TRN-档案中心-20170531-V1.0.pdf";
        File inputFile = new File(docpath);
        File outputFile = new File(pdfpath);

        OpenOfficeConnection connection = new SocketOpenOfficeConnection(8100);
        try {
            connection.connect();
        } catch (ConnectException e) {
            e.printStackTrace();
        }

        DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
        converter.convert(inputFile, outputFile);

        connection.disconnect();
    }

}