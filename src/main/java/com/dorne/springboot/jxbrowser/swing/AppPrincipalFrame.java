package com.dorne.springboot.jxbrowser.swing;

import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.*;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.teamdev.jxbrowser.chromium.swing.DefaultDialogHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
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

    public JFrame frame;
    public Browser browser;
    public Browser browserDebug;

    @PreDestroy
    public void onDestroy() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                browser.getCacheStorage().clearCache();
                browser.dispose();
                if (browserDebug != null){
                    browserDebug.getCacheStorage().clearCache();
                    browserDebug.dispose();
                }

            }
        }).start();
        System.out.println("Spring Container is destroyed!");
    }

    @Override
    public void run(String... arg0) throws Exception {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (debugger) {
                        BrowserPreferences.setChromiumSwitches("--remote-debugging-port="+debuggingPort);
                    }
                    frame = new JFrame();
                    frame.setTitle(title);
                    browser = new Browser(BrowserType.HEAVYWEIGHT);
                    BrowserView view = new BrowserView(browser);
                    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    frame.add(view, BorderLayout.CENTER);
                    frame.setSize(width, height);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(false);
                    String css =
                            "*, body { " +
                                    "font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", \"PingFang SC\", \"Hiragino Sans GB\", \"Microsoft YaHei\", \"Helvetica Neue\", Helvetica, Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\";" +
                                    "font-size: 14px; " +
                                    "font-weight: normal;" +
                            "}";
                    System.out.println("css");
                    System.out.println(css);
                    browser.setCustomStyleSheet(css);
                    browser.loadURL(index);

                    if (debugger) {
                        String remoteDebuggingURL = browser.getRemoteDebuggingURL();
                        browserDebug = new Browser();
                        BrowserView browserViewDebug = new BrowserView(browserDebug);
                        JFrame frameDebug = new JFrame();
                        frameDebug.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                        frameDebug.add(browserViewDebug, BorderLayout.CENTER);
                        frameDebug.setSize(width, height);
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

                    browser.addLoadListener(new LoadAdapter() {
                        @Override
                        public void onStartLoadingFrame(StartLoadingEvent event) {
                            if (event.isMainFrame()) {
                                System.out.println("Main frame has started loading");
                            }
                        }

                        @Override
                        public void onProvisionalLoadingFrame(ProvisionalLoadingEvent event) {
                            if (event.isMainFrame()) {
                                System.out.println("Provisional load was committed for a frame");
                            }
                        }

                        @Override
                        public void onFinishLoadingFrame(FinishLoadingEvent event) {
                            if (event.isMainFrame()) {
                                System.out.println("<<<<-----Main frame has finished loading------>>>>");
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                }
                                frame.setVisible(true);
                            }
                        }

                        @Override
                        public void onFailLoadingFrame(FailLoadingEvent event) {
                            NetError errorCode = event.getErrorCode();
                            if (event.isMainFrame()) {
                                System.out.println("Main frame has failed loading: " + errorCode);
                            }
                        }

                        @Override
                        public void onDocumentLoadedInFrame(FrameLoadEvent event) {
                            System.out.println("Frame document is loaded.");
                        }

                        @Override
                        public void onDocumentLoadedInMainFrame(LoadEvent event) {
                            System.out.println("Main frame document is loaded.");
                        }
                    });

                    //js调用java
                    browser.addScriptContextListener(new ScriptContextAdapter() {
                        @Override
                        public void onScriptContextCreated(ScriptContextEvent event) {
                            Browser browser = event.getBrowser();
                            JSValue window = browser.executeJavaScriptAndReturnValue("window");
                            window.asObject().setProperty("JAVA_utils", new Utils());
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
