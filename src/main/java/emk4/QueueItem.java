package emk4;

public class QueueItem {

    public String topicName;
    public String newsName;
    public Operation operation;
    public Type type;

    public QueueItem(String topicName, String newsName, Operation operation, Type type) {
        this.topicName = topicName;
        this.newsName = newsName;
        this.operation = operation;
        this.type = type;
    }

    public QueueItem(String topicName, Operation operation, Type type) {
        this.topicName = topicName;
        this.operation = operation;
        this.type = type;
    }

    public enum Operation{
        DELETED, ADDED
    }

    public enum Type{
        NEWS, TOPIC
    }

}
