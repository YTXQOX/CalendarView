package com.prolificinteractive.materialcalendarview.sample;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Shows off the most basic usage
 */
public class BasicActivity extends AppCompatActivity implements OnDateSelectedListener, OnMonthChangedListener, OnRangeSelectedListener {

    private static final DateFormat FORMATTER = SimpleDateFormat.getDateInstance();

    @BindView(R.id.calendarView)
    MaterialCalendarView widget;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.id_global_toolbar_tv_left)
    TextView tvLeft;
    @BindView(R.id.id_global_toolbar_tv_right)
    TextView tvRight;
    @BindView(R.id.id_global_toolbar_tv_title)
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        setStatusBar(ContextCompat.getColor(this, R.color.sample_accent));

        ButterKnife.bind(this);

        widget.setSelectionMode(MaterialCalendarView.SELECTION_MODE_RANGE);

        widget.setOnMonthChangedListener(this);
        widget.setOnDateChangedListener(this);
        widget.setOnRangeSelectedListener(this);

        //Setup initial text
        tvLeft.setVisibility(View.VISIBLE);
        tvRight.setVisibility(View.VISIBLE);

        tvLeft.setText("取消");
        tvRight.setText("确定");
        tvTitle.setText("业务流水");
        textView.setText(getSelectedDatesString());
    }

    @OnClick({R.id.id_global_toolbar_tv_right, R.id.id_global_toolbar_tv_left})
    public void onClick (View view){
        switch (view.getId()) {
            case R.id.id_global_toolbar_tv_right:
                Toast.makeText(this, "已选择：\n" + textView.getText().toString(), Toast.LENGTH_SHORT).show();

                BasicActivity.this.finish();
                break;
            case R.id.id_global_toolbar_tv_left:
                BasicActivity.this.finish();
                break;
        }
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @Nullable CalendarDay date, boolean selected) {
        textView.setText(getSelectedDatesString());
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
//        getSupportActionBar().setTitle(FORMATTER.format(date.getDate()));
    }

    @Override
    public void onRangeSelected(@NonNull MaterialCalendarView widget, @NonNull List<CalendarDay> dates) {
        if (dates.size() == 0) {
            textView.setText("请选择日期");
        } else if (dates.size() == 1) {
            String strStartEnd = FORMATTER.format(dates.get(0).getDate());
            textView.setText(strStartEnd + " - " + strStartEnd);
        } else {
            String strStart = FORMATTER.format(dates.get(0).getDate());
            String strEnd = FORMATTER.format(dates.get(dates.size() - 1).getDate());

            textView.setText(strStart + " - " + strEnd);
        }
    }

    private String getSelectedDatesString() {
        CalendarDay date = widget.getSelectedDate();
        if (date == null) {
            return "请选择日期";
        }
        return FORMATTER.format(date.getDate());
    }

    private void setStatusBar(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(false);
            tintManager.setTintColor(color);
        }
    }
}
