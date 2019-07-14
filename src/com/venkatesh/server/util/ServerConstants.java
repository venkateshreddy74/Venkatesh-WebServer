package com.venkatesh.server.util;

import java.io.File;

public class ServerConstants {

    public static final File WEB_ROOT = new File("/Users/venkatesh/IdeaProjects/Venkatesh-WebServer/src/com/venkatesh/server");
    public static final String DEFAULT_FILE = "index.html";
    public static final String FILE_NOT_FOUND = "404.html";
    public static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    //ports
    public static  int PORT = 8080;

    //logging
    public static boolean ENABLE_LOGGING = true;

}
