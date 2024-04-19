package net.idonow.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import net.idonow.controller.exception.common.BigDecimalScaleException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.idonow.common.data.NumericConstants.SCALE_MONETARY;

public class MonetaryBigDecimalDeserializer extends NumberDeserializers.BigDecimalDeserializer {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        BigDecimal value = super.deserialize(p, ctxt);
        if (value.scale() > SCALE_MONETARY) {
            throw new BigDecimalScaleException(p.getCurrentName(), "Scale max value is exceeded");
        }
        value = value.setScale(SCALE_MONETARY, RoundingMode.UNNECESSARY);
        return value;
    }
}
