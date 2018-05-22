package com.ztdx.eams.basic;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户 凭证
 */
@Data
@AllArgsConstructor
public class UserCredential {

    /**
     * 用户标识
     */
    private int userId;
    /**
     * 真实姓名
     */
    private String name;
}
