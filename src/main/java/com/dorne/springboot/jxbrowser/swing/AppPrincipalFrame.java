package com.dorne.springboot.jxbrowser.swing;

import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.teamdev.jxbrowser.chromium.swing.DefaultDialogHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

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

                    Browser browserDebug = null;
                    if (debugger) {
                        String remoteDebuggingURL = browser.getRemoteDebuggingURL();
                        browserDebug = new Browser();
                        BrowserView browserViewDebug = new BrowserView(browserDebug);
                        JFrame frameDebug = new JFrame();
                        frameDebug.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                        frameDebug.add(browserViewDebug, BorderLayout.CENTER);
                        frameDebug.setSize(700, 500);
                        frameDebug.setLocationRelativeTo(null);
                        frameDebug.setVisible(true);
                        browserDebug.loadURL(remoteDebuggingURL);
                    }

                    //选择文件
                    browser.setDialogHandler(new DefaultDialogHandler(view) {
                        @Override
                        public CloseStatus onFileChooser(final FileChooserParams params) {
                            final AtomicReference<CloseStatus> result = new AtomicReference<CloseStatus>(
                                    CloseStatus.CANCEL);

                            try {
                                SwingUtilities.invokeAndWait(new Runnable() {
                                    @Override
                                    public void run() {
                                            System.out.println("params.getMode(): "+params.getMode());
                                            JFileChooser fileChooser = new JFileChooser();
                                            if (params.getMode() == FileChooserMode.OpenFolder){
                                                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                            }else {
                                                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                            }
                                            if (fileChooser.showOpenDialog(view)
                                                    == JFileChooser.APPROVE_OPTION) {
                                                File selectedFile = fileChooser.getSelectedFile();
                                                params.setSelectedFiles(selectedFile.getAbsolutePath());
                                                browser.executeJavaScript("window.__JFileChooser = '"+selectedFile.getAbsolutePath()+"';");
                                                result.set(CloseStatus.OK);
                                            }
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }

                            return result.get();
                        }
                    });

                    browser.addScriptContextListener(new ScriptContextAdapter() {
                        @Override
                        public void onScriptContextCreated(ScriptContextEvent event) {
                            Browser browser = event.getBrowser();
                            JSValue window = browser.executeJavaScriptAndReturnValue("window");
                            window.asObject().setProperty("JAVA_utils", new Utils());
                        }
                    });


                    Browser finalBrowserDebug = browserDebug;
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
                                    if (finalBrowserDebug != null) {
                                        finalBrowserDebug.dispose();
                                    }
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
