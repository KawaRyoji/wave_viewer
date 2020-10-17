import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;

public class ViewController implements Initializable {
    @FXML
    private MenuBar menuBar;
    @FXML
    private LineChart<Integer, Short> graphView;
    @FXML
    private Slider horizontalSlider;
    @FXML
    private Slider rangeSlider;
    @FXML
    private TextField horizontalSliderValue;
    @FXML
    private TextField rangeSliderValue;

    private Window window;
    private Short[] waveData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setDisableProperty(false);
    }
    
    @FXML
    protected void onFileMenuClicked(ActionEvent event) {
        final int MIN_DROW_INDEX = 150;
        final int MAX_DROW_INDEX = 15000;

        File chosenFile = choseFile();
        
        if (chosenFile == null)
            return;

        WaveFileLoadTask loader = new WaveFileLoadTask(chosenFile);

        loader.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, workerEvent -> {
            waveData = loader.getValue();
            drowLineChart(0, MIN_DROW_INDEX);

            graphView.setTitle(chosenFile.getName());
            initSliderProperty(rangeSlider, MIN_DROW_INDEX, MAX_DROW_INDEX);
            initSliderProperty(horizontalSlider, 0, waveData.length);
            horizontalSliderValue.setText(String.valueOf((int) horizontalSlider.getValue()));
            rangeSliderValue.setText(String.valueOf((int) rangeSlider.getValue()));
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

    private void drowLineChart(int start, int end) {
        setDisableProperty(true);

        lineChartClear();

        // 範囲をチェック
        int start_normal = Math.max(start, 0);
        int end_normal = Math.min(end, waveData.length);

        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    ObservableList<XYChart.Data<Integer, Short>> seriesData = FXCollections.observableArrayList();

                    for (int i = start_normal; i < end_normal; i++) {
                        seriesData.add(new XYChart.Data<Integer, Short>(i, waveData[i]));
                    }

                    XYChart.Series<Integer, Short> series = new XYChart.Series<>(seriesData);
                    series.setName("波形データ");

                    ObservableList<XYChart.Series<Integer, Short>> seriesList = FXCollections.observableArrayList();
                    seriesList.add(series);
                    graphView.getData().setAll(seriesList);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // これをしないとメモリリークが起こるかも？
    private void lineChartClear() {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    for (XYChart.Series<Integer, Short> series : graphView.getData()) {
                        series.getData().clear();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    protected void handleSlider(MouseEvent event) {
        int min = (int) horizontalSlider.getValue();
        int max = min + (int) rangeSlider.getValue();
        horizontalSliderValue.setText(String.valueOf(min));
        rangeSliderValue.setText(String.valueOf((int) rangeSlider.getValue()));

        drowLineChart(min, max);
    }

    private void initSliderProperty(Slider slider, int min, int max) {
        slider.setMin(min);
        slider.setMax(max);
        slider.setMajorTickUnit((max - min) / 4);
        slider.setValue(min);
    }

    private void setDisableProperty(boolean isGraphPainted) {
        horizontalSlider.setDisable(!isGraphPainted);
        rangeSlider.setDisable(!isGraphPainted);
        horizontalSliderValue.setDisable(!isGraphPainted);
        rangeSliderValue.setDisable(!isGraphPainted);
    }

    @FXML
    protected void handleHorizontalSliderValueField(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            return;

        String text = horizontalSliderValue.getText();
        try {
            int value = Integer.parseInt(text);

            value = Math.max((int) horizontalSlider.getMin(), value);
            value = Math.min((int) horizontalSlider.getMax(), value);

            horizontalSlider.setValue(value);
            horizontalSliderValue.setText(String.valueOf(value));

            int min = (int) horizontalSlider.getValue();
            int max = min + (int) rangeSlider.getValue();
            drowLineChart(min, max);
        } catch (Exception e) {
            horizontalSliderValue.setText(String.valueOf((int) horizontalSlider.getValue()));
            return;
        }
    }
    
    @FXML
    protected void handleRangeSliderValueField(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER)
            return;

        String text = rangeSliderValue.getText();
        try {
            int value = Integer.parseInt(text);

            value = Math.max((int) rangeSlider.getMin(), value);
            value = Math.min((int) rangeSlider.getMax(), value);

            rangeSlider.setValue(value);
            rangeSliderValue.setText(String.valueOf(value));

            int min = (int) horizontalSlider.getValue();
            int max = min + (int) rangeSlider.getValue();
            drowLineChart(min, max);
        } catch (Exception e) {
            rangeSliderValue.setText(String.valueOf((int) rangeSlider.getValue()));
            return;
        }
    }
}
