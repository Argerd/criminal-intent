package argerd.ru;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class CrimeListFragment extends Fragment {
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView recyclerView;
    private CrimeAdapter adapter;

    private Button createFirstCrimeButton;
    private boolean isSubtittleVisible;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, isSubtittleVisible);
    }

    private void createNewCrime() {
        Crime crime = new Crime();
        CrimeLab.get(getActivity()).addCrime(crime);
        Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            isSubtittleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        createFirstCrimeButton = view.findViewById(R.id.create_first_crime_button);
        createFirstCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewCrime();
            }
        });

        recyclerView = view.findViewById(R.id.crime_recycler_view);
        recyclerView.setLayoutManager((new LinearLayoutManager(getActivity())));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtittle);
        if (isSubtittleVisible) {
            subtitleItem.setTitle(R.string.hide_subtittle);
        } else {
            subtitleItem.setTitle(R.string.show_subtittle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                createNewCrime();
                return true;
            case R.id.show_subtittle:
                isSubtittleVisible = !isSubtittleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtittle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtittle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int countOfCrimes = crimeLab.getCrimes().size();
        String subtittle = getResources().getQuantityString(R.plurals.subtitle_plural,
                countOfCrimes, countOfCrimes);

        if (!isSubtittleVisible) {
            subtittle = null;
        }

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setSubtitle(subtittle);
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        if (adapter == null) {
            adapter = new CrimeAdapter(crimeLab.getCrimes());
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        updateSubtittle();
        if (crimeLab.getCrimes().size() > 0) {
            createFirstCrimeButton.setVisibility(View.GONE);
        }
    }

    /**
     * здесь распологается весь код, выполняющий работу по связыванию и обработки нажатий
     */
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView titleTextView;
        private TextView dateTextView;
        private Button policeButton;
        private ImageView solvedImageView;

        private Crime crime;

        /**
         * связь итема с моделью
         *
         * @param crime
         */
        public void bind(Crime crime) {
            this.crime = crime;
            titleTextView.setText(crime.getTitle());
            DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.ENGLISH);
            dateTextView.setText(df.format(crime.getDate()));
            solvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        /**
         * конструктор для айтема без полиции
         *
         * @param inflater
         * @param parent
         */
        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            // itemView - представление всей строки
            itemView.setOnClickListener(this);

            titleTextView = itemView.findViewById(R.id.crime_title);
            dateTextView = itemView.findViewById(R.id.crime_date);
            solvedImageView = itemView.findViewById(R.id.crime_solved);
        }

        /**
         * Конструктор для итема с полицией
         *
         * @param view
         */
        public CrimeHolder(View view) {
            super(view);
            itemView.setOnClickListener(this);

            titleTextView = itemView.findViewById(R.id.crime_title);
            dateTextView = itemView.findViewById(R.id.crime_date);
            policeButton = itemView.findViewById(R.id.button_for_police);
            policeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Message send to police!",
                            Toast.LENGTH_SHORT).show();
                }
            });
            solvedImageView = itemView.findViewById(R.id.crime_solved);
        }

        @Override
        public void onClick(View v) {
            startActivity(CrimePagerActivity.newIntent(getActivity(), crime.getId()));
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        public static final int POLICE_FREE = 0;
        public static final int FOR_POLICE = 1;

        private List<Crime> crimes;

        public CrimeAdapter(List<Crime> crimes) {
            this.crimes = crimes;
        }

        @Override
        public int getItemViewType(int position) {
            if (crimes.get(position).isRequiresPolice()) {
                return FOR_POLICE;
            } else {
                return POLICE_FREE;
            }
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (viewType == POLICE_FREE) {
                return new CrimeHolder(layoutInflater, parent);
            } else {
                View view = layoutInflater.inflate(R.layout.list_item_crime_for_police, parent,
                        false);
                return new CrimeHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(CrimeHolder crimeHolder, int position) {
            Crime crime = crimes.get(position);
            crimeHolder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return crimes.size();
        }
    }
}