package cloudgene.mapred.core;

public class News {

    private int id;
    private String timestamp;
    private String text;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String ts) {
        this.timestamp = ts;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text=text;
    }
}
