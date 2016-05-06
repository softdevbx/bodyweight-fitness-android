package com.bodyweight.fitness.adapter;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bodyweight.fitness.Constants;
import com.bodyweight.fitness.R;
import com.bodyweight.fitness.model.repository.RepositoryExercise;
import com.bodyweight.fitness.model.repository.RepositoryRoutine;
import com.bodyweight.fitness.model.repository.RepositorySet;
import com.bodyweight.fitness.stream.RepositoryStream;
import com.bodyweight.fitness.stream.Stream;
import com.bodyweight.fitness.ui.ProgressActivity;
import com.bodyweight.fitness.utils.PreferenceUtils;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatterBuilder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import io.realm.Realm;
import io.realm.RealmResults;

public class CalendarListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RealmResults<RepositoryRoutine> mResults;

    public CalendarListAdapter() {
        super();
    }

    public void setItems(RealmResults<RepositoryRoutine> results) {
        mResults = results;

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CalendarRoutinePresenter(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.view_calendar_card, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((CalendarRoutinePresenter) holder).onBindView(mResults.get(position));
    }

    @Override
    public int getItemCount() {
        if (mResults != null) {
            return mResults.size();
        }

        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public class CalendarRoutinePresenter extends RecyclerView.ViewHolder {
        @InjectView(R.id.view_calendar_routine_title)
        TextView mTitle;

        @InjectView(R.id.view_calendar_routine_subtitle)
        TextView mSubtitle;

        private RepositoryRoutine mRepositoryRoutine;

        public CalendarRoutinePresenter(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);
        }

        public void onBindView(RepositoryRoutine repositoryRoutine) {
            mRepositoryRoutine = repositoryRoutine;

            mTitle.setText(repositoryRoutine.getTitle());
            mSubtitle.setText(repositoryRoutine.getSubtitle());
        }

        @OnClick(R.id.view_calendar_card_view_button)
        @SuppressWarnings("unused")
        public void onClickViewButton(View view) {
            Intent intent = new Intent(view.getContext(), ProgressActivity.class);
            intent.putExtra(Constants.INSTANCE.getPRIMARY_KEY_ROUTINE_ID(), mRepositoryRoutine.getId());

            view.getContext().startActivity(intent);
        }

        @OnClick(R.id.view_calendar_card_export_button)
        @SuppressWarnings("unused")
        public void onClickExportButton(View view) {
            String title = exportTitle();
            String content = exportHTML();

            Intent sendIntent = new Intent();

            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TITLE, title);
            sendIntent.putExtra(Intent.EXTRA_TEXT, content);
            sendIntent.setType("text/plain");

            view.getContext().startActivity(sendIntent);
        }

        @OnClick(R.id.view_calendar_card_remove_button)
        @SuppressWarnings("unused")
        public void onClickRemoveButton(View view) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext())
                    .setTitle("Remove Logged Workout?")
                    .setPositiveButton("Ok", (dialog, which) -> {
                        Realm realm = RepositoryStream.getInstance().getRealm();

                        realm.beginTransaction();
                        mRepositoryRoutine.deleteFromRealm();
                        realm.commitTransaction();

                        mResults = null;
                        mRepositoryRoutine = null;

                        notifyDataSetChanged();

                        Stream.INSTANCE.setRepository();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {});

            alertDialog.show();
        }

        private String exportTitle() {
            String title = "";

            title += String.format("%s - %s - %s.",
                    mRepositoryRoutine.getTitle(),
                    mRepositoryRoutine.getSubtitle(),
                    new DateTime(mRepositoryRoutine.getStartTime()).toString("EEEE, d MMMM YYYY - HH:mm")
            );

            return title;
        }

        private String exportHTML() {
            String content = "";

            content += "Hello, The following is your workout in Text/HTML format.";

            content += String.format("\n\nWorkout on %s.", new DateTime(mRepositoryRoutine.getStartTime()).toString("EEEE, d MMMM YYYY - HH:mm"));
            content += String.format("\nLast Updated at %s.", new DateTime(mRepositoryRoutine.getLastUpdatedTime()).toString("HH:mm"));
            content += String.format("\nWorkout length: %s.", getWorkoutLength());
            content += String.format("\n\n%s - %s", mRepositoryRoutine.getTitle(), mRepositoryRoutine.getSubtitle());

            for (RepositoryExercise exercise : mRepositoryRoutine.getExercises()) {
                if (exercise.isVisible()) {
                    content += String.format("\n\n%s", exercise.getTitle());

                    String weightUnit = PreferenceUtils.getInstance().getWeightMeasurementUnit().toString();

                    int index = 0;
                    for (RepositorySet set : exercise.getSets()) {
                        index++;

                        content += String.format("\nSet %s", index);

                        if (set.isTimed()) {
                            content += String.format("\nMinutes: %s", formatMinutes(set.getSeconds()));
                            content += String.format("\nSeconds: %s", formatSeconds(set.getSeconds()));
                        } else {
                            content += String.format("\nReps: %s", set.getReps());
                            content += String.format("\nWeight: %s %s", set.getWeight(), weightUnit);
                        }
                    }
                }
            }

            return content;
        }

        public String getWorkoutLength() {
            DateTime startTime = new DateTime(mRepositoryRoutine.getStartTime());
            DateTime lastUpdatedTime = new DateTime(mRepositoryRoutine.getLastUpdatedTime());

            Duration duration = new Duration(startTime, lastUpdatedTime);

            if (duration.toStandardMinutes().getMinutes() < 10) {
                return "--";
            } else {
                return new PeriodFormatterBuilder()
                        .appendHours()
                        .appendSuffix("h ")
                        .appendMinutes()
                        .appendSuffix("m")
                        .toFormatter()
                        .print(duration.toPeriod());
            }
        }

        public String formatMinutes(int seconds) {
            int minutes = seconds / 60;

            if (minutes == 0) {
                return "0";
            } else if (minutes < 10) {
                return "" + minutes;
            }

            return String.valueOf(minutes);
        }

        public String formatSeconds(int seconds) {
            int s = seconds % 60;

            if (s == 0) {
                return "0";
            } else if (s < 10) {
                return "0" + s;
            }

            return String.valueOf(s);
        }
    }
}
