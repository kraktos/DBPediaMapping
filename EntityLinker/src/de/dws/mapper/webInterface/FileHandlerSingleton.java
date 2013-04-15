/**
 * 
 */

package de.dws.mapper.webInterface;

/**
 * This class helps to keep a file positon pointer so that when multiple clients
 * access the same file, the same line is not provided to them
 * 
 * @author Arnab Dutta
 */

public class FileHandlerSingleton {
    // stores the file position
    private static int fileLineCounter = 0;

    private static FileHandlerSingleton instance = null;

    protected FileHandlerSingleton() {
        // Exists only to defeat instantiation.
    }

    public static FileHandlerSingleton getInstance() {
        if (instance == null) {
            instance = new FileHandlerSingleton();
        }
        return instance;
    }

    /**
     * @return the fileLineCOunter
     */
    public static int getFileLineCounter() {
        return fileLineCounter;
    }

    /**
     * @param fileLineCOunter the fileLineCOunter to set
     */
    public static void setFileLineCounter(int fileLineCOunter) {
        FileHandlerSingleton.fileLineCounter = fileLineCOunter;
    }

}
