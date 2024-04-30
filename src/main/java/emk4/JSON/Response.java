package emk4.JSON;

import com.google.gson.GsonBuilder;
import emk4.QueueItem;

import java.util.List;

public class Response {

    public String message;

    public Request.Command command;
    public List<QueueItem> subscriberQueue;

    public Response(String message, Request.Command command) {
        this.message = message;
        this.command = command;
    }

    public Response(List<QueueItem> subscriberQueue) {
        this.subscriberQueue = subscriberQueue;
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
