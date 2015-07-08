package io.mazur.fit.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class JSONRoutine {
    @SerializedName("routine")
    private ArrayList<PartRoutine> mPartRoutines = new ArrayList<>();

    public ArrayList<PartRoutine> getPartRoutines() {
        return mPartRoutines;
    }

    public int getSize() {
        if(mPartRoutines != null) {
            return mPartRoutines.size();
        }

        return 0;
    }

    public class PartRoutine {
        @SerializedName("id")
        private String mId;

        @SerializedName("title")
        private String mTitle;

        @SerializedName("description")
        private String mDescription;

        @SerializedName("type")
        private String mType;

        /**
         * Mode can be one of those: All, Pick, Levels
         */
        @SerializedName("mode")
        private String mMode;

        public String getId() {
            return mId;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }

        public RoutineType getType() {
            if(mType.matches("category")) {
                return RoutineType.CATEGORY;
            } else if(mType.matches("section")) {
                return RoutineType.SECTION;
            } else {
                return RoutineType.EXERCISE;
            }
        }

        public SectionMode getMode() {
            if(mMode.matches("all")) {
                return SectionMode.ALL;
            } else if(mMode.matches("pick")) {
                return SectionMode.PICK;
            } else {
                return SectionMode.LEVELS;
            }
        }
    }
}
