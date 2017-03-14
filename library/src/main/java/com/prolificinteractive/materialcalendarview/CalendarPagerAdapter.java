package com.prolificinteractive.materialcalendarview;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView.ShowOtherDates;
import com.prolificinteractive.materialcalendarview.format.DayFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Pager adapter backing the calendar view
 */
abstract class CalendarPagerAdapter<V extends CalendarPagerView> extends PagerAdapter {

    private final ArrayDeque<V> currentViews;

    protected final MaterialCalendarView mcv;
    private final CalendarDay today;

    private TitleFormatter titleFormatter = null;
    private Integer color = null;
    private Integer dateTextAppearance = null;
    private Integer weekDayTextAppearance = null;
    @ShowOtherDates
    private int showOtherDates = MaterialCalendarView.SHOW_DEFAULTS;
    private CalendarDay minDate = null;
    private CalendarDay maxDate = null;
    private DateRangeIndex rangeIndex;
    private List<CalendarDay> selectedDates = new ArrayList<>();
    private WeekDayFormatter weekDayFormatter = WeekDayFormatter.DEFAULT;
    private DayFormatter dayFormatter = DayFormatter.DEFAULT;
    private List<DayViewDecorator> decorators = new ArrayList<>();
    private List<DecoratorResult> decoratorResults = null;
    private boolean selectionEnabled = true;

    CalendarPagerAdapter(MaterialCalendarView mcv) {
        this.mcv = mcv;
        this.today = CalendarDay.today();
        currentViews = new ArrayDeque<>();
        currentViews.iterator();
        setRangeDates(null, null);
    }

    public void setDecorators(List<DayViewDecorator> decorators) {
        this.decorators = decorators;
        invalidateDecorators();
    }

    public void invalidateDecorators() {
        decoratorResults = new ArrayList<>();
        for (DayViewDecorator decorator : decorators) {
            DayViewFacade facade = new DayViewFacade();
            decorator.decorate(facade);
            if (facade.isDecorated()) {
                decoratorResults.add(new DecoratorResult(decorator, facade));
            }
        }
        for (V pagerView : currentViews) {
            pagerView.setDayViewDecorators(decoratorResults);
        }
    }

    @Override
    public int getCount() {
        return rangeIndex.getCount();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleFormatter == null ? "" : titleFormatter.format(getItem(position));
    }

    public CalendarPagerAdapter<?> migrateStateAndReturn(CalendarPagerAdapter<?> newAdapter) {
        newAdapter.titleFormatter = titleFormatter;
        newAdapter.color = color;
        newAdapter.dateTextAppearance = dateTextAppearance;
        newAdapter.weekDayTextAppearance = weekDayTextAppearance;
        newAdapter.showOtherDates = showOtherDates;
        newAdapter.minDate = minDate;
        newAdapter.maxDate = maxDate;
        newAdapter.selectedDates = selectedDates;
        newAdapter.weekDayFormatter = weekDayFormatter;
        newAdapter.dayFormatter = dayFormatter;
        newAdapter.decorators = decorators;
        newAdapter.decoratorResults = decoratorResults;
        newAdapter.selectionEnabled = selectionEnabled;
        return newAdapter;
    }

    public int getIndexForDay(CalendarDay day) {
        if (day == null) {
            return getCount() / 2;
        }
        if (minDate != null && day.isBefore(minDate)) {
            return 0;
        }
        if (maxDate != null && day.isAfter(maxDate)) {
            return getCount() - 1;
        }
        return rangeIndex.indexOf(day);
    }

    protected abstract V createView(int position);

    protected abstract int indexOf(V view);

    protected abstract boolean isInstanceOfView(Object object);

    protected abstract DateRangeIndex createRangeIndex(CalendarDay min, CalendarDay max);

    @Override
    public int getItemPosition(Object object) {
        if (!(isInstanceOfView(object))) {
            return POSITION_NONE;
        }
        CalendarPagerView pagerView = (CalendarPagerView) object;
        CalendarDay firstViewDay = pagerView.getFirstViewDay();
        if (firstViewDay == null) {
            return POSITION_NONE;
        }
        int index = indexOf((V) object);
        if (index < 0) {
            return POSITION_NONE;
        }
        return index;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        V pagerView = createView(position);
        pagerView.setContentDescription(mcv.getCalendarContentDescription());
        pagerView.setAlpha(0);
        pagerView.setSelectionEnabled(selectionEnabled);

        pagerView.setWeekDayFormatter(weekDayFormatter);
        pagerView.setDayFormatter(dayFormatter);
        if (color != null) {
            pagerView.setSelectionColor(color);
        }
        if (dateTextAppearance != null) {
            pagerView.setDateTextAppearance(dateTextAppearance);
        }
        if (weekDayTextAppearance != null) {
            pagerView.setWeekDayTextAppearance(weekDayTextAppearance);
        }
        pagerView.setShowOtherDates(showOtherDates);
        pagerView.setMinimumDate(minDate);
        pagerView.setMaximumDate(maxDate);
        pagerView.setSelectedDates(selectedDates);

        container.addView(pagerView);
        currentViews.add(pagerView);

        pagerView.setDayViewDecorators(decoratorResults);

        return pagerView;
    }

    public void setSelectionEnabled(boolean enabled) {
        selectionEnabled = enabled;
        for (V pagerView : currentViews) {
            pagerView.setSelectionEnabled(selectionEnabled);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        CalendarPagerView pagerView = (CalendarPagerView) object;
        currentViews.remove(pagerView);
        container.removeView(pagerView);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setTitleFormatter(@NonNull TitleFormatter titleFormatter) {
        this.titleFormatter = titleFormatter;
    }

    public void setSelectionColor(int color) {
        this.color = color;
        for (V pagerView : currentViews) {
            pagerView.setSelectionColor(color);
        }
    }

    public void setDateTextAppearance(int taId) {
        if (taId == 0) {
            return;
        }
        this.dateTextAppearance = taId;
        for (V pagerView : currentViews) {
            pagerView.setDateTextAppearance(taId);
        }
    }

    public void setShowOtherDates(@ShowOtherDates int showFlags) {
        this.showOtherDates = showFlags;
        for (V pagerView : currentViews) {
            pagerView.setShowOtherDates(showFlags);
        }
    }

    public void setWeekDayFormatter(WeekDayFormatter formatter) {
        this.weekDayFormatter = formatter;
        for (V pagerView : currentViews) {
            pagerView.setWeekDayFormatter(formatter);
        }
    }

    public void setDayFormatter(DayFormatter formatter) {
        this.dayFormatter = formatter;
        for (V pagerView : currentViews) {
            pagerView.setDayFormatter(formatter);
        }
    }

    @ShowOtherDates
    public int getShowOtherDates() {
        return showOtherDates;
    }

    public void setWeekDayTextAppearance(int taId) {
        if (taId == 0) {
            return;
        }
        this.weekDayTextAppearance = taId;
        for (V pagerView : currentViews) {
            pagerView.setWeekDayTextAppearance(taId);
        }
    }

    public void setRangeDates(CalendarDay min, CalendarDay max) {
        this.minDate = min;
        this.maxDate = max;
        for (V pagerView : currentViews) {
            pagerView.setMinimumDate(min);
            pagerView.setMaximumDate(max);
        }

        if (min == null) {
            min = CalendarDay.from(today.getYear() - 200, today.getMonth(), today.getDay());
        }

        if (max == null) {
            max = CalendarDay.from(today.getYear() + 200, today.getMonth(), today.getDay());
        }

        rangeIndex = createRangeIndex(min, max);

        notifyDataSetChanged();
        invalidateSelectedDates();
    }

    public DateRangeIndex getRangeIndex() {
        return rangeIndex;
    }

    public void clearSelections() {
        selectedDates.clear();
        invalidateSelectedDates();
    }

    public void setDateSelected(CalendarDay day, boolean selected) {
        Collections.sort(selectedDates, new DayComparator());   //通过重写Comparator的实现类DayComparator来实现日期先后排序。

        if (selected) {
            if (!selectedDates.contains(day)) {
                selectedDates.add(day);
                invalidateSelectedDates();
            }
        } else {
            if (selectedDates.contains(day)) {
//                selectedDates.remove(day);
//                invalidateSelectedDates();

                if (selectedDates.size() == 1) {
                    selectedDates.remove(day);
                    invalidateSelectedDates();
                    return;
                }

                int index = 0;
                int size = selectedDates.size();
                CalendarDay calendarDay0 = selectedDates.get(0);
                CalendarDay calendarDayLast = selectedDates.get(size - 1);
                Date dateSelected = day.getDate();
                Date date0 = calendarDay0.getDate();
                Date dateLast = calendarDayLast.getDate();
                for (int i = 0; i < selectedDates.size(); i++) {
                    if (day.toString().equals(selectedDates.get(i).toString())) {
                        index = i;
                        break;
                    }
                }
                if (0 == betweenDays(date0, dateSelected)) {
                    selectedDates.clear();
                    selectedDates.add(calendarDayLast);
                } else if (0 == betweenDays(dateLast, dateSelected)) {
                    selectedDates.clear();
                    selectedDates.add(calendarDay0);
                } else if (betweenDays(date0, dateSelected) >= betweenDays(dateSelected, dateLast)) {
                    List<CalendarDay> selectedDatesTemp = new ArrayList<>();
                    for (int i = 0; i <= index; i++) {
                        selectedDatesTemp.add(selectedDates.get(i));
                    }
                    selectedDates.clear();
                    selectedDates.addAll(selectedDatesTemp);
                    selectedDatesTemp.clear();
                } else if (betweenDays(date0, dateSelected) < betweenDays(dateSelected, dateLast)) {
                    List<CalendarDay> selectedDatesTemp = new ArrayList<>();
                    for (int i = index; i < size; i++) {
                        selectedDatesTemp.add(selectedDates.get(i));
                    }
                    selectedDates.clear();
                    selectedDates.addAll(selectedDatesTemp);
                    selectedDatesTemp.clear();
                }
                invalidateSelectedDates();
            }
        }
    }

    private void invalidateSelectedDates() {
        validateSelectedDates();
        for (V pagerView : currentViews) {
            pagerView.setSelectedDates(selectedDates);
        }
    }

    private void validateSelectedDates() {
        for (int i = 0; i < selectedDates.size(); i++) {
            CalendarDay date = selectedDates.get(i);

            if ((minDate != null && minDate.isAfter(date)) || (maxDate != null && maxDate.isBefore(date))) {
                selectedDates.remove(i);
                mcv.onDateUnselected(date);
                i -= 1;
            }
        }
    }

    public CalendarDay getItem(int position) {
        return rangeIndex.getItem(position);
    }

    @NonNull
    public List<CalendarDay> getSelectedDates() {
        Collections.sort(selectedDates, new DayComparator());   //通过重写Comparator的实现类DayComparator来实现日期先后排序。

        return selectedDates;
//        return Collections.unmodifiableList(selectedDates);
    }

    public class DayComparator implements Comparator<CalendarDay> {
        public int compare(CalendarDay day1, CalendarDay day2) {
            if (day1.isAfter(day2)) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public int betweenDays(Date date1, Date date2) {
        long day = (date1.getTime() - date2.getTime()) / (24 * 60 * 60 * 1000) > 0 ? (date1
                .getTime() - date2.getTime()) / (24 * 60 * 60 * 1000)
                : (date2.getTime() - date1.getTime()) / (24 * 60 * 60 * 1000);
        return (int) day;
    }

    protected int getDateTextAppearance() {
        return dateTextAppearance == null ? 0 : dateTextAppearance;
    }

    protected int getWeekDayTextAppearance() {
        return weekDayTextAppearance == null ? 0 : weekDayTextAppearance;
    }
}
