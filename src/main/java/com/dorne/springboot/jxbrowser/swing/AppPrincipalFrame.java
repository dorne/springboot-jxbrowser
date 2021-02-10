package com.dorne.springboot.jxbrowser.swing;

import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
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

    Browser browser;
    Browser browserDebug;

    @PreDestroy
    public void onDestroy() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                browser.dispose();
                browserDebug.dispose();
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

                    AppPrincipalFrame frame = new AppPrincipalFrame();
                    frame.setTitle(title);
                    browser = new Browser();
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
