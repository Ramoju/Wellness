package com.example.pharma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.example.pharma.Model.CartModel;
import com.example.pharma.Model.ProductModel;
import com.example.pharma.Model.SalesModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class SalesReportsActivity extends AppCompatActivity {

    BarChart barChart;
    DatabaseReference reference;
    FirebaseDatabase rootNode;
    ArrayList<SalesModel> saleslist = new ArrayList<>();
    ArrayList<String> products = new ArrayList<>();
    ArrayList<Double> quantities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_reports);

        Window window = SalesReportsActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(SalesReportsActivity.this, R.color.black));
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference(SalesModel.class.getSimpleName());
        barChart = findViewById(R.id.barchart);

        ValueEventListener vle = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    SalesModel saleitem = new SalesModel();
                    saleitem = ds.getValue(SalesModel.class);
                    System.out.println(saleitem.getProductName());
                    quantities.add(Double.valueOf(saleitem.getQuantitysold()));
                    products.add(saleitem.getProductName());
                    saleslist.add(saleitem);
                    System.out.println(saleslist.size() + "%%%%%%%%%%%%%%%");
                }
                ArrayList<BarEntry> sales = new ArrayList<>();
                ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
                for(int i=0; i < saleslist.size(); i++) {
                    BarEntry entry = new BarEntry(i+1, saleslist.get(i).getQuantitysold());
                    yVals.add(entry);
                }
                XAxis xAxis = barChart.getXAxis();
                xAxis.enableGridDashedLine(10f, 10f, 0f);
                xAxis.setTextColor(Color.BLACK);
                xAxis.setTextSize(10);
                xAxis.setDrawAxisLine(true);
                xAxis.setAxisLineColor(Color.BLACK);
                xAxis.setDrawGridLines(true);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setXOffset(0f); //labels x offset in dps
                xAxis.setYOffset(0f); //labels y offset in dps
                xAxis.setValueFormatter(new IndexAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        System.out.println(value + "X axis value");
                        return saleslist.get((int) value-1).getProductName();
                    }
                });
                YAxis rightAxis = barChart.getAxisRight();
                rightAxis.setTextColor(Color.BLACK);
                rightAxis.setTextSize(14);
                rightAxis.setDrawAxisLine(true);
                rightAxis.setAxisLineColor(Color.BLACK);
                rightAxis.setDrawGridLines(true);
                rightAxis.setGranularity(1f);
                rightAxis.setGranularityEnabled(true);
//                rightAxis.setAxisMinimum(0);
//                rightAxis.setAxisMaximum(1500f);
                rightAxis.setLabelCount(4, true); //labels (Y-Values) for 4 horizontal grid lines
                rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

                YAxis leftAxis = barChart.getAxisLeft();
                leftAxis.setAxisMinimum(0);
                leftAxis.setDrawAxisLine(true);
                leftAxis.setLabelCount(0, true);
                leftAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return "";
                    }
                });

                BarDataSet barDataSet = new BarDataSet(yVals, "Sales");
                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                barDataSet.setValueTextColor(Color.BLACK);
                barDataSet.setValueTextSize(16f);

                BarData barData = new BarData(barDataSet);

                barChart.setFitBars(true);
                barChart.setData(barData);
                barChart.getDescription().setText("Sales graph");
                barChart.setBackgroundColor(Color.WHITE);
                barChart.setXAxisRenderer(new XAxisRenderer(barChart.getViewPortHandler(), barChart.getXAxis(), barChart.getTransformer(YAxis.AxisDependency.LEFT)){
                    @Override
                    protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
                        Utils.drawXAxisValue(c, formattedLabel, x+Utils.convertDpToPixel(5f), y+ Utils.convertDpToPixel(1f), mAxisLabelPaint, anchor, angleDegrees);
                    }
                });
                barChart.invalidate();
                barChart.animateY(3000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        reference.addValueEventListener(vle);

    }
}