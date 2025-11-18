package org.xiaobuding.hotsearchaiplatform.model;
public class QNAResponse {
    private String conversationId;
    private String answer;
    private String status;
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
