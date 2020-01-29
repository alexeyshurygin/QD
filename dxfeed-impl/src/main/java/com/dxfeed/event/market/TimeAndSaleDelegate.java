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
package com.dxfeed.event.market;

import com.devexperts.qd.DataRecord;
import com.devexperts.qd.QDContract;
import com.devexperts.qd.ng.RecordBuffer;
import com.devexperts.qd.ng.RecordCursor;
import com.dxfeed.api.impl.EventDelegateFlags;
import com.dxfeed.event.market.impl.TimeAndSaleMapping;

import java.util.EnumSet;

public final class TimeAndSaleDelegate extends MarketEventDelegateImpl<TimeAndSale> {
// BEGIN: CODE AUTOMATICALLY GENERATED: DO NOT MODIFY. IT IS REGENERATED BY com.dxfeed.api.codegen.ImplCodeGen
    private final TimeAndSaleMapping m;

    public TimeAndSaleDelegate(DataRecord record, QDContract contract, EnumSet<EventDelegateFlags> flags) {
        super(record, contract, flags);
        m = record.getMapping(TimeAndSaleMapping.class);
    }

    @Override
    public TimeAndSaleMapping getMapping() {
        return m;
    }

    @Override
    public TimeAndSale createEvent() {
        return new TimeAndSale();
    }

    @Override
    public TimeAndSale getEvent(TimeAndSale event, RecordCursor cursor) {
        super.getEvent(event, cursor);
        event.setEventFlags(cursor.getEventFlags());
        event.setIndex((((long) m.getTimeSeconds(cursor)) << 32) | (m.getSequence(cursor) & 0xFFFFFFFFL));
        event.setTimeNanoPart(m.getTimeNanoPart(cursor));
        event.setExchangeCode(m.getExchangeCode(cursor));
        event.setPrice(m.getPrice(cursor));
        event.setSizeAsDouble(m.getSizeDouble(cursor));
        event.setBidPrice(m.getBidPrice(cursor));
        event.setAskPrice(m.getAskPrice(cursor));
        event.setExchangeSaleConditions(m.getSaleConditionsString(cursor));
        event.setFlags(m.getFlags(cursor));
        event.setBuyer(m.getBuyer(cursor));
        event.setSeller(m.getSeller(cursor));
        return event;
    }

    @Override
    public RecordCursor putEvent(TimeAndSale event, RecordBuffer buf) {
        RecordCursor cursor = super.putEvent(event, buf);
        cursor.setEventFlags(event.getEventFlags());
        m.setTimeSeconds(cursor, (int) (event.getIndex() >>> 32));
        m.setSequence(cursor, (int) event.getIndex());
        m.setTimeNanoPart(cursor, event.getTimeNanoPart());
        m.setExchangeCode(cursor, event.getExchangeCode());
        m.setPrice(cursor, event.getPrice());
        m.setSizeDouble(cursor, event.getSizeAsDouble());
        m.setBidPrice(cursor, event.getBidPrice());
        m.setAskPrice(cursor, event.getAskPrice());
        m.setSaleConditionsString(cursor, event.getExchangeSaleConditions());
        m.setFlags(cursor, event.getFlags());
        m.setBuyer(cursor, event.getBuyer());
        m.setSeller(cursor, event.getSeller());
        return cursor;
    }
// END: CODE AUTOMATICALLY GENERATED
}
