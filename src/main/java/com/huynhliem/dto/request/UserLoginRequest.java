package com.huynhliem.dto.request;

import com.huynhliem.common.Platform;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Password is required")
    private String password;
    @NonNull
    private Platform platform;
    private String deviceToken;
    private String version;
}
