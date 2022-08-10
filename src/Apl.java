import java.util.Scanner;

public class Apl {
    public static void main(String[] args) {
        if (args.length == 0) new Apl().gui();
        else {
            try {
                new Apl().command(args);
            } catch (MyException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    public void gui() {
        new WebCrawlerGUI();
        new Scanner(System.in).nextLine();
    }

    public boolean isNumber(String s) {
        for (int i = 'A'; i < 'z'; i++) {
            if (s.charAt(0) == i) return false;
        }
        return true;
    }

    public void command(String[] args) throws MyException {
        String url = "";
        int layers = 3;
        boolean verbose = false;
        boolean images = false;
        boolean save = true;
        boolean videos = false;
        boolean repeat = false;
        boolean cloneSite = false;
        boolean noGui = true;

        boolean wrong = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].contains("-")) {
                if (args[i].split("-").length < 2) {
                    wrong = true;
                    break;
                }
                String argument = args[i].split("-")[1];
                if (args[i].split("-").length == 3) argument = args[i].split("-")[2];
                switch (argument) {
                    case "help": case "h":
                        help(); System.exit(0);
                    case "gui": case "g":
                        gui(); noGui = false; break;
                    case "url": case "u":
                        if (i != args.length - 1 && !args[i + 1].split("-")[0].equals(""))
                            url = args[i + 1]; break;
                    case "layers": case "l":
                        if (i != args.length - 1 && !args[i + 1].split("-")[0].equals("") && isNumber(args[i + 1]))
                            layers = Integer.parseInt(args[i + 1]);
                        else wrong = true; break;
                    case "verbose": case "v":
                        verbose = true; break;
                    case "dontsave": case "ds":
                        save = false; break;
                    case "pictures": case "p":
                        images = true; break;
                    case "videos": case "vi":
                        videos = true; break;
                    case "repeat": case "r":
                        repeat = true; break;
                    case "clonesite": case "cs":
                        cloneSite = true; break;
                }
            }
        }

        if (!noGui) {
            System.out.println("Enter to Stop");
            new Scanner(System.in).nextLine();
            System.exit(1);
        }
        if (!url.contains("://")) {
            System.out.println("Incorrect url! Use --help for help.");
            System.exit(-1);
        }
        if (wrong) {
            System.out.println("An error occurred!");
        }

        System.out.println();
        System.out.println("URL:         " + url);
        System.out.println("Layers:      " + layers);
        System.out.println("Verbose:     " + verbose);
        System.out.println("Repeat:      " + repeat);
        System.out.println("Clone        " + cloneSite);
        System.out.println("Save Output: " + save);
        System.out.println("Save Pics:   " + images);
        System.out.println("Save Vids:   " + videos);
        System.out.println();

        WebCrawler wc = new WebCrawler(url);
        wc.setVerbose(verbose);
        wc.setRepeat(repeat);
        wc.crawlLayers(layers);
        Download download = new Download();
        download.setGui(false);
        if (save) {
            System.out.print("Internal: ");
            download.saveFile(wc.getInternalLinks(), wc.getSiteName() + "/links", "internal.txt");
            System.out.print("External: ");
            download.saveFile(wc.getExternalLinks(), wc.getSiteName() + "/links", "external.txt");
            System.out.println();
        }
        if (images) {
            download.downloadImagesOrVideos(wc.getInternalLinks(), true);
            System.out.println();
        }
        if (videos) {
            download.downloadImagesOrVideos(wc.getInternalLinks(), false);
            System.out.println();
        }
        if (cloneSite) {
            wc.makeSite(wc.getInternalLinks());
        }

    }

    public void help() {
        System.out.println("\n88       88 8b      db      d8 88       88  \n" +
                "88       88 `8b    d88b    d8' 88       88  \n" +
                "88       88  `8b  d8'`8b  d8'  88       88  \n" +
                "\"8a,   ,a88   `8bd8'  `8bd8'   \"8a,   ,a88  \n" +
                " `\"YbbdP'Y8     YP      YP      `\"YbbdP'Y8  \n");
        System.out.println("                WebCrawler!\n");
        System.out.println("Commands:");
        System.out.println("-h  or --help      | This help screen.");
        System.out.println("-t  or --tui       | Text interface for WebCrawler.");
        System.out.println("-g  or --gui       | Graphical interface for WebCrawler.");
        System.out.println("-u  or --url       | Give link you want to crawl. Eg. -u https://google.com/");
        System.out.println("-l  or --layers    | Depth of crawling. Default is 5.");
        System.out.println("-v  or --verbose   | Verbose execution. Default is false");
        System.out.println("-r  or --repeat    | Repeat the crawling for maximum yield");
        System.out.println("-cs or --clonesite | Clone site");
        System.out.println("-ds or --dontsave  | Don't save output.");
        System.out.println("-p  or --pictures  | Download all found pictures. Default is false. (asks for confirmation)");
        System.out.println("-vi or --videos    | Download all found videos. Default  is false. (asks for confirmation)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("java -jar WebCrawlerV2.jar -u https://stackoverflow.com/ -l 20 -v");
        System.out.println("java -jar WebCrawlerV2.jar -u saxion.nl -r -p -vi");
        System.out.println("java =jar WebCrawlerV2.jar --gui");
        System.out.println();
    }
}
