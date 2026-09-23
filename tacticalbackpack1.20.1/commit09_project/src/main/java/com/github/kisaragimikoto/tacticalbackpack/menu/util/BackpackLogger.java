package com.github.kisaragimikoto.tacticalbackpack.util;

import org.slf4j.Logger;

public final class BackpackLogger {

    private static Logger LOGGER;

    private BackpackLogger(){}

    public static void init(Logger logger){
        LOGGER = logger;
    }

    public static void info(String msg){
        if(LOGGER!=null){
            LOGGER.info("[Backpack] {}",msg);
        }
    }

    public static void warn(String msg){
        if(LOGGER!=null){
            LOGGER.warn("[Backpack] {}",msg);
        }
    }

    public static void error(String msg){
        if(LOGGER!=null){
            LOGGER.error("[Backpack] {}",msg);
        }
    }

}