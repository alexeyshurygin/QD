/*
 * !++
 * QDS - Quick Data Signalling Library
 * !-
 * Copyright (C) 2002 - 2020 Devexperts LLC
 * !-
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * !__
 */
package com.dxfeed.event.option.impl;

import com.devexperts.qd.DataRecord;
import com.devexperts.qd.ng.RecordCursor;
import com.devexperts.qd.ng.RecordMapping;
import com.devexperts.qd.util.MappingUtil;
import com.devexperts.util.TimeUtil;

public class UnderlyingMapping extends RecordMapping {
// BEGIN: CODE AUTOMATICALLY GENERATED: DO NOT MODIFY. IT IS REGENERATED BY com.dxfeed.api.codegen.ImplCodeGen
    private final int iTime;
    private final int iSequence;
    private final int iVolatility;
    private final int iFrontVolatility;
    private final int iBackVolatility;
    private final int iPutCallRatio;

    public UnderlyingMapping(DataRecord record) {
        super(record);
        iTime = MappingUtil.findIntField(record, "Time", false);
        iSequence = MappingUtil.findIntField(record, "Sequence", false);
        iVolatility = findIntField("Volatility", false);
        iFrontVolatility = findIntField("FrontVolatility", false);
        iBackVolatility = findIntField("BackVolatility", false);
        iPutCallRatio = findIntField("PutCallRatio", false);
    }

    public long getTimeMillis(RecordCursor cursor) {
        if (iTime < 0)
            return 0;
        return getInt(cursor, iTime) * 1000L;
    }

    public void setTimeMillis(RecordCursor cursor, long time) {
        if (iTime < 0)
            return;
        setInt(cursor, iTime, TimeUtil.getSecondsFromTime(time));
    }

    public int getTimeSeconds(RecordCursor cursor) {
        if (iTime < 0)
            return 0;
        return getInt(cursor, iTime);
    }

    public void setTimeSeconds(RecordCursor cursor, int time) {
        if (iTime < 0)
            return;
        setInt(cursor, iTime, time);
    }

    public int getSequence(RecordCursor cursor) {
        if (iSequence < 0)
            return 0;
        return getInt(cursor, iSequence);
    }

    public void setSequence(RecordCursor cursor, int sequence) {
        if (iSequence < 0)
            return;
        setInt(cursor, iSequence, sequence);
    }

    public double getVolatility(RecordCursor cursor) {
        if (iVolatility < 0)
            return Double.NaN;
        return getAsDouble(cursor, iVolatility);
    }

    public void setVolatility(RecordCursor cursor, double volatility) {
        if (iVolatility < 0)
            return;
        setAsDouble(cursor, iVolatility, volatility);
    }

    public int getVolatilityDecimal(RecordCursor cursor) {
        if (iVolatility < 0)
            return 0;
        return getAsTinyDecimal(cursor, iVolatility);
    }

    public void setVolatilityDecimal(RecordCursor cursor, int volatility) {
        if (iVolatility < 0)
            return;
        setAsTinyDecimal(cursor, iVolatility, volatility);
    }

    public long getVolatilityWideDecimal(RecordCursor cursor) {
        if (iVolatility < 0)
            return 0;
        return getAsWideDecimal(cursor, iVolatility);
    }

    public void setVolatilityWideDecimal(RecordCursor cursor, long volatility) {
        if (iVolatility < 0)
            return;
        setAsWideDecimal(cursor, iVolatility, volatility);
    }

    public double getFrontVolatility(RecordCursor cursor) {
        if (iFrontVolatility < 0)
            return Double.NaN;
        return getAsDouble(cursor, iFrontVolatility);
    }

    public void setFrontVolatility(RecordCursor cursor, double frontVolatility) {
        if (iFrontVolatility < 0)
            return;
        setAsDouble(cursor, iFrontVolatility, frontVolatility);
    }

    public int getFrontVolatilityDecimal(RecordCursor cursor) {
        if (iFrontVolatility < 0)
            return 0;
        return getAsTinyDecimal(cursor, iFrontVolatility);
    }

    public void setFrontVolatilityDecimal(RecordCursor cursor, int frontVolatility) {
        if (iFrontVolatility < 0)
            return;
        setAsTinyDecimal(cursor, iFrontVolatility, frontVolatility);
    }

    public long getFrontVolatilityWideDecimal(RecordCursor cursor) {
        if (iFrontVolatility < 0)
            return 0;
        return getAsWideDecimal(cursor, iFrontVolatility);
    }

    public void setFrontVolatilityWideDecimal(RecordCursor cursor, long frontVolatility) {
        if (iFrontVolatility < 0)
            return;
        setAsWideDecimal(cursor, iFrontVolatility, frontVolatility);
    }

    public double getBackVolatility(RecordCursor cursor) {
        if (iBackVolatility < 0)
            return Double.NaN;
        return getAsDouble(cursor, iBackVolatility);
    }

    public void setBackVolatility(RecordCursor cursor, double backVolatility) {
        if (iBackVolatility < 0)
            return;
        setAsDouble(cursor, iBackVolatility, backVolatility);
    }

    public int getBackVolatilityDecimal(RecordCursor cursor) {
        if (iBackVolatility < 0)
            return 0;
        return getAsTinyDecimal(cursor, iBackVolatility);
    }

    public void setBackVolatilityDecimal(RecordCursor cursor, int backVolatility) {
        if (iBackVolatility < 0)
            return;
        setAsTinyDecimal(cursor, iBackVolatility, backVolatility);
    }

    public long getBackVolatilityWideDecimal(RecordCursor cursor) {
        if (iBackVolatility < 0)
            return 0;
        return getAsWideDecimal(cursor, iBackVolatility);
    }

    public void setBackVolatilityWideDecimal(RecordCursor cursor, long backVolatility) {
        if (iBackVolatility < 0)
            return;
        setAsWideDecimal(cursor, iBackVolatility, backVolatility);
    }

    public double getPutCallRatio(RecordCursor cursor) {
        if (iPutCallRatio < 0)
            return Double.NaN;
        return getAsDouble(cursor, iPutCallRatio);
    }

    public void setPutCallRatio(RecordCursor cursor, double putCallRatio) {
        if (iPutCallRatio < 0)
            return;
        setAsDouble(cursor, iPutCallRatio, putCallRatio);
    }

    public int getPutCallRatioDecimal(RecordCursor cursor) {
        if (iPutCallRatio < 0)
            return 0;
        return getAsTinyDecimal(cursor, iPutCallRatio);
    }

    public void setPutCallRatioDecimal(RecordCursor cursor, int putCallRatio) {
        if (iPutCallRatio < 0)
            return;
        setAsTinyDecimal(cursor, iPutCallRatio, putCallRatio);
    }

    public long getPutCallRatioWideDecimal(RecordCursor cursor) {
        if (iPutCallRatio < 0)
            return 0;
        return getAsWideDecimal(cursor, iPutCallRatio);
    }

    public void setPutCallRatioWideDecimal(RecordCursor cursor, long putCallRatio) {
        if (iPutCallRatio < 0)
            return;
        setAsWideDecimal(cursor, iPutCallRatio, putCallRatio);
    }
// END: CODE AUTOMATICALLY GENERATED
}
