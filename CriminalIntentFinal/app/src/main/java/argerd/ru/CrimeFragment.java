package argerd.ru;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import argerd.ru.database.DialogFragmentPhoto;

import static argerd.ru.R.string.crime_photo_no_image_description;

public class CrimeFragment extends Fragment {
    // константа для получения из аргумента фрагмента crimeId
    private static final String ARG_CRIME_ID = "crime_id";

    // константа для вызова диалогового окна
    private static final String DIALOG = "Dialog";

    // константа для указания CrimeFragment целевым фрагментом для DatePickerFragment
    private static final int REQUEST_DATE = 0;

    // константа для указания CrimeFragment целевым фрагментом для TimePickerFragment
    private static final int REQUEST_TIME = 1488;

    // константа для запроса к Андроид для контакта
    private static final int REQUEST_CONTACT = 18;

    // для запроса разрешения для контактов
    private static final int REQUEST_CODE_PERMISSION_READ_CONTACTS = 88;

    // Для интента к приложению камеры
    private static final int REQUEST_CODE_PHOTO = 99;

    private Crime crime;

    private EditText titleField;
    private Button dateButton;
    private Button timeButton;
    private CheckBox solvedCheckBox;
    private CheckBox policeCheckBox;
    private Button deleteButton;
    private Button reportButton;
    private Button suspectButton;
    private Button callToSuspectButton;
    private ImageView photoView;
    private ImageButton photoButton;
    private File photoFile;

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
        photoFile = CrimeLab.get(getActivity()).getPhotoFile(crime);
    }

    private String getPhoneNumber(String name) {
        String number = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%"
                + name + "%'";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = getContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection,
                null, null);
        if (cursor.moveToFirst()) {
            number = cursor.getString(0);
        }
        cursor.close();
        if (number == null) {
            number = "unsaved";
        }

        return number;
    }


    private void updatePhotoView() {
        ViewTreeObserver viewTreeObserver = photoView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (photoFile == null || !photoFile.exists()) {
                    photoView.setImageDrawable(null);
                    photoView.setContentDescription(
                            getString(R.string.crime_photo_no_image_description));
                } else {
                    photoView.setImageBitmap(PictureUtils.getScaledBitmap(photoFile.getPath(),
                            photoView.getMaxWidth(), photoView.getMaxHeight()));
                    photoView.setContentDescription(
                            getString(R.string.crime_photo_image_description));
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            crime.setDate((Date) intent.getSerializableExtra(DatePickerFragment.EXTRA_DATE));
            updateDate();
        }
        if (requestCode == REQUEST_TIME) {
            crime.setDate((Date) intent.getSerializableExtra(TimePickerFragment.EXTRA_TIME));
            updateDate();
        }
        if (requestCode == REQUEST_CONTACT && intent != null) {
            Uri contactUri = intent.getData();
            // определения полей, значения которых должны быть возвращены запросом
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            // выполнение запроса - contactUri здесь выполняет функции условия where
            Cursor cursor = getActivity().getContentResolver().query(contactUri, queryFields,
                    null, null, null);
            try {
                //проверка получения результатов
                if (cursor.getCount() == 0) {
                    return;
                }
                // извлечение первого столбца данных - имени подозреваемого.
                cursor.moveToFirst();
                String suspect = cursor.getString(0);
                crime.setPhoneNumberOfSuspect(getPhoneNumber(suspect));
                crime.setSuspect(suspect);
                suspectButton.setText(suspect);
                callToSuspectButton.setText(crime.getPhoneNumberOfSuspect());
            } finally {
                cursor.close();
            }
        } else if (requestCode == REQUEST_CODE_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "ru.argerd.android.criminalintentfinal.fileprovider", photoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }
    }

    private void updateDate() {
        dateButton.setText(crime.getDate().toString());
    }

    // метод, который делает кнопки, связанные с работой контактов, неактивными, если нет приложений
    // для работы с контактами
    private void setEnabledButtonForContactsIfContactApplicationIsEmpty(Intent intent) {
        // проверка наличия приложения с контактами
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            suspectButton.setEnabled(false);
            callToSuspectButton.setEnabled(false);
        } else {
            suspectButton.setEnabled(true);
            callToSuspectButton.setEnabled(true);
        }
    }

    // метод, создающий неявный интент для вызова приложения для контактов
    private void newIntentForContacts() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        setEnabledButtonForContactsIfContactApplicationIsEmpty(pickContactIntent);
        startActivityForResult(pickContactIntent, REQUEST_CONTACT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                newIntentForContacts();
            }
        }
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

        deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < CrimeLab.get(getActivity()).getCrimes().size(); i++) {
                    if (CrimeLab.get(getActivity()).getCrimes().get(i).getId() == crime.getId()) {
                        CrimeLab.get(getActivity()).getCrimes().remove(i);
                        break;
                    }
                }
                CrimeLab.get(getActivity()).deleteCrime(crime);
                getActivity().finish();
            }
        });

        reportButton = view.findViewById(R.id.crime_report);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                // тема для почты
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                // окно выбора приложения для отправки
                intent = Intent.createChooser(intent, getString(R.string.send_report));
                startActivity(intent);
            }
        });

        suspectButton = view.findViewById(R.id.crime_suspect);
        suspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // узнаем разрешение на контакты
                int permissionStatus = ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_CONTACTS);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    newIntentForContacts();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_CODE_PERMISSION_READ_CONTACTS);
                }
            }
        });

        callToSuspectButton = view.findViewById(R.id.call_to_suspect_button);
        callToSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" +
                        crime.getPhoneNumberOfSuspect()));
                startActivity(intent);
            }
        });

        if (crime.getSuspect() != null) {
            suspectButton.setText(crime.getSuspect());
            callToSuspectButton.setText(crime.getPhoneNumberOfSuspect());
        }

        photoButton = view.findViewById(R.id.crime_camera);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                PackageManager packageManager = getActivity().getPackageManager();

                if (photoFile != null && intent.resolveActivity(packageManager) != null) {
                    photoButton.setEnabled(true);
                } else {
                    photoButton.setEnabled(false);
                    Toast.makeText(getContext(), "Приложение камеры не найдено!" +
                            " Фото сделать невозможно.", Toast.LENGTH_LONG).show();
                }

                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "ru.argerd.android.criminalintentfinal.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                startActivityForResult(intent, REQUEST_CODE_PHOTO);
            }
        });

        photoView = view.findViewById(R.id.crime_photo);
        updatePhotoView();
        photoView.setClickable(true);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(photoFile.getPath());
                DialogFragmentPhoto dialogFragmentPhoto =
                        DialogFragmentPhoto.newInstance(photoFile.getPath());
                dialogFragmentPhoto.show(getFragmentManager(), DIALOG);
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(crime);
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (crime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = DateFormat.format("EEE, MMM dd", crime.getDate()).toString();

        String suspect = crime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, crime.getTitle(), dateString, solvedString,
                suspect);
        return report;
    }
}
