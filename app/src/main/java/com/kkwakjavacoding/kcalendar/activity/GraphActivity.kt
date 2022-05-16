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
    private lateinit var yearadapter: ArrayAdapter<String>
    lateinit var mydbhelper: MyDBHelper
    private lateinit var chartData : ArrayList<ChartData>

    var mon: String = "5"
    var year:String = "2022"

    var yearArray = arrayOf<String>("2016년", "2017년", "2018년", "2019년", "2020년", "2021년", "2022년", "2023년")

    var monthArray = arrayOf<String>(" 1월", " 2월", " 3월", " 4월", " 5월", " 6월", " 7월",
        " 8월", " 9월", "10월", "11월", "12월")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mydbhelper = MyDBHelper(this)


        //mydbhelper.insertData("2022", "5", "5", 65.6)
        ///mydbhelper.insertData("2022", "5", "10", 66.6)
        //mydbhelper.insertData("2022", "5", "15", 67.6)
        //mydbhelper.insertData("2022", "5", "20", 66.3)
        //mydbhelper.insertData("2022", "5", "25", 66.0)
        //mydbhelper.insertData("2022", "5", "30", 67.1)

        init()

    }

    private fun init(){
        yearadapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearArray)
        binding.yearSpinner.adapter = yearadapter
        monthadapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthArray)
        binding.monthSpinner.adapter = monthadapter

        binding.monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mon = (p2+1).toString()
                LineChartGraph(mydbhelper.getData(year, mon))

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        binding.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                year = binding.yearSpinner.selectedItem.toString().substring(0,4)
                LineChartGraph(mydbhelper.getData(year, mon))

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
        entries.clear()


        for (i in chartItem.indices) {
            entries.add(Entry(chartItem[i].weight.toFloat(), i))
        }

        //LineDataSet 선언
        val lineDataSet: LineDataSet
        val yeardata = year.plus("년")
        val mondata = mon.plus("월")
        lineDataSet = LineDataSet(entries, yeardata.plus(mondata))
        lineDataSet.color = Color.BLUE  //LineChart에서 Line Color 설정
        lineDataSet.setCircleColor(Color.DKGRAY)  // LineChart에서 Line Circle Color 설정
        lineDataSet.setCircleColorHole(Color.DKGRAY) // LineChart에서 Line Hole Circle Color 설정
        lineDataSet.notifyDataSetChanged()


        val labels = ArrayList<String>()
        labels.clear()
        for (i in chartItem.indices) {
            labels.add(chartItem[i].date)
        }

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.clear()
        dataSets.add(lineDataSet as ILineDataSet)

        val data = LineData(labels, dataSets)
        data.setValueTextSize(10f)
        data.notifyDataChanged()


        val xAxis = binding.lineChart.getXAxis(); // x 축 설정
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //x 축 표시에 대한 위치 설정

        val YAxis  = binding.lineChart.getAxisRight(); //Y축의 오른쪽면 설정
        YAxis.setDrawLabels(false);
        YAxis.setDrawAxisLine(false);
        YAxis.setDrawGridLines(false);


        binding.lineChart.data = data
        //lineChart.animateXY(1000,1000);
        binding.lineChart.notifyDataSetChanged()
        binding.lineChart.invalidate()
    }

}
