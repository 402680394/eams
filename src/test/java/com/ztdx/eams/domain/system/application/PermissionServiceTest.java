package com.ztdx.eams.domain.system.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PermissionServiceTest {

    @Autowired
    private PermissionService permissionService;

    @Test
    public void addUserPermission() {
        //permissionService.addUserPermission(23, "object_original_text_view_c837ce6a-2081-435a-8fd7-7ca9446aab80", 3);
    }
}