package com.ztdx.eams.basic.utils;

import com.ztdx.eams.basic.exception.BusinessException;
import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.POIXMLTextExtractor;
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
import java.util.HashMap;

/**
 * Created by li on 2018/6/6.
 */
public class FileReader {

    /**
     * 读取txt文件的内容
     */
    public static String txtContentRead(File file) {
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
    public static String docContentRead(File file) {
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
     * 读取docx文件内容(文本)
     */
    public static String docxContentRead(File file) {
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
     * 读取ppt文件内容(文本)
     */
    public static String pptContentRead(File file) {
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
     * 读取pptx文件内容(文本)
     */
    public static String pptxContentRead(File file) {
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
     * 读取xls文件内容(文本)
     */
    public static String xlsContentRead(File file) {
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
     * 读取xlsx文件内容(文本)
     */
    public static String xlsxContentRead(File file) {
        String content = "";
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            XSSFWorkbook xlsx = new XSSFWorkbook(fis);
            XSSFExcelExtractor extractor = new XSSFExcelExtractor(xlsx);
            content += extractor.getText();
        } catch (Exception e) {
            throw new BusinessException("xlsx/xlsm文件读取失败");
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
     * 读取office2003属性内容
     */
    public static HashMap office2003MetadataRead(File file) {
        HashMap metadataMap = new HashMap<String, Object>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            POIOLE2TextExtractor extractor = null;
            String fileType = file.getName().substring(file.getName().lastIndexOf("."));
            switch (fileType) {
                case ".doc": {
                    extractor = new WordExtractor(fis);
                    break;
                }
                case ".xls": {
                    extractor = new ExcelExtractor(new POIFSFileSystem(fis));
                    break;
                }
                case ".ppt": {
                    extractor = new PowerPointExtractor(fis);
                    break;
                }
            }
            String text = extractor.getMetadataTextExtractor().getText();
            String[] metadataText = text.split("\n");
            for (String str : metadataText) {
                int index = str.indexOf(" = ");
                if (index == -1) {
                    continue;
                }
                String key = str.substring(0, index);
                String value = str.substring(index + 3);
                switch (key) {
                    case "PID_TITLE": {
                        metadataMap.put("标题", value);
                        break;
                    }
                    case "PID_SUBJECT": {
                        metadataMap.put("主题", value);
                        break;
                    }
                    case "PID_COMPANY": {
                        metadataMap.put("公司", value);
                        break;
                    }
                    case "PID_AUTHOR": {
                        metadataMap.put("作者", value);
                        break;
                    }
                    case "PID_LASTAUTHOR": {
                        metadataMap.put("最后一次保存者", value);
                        break;
                    }
                    case "PID_REVNUMBER": {
                        metadataMap.put("修订号", value);
                        break;
                    }
                    case "PID_APPNAME": {
                        metadataMap.put("程序名称", value);
                        break;
                    }
                    case "PID_CREATE_DTM": {
                        metadataMap.put("创建内容的时间", value);
                        break;
                    }
                    case "PID_LASTSAVE_DTM": {
                        metadataMap.put("最后一次保存的日期", value);
                        break;
                    }
                    case "PID_PAGECOUNT": {
                        metadataMap.put("页码范围", value);
                        break;
                    }
                    case "PID_WORDCOUNT": {
                        metadataMap.put("字数", value);
                        break;
                    }
                    case "PID_CHARCOUNT": {
                        metadataMap.put("字符数", value);
                        break;
                    }
                    case "PID_LINECOUNT": {
                        metadataMap.put("行数", value);
                        break;
                    }
                    case "PID_PARCOUNT": {
                        metadataMap.put("段落数", value);
                        break;
                    }
                    default: {
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException("doc文件读取属性失败");
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new BusinessException("文件读取流未关闭");
            }
        }
        return metadataMap;
    }

    /**
     * 读取office2007及以上版本属性内容
     */
    public static HashMap office2007MetadataRead(File file) {
        HashMap metadataMap = new HashMap<String, Object>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            POIXMLTextExtractor extractor = null;
            String fileType = file.getName().substring(file.getName().lastIndexOf("."));
            switch (fileType) {
                case ".docx": {
                    XWPFDocument docx = new XWPFDocument(fis);
                    extractor = new XWPFWordExtractor(docx);
                    break;
                }
                case ".xlsx": {
                    XSSFWorkbook xlsx = new XSSFWorkbook(fis);
                    extractor = new XSSFExcelExtractor(xlsx);
                    break;
                }
                case ".xlsm": {
                    XSSFWorkbook xlsx = new XSSFWorkbook(fis);
                    extractor = new XSSFExcelExtractor(xlsx);
                    break;
                }
                case ".pptx": {
                    XMLSlideShow xlsx = new XMLSlideShow(fis);
                    extractor = new XSLFPowerPointExtractor(xlsx);
                    break;
                }
            }
            POIXMLProperties.CoreProperties core = extractor.getCoreProperties();
            POIXMLProperties.ExtendedProperties extended = extractor.getExtendedProperties();
            metadataMap.put("标题", core.getTitle());
            metadataMap.put("主题", core.getSubject());
            metadataMap.put("标记", core.getIdentifier());
            metadataMap.put("类别", core.getCategory());
            metadataMap.put("备注", core.getDescription());
            metadataMap.put("作者", core.getCreator());
            metadataMap.put("内容状态", core.getContentStatus());
            metadataMap.put("内容类型", core.getContentType());
            metadataMap.put("创建内容时间", core.getCreated());
            metadataMap.put("最后一次保存者", core.getLastModifiedByUser());
            metadataMap.put("最后一次打印时间", core.getLastPrinted());
            metadataMap.put("修订号", core.getRevision());
            metadataMap.put("程序名称", extended.getAppVersion());
            metadataMap.put("字符数", extended.getCharacters());
            metadataMap.put("公司", extended.getCompany());
            metadataMap.put("行数", extended.getLines());
            metadataMap.put("管理者", extended.getManager());
            metadataMap.put("页码范围", extended.getPages());
            metadataMap.put("段落数", extended.getParagraphs());
        } catch (Exception e) {
            throw new BusinessException("doc文件读取属性失败");
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new BusinessException("文件读取流未关闭");
            }
        }
        return metadataMap;
    }
}
