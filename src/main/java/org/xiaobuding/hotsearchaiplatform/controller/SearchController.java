package org.xiaobuding.hotsearchaiplatform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xiaobuding.hotsearchaiplatform.model.*;
import org.xiaobuding.hotsearchaiplatform.service.SearchService;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<HotSearchItem>> search(@RequestParam String keyword) {
        List<HotSearchItem> results = searchService.searchByKeyword(keyword);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<HotSearchItem>>> getSearchSuggestions(
            @RequestParam(required = false) String keyword) {
        List<HotSearchItem> suggestions = keyword != null && !keyword.isEmpty()
                ? searchService.searchByKeyword(keyword)
                : List.of();
        return ResponseEntity.ok(ApiResponse.success("Success", suggestions));
    }
}