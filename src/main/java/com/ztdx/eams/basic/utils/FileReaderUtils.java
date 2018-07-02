package com.ztdx.eams.basic.utils;

import com.ztdx.eams.basic.exception.BusinessException;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;

/**
 * Created by li on 2018/6/6.
 */
public class FileReaderUtils {

    /**
     * 读取txt文件的内容
     */
    public static String txtRead(File file) {
        String content = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, "GBK");
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                content = content + "\n" + line;
            }
        } catch (Exception e) {
            throw new BusinessException("txt文件读取失败");
        } finally {
            try {
                br.close();
                isr.close();
                fis.close();
            } catch (IOException e) {
                throw new BusinessException("文件读取流未关闭");
            }
        }
        return content;
    }

    /**
     * 读取doc文件内容(文本)
     */
    public static String docRead(File file) {
        String content = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            WordExtractor extractor = new WordExtractor(fis);
            content += extractor.getText();
        } catch (Exception e) {
            throw new BusinessException("doc文件读取失败");
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new BusinessException("文件读取流未关闭");
            }
        }
        return content;
    }

    /**
     * 读取docx文件(文本)
     */
    public static String docxRead(File file) {
        String content = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            XWPFDocument docx = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
            content += extractor.getText();
        } catch (Exception e) {
            throw new BusinessException("docx文件读取失败");
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new BusinessException("文件读取流未关闭");
            }
        }
        return content;
    }

    /**
     * 读取ppt文件(文本)
     */
    public static String pptRead(File file) {
        String content = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            PowerPointExtractor extractor = new PowerPointExtractor(fis);
            content += extractor.getText();
        } catch (Exception e) {
            throw new BusinessException("ppt文件读取失败");
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new BusinessException("文件读取流未关闭");
            }
        }
        return content;
    }

    /**
     * 读取pptx文件(文本)
     */
    public static String pptxRead(File file) {
        String content = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            XMLSlideShow xlsx = new XMLSlideShow(fis);
            XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(xlsx);
            content += extractor.getText();
        } catch (Exception e) {
            throw new BusinessException("pptx文件读取失败");
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new BusinessException("文件读取流未关闭");
            }
        }
        return content;
    }

    /**
     * 读取xls文件(文本)
     */
    public static String xlsRead(File file) {
        String content = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ExcelExtractor extractor = new ExcelExtractor(new POIFSFileSystem(fis));
            content += extractor.getText();
        } catch (Exception e) {
            throw new BusinessException("xls文件读取失败");
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new BusinessException("文件读取流未关闭");
            }
        }
        return content;
    }

    /**
     * 读取xlsx文件(文本)
     */
    public static String xlsxRead(File file) {
        String content = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            XSSFWorkbook xlsx = new XSSFWorkbook(fis);
            XSSFExcelExtractor extractor = new XSSFExcelExtractor(xlsx);
            content += extractor.getText();
        } catch (Exception e) {
            throw new BusinessException("xlsx文件读取失败");
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new BusinessException("文件读取流未关闭");
            }
        }
        return content;
    }
}
