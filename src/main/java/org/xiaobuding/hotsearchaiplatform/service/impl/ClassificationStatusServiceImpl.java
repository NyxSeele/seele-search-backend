package org.xiaobuding.hotsearchaiplatform.service.impl;

import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.service.ClassificationStatusService;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ClassificationStatusServiceImpl implements ClassificationStatusService {
    private final AtomicBoolean classifying = new AtomicBoolean(false);

    @Override
    public boolean tryStartClassification() {
        return classifying.compareAndSet(false, true);
    }

    @Override
    public void endClassification() {
        classifying.set(false);
    }
}
