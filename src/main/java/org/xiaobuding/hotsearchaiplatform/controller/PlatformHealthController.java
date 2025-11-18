package org.xiaobuding.hotsearchaiplatform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;
import org.xiaobuding.hotsearchaiplatform.service.PlatformHealthService;
import org.xiaobuding.hotsearchaiplatform.service.PlatformHealthService.PlatformHealth;

@RestController
@RequestMapping("/api/health/platform")
public class PlatformHealthController {

    private final PlatformHealthService platformHealthService;

    public PlatformHealthController(PlatformHealthService platformHealthService) {
        this.platformHealthService = platformHealthService;
    }

    @GetMapping("/{platform}")
    public ResponseEntity<PlatformHealth> getHealth(@PathVariable("platform") String platform) {
        try {
            PlatformType platformType = PlatformType.valueOf(platform.toUpperCase());
            return ResponseEntity.ok(platformHealthService.getPlatformHealth(platformType));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}

