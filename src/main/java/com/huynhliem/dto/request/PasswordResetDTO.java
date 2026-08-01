package com.huynhliem.dto.request;

import lombok.Getter;

import java.io.Serializable;
@Getter
public class PasswordResetDTO implements Serializable {
    private String secretKey;
    private String password;
    private String confirmPassword;

}
