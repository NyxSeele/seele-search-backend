package org.xiaobuding.hotsearchaiplatform.controller;
public class ClassificationStats {
    private int totalCount;
    private int pendingCount;
    private int classifiedCount;
    private int classificationPercentage;
    private boolean classifying;
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
    public int getClassifiedCount() { return classifiedCount; }
    public void setClassifiedCount(int classifiedCount) { this.classifiedCount = classifiedCount; }
    public int getClassificationPercentage() { return classificationPercentage; }
    public void setClassificationPercentage(int classificationPercentage) { this.classificationPercentage = classificationPercentage; }
    public boolean isClassifying() { return classifying; }
    public void setClassifying(boolean classifying) { this.classifying = classifying; }
}
