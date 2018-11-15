package at.sschmid.hcc.sbv1.utility;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.util.Map;

public class LineChart extends ApplicationFrame {
  
  private static final int WIDTH = 1600;
//  private static final Integer STEP_WIDTH = 40;
//  private static final Integer CHART_START = null;
//  private static final Integer CHART_END = null;
private static final Integer STEP_WIDTH = 20;
  private static final Integer CHART_START = 0;
  private static final Integer CHART_END = 20000;
  
  private final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
  private final Map<String, int[]> data;
  
  public LineChart(final String name, final Map<String, int[]> data) {
    super(name);
    
    this.data = data;
    
    initGui(name);
    initFrame();

//    updateDataSet(dataSet);
    updateDataSetAsync();
  }
  
  public void initFrame() {
    pack();
    RefineryUtilities.centerFrameOnScreen(this);
    setVisible(true);
  }
  
  private void initGui(final String name) {
    final JFreeChart lineChart = ChartFactory.createLineChart(
        name,
        "Time", "Value",
        dataSet,
        PlotOrientation.VERTICAL,
        true, false, false);
    
    final ChartPanel chartPanel = new ChartPanel(lineChart);
    chartPanel.setPreferredSize(new Dimension(WIDTH, WIDTH / 16 * 9));
    setContentPane(chartPanel);
  }

//  private void updateDataSet(final DefaultCategoryDataset dataSet) {
//    final int[] mps = getChartValues();
//    final int start = CHART_START != null ? Math.max(CHART_START, 0) : 0;
//    final int end = CHART_END != null ? Math.min(CHART_END, mps.length) : mps.length;
//    final int stepWidth = STEP_WIDTH; // end / start;
//    for (int i = start; i < end; i += stepWidth) {
//      dataSet.addValue(mps[i], "ECG", Integer.toString(i));
//    }
//  }
  
  private void updateDataSetAsync() {
    data.forEach((rowKey, values) -> {
      final Thread t = new Thread(() -> {
        final int start = CHART_START != null ? Math.max(CHART_START, 0) : 0;
        final int end = CHART_END != null ? Math.min(CHART_END, values.length) : values.length;
        final int stepWidth = STEP_WIDTH; // end / start;
        for (int i = start; i < end; i += stepWidth) {
          final int idx = i;
          EventQueue.invokeLater(() -> dataSet.addValue(values[idx], rowKey, Integer.toString(idx)));
        }
      });
  
      t.start();
    });
  }
  
}
