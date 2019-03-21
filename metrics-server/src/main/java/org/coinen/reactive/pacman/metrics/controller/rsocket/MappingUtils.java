package org.coinen.reactive.pacman.metrics.controller.rsocket;

import java.util.List;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.internal.DefaultMeter;
import io.rsocket.rpc.metrics.om.MeterId;
import io.rsocket.rpc.metrics.om.MeterMeasurement;
import io.rsocket.rpc.metrics.om.MeterStatistic;
import io.rsocket.rpc.metrics.om.MeterTag;
import io.rsocket.rpc.metrics.om.MeterType;

public class MappingUtils {

    static Meter mapMeter(io.rsocket.rpc.metrics.om.Meter apiMeter) {
        MeterId id = apiMeter.getId();

        Meter.Type type = mapType(id.getType());

        return new DefaultMeter(
            new Meter.Id(id.getName(),
                mapTags(id.getTagList()),
                id.getBaseUnit(),
                id.getDescription(),
                type
            ),
            type,
            apiMeter.getMeasureList()
                    .stream()
                    .map(MappingUtils::mapMeasurement)
                    .collect(Collectors.toList())
        );
    }

    static Measurement mapMeasurement(MeterMeasurement measurement) {
        return new Measurement(measurement::getValue, mapStatistic(measurement.getStatistic()));
    }

    static Meter.Type mapType(MeterType meterType) {
        switch (meterType) {
            case GAUGE:
                return Meter.Type.GAUGE;
            case TIMER:
                return Meter.Type.TIMER;
            case COUNTER:
                return Meter.Type.COUNTER;
            case LONG_TASK_TIMER:
                return Meter.Type.LONG_TASK_TIMER;
            case DISTRIBUTION_SUMMARY:
                return Meter.Type.DISTRIBUTION_SUMMARY;
            case UNRECOGNIZED:
            case OTHER:
            default:
                return Meter.Type.OTHER;
        }
    }

    static Statistic mapStatistic(MeterStatistic meterStatistic) {
        switch (meterStatistic) {
            case MAX:
                return Statistic.MAX;
            case COUNT:
                return Statistic.COUNT;
            case TOTAL:
                return Statistic.TOTAL;
            case VALUE:
                return Statistic.VALUE;
            case DURATION:
                return Statistic.DURATION;
            case TOTAL_TIME:
                return Statistic.TOTAL_TIME;
            case ACTIVE_TASKS:
                return Statistic.ACTIVE_TASKS;
            case UNKNOWN:
            case UNRECOGNIZED:
            default:
                return Statistic.UNKNOWN;
        }
    }

    static Tags mapTags(List<MeterTag> tags) {
        return Tags.of(tags.stream()
                           .map(meterTag -> Tag.of(meterTag.getKey(), meterTag.getValue()))
                           .collect(Collectors.toList()));
    }
}
