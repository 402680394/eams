package com.ztdx.eams.domain.system.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;
import java.util.Date;
import java.util.UUID;

@Data
@Document(collection = "operation_log")
public class OperationLog {

//    public OperationLog(){
//
//    }
//
//    public OperationLog(String message,int operatorId,int operatorName){
//        id =UUID.randomUUID().toString();
//        gmtCreate =new Date();
//        gmtModified=new Date();
//    }

    /**
     * 主键
     */
    @Id
    private String id;

    /**
     * 日志内容
     */
    private String message;

    /**
     * 是否成功
     */
    private boolean isException;

    /**
     * 操作人Id
     */
    private int operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 异常内容
     */
    private Exception exception;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;
}
