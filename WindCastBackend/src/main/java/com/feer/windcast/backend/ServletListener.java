package com.feer.windcast.backend;

import com.googlecode.objectify.ObjectifyService;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by Reef on 15/10/2015.
 */
public class ServletListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(ServletContextListener.class.getName());
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ObjectifyService.begin();
        ObjectifyService.register(StationsInUpdate.class);
        ObjectifyService.register(LastUpdateResult.class);
        log.info("new ServletContextListener contextInitialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
