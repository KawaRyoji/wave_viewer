import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.MenuBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;

public class ViewController implements Initializable {
    @FXML
    private MenuBar menuBar;
    @FXML
    private LineChart<Integer, Short> graphView;

    private Window window;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
    
    @FXML
    protected void onFileMenuClicked(ActionEvent event) {
        File chosenFile = choseFile();
        
        if (chosenFile == null)
            return;

        WaveFileLoadTask loader = new WaveFileLoadTask(chosenFile);

        loader.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, workerEvent -> {
            Short[] waveData = loader.getValue();
            drowLineChart(waveData);
        });

        loader.start();
    }
    
    private File choseFile() {
        final String INIT_FILEPASS = "./";
        window = menuBar.getScene().getWindow();

        FileChooser fc = new FileChooser();
        fc.setTitle("ファイルの選択");
        fc.getExtensionFilters().add(new ExtensionFilter("waveファイル", "*.wav"));
        fc.setInitialDirectory(new File(INIT_FILEPASS));
        return fc.showOpenDialog(window);
    }

    private void drowLineChart(Short[] waveData) {
        final int MAX_DROW = 150;

        ObservableList<XYChart.Data<Integer, Short>> seriesData = FXCollections.observableArrayList();
        for (int i = 0; i < MAX_DROW; i++) {
            seriesData.add(new XYChart.Data<Integer, Short>(i, waveData[i]));
        }

        XYChart.Series<Integer, Short> series = new XYChart.Series<>(seriesData);
        series.setName("波形データ");

        ObservableList<XYChart.Series<Integer, Short>> seriesList = FXCollections.observableArrayList();
        seriesList.add(series);

        graphView.getData().setAll(seriesList);
    }
}
