package net.idonow.controller.config;

import net.idonow.common.api.ApiResponse;
import net.idonow.service.common.SystemConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemInfoController {

    private final SystemConfigService systemConfigService;

    public SystemInfoController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("info")
    public ApiResponse<Map<String, Object>> getPublicConfig() {

        Map<String, Object> publicConfig = systemConfigService.getPublicConfig();
        return ApiResponse.ok("Server public config", publicConfig);
    }
}
