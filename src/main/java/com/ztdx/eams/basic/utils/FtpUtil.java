package com.ztdx.eams.basic.utils;

import com.ztdx.eams.basic.exception.BusinessException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * Created by li on 2018/5/25.
 */
@Component
public class FtpUtil {
    //ftp服务器ip地址
    @Value("${ftp.address}")
    private String FTP_ADDRESS;
    //端口号
    @Value("${ftp.port}")
    private int FTP_PORT;
    //用户名
    @Value("${ftp.username}")
    private String FTP_USERNAME;
    //密码
    @Value("${ftp.password}")
    private String FTP_PASSWORD;
    //基础路径
    @Value("${ftp.path}")
    private String FTP_BASEPATH;

    public FTPClient getFTPClient() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_ADDRESS, FTP_PORT);// 连接FTP服务器
            ftpClient.login(FTP_USERNAME, FTP_PASSWORD);// 登陆FTP服务器
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
                throw new BusinessException("未连接到ftp服务");
            }
        } catch (Exception e) {
            throw new BusinessException("未连接到ftp服务");
        }
        return ftpClient;
    }

    /*
     * 上传文件
     */
    public void uploadFile(String[] path, String fileName, File file) {

        FTPClient ftp = getFTPClient();
        ftp.setControlEncoding("UTF-8");
        FileInputStream fis = null;
        try {
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

            //设置存放路径
            ftp.makeDirectory(FTP_BASEPATH);
            ftp.changeWorkingDirectory(FTP_BASEPATH);

            for (int i = 0; i < path.length; i++) {
                ftp.makeDirectory(path[i]);
                ftp.changeWorkingDirectory(path[i]);
            }

            fis = new FileInputStream(file);
            //上传
            ftp.storeFile(fileName, fis);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        } finally {
            try {
                fis.close();
                ftp.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    throw new BusinessException("ftp服务未正常关闭");
                }
            }
        }
    }


    public void downloadFile(String[] path, String fileName, File file) {

        FTPClient ftp = getFTPClient();
        ftp.setControlEncoding("UTF-8");
        OutputStream os = null;
        try {
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

            ftp.changeWorkingDirectory(FTP_BASEPATH);

            for (int i = 0; i < path.length; i++) {
                ftp.changeWorkingDirectory(path[i]);
            }
            os = new FileOutputStream(file);
            ftp.retrieveFile(fileName, os);
        } catch (Exception e) {
            throw new BusinessException("文件下载失败");
        } finally {
            try {
                os.close();
                ftp.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    throw new BusinessException("ftp服务未正常关闭");
                }
            }
        }
    }
}
