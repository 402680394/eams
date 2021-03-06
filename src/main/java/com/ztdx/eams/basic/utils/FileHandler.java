package com.ztdx.eams.basic.utils;

import com.ztdx.eams.basic.exception.BusinessException;
import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by li on 2018/6/6.
 */
public class FileHandler {

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
        HashMap<String, Object> metadataMap = new HashMap();
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
        HashMap<String, String> metadataMap = new HashMap();
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
            metadataMap.put("标题", (core.getTitle() == null ? "" : core.getTitle()));
            metadataMap.put("主题", (core.getSubject() == null ? "" : core.getSubject()));
            metadataMap.put("标记", (core.getIdentifier() == null ? "" : core.getIdentifier()));
            metadataMap.put("类别", (core.getCategory() == null ? "" : core.getCategory()));
            metadataMap.put("备注", (core.getDescription() == null ? "" : core.getDescription()));
            metadataMap.put("作者", (core.getCreator() == null ? "" : core.getCreator()));
            metadataMap.put("内容状态", (core.getContentStatus() == null ? "" : core.getContentStatus()));
            metadataMap.put("内容类型", (core.getContentType() == null ? "" : core.getContentType()));
            metadataMap.put("最后一次保存者", (core.getLastModifiedByUser() == null ? "" : core.getLastModifiedByUser()));
            metadataMap.put("修订号", (core.getRevision() == null ? "" : core.getRevision()));
            metadataMap.put("程序名称", (extended.getApplication() == null ? "" : extended.getApplication()));
            metadataMap.put("字符数", (extended.getCharacters() == -1 ? "0" : String.valueOf(extended.getCharacters())));
            metadataMap.put("公司", (extended.getCompany() == null ? "" : extended.getCompany()));
            metadataMap.put("行数", (extended.getLines() == -1 ? "0" : String.valueOf(extended.getLines())));
            metadataMap.put("管理者", (extended.getManager() == null ? "" : extended.getManager()));
            metadataMap.put("页码范围", (extended.getPages() == -1 ? "0" : String.valueOf(extended.getPages())));
            metadataMap.put("段落数", (extended.getParagraphs() == -1 ? "0" : String.valueOf(extended.getParagraphs())));
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
     * 导出Excel
     *
     * @param sheetName sheet名称
     * @param content   内容
     * @param wb        HSSFWorkbook对象
     * @return
     */
    public static XSSFWorkbook buildXSSFWorkbook(String sheetName, List<List<String>> content, XSSFWorkbook wb) {

        // 第一步，创建一个XSSFWorkbook，对应一个Excel文件
        if (wb == null) {
            wb = new XSSFWorkbook();
        }

        // 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
        XSSFSheet sheet = wb.createSheet(sheetName);

        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
        XSSFRow row = sheet.createRow(0);

        // 第四步，创建单元格，并设置值表头 设置表头居中
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER); // 创建一个居中格式

        //创建内容
        for (int i = 0; i < content.size(); i++) {
            row = sheet.createRow(i);
            for (int j = 0; j < content.get(i).size(); j++) {
                //将内容按顺序赋给对应的列对象
                row.createCell(j).setCellValue(content.get(i).get(j));
            }
        }
        return wb;
    }

    public static List<List<List<String>>> xlsxRead(File excelFile) {

        //读取xlsx文件
        XSSFWorkbook xssfWorkbook = null;
        //寻找目录读取文件
        try {
            InputStream is = new FileInputStream(excelFile);
            xssfWorkbook = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<List<List<String>>> workBook = new ArrayList<>();
        //遍历xlsx中的sheet
        for (int numSheet = 0; numSheet < xssfWorkbook.getNumberOfSheets(); numSheet++) {
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(numSheet);
            if (xssfSheet == null) {
                continue;
            }
            List<List<String>> sheet = new ArrayList<>();
            // 对于每个sheet，读取其中的每一行
            for (int rowNum = 0; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
                XSSFRow xssfRow = xssfSheet.getRow(rowNum);
                if (xssfRow == null) continue;
                ArrayList<String> row = new ArrayList<>();
                for (int cellNum = 0; cellNum < xssfSheet.getRow(0).getLastCellNum(); cellNum++) {
                    XSSFCell cell = xssfRow.getCell(cellNum);
                    if (null != cell) {
                        cell.setCellType(CellType.STRING);
                        row.add(cell.getStringCellValue());
                    } else {
                        row.add("");
                    }

                }
                sheet.add(row);
            }
            workBook.add(sheet);
        }
        return workBook;
    }
}
