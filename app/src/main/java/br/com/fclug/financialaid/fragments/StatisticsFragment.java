package br.com.fclug.financialaid.fragments;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.database.TransactionDao;

public class StatisticsFragment extends Fragment {

    public class DateFormatter implements IAxisValueFormatter {
        List<String> mDates;

        DateFormatter(List<String> dates) {
            mDates = dates;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mDates.get((int) value);
        }
    }

    private LineChart chart;
    private PieChart pieChart;

    public StatisticsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;
        View view = inflater.inflate(R.layout.statistics_fragment, container, false);
        chart = (LineChart) view.findViewById(R.id.chart);
        pieChart = (PieChart) view.findViewById(R.id.pie_chart);
        TransactionDao dao = new TransactionDao(getContext());

        List<Map.Entry<String, Float>> values = dao.findSumOnLastSevenDays();
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        int entryNumber = 0;
        for (Map.Entry<String,Float> entry : values) {
            entries.add(new Entry(entryNumber, entry.getValue()));
            dates.add(entry.getKey());
            entryNumber++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Exemplo");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.transaction_type_debt));
        // desabilita as linhas de highlight(as amarelas, quando o usuario clica)
        //dataSet.setDrawHighlightIndicators(false);
        dataSet.setHighlightLineWidth(1f);
        //dataSet.setFillColor(R.color.transaction_type_debt);
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.transaction_type_debt));
        dataSet.setCircleRadius(5f);
        dataSet.setCircleHoleRadius(1f);
        dataSet.setValueTextSize(10f);

        // curvatura do gráfico
        //dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        LineData data = new LineData(dataSet);
        chart.setData(data);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new DateFormatter(dates));
        xAxis.setLabelRotationAngle(-45);
        xAxis.setLabelCount(dates.size(), true);

        YAxis yAxis = chart.getAxisLeft();
        //yAxis.setDrawGridLines(false);

        // hide right Y axis
        chart.getAxisRight().setEnabled(false);

        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(true);
        chart.setDrawBorders(false);
        chart.getLegend().setEnabled(false);
        chart.invalidate();

        //Pie chart

        //HashMap<String, Float> pie_entries = dao.findTransactionsPerAccount();
        List<PieEntry> pie_values = new ArrayList<>();
//        for (Map.Entry<String, Float> entry : pie_entries.entrySet()) {
//            pie_values.add(new PieEntry(entry.getValue(), entry.getKey()));
//        }

        pie_values.add(new PieEntry(18.5f, "Green"));
        pie_values.add(new PieEntry(26.7f, "Yellow"));
        pie_values.add(new PieEntry(24.0f, "Red"));
        pie_values.add(new PieEntry(30.8f, "Blue"));

        PieDataSet set = new PieDataSet(pie_values, "Expenses per account");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(set);
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);
        pieChart.setData(pieData);
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.invalidate(); // refresh

        pieChart.animateY(2000);


        return view;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
