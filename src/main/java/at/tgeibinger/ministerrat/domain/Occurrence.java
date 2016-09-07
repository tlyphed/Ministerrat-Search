package at.tgeibinger.ministerrat.domain;

import java.io.File;
import java.net.URL;

public class Occurrence implements Treeable {

    private String title;
    private File tmpFile;
    private URL url;
    private Session session;

    public Occurrence(){
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public File getTmpFile() {
        return tmpFile;
    }

    public void setTmpFile(File tmpFile) {
        this.tmpFile = tmpFile;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public String toString() {
        return title;
    }
}
