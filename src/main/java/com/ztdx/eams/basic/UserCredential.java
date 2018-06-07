package com.ztdx.eams.basic;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户 凭证
 */
@Data
@AllArgsConstructor
public class UserCredential {

    public static final String KEY ="LOGIN_USER";

    /**
     * 用户标识
     */
    private int userId;
    /**
     * 真实姓名
     */
    private String name;
}
