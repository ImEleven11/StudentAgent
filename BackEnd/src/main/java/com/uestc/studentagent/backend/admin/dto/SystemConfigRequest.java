package com.uestc.studentagent.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SystemConfigRequest {

    @NotBlank(message = "configKey is required")
    @Size(max = 128, message = "configKey is too long")
    private String configKey;

    @NotBlank(message = "configValue is required")
    @Size(max = 4000, message = "configValue is too long")
    private String configValue;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
}
