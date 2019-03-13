package argerd.ru;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimePickerFragment extends DialogFragment {
    // константа для аргумента фрагмента
    private static final String ARG_TIME = "time";

    // константа для интента
    public static final String EXTRA_TIME = "extraTime";

    private TimePicker timePicker;

    public static TimePickerFragment newInstance(Date time) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, time);

        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setArguments(args);
        return timePickerFragment;
    }

    private void sendResult(int resultCode, Date time) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_TIME, time);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, data);
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);

        //Date date = (Date) getArguments().getSerializable(ARG_TIME);
        timePicker = view.findViewById(R.id.dialog_time_picker);
        timePicker.setIs24HourView(true);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.time_picker_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Date date = (Date) getArguments().getSerializable(ARG_TIME);
                                    Calendar time = Calendar.getInstance();
                                    time.setTime(date);
                                    time.add(Calendar.HOUR, timePicker.getHour());
                                    time.add(Calendar.MINUTE, timePicker.getMinute());
                                    date = time.getTime();
                                    System.out.println("DATE " + date.toString());
                                    sendResult(Activity.RESULT_OK, date);
                                }
                                return;
                            }
                        })
                .create();
    }
}
