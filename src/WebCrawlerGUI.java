import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WebCrawlerGUI extends JFrame implements ActionListener {
    // URL Component
    private JTextField urlField;
    // Frame
    private JFrame frame;
    // Check Boxes
    private JCheckBox verbose, save, savePics, saveVids, clone, repeat;
    // Log
    private JTextArea log, result, links, extLinks;
    // Layers
    private JTextField layers;
    // Button
    private JButton button;

    // Constructor
    public WebCrawlerGUI() throws HeadlessException {
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();
        JPanel panel4 = new JPanel();
        JPanel panel5 = new JPanel();
        panel2.setLayout(new GridLayout());
        panel3.setLayout(new GridLayout());
        panel4.setLayout(new GridLayout());
        panel5.setLayout(new GridLayout());
        tabbedPane.addTab("Output", null, panel2, "Console Log");
        tabbedPane.addTab("Results", null, panel3, "Results");
        tabbedPane.addTab("Links", null, panel4, "Internal links");
        tabbedPane.addTab("ExtLinks", null, panel5, "External links");

        log = new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        log.setAutoscrolls(true);
        JScrollPane scroller1 = new JScrollPane(log);
        scroller1.setAutoscrolls(true);
        panel2.add(scroller1);

        result = new JTextArea();
        result.setEditable(false);
        result.setAutoscrolls(true);
        result.setLineWrap(true);
        panel3.add(result);

        links = new JTextArea();
        links.setLineWrap(true);
        links.setAutoscrolls(true);
        JScrollPane scroller2 = new JScrollPane(links);
        scroller2.setAutoscrolls(true);
        panel4.add(scroller2);

        extLinks = new JTextArea();
        extLinks.setLineWrap(true);
        extLinks.setAutoscrolls(true);
        JScrollPane scroller3 = new JScrollPane(extLinks);
        scroller3.setAutoscrolls(true);
        panel5.add(scroller3);

        // Set Frame
        frame = new JFrame("WebCrawler");
        frame.setLayout(new GridLayout(0,1));
        frame.add(tabbedPane);

        // Set Panels
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new GridLayout(0,3));

        // Set Label
        JLabel label = new JLabel("WebCrawler", SwingConstants.CENTER);
        label.setSize(100, 10);
        panel.add(label);

        // Set Check Boxes
        verbose = new JCheckBox("Vebose");
        save = new JCheckBox("Save Output", true);
        savePics = new JCheckBox("Download Pictures");
        saveVids = new JCheckBox("Download Videos");
        clone = new JCheckBox("Clone Site");
        repeat = new JCheckBox("Thorough");
        checkBoxPanel.add(verbose);
        checkBoxPanel.add(save);
        checkBoxPanel.add(savePics);
        checkBoxPanel.add(saveVids);
        checkBoxPanel.add(clone);
        checkBoxPanel.add(repeat);

        // Set Layer Field
        JPanel numInput = new JPanel();
        numInput.setLayout(new FlowLayout());
        JLabel label1 = new JLabel("Layers: ");
        layers = new JTextField(2);
        layers.setToolTipText("The depth of the scan, use 99 to cover everything");
        numInput.add(label1);
        numInput.add(layers);

        // Set URL Component
        JPanel url = new JPanel();
        url.setLayout(new FlowLayout());
        JLabel label2 = new JLabel("URL: ");
        urlField = new JTextField(20);
        urlField.setToolTipText("Enter the URL of choice");
        url.add(label2);
        url.add(urlField);

        // Set Button
        button = new JButton();
        button.setText("Crawl");
        button.addActionListener(this);

        // Create a Menu Bar
        JMenuBar menuBar = new JMenuBar();

        // Create a Menu for Menu Bar
        JMenu menu = new JMenu("Options");
        JMenu help = new JMenu("Help");

        // Create Menu Items
        JMenuItem mi1 = new JMenuItem("Reset");
        JMenuItem mi2 = new JMenuItem("Save Config");
        JMenuItem mi3 = new JMenuItem("Close");
        JMenuItem mi4 = new JMenuItem("Help");
        JMenuItem mi5 = new JMenuItem("Version");

        // Add Action listener
        mi1.addActionListener(this);
        mi2.addActionListener(this);
        mi3.addActionListener(this);
        mi4.addActionListener(this);
        mi5.addActionListener(this);

        // Add Items to Menu
        menu.add(mi1);
        menu.add(mi2);
        menu.add(mi3);
        help.add(mi4);
        help.add(mi5);

        // Add to Menu Bar
        menuBar.add(menu);
        menuBar.add(help);

        // Add to Panel
        panel.add(checkBoxPanel);
        panel.add(numInput);
        panel.add(url);
        panel.add(button);

        // Add Menu and URL Component to Frame
        frame.setJMenuBar(menuBar);
        frame.add(panel);
        frame.pack();
        frame.setSize(500, 500);
        frame.setVisible(true);

        // Confirm on Close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                int result = JOptionPane.showConfirmDialog(frame,
                        "Do you want to Exit ?", "Exit Confirmation : ",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION)
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                else if (result == JOptionPane.NO_OPTION)
                    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        });
    }

    public boolean isNumber(String s) {
        for (int i = 'A'; i < 'z'; i++) {
            if (s.charAt(0) == i) return false;
        }
        return true;
    }

    // If a Button is Pressed
    @Override
    public void actionPerformed(ActionEvent e) {
        String saveCommand = "java -jar WebCrawlerV2.jar ";
        String action = e.getActionCommand();
        switch (action){
            case "Crawl":
                if (!urlField.getText().contains("://")) {
                    log.append("Error: Invalid URL! There has to be a protocol.\n");
                } else {
                    button.setText("Cancel");
                    log.append("Starting...\n");
                    WebCrawler wc;
                    Download download = new Download();
                    try {
                        wc = new WebCrawler(urlField.getText());
                    } catch (MyException ex) {
                        log.append("[ERROR] " + ex.getMessage() + "\n");
                        return;
                    }
                    download.setGui(true);
                    download.setLog(log);
                    wc.setLog(log);
                    wc.setResultLog(result);
                    wc.setVerbose(verbose.isSelected());
                    wc.setRepeat(repeat.isSelected());

                    int layersNum;
                    if (layers.getText().length() != 0 && isNumber(layers.getText()))
                        layersNum = Integer.parseInt(layers.getText());
                    else {
                        if (layers.getText().length() == 0) log.append("No layer set.");
                        else log.append("Layer is not a number!");
                        log.append(" Defaulting to 3 layers...\n");
                        layersNum = 3;
                    }


                    Runnable crawl = () -> {
                        wc.crawlLayers(layersNum);
                        if (save.isSelected()) {
                            log.append("Internal: ");
                            download.saveFile(wc.getInternalLinks(), wc.getSiteName() + "/links", "internal.txt");
                            log.append("\nExternal: ");
                            download.saveFile(wc.getExternalLinks(), wc.getSiteName() + "/links", "external.txt");
                            log.append("\n\n");
                        }
                        if (savePics.isSelected()) {
                            download.downloadImagesOrVideos(wc.getInternalLinks(), true);
                            log.append("\n");
                        }
                        if (saveVids.isSelected()) {
                            download.downloadImagesOrVideos(wc.getInternalLinks(), false);
                            log.append("\n");
                        }
                        if (clone.isSelected()) {
                            wc.makeSite(wc.getInternalLinks());
                            log.append("\n");
                        }
                        for (String link : wc.getInternalLinks()) {
                            links.append(link + "\n");
                        }
                        for (String link : wc.getExternalLinks()) {
                            extLinks.append(link + "\n");
                        }
                        log.append("Done!\n");
                        button.setText("Reset");
                    };
                    Thread t = new Thread(crawl);
                    t.start();
                }
                break;
            case "Reset": case "Cancel":
                try {
                    Runtime.getRuntime().exec(new String[]{"java", "-jar", "WebCrawlerV2.jar"});
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
                break;
            case "Save Config":
                if (!urlField.getText().contains("://")) {
                    log.append("Please enter a link first.");
                    break;
                }
                saveCommand += "-u " + urlField.getText() + " ";
                if (verbose.isSelected()) saveCommand += "-v ";
                if (repeat.isSelected()) saveCommand += "-r ";
                saveCommand += "-l " + (layers.getText().length() != 0 && isNumber(layers.getText()) ? Integer.parseInt(layers.getText()) : 5) + " ";
                if (!save.isSelected()) saveCommand += "-ds ";
                if (savePics.isSelected()) saveCommand += "-p ";
                if (saveVids.isSelected()) saveCommand += "-vi ";
                if (clone.isSelected()) saveCommand += "-cs";

                String filename = JOptionPane.showInputDialog(frame,"Enter filename:");

                boolean success = true;
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(filename + ".bat"));
                    writer.write("@echo off");
                    writer.newLine();
                    writer.write(saveCommand);
                    writer.flush();
                } catch (IOException ex) {
                    log.append("[ERROR] " + ex.getMessage() + "\n\n");
                    success = false;
                }
                if (success) log.append("Autorun File Saved Successfully!\n");
                else log.append("Autorun File could not be saved...\n");
                break;
            case "Close":
                int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to Exit?", "Exit", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    System.exit(1);
                }
                break;
            case "Help":
                String help = "Help Screen\n\n" +
                              "A web crawler is for indexing a site.\n" +
                              "It scrapes all the referencing links it\n" +
                              "finds on the seed page aka the starting\n" +
                              "page. It then goes to those links it found\n" +
                              "and repeats the process. You can control\n" +
                              "the amount of times it repeats this process\n" +
                              "by changing the layer value.\n\n" +
                              "The 'clone site' option will, as expected,\n" +
                              "clone the enter site. This will not always work\n" +
                              "and might return errors from trying to download\n" +
                              "restricted files. It also works better if you give\n" +
                              "a specific page, like, https://example.com/login/\n" +
                              "instead of just https://example.com/.";
                JOptionPane.showMessageDialog(frame,
                        help, "Help", JOptionPane.PLAIN_MESSAGE);
                break;
            case "Version":
                String version = "WebCrawler Version: 2.0\n" +
                                 "Maker: Quentin Correia\n" +
                                 "Date: 01-11-2020";
                JOptionPane.showMessageDialog(frame,
                        version, "Version", JOptionPane.PLAIN_MESSAGE);
        }
    }
}

