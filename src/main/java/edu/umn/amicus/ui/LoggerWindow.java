package edu.umn.amicus.ui;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gpfinley on 3/10/17.
 */
public class LoggerWindow extends JFrame {

    private final static Logger LOGGER = Logger.getLogger(LoggerWindow.class.getName());

    private static class LoggingStream extends OutputStream {
        private JTextArea textArea;
        public LoggingStream(JTextArea textArea) {
            this.textArea = textArea;
        }
        @Override
        public void write(int b) throws IOException {
            // redirects data to the text area
            textArea.append(String.valueOf((char) b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    private LoggerWindow() {
        super("AMICUS");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1500, 480);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        add(new JScrollPane(textArea));

        PrintStream printStream = new PrintStream(new LoggingStream(textArea));

        System.setOut(printStream);
        System.setErr(printStream);
    }

    private File[] getFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setVisible(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
//        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        return fileChooser.getSelectedFiles();
    }

    public String[] getAllFilePaths() {
        List<String> allFilePaths = new ArrayList<>();
        for (File file : getFiles()) {
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    if (!subFile.isDirectory()) {
                        addConfigFile(subFile, allFilePaths);
                    }
                }
            } else {
                addConfigFile(file, allFilePaths);
            }
        }
        return allFilePaths.toArray(new String[allFilePaths.size()]);
    }

    private static void addConfigFile(File file, List<String> allFilePaths) {
        if (file.getName().startsWith(".")) return;
        if (file.getName().toLowerCase().endsWith("yml")) {
            allFilePaths.add(file.getAbsolutePath());
        } else {
            LOGGER.warning("Not loading a pipeline from " + file.getName() + "; only .yml files are considered.");
        }
    }

    public static String[] useLoggingWindowAndGetPaths() throws Exception {
        final List<String> paths = new ArrayList<>();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                LoggerWindow loggerWindow = new LoggerWindow();
                loggerWindow.setVisible(true);
                Collections.addAll(paths, loggerWindow.getAllFilePaths());
            }
        });
        return paths.toArray(new String[paths.size()]);
    }

    public static void useGuiLoggingWindow() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoggerWindow().setVisible(true);
            }
        });
    }

}
