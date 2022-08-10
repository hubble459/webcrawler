import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

public class Download {
    private ArrayList<String> domains = new ArrayList<>();
    private ArrayList<String> fileExtensions = new ArrayList<>();
    private JTextArea log = new JTextArea();
    private boolean gui;

    public Download() {
        // Fill ArrayLists
        setDomains();
        setFileExtensions();
    }

    private void setDomains() {
        domains.add(".nl/");
        domains.add(".be/");
        domains.add(".de/");
        domains.add(".uk/");
        domains.add(".jp/");
        domains.add(".edu/");
        domains.add(".com/");
        domains.add(".net/");
        domains.add(".org/");
        domains.add(".to/");
        domains.add(".tv/");
        domains.add(".io/");
    }
    private void setFileExtensions() {
        fileExtensions.add(".docx");
        fileExtensions.add(".pdf");
        fileExtensions.add(".webm");
        fileExtensions.add(".mkv");
        fileExtensions.add(".flv");
        fileExtensions.add(".vob");
        fileExtensions.add(".ogg");
        fileExtensions.add(".mov");
        fileExtensions.add(".mp");
        fileExtensions.add(".ico");
        fileExtensions.add(".png");
        fileExtensions.add(".jpg");
        fileExtensions.add(".gif");
        fileExtensions.add(".svg");
        fileExtensions.add(".bmp");
        fileExtensions.add(".wav");
        fileExtensions.add(".css");
        fileExtensions.add(".htm");
        fileExtensions.add(".csv");
        fileExtensions.add(".xml");
        fileExtensions.add(".php");
        fileExtensions.add(".js");
        fileExtensions.add(".eot");
        fileExtensions.add(".ttf");
        fileExtensions.add(".woff");
    }

    // Setters
    public void setGui(boolean gui) {
        this.gui = gui;
    }
    public void setLog(JTextArea log) {
        this.log = log;
    }

    // Getters
    public ArrayList<String> getVideos(ArrayList<String> arrayList) {
        ArrayList<String> array = new ArrayList<>();
        String[] extensions = {".webm", ".mkv", ".flv", ".vob", ".ogg", ".mov", ".mp4", ".mpg"};
        for (String s: arrayList) {
            for (String picExtension: extensions) {
                if (s.contains(picExtension)) array.add(s);
            }
        }
        return array;
    }
    public ArrayList<String> getImages(ArrayList<String> arrayList) {
        ArrayList<String> array = new ArrayList<>();
        String[] extensions = {".ico", ".png", ".jpg", ".gif", ".svg", ".bmp", ".wav", ".mpeg"};
        for (String s: arrayList) {
            for (String picExtension: extensions) {
                if (s.contains(picExtension)) array.add(s);
            }
        }
        return array;
    }
    public ArrayList<String> getDomains() {
        return this.domains;
    }
    public ArrayList<String> getFileExtensions() {return this.fileExtensions;}

    public void download(String link, String filename) throws IOException {
        URL url = new URL(link);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(filename);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

    public void saveFile(ArrayList<String> arrayList, String dirName, String name) {
        if (arrayList.size() == 0) {
            log.append("No links found...");
            System.out.println("No links found...");
            return;
        }
        try {
            Files.createDirectories(new File(dirName).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String filename = dirName + "/" + name;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
            for (int i = 0; i < arrayList.size(); i++) {
                writer.write(arrayList.get(i));
                if (i != arrayList.size() - 1) writer.newLine();
            }
            writer.flush();
            log.append("File Downloaded");
            System.out.println("File Downloaded!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    public void downloadImagesOrVideos(ArrayList<String> arrayList, boolean image) {
        String fileType;
        if (image) {
            arrayList = getImages(arrayList);
            fileType = "images";
        } else {
            arrayList = getVideos(arrayList);
            fileType = "videos";
        }

        if (arrayList.size() == 0) {
            log.append("No "+fileType+" found...\n");
            System.out.println("No "+fileType+" found...");
            return;
        }

        if (gui) {
            int input = JOptionPane.showConfirmDialog(null, "There's " +arrayList.size()+" "+fileType+"! Continue?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (input != JOptionPane.YES_OPTION) return;
        }else {
            if (!doConfirm("There's " +arrayList.size()+" "+fileType+"!")) return;
        }


        ArrayList<String> dirNameList = new ArrayList<>();
        for (String s : arrayList) {
            if (!dirNameList.contains(s.split("://")[1].split("/")[0])) {
                dirNameList.add(s.split("://")[1].split("/")[0]);
            }
        }

        try {
            for (String dirName : dirNameList) {
                Files.createDirectories(new File(dirName + "/" + fileType).toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String urlString: arrayList) {
            String name = urlString.split("/")[urlString.split("/").length-1];

            int dir = 0;
            for (int i = 0; i < dirNameList.size(); i++) {
                if (dirNameList.get(i).contains(urlString.split("://")[1].split("/")[0])) {
                    dir = i;
                }
            }

            log.append("Downloading " + urlString + "\n");
            System.out.println("Downloading " + urlString);
            String filename = dirNameList.get(dir) + "/"+fileType+"/" + name;
            log.append("\tTo --> " + filename + "\n");
            System.out.println("\tTo --> " + filename);

            try {
                download(urlString, filename);
            } catch (IOException e) {
                log.append("[ERROR] Skipped: " + e.getMessage());
                System.out.println("[ERROR] Skipped: " + e.getMessage());
            }

            System.out.println();
        }
    }
    private static boolean doConfirm(String message) {
        Scanner sc = new Scanner(System.in);
        System.out.println(message);
        System.out.print("Do you want to continue? (y/N) ");
        return sc.nextLine().toLowerCase().equals("y");
    }
}
