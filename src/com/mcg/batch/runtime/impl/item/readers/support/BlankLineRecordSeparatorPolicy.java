package com.mcg.batch.runtime.impl.item.readers.support;

import org.springframework.batch.item.file.
separator.SimpleRecordSeparatorPolicy;

/**
 * BlankLineRecordSeparatorPolicy to ignore blank lines
 *  between flat file records.
 */
public class BlankLineRecordSeparatorPolicy
extends SimpleRecordSeparatorPolicy {

        /* (non-Javadoc)
         * @see org.springframework.batch.item.file.
         * separator.SimpleRecordSeparatorPolicy#
         * isEndOfRecord(java.lang.String)
         */
        @Override
        public final boolean isEndOfRecord(final String line) {
            if (line.trim().length() == 0) {
                return false;
            }
            return super.isEndOfRecord(line);
        }

        /* (non-Javadoc)
         * @see org.springframework.batch.item.file.
         * separator.SimpleRecordSeparatorPolicy#
         * postProcess(java.lang.String)
         */
        @Override
        public final String postProcess(final String record) {
            if (record == null || record.trim().length() == 0) {
                return null;
            }
            return super.postProcess(record);
        }
    }
