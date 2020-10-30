package freq_view;

import java.net.URL;
import java.util.ResourceBundle;

import javax.sound.sampled.AudioFormat;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import util.Complex;
import util.Fourier;

public class FreqViewController implements Initializable {
    @FXML
    private LineChart<Integer, Double> freqView;
    @FXML
    private NumberAxis xAxis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void drowLineChart(Short[] waveData, AudioFormat format, int start, int end) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    Complex[] freqData = dft(waveData, start, end);
                    double fs = format.getSampleRate();

                    ObservableList<XYChart.Data<Integer, Double>> seriesData = FXCollections.observableArrayList();

                    for (int i = 0; i < freqData.length; i++) {
                        double f = i * fs / freqData.length - (fs / 2);
                        seriesData.add(new XYChart.Data<Integer, Double>((int)f, Complex.amp(freqData[i])));
                    }

                    XYChart.Series<Integer, Double> series = new XYChart.Series<>(seriesData);
                    ObservableList<XYChart.Series<Integer, Double>> seriesList = FXCollections.observableArrayList();
                    seriesList.add(series);
                    freqView.getData().setAll(seriesList);

                    // グラフの設定
                    double startTime = (double) start / fs;
                    double endTime = (double) end / fs;
                    freqView.setTitle("fs = " + fs + "Hz, " + startTime + "s - " + endTime + "s");
                    xAxis.setAutoRanging(false);
                    xAxis.setLowerBound(- fs / 2);
                    xAxis.setUpperBound(fs / 2);
                    xAxis.setTickUnit(fs / 16);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 後にFFTの実装に変える
    private Complex[] dft(Short[] waveData, int start, int end) {
        short[] useData = new short[end - start];

        for (int i = 0; i < useData.length; i++) {
            useData[i] = waveData[i + start];
        }

        return Fourier.DFT(useData, useData.length);
    }
}
