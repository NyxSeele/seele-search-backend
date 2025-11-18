package org.xiaobuding.hotsearchaiplatform.service.impl;

import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.repository.HotSearchRepository;
import org.xiaobuding.hotsearchaiplatform.service.SearchService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private final HotSearchRepository hotSearchRepository;

    public SearchServiceImpl(HotSearchRepository hotSearchRepository) {
        this.hotSearchRepository = hotSearchRepository;
    }

    @Override
    public List<HotSearchItem> searchByKeyword(String keyword) {
        List<HotSearchItem> allItems = hotSearchRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getTitle() != null && 
                               item.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}
