package com.kkwakjavacoding.kcalendar.activity


import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AbsSpinner
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.kkwakjavacoding.kcalendar.databinding.ActivityGraphBinding


class GraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding
    private lateinit var monthadapter: ArrayAdapter<String>
    private val chartData = ArrayList<ChartData>()
    var mon: String = "1월"

    var month = arrayOf<String>("1월", "2월", "3월", "4월", "5월", "6월", "7월",
        "8월", "9월", "10월", "11월", "12월")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        chartData.clear()
        addChartItem("5일", 67.9)
        addChartItem("10일", 68.6)
        addChartItem("15일", 68.3)
        addChartItem("20일", 68.1)
        addChartItem("25일", 67.3)
        addChartItem("30일", 66.5)

        LineChartGraph(chartData)

    }

    private fun init(){
        monthadapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, month)
        binding.monthSpinner.adapter = monthadapter


        binding.monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mon = binding.monthSpinner.selectedItem.toString()
                LineChartGraph(chartData)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

    }


    private fun addChartItem(lableitem: String, dataitem: Double) {
        val item = ChartData(lableitem, dataitem)
        chartData.add(item)
    }

    private fun LineChartGraph(chartItem: ArrayList<ChartData>) {
        val entries = ArrayList<Entry>()

        for (i in chartItem.indices) {
            entries.add(Entry(chartItem[i].weight.toFloat(), i))
        }

        //LineDataSet 선언
        val lineDataSet: LineDataSet
        lineDataSet = LineDataSet(entries, mon)
        lineDataSet.color = Color.BLUE  //LineChart에서 Line Color 설정
        lineDataSet.setCircleColor(Color.DKGRAY)  // LineChart에서 Line Circle Color 설정
        lineDataSet.setCircleColorHole(Color.DKGRAY) // LineChart에서 Line Hole Circle Color 설정


        val labels = ArrayList<String>()
        for (i in chartItem.indices) {
            labels.add(chartItem[i].date)
        }


        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet as ILineDataSet)

        val data = LineData(labels, dataSets)
        data.setValueTextSize(10f)


        val xAxis = binding.lineChart.getXAxis(); // x 축 설정
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //x 축 표시에 대한 위치 설정

        val YAxis  = binding.lineChart.getAxisRight(); //Y축의 오른쪽면 설정
        YAxis.setDrawLabels(false);
        YAxis.setDrawAxisLine(false);
        YAxis.setDrawGridLines(false);


        binding.lineChart.data = data
        //lineChart.animateXY(1000,1000);
        binding.lineChart.invalidate()
    }

}
