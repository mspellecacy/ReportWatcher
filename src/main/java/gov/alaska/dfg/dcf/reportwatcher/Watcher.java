package gov.alaska.dfg.dcf.reportwatcher;

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

/**
 *
 * @author mhspellecacy
 */
public class Watcher {
    
    //Liberally lifted from Oracle Docs and stackoverflow.
    public static void watchPath(Path path) throws IOException, InterruptedException {
        
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
}
