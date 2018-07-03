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
    private String address;
    //端口号
    @Value("${ftp.port}")
    private int port;
    //用户名
    @Value("${ftp.username}")
    private String username;
    //密码
    @Value("${ftp.password}")
    private String password;
    //基础路径
    @Value("${ftp.path}")
    private String basePath;
    //允许的文件类型
    @Value("${ftp.allow-type}")
    private String allowType;

    public FTPClient getFTPClient() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(address, port);// 连接FTP服务器
            ftpClient.login(username, password);// 登陆FTP服务器
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

        String[] fileType = allowType.split(",");
        boolean isAllow = false;
        for (String type : fileType) {
            if (file.getName().substring(file.getName().lastIndexOf(".") + 1).equals(type)) {
                isAllow = true;
            }
        }
        if (!isAllow) {
            throw new BusinessException("不允许的文件类型");
        }

        FTPClient ftp = getFTPClient();
        ftp.setControlEncoding("UTF-8");
        FileInputStream fis = null;
        try {
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

            //设置存放路径
            ftp.makeDirectory(basePath);
            ftp.changeWorkingDirectory(basePath);

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

    /*
     * 下载文件
     */
    public void downloadFile(String[] path, String fileName, File file) {

        FTPClient ftp = getFTPClient();
        ftp.setControlEncoding("UTF-8");
        OutputStream os = null;
        try {
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

            ftp.changeWorkingDirectory(basePath);

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
