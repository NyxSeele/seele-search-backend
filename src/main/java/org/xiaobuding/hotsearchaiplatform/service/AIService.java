package org.xiaobuding.hotsearchaiplatform.service;
import org.xiaobuding.hotsearchaiplatform.model.*;
public interface AIService {
    TrendSummary analyzeGlobalTrends();
    EvaluationResult evaluateSingleItem(PlatformType platform, String itemId);
    QNAResponse answerQuestion(String question, String conversationId, String platformFilter);
}
