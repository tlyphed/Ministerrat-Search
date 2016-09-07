package at.tgeibinger.ministerrat.domain;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Session implements Treeable{

    private String title;
    private URL url;
    private final List<Occurrence> occurrences;

    public Session(){
        occurrences = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public List<Occurrence> getOccurrences() {
        return occurrences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        return url != null ? url.equals(session.url) : session.url == null;

    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public String toString() {
        return title;
    }
}
