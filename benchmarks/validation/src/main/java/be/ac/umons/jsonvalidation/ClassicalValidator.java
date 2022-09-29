package be.ac.umons.jsonvalidation;

import org.json.JSONObject;

import be.ac.umons.jsonschematools.JSONSchema;
import be.ac.umons.jsonschematools.JSONSchemaException;
import be.ac.umons.jsonschematools.validator.handlers.DefaultArrayHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultBooleanHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultEnumHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultIntegerHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultNumberHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultObjectHandler;
import be.ac.umons.jsonschematools.validator.handlers.DefaultStringHandler;
import be.ac.umons.jsonschematools.validator.Validator;
import be.ac.umons.jsonschematools.validator.handlers.Handler;

/**
 * Extension of the classical validator to measure the memory consumed.
 * 
 * @author Gaëtan Staquet
 */
public class ClassicalValidator extends Validator {

    private long maxMemory = 0L;
    private long memoryStart = 0L;
    private final boolean measureMemory;

    public ClassicalValidator(boolean measureMemory) {
        this(new DefaultStringHandler(), new DefaultIntegerHandler(), new DefaultNumberHandler(), new DefaultBooleanHandler(), new DefaultEnumHandler(), new DefaultObjectHandler(), new DefaultArrayHandler(), measureMemory);
    }

    public ClassicalValidator(final Handler stringHandler, final Handler integerHandler, final Handler numberHandler,
            final Handler booleanHandler, final Handler enumHandler, final Handler objectHandler,
            final Handler arrayHandler, boolean measureMemory) {
        super(stringHandler, integerHandler, numberHandler, booleanHandler, enumHandler, objectHandler, arrayHandler);
        this.measureMemory = measureMemory;
    }

    @Override
    public boolean validate(final JSONSchema schema, final JSONObject document) throws JSONSchemaException {
        if (measureMemory) {
            memoryStart = getMemoryInUse();
            maxMemory = memoryStart;
        }
        return super.validate(schema, document);
    }

    @Override
    public boolean validateValue(final JSONSchema schema, final Object value) throws JSONSchemaException {
        if (measureMemory) {
            System.gc();
            maxMemory = Math.max(maxMemory, getMemoryInUse());
        }
        boolean valid = super.validateValue(schema, value);
        if (measureMemory) {
            maxMemory = Math.max(maxMemory, getMemoryInUse());
        }
        return valid;
    }

    /**
     * Gets the maximal memory (in kilobytes) used by the validator during the last
     * run.
     * 
     * @return The maximal memory consumed
     */
    public long getMaxMemoryUsed() {
        return maxMemory - memoryStart;
    }

    private long getMemoryInUse() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;
    }
}