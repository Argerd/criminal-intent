package argerd.ru;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    // константа для получения из аргумента фрагмента crimeId
    private static final String ARG_CRIME_ID = "crime_id";

    // константа для вызова диалогового окна
    private static final String DIALOG = "Dialog";

    // константа для указания CrimeFragment целевым фрагментом для DatePickerFragment
    private static final int REQUEST_DATE = 0;

    // константа для указания CrimeFragment целевым фрагментом для TimePickerFragment
    private static final int REQUEST_TIME = 1488;

    private Crime crime;

    private EditText titleField;
    private Button dateButton;
    private Button timeButton;
    private CheckBox solvedCheckBox;
    private CheckBox policeCheckBox;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment crimeFragment = new CrimeFragment();
        crimeFragment.setArguments(args);
        return crimeFragment;
    }

    /**
     * Тут настраивается фрагмент
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        crime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            crime.setDate((Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE));
            updateDate();
        }
        if (requestCode == REQUEST_TIME) {
            crime.setDate((Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME));
            updateDate();
        }
    }

    private void updateDate() {
        dateButton.setText(crime.getDate().toString());
    }

    /**
     * в этом методе заполняется макет представления фрагмента
     * все views тут, короче
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View активити-хосту
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        titleField = view.findViewById(R.id.crime_title);
        titleField.setText(crime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dateButton = view.findViewById(R.id.crime_date);
        updateDate();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(crime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fragmentManager, DIALOG);
            }
        });

        timeButton = view.findViewById(R.id.crime_time);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(crime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(fragmentManager, DIALOG);
            }
        });

        solvedCheckBox = view.findViewById(R.id.crime_solved);
        solvedCheckBox.setChecked(crime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!crime.isRequiresPolice()) {
                    crime.setSolved(isChecked);
                } else {
                    solvedCheckBox.setChecked(false);
                    Toast.makeText(getActivity(), "У вас есть возможность вызвать полицию. " +
                            "Если полиция приехала и решила проблему, то снимите опцию, обозначающую " +
                            "это дело для полиции, и попробуйте отметить это дело решенным еще раз. " +
                            "Если полиция не была вызвана, то вернитесь на предыдущий экран и нажмите " +
                            "кнопку вызова.", Toast.LENGTH_LONG).show();
                }
            }
        });

        policeCheckBox = view.findViewById(R.id.for_police);
        policeCheckBox.setChecked(crime.isRequiresPolice());
        policeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!crime.isSolved()) {
                    crime.setRequiresPolice(isChecked);
                } else {
                    policeCheckBox.setChecked(false);
                    Toast.makeText(getActivity(),
                            "Вызов полиции не требуется, так как преступление уже решено.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
