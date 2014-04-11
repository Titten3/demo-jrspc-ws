package habr.metalfire.chat;

public class ChatMessage {

    private String text, from, to ;
    private Long clientTime, serverTime;
    
    public ChatMessage() {}

    public Long getClientTime() {
        return clientTime;
    }

    public void setClientTime(Long clientTime) {
        this.clientTime = clientTime;
    }

    public Long getServerTime() {
        return serverTime;
    }


    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }


    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }


    public String getFrom() {
        return from;
    }


    public void setFrom(String from) {
        this.from = from;
    }


    public String getTo() {
        return to;
    }


    public void setTo(String to) {
        this.to = to;
    } 
    
    
    
    
}
