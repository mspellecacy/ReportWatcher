package gov.alaska.dfg.dcf.reportwatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javafx.concurrent.Task;

/**
 *
 * @author mhspellecacy
 */
public class Watcher {
    private String activePath = "C:\\testPath";
    private Task<Void> watcherTask;
    private Thread watcherThread;
    
    public Watcher(String path) {
        this.activePath = path;
    }
    
    public void setupWatcher() throws IOException, InterruptedException {
        final Path path = new File(activePath).toPath();

        //Setup our watcher task...
        watcherTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                watchPath(path);
                return null;
            }
        };

        //Setup our watcher thread...
        watcherThread = new Thread(watcherTask);
        watcherThread.setDaemon(true);

    }

    public boolean startWatcher() throws IOException, InterruptedException {
        boolean watcherStarted = false;
        //Always stop the Watcher Thread before starting up a new one...
        stopWatcher();

        //Setup our new Watcher Thread. 
        setupWatcher();

        if (!watcherThread.isAlive()) {
            watcherThread.start();
            watcherStarted = true;
        }

        return watcherStarted;
    }

    public boolean stopWatcher() {
        boolean watcherStopped = false;

        if (watcherThread != null) {
            if (watcherThread.isAlive()) {
                watcherTask.cancel();
                watcherThread.interrupt();
                watcherTask = null;
                watcherThread = null;
                watcherStopped = true;
            }
        } else {
            watcherStopped = true;
        }

        System.out.println("Watcher Stopped.");
        return watcherStopped;

    }
    
    public boolean isRunning(){
        return watcherTask.isRunning();
    }
    
    //Liberally lifted from Oracle Docs and stackoverflow.
    @SuppressWarnings("unchecked")
    public void watchPath(Path path) throws IOException, InterruptedException {

        //Check if the path is legit first...
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path, "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path + " is not a folder");
            }
        } catch (IOException ioe) {
            //Path does not exist...
            ioe.printStackTrace();
        }

        System.out.println("Watching path: " + path);

        FileSystem fs = path.getFileSystem();

        //Create the new watch service in try block...
        try (WatchService service = fs.newWatchService()) {

            //Start Listening for new files...
            path.register(service, ENTRY_CREATE);

            WatchKey key = null;

            while (true) {
                key = service.take();

                //Dequeue events ...
                Kind<?> kind = null;

                for (WatchEvent<?> watchEvent : key.pollEvents()) {

                    //Fetch Even Type
                    kind = watchEvent.kind();

                    //Do work based on Event Type.
                    //In this case we only care about new files coming in.
                    if (OVERFLOW == kind) {
                        continue;
                    } else if (ENTRY_CREATE == kind) {
                        Path newReportPath = ((WatchEvent<Path>) watchEvent).context();

                        System.out.println("Found new path: " + newReportPath.getFileName().toString());
                    }

                }

                //Keep watching unless we're told otherwise. 
                if (!key.reset()) {
                    break; // loop
                }

            }

        }

    }

    public String getActivePath() {
        return activePath;
    }

    public void setActivePath(String activePath) {
        this.activePath = activePath;
    }
    
    
}
