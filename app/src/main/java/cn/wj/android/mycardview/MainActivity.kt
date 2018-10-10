package cn.wj.android.mycardview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cv1.setOnClickListener { onClick(it) }
        cv2.setOnClickListener { onClick(it) }
    }

    fun onClick(v: View) {
        v.isSelected = !v.isSelected
    }
}
