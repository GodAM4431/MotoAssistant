package com.petyachoeva.motoassistant;

import android.os.Bundle;
import android.os.Trace;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class settingsFragment extends Fragment {

    private static final String Job_Tag = "my_job_tag";
    private FirebaseJobDispatcher jobDispatcher;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, null);
    }


    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.getActivity().setTitle("Settings");
        DatabaseHelper myDb;
        myDb = new DatabaseHelper(getContext());

        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getContext()));

        view.findViewById(R.id.ButtonStartJob).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Job job = jobDispatcher.newJobBuilder()
                        .setService(MyService.class)
                        .setLifetime(Lifetime.FOREVER)
                        .setRecurring(true)
                        .setTag(Job_Tag)
                        .setTrigger(Trigger.executionWindow(10,15))
                        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                        .setReplaceCurrent(false)
                        .setConstraints(Constraint.ON_ANY_NETWORK)
                        .build();

                jobDispatcher.mustSchedule(job);
            }
        });

        view.findViewById(R.id.ButtonStopJob).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobDispatcher.cancel(Job_Tag);
                Toast.makeText(getContext(), "Job Cancelled.", Toast.LENGTH_SHORT).show();;
            }
        });
    }

}
