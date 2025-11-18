package org.xiaobuding.hotsearchaiplatform.service;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import java.util.List;
public interface SearchService {
    List<HotSearchItem> searchByKeyword(String keyword);
}
