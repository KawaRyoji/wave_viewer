package main_view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import freq_view.FreqViewController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;

public class ViewController implements Initializable {
    @FXML
    private MenuBar menuBar;
    @FXML
    private LineChart<Integer, Short> graphView;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private Slider horizontalSlider;
    @FXML
    private TextField horizontalSliderValue;
    @FXML
    private ToggleGroup selectRange;
    @FXML
    private RadioButton range256;
    @FXML
    private RadioButton range512;
    @FXML
    private RadioButton range1024;
    @FXML
    private RadioButton range2056;
    @FXML
    private Button viewFreq;

    private Window window;
    private Short[] waveData;
    private AudioFormat format;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setDisableProperty(false);
        selectRange.selectedToggleProperty().addListener(s -> handleSelectRange());
    }

    @FXML
    protected void onFileMenuClicked(ActionEvent event) {
        File chosenFile = choseFile();

        if (chosenFile == null)
            return;

        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(chosenFile);
            format = ais.getFormat();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }

        WaveFileLoadTask loader = new WaveFileLoadTask(chosenFile);

        loader.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, workerEvent -> {
            waveData = loader.getValue();
            drowLineChart();

            graphView.setTitle(chosenFile.getName());

            initSliderProperty(0, waveData.length - getRange(), 0);
            horizontalSliderValue.setText(String.valueOf((int) horizontalSlider.getValue()));
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

    private void drowLineChart() {
        setDisableProperty(true);

        lineChartClear();

        int start = getOffset();
        int end = start + getRange();

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

                    xAxis.setLowerBound(start);
                    xAxis.setUpperBound(end);
                    xAxis.setTickUnit(getRange() / 16);
                    initSliderProperty(0, waveData.length - getRange(), start);
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
        horizontalSliderValue.setText(String.valueOf((int) horizontalSlider.getValue()));

        drowLineChart();
    }

    private void initSliderProperty(int min, int max, int ini) {
        horizontalSlider.setMin(min);
        horizontalSlider.setMax(max);
        horizontalSlider.setMajorTickUnit((max - min) / 4);
        horizontalSlider.setValue(ini);
    }

    /** グラフを表示したかでdisablePropertyを操作する */
    private void setDisableProperty(boolean isGraphPainted) {
        horizontalSlider.setDisable(!isGraphPainted);
        range256.setDisable(!isGraphPainted);
        range512.setDisable(!isGraphPainted);
        range1024.setDisable(!isGraphPainted);
        range2056.setDisable(!isGraphPainted);
        horizontalSliderValue.setDisable(!isGraphPainted);
        viewFreq.setDisable(!isGraphPainted);
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

            drowLineChart();
        } catch (Exception e) {
            horizontalSliderValue.setText(String.valueOf((int) horizontalSlider.getValue()));
            return;
        }
    }

    private void handleSelectRange() {
        drowLineChart();
    }

    /** グラフの表示範囲を取得 */
    private int getRange() {
        RadioButton range = (RadioButton) selectRange.getSelectedToggle();
        int num = 0;
        try {
            num = Integer.parseInt(range.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return num;
    }

    /** グラフの表示位置を取得 */
    private int getOffset() {
        return (int) horizontalSlider.getValue();
    }

    @FXML
    protected void handleViewFreqButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../freq_view/freqView.fxml"));
            GridPane pane = loader.load();
            
            FreqViewController controller = loader.getController();
            controller.drowLineChart(waveData, format, getOffset(), getOffset() + getRange());
            
            Scene scene = new Scene(pane, 600, 400);
            Stage sub = new Stage();
            sub.setTitle("Frequency View");
            sub.setScene(scene);
            sub.initOwner(window);
            sub.setFullScreen(false);
            sub.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
