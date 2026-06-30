package androidx.preference;

/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.Context;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class TwoStatePreference extends Preference {
    // Note: remove @JsonIgnore and implement methods if any extension ever uses these methods or the variables behind them

    private CharSequence summaryOn;
    private CharSequence summaryOff;

    public TwoStatePreference(Context context) {
        super(context);
        setDefaultValue(false);
    }

    private boolean mChecked = false;
    private boolean mDisableDependentsState = false;

    @JsonIgnore
    public boolean isChecked() { return mChecked; }

    @JsonIgnore
    public void setChecked(boolean checked) { mChecked = checked; }

    @JsonIgnore
    public CharSequence getSummaryOn() { return summaryOn; }

    @JsonIgnore
    public void setSummaryOn(CharSequence summary) {
        this.summaryOn = summary;
        updateSummary();
    }

    @JsonIgnore
    public CharSequence getSummaryOff() { return summaryOff; }

    @JsonIgnore
    public void setSummaryOff(CharSequence summary) {
        this.summaryOff = summary;
        updateSummary();
    }

    private void updateSummary() {
        if (this.summaryOn != null && this.summaryOff != null) {
            super.setSummary("ON: " + this.summaryOn + "\nOFF: " + this.summaryOff);
        } else if (this.summaryOn != null) {
            super.setSummary("ON: " + this.summaryOn);
        } else if (this.summaryOff != null) {
            super.setSummary("OFF: " + this.summaryOff);
        }
    }

    @JsonIgnore
    public boolean getDisableDependentsState() { return mDisableDependentsState; }

    @JsonIgnore
    public void setDisableDependentsState(boolean disableDependentsState) { mDisableDependentsState = disableDependentsState; }

    /** Tachidesk specific API */
    @Override
    public String getDefaultValueType() {
        return "Boolean";
    }
}