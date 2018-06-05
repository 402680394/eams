package com.ztdx.eams.basic.utils;

import com.ztdx.eams.basic.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
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
    public String uploadFile(int fondsId, File file) {

        FTPClient ftp = getFTPClient();
        ftp.setControlEncoding("UTF-8");
        FileInputStream fisMD5 = null;
        FileInputStream fis = null;
        try {
            ftp.enterLocalPassiveMode();
            ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            //计算文件MD5
            fisMD5 = new FileInputStream(file);
            String MD5 = DigestUtils.md5Hex(fisMD5);

            //设置存放路径
            String path = FTP_BASEPATH + fondsId;
            ftp.makeDirectory(path);
            ftp.changeWorkingDirectory(path);
            path = MD5.substring(0, 2);
            ftp.makeDirectory(path);
            ftp.changeWorkingDirectory(path);
            path = MD5.substring(2, 4);
            ftp.makeDirectory(path);
            ftp.changeWorkingDirectory(path);
            fis = new FileInputStream(file);
            //上传
            ftp.storeFile(MD5, fis);
            ftp.logout();
            return MD5;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        } finally {
            try {
                fisMD5.close();
                fis.close();
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

    public File downloadFile(int fondsId, String MD5, String fileName) {

        FTPClient ftp = getFTPClient();
        ftp.setControlEncoding("UTF-8");
        OutputStream os = null;
        try {
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();

            String path = FTP_BASEPATH + fondsId;
            ftp.changeWorkingDirectory(path);
            path = MD5.substring(0, 2);
            ftp.changeWorkingDirectory(path);
            path = MD5.substring(2, 4);
            ftp.changeWorkingDirectory(path);

            File localFile = new File(fileName);
            os = new FileOutputStream(localFile);
            ftp.retrieveFile(MD5, os);
            ftp.logout();
            return localFile;
        } catch (Exception e) {
            throw new BusinessException("文件下载失败");
        } finally {
            try {
                os.close();
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
