package emk4.JSON;

import java.util.UUID;

public class Request {

    public Role role;
    public Command command;
    public String topicName;
    public String newsHeader;
    public UUID senderId;

    public Request(Role role, Command command, UUID senderId) {
        this.role = role;
        this.command = command;
        this.senderId = senderId;
    }

    public Request(Role role, Command command, String topicName, UUID senderId) {
        this.role = role;
        this.command = command;
        this.topicName = topicName;
        this.senderId = senderId;
    }

    public Request(Role role, Command command, String topicName, String newsHeader, UUID senderId) {
        this.role = role;
        this.command = command;
        this.topicName = topicName;
        this.newsHeader = newsHeader;
        this.senderId = senderId;
    }

    public enum Role{
        ADMIN, CLIENT
    }
    public enum Command{
        ADD_TOPIC, REMOVE_TOPIC, ADD_NEWS, SUBSCRIBE, UNSUBSCRIBE, REGISTER_CLIENT
    }


}

