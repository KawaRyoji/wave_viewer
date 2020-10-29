import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * @author kawa ryoji
 */
public class AppMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            GridPane root = FXMLLoader.load(getClass().getResource("mainView.fxml"));

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);

            primaryStage.setTitle("Graph Viewer");
            primaryStage.setFullScreen(false);

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}