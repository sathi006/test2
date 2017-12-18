package com.mcg.batch.core.support;
import org.springframework.batch.core.step.skip.SkipPolicy;

/**
 *
 * Implementation of SkipPolicy interface for skipping known
 * Exceptions. Mainly used with JDBC Adapter to skip Duplicate Key
 * Exceptions. Used as part of Step definition.
 *
 */
public class ExceptionSkipPolicy implements SkipPolicy {

    /**
     * exceptionClassToSkip.
     */
    private final Class<? extends Exception> exceptionClassToSkip;

    /**
     *
     * @param exceptionClasToSkip Exception
     */
    public ExceptionSkipPolicy(final Class<? extends Exception>
           exceptionClasToSkip) {
        super();
        this.exceptionClassToSkip = exceptionClasToSkip;
    }

    /**
     * @param t Throwable
     * @param skipCount Integer
     * @return Boolean
     */
    @Override
    public final boolean shouldSkip(final Throwable t,
            final int skipCount) {
        return exceptionClassToSkip.isAssignableFrom(t.getClass());
    }
}
