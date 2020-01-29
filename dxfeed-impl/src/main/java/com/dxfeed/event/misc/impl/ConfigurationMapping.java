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
package com.dxfeed.event.misc.impl;

import com.devexperts.io.Marshalled;
import com.devexperts.qd.DataRecord;
import com.devexperts.qd.ng.RecordCursor;
import com.devexperts.qd.ng.RecordMapping;
import com.devexperts.qd.util.MappingUtil;

public class ConfigurationMapping extends RecordMapping {
// BEGIN: CODE AUTOMATICALLY GENERATED: DO NOT MODIFY. IT IS REGENERATED BY com.dxfeed.api.codegen.ImplCodeGen
    private final int iVersion;
    private final int oConfiguration;

    public ConfigurationMapping(DataRecord record) {
        super(record);
        iVersion = MappingUtil.findIntField(record, "Version", false);
        oConfiguration = MappingUtil.findObjField(record, "Configuration", true);
    }

    public int getVersion(RecordCursor cursor) {
        if (iVersion < 0)
            return 0;
        return getInt(cursor, iVersion);
    }

    public void setVersion(RecordCursor cursor, int version) {
        if (iVersion < 0)
            return;
        setInt(cursor, iVersion, version);
    }

    public Marshalled<?> getConfiguration(RecordCursor cursor) {
        return MappingUtil.getMarshalled(getObj(cursor, oConfiguration));
    }

    public void setConfiguration(RecordCursor cursor, Marshalled<?> configuration) {
        setObj(cursor, oConfiguration, configuration);
    }
// END: CODE AUTOMATICALLY GENERATED
}
