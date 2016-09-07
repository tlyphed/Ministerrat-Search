package at.tgeibinger.ministerrat.ui;

import at.tgeibinger.ministerrat.domain.Occurrence;
import at.tgeibinger.ministerrat.domain.Session;
import at.tgeibinger.ministerrat.domain.Treeable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MainController {

    private static final String BASE_URL = "https://www.bka.gv.at";
    private static final String MR_URL = "/site/8279/default.aspx";

    private static final ConcurrentHashMap<URL, Session> foundSessions = new ConcurrentHashMap<>();

    @FXML
    private TextField textfieldSearch;
    @FXML
    private Button buttonSearch;
    @FXML
    private TreeView tree;
    @FXML
    private ProgressIndicator progress;

    private TreeItem<Treeable> treeRoot;

    @FXML
    private void initialize(){
        buttonSearch.setOnAction(event -> onButtonSearchClicked());
        textfieldSearch.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                if(!buttonSearch.isDisabled()){
                    onButtonSearchClicked();
                }
            }
        });
        treeRoot = new TreeItem<> ();
        treeRoot.setExpanded(true);
        tree.setRoot(treeRoot);
        progress.setVisible(false);
        tree.showRootProperty().setValue(false);

        tree.setCellFactory(tree -> {
            TreeCell<Treeable> cell = new TreeCell<Treeable>() {
                @Override
                public void updateItem(Treeable item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };
            cell.setOnMouseClicked(event -> {
                if (! cell.isEmpty() && event.getClickCount() == 2) {
                    TreeItem<Treeable> treeItem = cell.getTreeItem();
                    treeItem.setExpanded(true);
                    if(treeItem.getValue() instanceof Session){
                        onTreeItemClicked((Session) treeItem.getValue());
                    } else if (treeItem.getValue() instanceof Occurrence){
                        onTreeItemClicked((Occurrence) treeItem.getValue());
                    }

                }
            });
            return cell ;
        });
    }

    private void onButtonSearchClicked(){
        String text = textfieldSearch.getText();
        (new Thread(() -> {
            try {
                progress.setVisible(true);
                treeRoot.getChildren().clear();
                buttonSearch.setDisable(true);
                search(text);
                buttonSearch.setDisable(false);
                progress.setVisible(false);
            } catch (IOException | InterruptedException e) {
                System.err.println("ERROR: " + e.getLocalizedMessage());
            }
        })).start();
    }

    private void onTreeItemClicked(Session session){
        try {
            Desktop.getDesktop().browse(session.getUrl().toURI());
        } catch (IOException | URISyntaxException e) {
            System.err.println("ERROR: " + e.getLocalizedMessage());
        }
    }

    private void onTreeItemClicked(Occurrence occurrence){
        try {
            Desktop.getDesktop().open(occurrence.getTmpFile());
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getLocalizedMessage());
        }
    }

    private void notifyChange(){
        for(Session session : foundSessions.values()){
            Optional<TreeItem<Treeable>> optionalItem = treeRoot.getChildren().stream().filter(t -> t.getValue() == session).findAny();
            TreeItem<Treeable> item;
            if(!optionalItem.isPresent()){
                item = new TreeItem<>(session);
                item.expandedProperty().setValue(true);
                treeRoot.getChildren().add(item);
            } else {
                item = optionalItem.get();
            }

            for(Occurrence occurrence : session.getOccurrences()){
                Optional<TreeItem<Treeable>> optionalSubItem = item.getChildren().stream().filter(t -> t.getValue() == occurrence).findAny();
                TreeItem<Treeable> subItem;
                if(!optionalSubItem.isPresent()){
                    subItem = new TreeItem<>(occurrence);
                    item.getChildren().add(subItem);
                }
            }

        }
    }

    private boolean contains(String text, String search){
        return text.toLowerCase().contains(search.toLowerCase());
    }

    private static int fileCounter = 0;
    private static synchronized File getTmp() throws IOException {
        return File.createTempFile("minsterrat_" + fileCounter++, ".pdf");
    }

    private void search(String text) throws IOException, InterruptedException {
        foundSessions.clear();

        Document base = Jsoup.connect(BASE_URL + MR_URL).get();

        for(Element elem : base.select(".content ul li a[href]")){
            Document doc = Jsoup.connect(BASE_URL + elem.attr("href")).get();
            String title = doc.select(".cobTitle span").text();

            URL sessionUrl = new URL(doc.location());
            Session s = foundSessions.get(sessionUrl);
            if(s == null){
                s = new Session();
                s.setUrl(sessionUrl);
                s.setTitle(title);
            }
            final Session session = s;

            Elements content = doc.select(".content");
            if(contains(content.text(), text)){
                foundSessions.put(session.getUrl(), session);
                notifyChange();
            }
            List<Thread> threads = new ArrayList<>();
            for(Element pdf : doc.select(".downloads li .pdf")){
                Thread thread = new Thread(() -> {
                    try {
                        URL url = new URL(BASE_URL + pdf.attr("href"));
                        File tmp = getTmp();
                        FileUtils.copyURLToFile(url, tmp);

                        String pdfTitle = pdf.text();
                        pdfTitle = pdfTitle.substring(0, pdfTitle.lastIndexOf('(') - 1);

                        PDDocument pdd = PDDocument.load(tmp);
                        String pdfText = new PDFTextStripper().getText(pdd);
                        if(contains(pdfText, text)){
                            Occurrence occurrence = new Occurrence();
                            occurrence.setUrl(url);
                            occurrence.setSession(session);
                            occurrence.setTitle(pdfTitle);
                            occurrence.setTmpFile(tmp);

                            session.getOccurrences().add(occurrence);
                            foundSessions.put(session.getUrl(), session);
                            notifyChange();
                        } else {
                            tmp.delete();
                        }
                        pdd.close();

                    } catch (IOException e){
                        System.err.println("ERROR: " + e.getLocalizedMessage());
                    }

                });
                threads.add(thread);
                thread.start();
            }
            for(Thread thread : threads){
                thread.join();
            }
        }

    }

}
