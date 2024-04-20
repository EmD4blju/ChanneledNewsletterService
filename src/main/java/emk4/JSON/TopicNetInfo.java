package emk4.JSON;

import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TopicNetInfo{

    public String name;
    public List<String> headers;

    public TopicNetInfo(String name) {
        this.name = name;
        headers = new ArrayList<>();
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicNetInfo that = (TopicNetInfo) o;
        return Objects.equals(name, that.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
