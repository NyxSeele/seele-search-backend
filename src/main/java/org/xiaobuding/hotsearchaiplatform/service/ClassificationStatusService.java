package org.xiaobuding.hotsearchaiplatform.service;
public interface ClassificationStatusService {
    boolean tryStartClassification();
    void endClassification();
}
