package com.dorne.springboot.jxbrowser;

import com.dorne.springboot.jxbrowser.swing.AppPrincipalFrame;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.teamdev.jxbrowser.chromium.be;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;

@SpringBootApplication
public class MainApplication {

    static {
        try {
            Field e = be.class.getDeclaredField("e");
            e.setAccessible(true);
            Field f = be.class.getDeclaredField("f");
            f.setAccessible(true);
            Field modifersField = Field.class.getDeclaredField("modifiers");
            modifersField.setAccessible(true);
            modifersField.setInt(e, e.getModifiers() & ~Modifier.FINAL);
            modifersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            e.set(null, new BigInteger("1"));
            f.set(null, new BigInteger("1"));
            modifersField.setAccessible(false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(MainApplication.class).headless(false).run(args);
        context.getBean(AppPrincipalFrame.class);
    }

}
