package org.openmhealth.shim.withings.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.openmhealth.schema.domain.omh.HeartRate;

import java.util.Optional;

import static org.openmhealth.shim.common.mapper.JsonNodeMappingSupport.*;
import static org.openmhealth.shim.withings.mapper.WithingsBodyMeasureDataPointMapper.BodyMeasureType.HEART_RATE;


/**
 * A mapper from Withings Body Measure endpoint responses (/measure?action=getmeas) to {@link HeartRate} objects when a
 * heart rate value is present in the body measure group.
 *
 * @author Chris Schaefbauer
 * @see <a href="http://oauth.withings.com/api/doc#api-Measure-get_measure">Body Measures API documentation</a>
 */
public class WithingsHeartRateDataPointMapper extends WithingsBodyMeasureDataPointMapper<HeartRate> {

    @Override
    public Optional<DataPoint<HeartRate>> asDataPoint(JsonNode listEntryNode) {

        JsonNode measuresNode = asRequiredNode(listEntryNode, "measures");

        Double value = null;
        Long unit = null;

        for (JsonNode measureNode : measuresNode) {
            Long type = asRequiredLong(measureNode, "type");
            if (type == HEART_RATE.getMagicNumber()) {
                value = asRequiredDouble(measureNode, "value");
                unit = asRequiredLong(measureNode, "unit");
            }
        }

        if (value == null || unit == null) {
            //this measuregrp does not have a heart rate measure
            return Optional.empty();
        }

        HeartRate.Builder heartRateBuilder = new HeartRate.Builder(actualValueOf(value, unit));


        setEffectiveTimeFrame(heartRateBuilder, listEntryNode);
        setUserComment(heartRateBuilder, listEntryNode);

        HeartRate heartRate = heartRateBuilder.build();
        Optional<Long> externalId = asOptionalLong(listEntryNode, "grpid");
        DataPoint<HeartRate> heartRateDataPoint =
                newDataPoint(heartRate, externalId.orElse(null), isSensed(listEntryNode).orElse(null),
                        null);
        return Optional.of(heartRateDataPoint);

    }

}
