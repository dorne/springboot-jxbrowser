package com.dorne.springboot.jxbrowser.swing;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

@Component
public class Utils {

    public String jarHome(){
        ApplicationHome applicationHome = new ApplicationHome();
        System.out.println(System.getProperty("java.home"));
        System.out.println(System.getProperty("user.dir"));
        System.out.println(applicationHome.getDir().getAbsolutePath());
        return applicationHome.getDir().getAbsolutePath();
    }

}
