package androidx.preference;

/*
 * Copyright (C) Contributors to the Suwayomi project
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

    @JsonIgnore
    public boolean isChecked() { throw new RuntimeException("Stub!"); }

    @JsonIgnore
    public void setChecked(boolean checked) { throw new RuntimeException("Stub!"); }

    @JsonIgnore
    public CharSequence getSummaryOn() { throw new RuntimeException("Stub!"); }

    @JsonIgnore
    public void setSummaryOn(CharSequence summary) {
        this.summaryOn = summary;
        updateSummary();
    }

    @JsonIgnore
    public CharSequence getSummaryOff() { throw new RuntimeException("Stub!"); }

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
    public boolean getDisableDependentsState() { throw new RuntimeException("Stub!"); }

    @JsonIgnore
    public void setDisableDependentsState(boolean disableDependentsState) { throw new RuntimeException("Stub!"); }

    /** Tachidesk specific API */
    @Override
    public String getDefaultValueType() {
        return "Boolean";
    }
}