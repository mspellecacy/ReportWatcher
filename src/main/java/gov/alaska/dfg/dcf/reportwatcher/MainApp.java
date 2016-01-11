package gov.alaska.dfg.dcf.reportwatcher;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.joda.time.DateTime;

public class MainApp extends Application {

    private static final String DEFAULT_PATH = "C:\\testPath";
    private static final Watcher watcher = new Watcher(DEFAULT_PATH);

    //Setup our buttons
    final DirectoryChooser directoryChooser = new DirectoryChooser();
    final Button changePathButton = new Button("Change Path");
    final Button stopWatcherButton = new Button("Stop Watcher");
    final Button startWatcherButton = new Button("Start Watcher");

    //Setup our labels (should probably be Text's...but whatever)
    final Label pathLabel = new Label("Active Path: ");
    final Label activePathLabel = new Label(watcher.getActivePath());

    //Setup our TextArea for logging output.
    final TextArea logTextArea = new TextArea();

    public void updateLogger(String logEvent) {
        String outputFormat = "%1$tF %1$tT | %2$s \n";

        logTextArea.insertText(0, String.format(outputFormat,
          new DateTime().toDate(), logEvent));

    }

    @Override
    public void start(final Stage stage) throws Exception {

        //Do some UI component tweaking...
        pathLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        logTextArea.setMaxWidth(400);
        logTextArea.setEditable(false);

        //Give our buttons something to do...
        changePathButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File newPath = directoryChooser.showDialog(stage);
                if (newPath.isDirectory()) {
                    watcher.setActivePath(newPath.getPath());
                    activePathLabel.setText(watcher.getActivePath());
                }
            }
        });

        startWatcherButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    watcher.startWatcher();
                    updateLogger("Starting Watcher");
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        stopWatcherButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                updateLogger("Stopping Watcher");
                watcher.stopWatcher();
            }
        });

        //Setup a simple grid for our buttons, labels and text boxes to live in.
        final GridPane containerPane = new GridPane();
        //Grid Row 0
        GridPane.setConstraints(pathLabel, 0, 0);
        GridPane.setConstraints(activePathLabel, 1, 0);
        
        //Grid Row 1
        GridPane.setConstraints(startWatcherButton, 0, 1);
        GridPane.setConstraints(stopWatcherButton, 1, 1);
        GridPane.setConstraints(changePathButton, 2, 1);
        
        //Grid Row 2
        GridPane.setConstraints(logTextArea, 0, 2);
        GridPane.setColumnSpan(logTextArea, 3);
        
        //Container Pane component tweaks...
        containerPane.setHgap(6);
        containerPane.setVgap(6);

        //Add the buttons to our container GridPane
        containerPane.getChildren().addAll(changePathButton, startWatcherButton, stopWatcherButton);

        //Add the labels to our container GridPane
        containerPane.getChildren().addAll(pathLabel, activePathLabel);

        //Add the logging Text Area to our container GridPane
        containerPane.getChildren().add(logTextArea);

        //Setup our root pane...
        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(containerPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));

        //Finally show all of our stuffs.
        stage.setTitle("DRS Report Watcher");
        stage.setScene(new Scene(rootGroup, 500, 200));
        stage.show();

        //Just to let our user(s) know...
        updateLogger("Ready");
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
