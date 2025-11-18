package org.xiaobuding.hotsearchaiplatform.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xiaobuding.hotsearchaiplatform.service.CacheManagementService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@CrossOrigin(origins = "*")
public class CacheController {
    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    private final CacheManagementService cacheManagementService;

    public CacheController(CacheManagementService cacheManagementService) {
        this.cacheManagementService = cacheManagementService;
    }

    @PostMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllCache() {
        logger.info("Clear all cache request");
        try {
            cacheManagementService.clearAllCache();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All cache cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Clear all cache failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Clear cache failed");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/clear/{platform}")
    public ResponseEntity<Map<String, Object>> clearPlatformCache(@PathVariable String platform) {
        logger.info("Clear platform cache request: {}", platform);
        try {
            cacheManagementService.clearPlatformCache(platform);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Platform cache cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Clear platform cache failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Clear cache failed");
            return ResponseEntity.status(500).body(response);
        }
    }
}
