package com.dorne.springboot.jxbrowser.swing;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

@Component
public class AppPrincipalFrame extends JFrame implements CommandLineRunner {

    @Value( "${dorne.swing.title}" )
    private String title;

    @Value( "${dorne.swing.height}" )
    private int height;

    @Value( "${dorne.swing.width}" )
    private int width;

    @Value( "${dorne.jxbrowser.index}" )
    private String index;

    @Value( "${dorne.jxbrowser.debugger}" )
    private boolean debugger;

    @Value( "${dorne.jxbrowser.debugging-port}" )
    private String debuggingPort;

    private JPanel contentPane;

    @Override
    public void run(String... arg0) throws Exception {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (debugger) {
                        BrowserPreferences.setChromiumSwitches("--remote-debugging-port="+debuggingPort);
                    }

                    AppPrincipalFrame frame = new AppPrincipalFrame();
                    frame.setTitle(title);
                    Browser browser = new Browser();
                    BrowserView view = new BrowserView(browser);
                    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    frame.add(view, BorderLayout.CENTER);
                    frame.setSize(width, height);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                    browser.loadURL(index);

                    if (debugger) {
                        String remoteDebuggingURL = browser.getRemoteDebuggingURL();
                        Browser browserDebug = new Browser();
                        BrowserView browserViewDebug = new BrowserView(browserDebug);
                        JFrame frameDebug = new JFrame();
                        frameDebug.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                        frameDebug.add(browserViewDebug, BorderLayout.CENTER);
                        frameDebug.setSize(700, 500);
                        frameDebug.setLocationRelativeTo(null);
                        frameDebug.setVisible(true);
                        browserDebug.loadURL(remoteDebuggingURL);
                    }

                    frame.addWindowListener(new WindowListener() {
                        @Override
                        public void windowOpened(WindowEvent e) {

                        }

                        @Override
                        public void windowClosing(WindowEvent e) {
                            System.out.println("windowClosing");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    browser.dispose();
                                }
                            }).start();
                        }

                        @Override
                        public void windowClosed(WindowEvent e) {

                        }

                        @Override
                        public void windowIconified(WindowEvent e) {

                        }

                        @Override
                        public void windowDeiconified(WindowEvent e) {

                        }

                        @Override
                        public void windowActivated(WindowEvent e) {

                        }

                        @Override
                        public void windowDeactivated(WindowEvent e) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
