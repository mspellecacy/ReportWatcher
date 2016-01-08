package gov.alaska.dfg.dcf.reportwatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static String activePath = "C:\\testPath";
    private Task<Void> watcherTask;
    private Thread watcherThread;

    public void setupWatcher() throws IOException, InterruptedException {
        final Path path = new File(activePath).toPath();

        //Setup our watcher task...
        watcherTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Watcher.watchPath(path);
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

    @Override
    public void start(final Stage stage) throws Exception {

        //Setup our buttons
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        final Button changePathButton = new Button("Change Path");
        final Button stopWatcherButton = new Button("Stop Watcher");
        final Button startWatcherButton = new Button("Start Watcher");

        //Setup our labels (should probably be Text's...but whatever)
        final Label pathLabel = new Label("Active Path: ");
        pathLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        final Label activePathLabel = new Label(activePath);

        //Give our buttons something to do...
        changePathButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File newPath = directoryChooser.showDialog(stage);
                if (newPath.isDirectory()) {
                    activePath = newPath.getPath();
                    activePathLabel.setText(activePath);
                }
            }
        });

        startWatcherButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    startWatcher();
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        stopWatcherButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stopWatcher();
            }
        });

        //Setup a simple grid for our button to live in
        final GridPane buttonPane = new GridPane();
        GridPane.setConstraints(startWatcherButton, 0, 0);
        GridPane.setConstraints(stopWatcherButton, 0, 1);
        GridPane.setConstraints(changePathButton, 1, 0);
        GridPane.setConstraints(pathLabel, 0, 2);
        GridPane.setConstraints(activePathLabel, 1, 2);
        buttonPane.setHgap(6);
        buttonPane.setVgap(6);

        //Add the buttons to our container GridPane
        buttonPane.getChildren().addAll(changePathButton, startWatcherButton, stopWatcherButton);

        //Add the labels to our container GridPane
        buttonPane.getChildren().addAll(pathLabel, activePathLabel);

        //Setup our root pane...
        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(buttonPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));

        //Finally show all of our stuffs.
        stage.setTitle("DRS Report Watcher");
        stage.setScene(new Scene(rootGroup, 320, 200));
        stage.show();

    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
