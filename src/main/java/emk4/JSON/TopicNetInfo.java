package emk4.JSON;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class TopicNetInfo {

    public String name;
    public List<Article> articles;

    public TopicNetInfo(String name) {
        this.name = name;
        articles = new ArrayList<>();
    }

    private class Article{
         String title;
         String content;
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
