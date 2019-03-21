package org.coinen.reactive.pacman.metrics;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.internal.DefaultMeter;

public class MappingUtils {

    public static Meter mapMeter(org.coinen.pacman.metrics.Meter apiMeter) {
        org.coinen.pacman.metrics.Meter.Id id = apiMeter.getId();

        Meter.Type type = mapType(id.getType());

        return new DefaultMeter(
            mapId(id),
            type,
            apiMeter.getMeasureList()
                    .stream()
                    .map(MappingUtils::mapMeasurement)
                    .collect(Collectors.toList())
        );
    }

    private static Meter.Id mapId(org.coinen.pacman.metrics.Meter.Id id) {
        return new Meter.Id(id.getName(),
            mapTags(id.getTagList()),
            id.getBaseUnit(),
            id.getDescription(),
            mapType(id.getType())
        );
    }

    private static Measurement mapMeasurement(org.coinen.pacman.metrics.Meter.Measurement measurement) {
        return new Measurement(measurement::getValue, mapStatistic(measurement.getStatistic()));
    }

    private static Meter.Type mapType(org.coinen.pacman.metrics.Meter.Type meterType) {
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

    private static Statistic mapStatistic(org.coinen.pacman.metrics.Meter.Statistic meterStatistic) {
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

    private static Tags mapTags(List<org.coinen.pacman.metrics.Meter.Tag> tags) {
        return Tags.of(tags.stream()
                           .map(meterTag -> Tag.of(meterTag.getKey(), meterTag.getValue()))
                           .collect(Collectors.toList()));
    }



    public static org.coinen.pacman.metrics.Meter mapMeter(Meter meter) {
        Meter.Id id = meter.getId();


        return org.coinen.pacman.metrics.Meter.newBuilder()
            .addAllMeasure(StreamSupport.stream(meter.measure().spliterator(), false)
                                .map(MappingUtils::mapMeasurement)
                                .collect(Collectors.toList()))
            .setId(mapId(id))
            .build();
    }

    private static org.coinen.pacman.metrics.Meter.Id mapId(Meter.Id id) {
        Meter.Id syntheticAssociation = id.syntheticAssociation();

        org.coinen.pacman.metrics.Meter.Id.Builder builder =
            org.coinen.pacman.metrics.Meter.Id.newBuilder()
                                              .setType(mapType(id.getType()))
                                              .setName(id.getName() == null ? "" : id.getName())
                                              .setBaseUnit(id.getBaseUnit() == null ? "" : id.getBaseUnit() )
                                              .setDescription(id.getDescription() == null ? "" : id.getDescription())
                                              .addAllTag(mapTags(id.getTagsAsIterable()));

        if (syntheticAssociation != null) {
           builder.setSyntheticAssociation(mapId(syntheticAssociation));
        }

        return builder.build();
    }

    public static org.coinen.pacman.metrics.Meter.Measurement mapMeasurement(Measurement measurement) {
        return org.coinen.pacman.metrics.Meter.Measurement.newBuilder()
                                                    .setValue(measurement.getValue())
                                                    .setStatistic(mapStatistic(measurement.getStatistic()))
                                                    .build();
    }

    public static org.coinen.pacman.metrics.Meter.Type mapType(Meter.Type meterType) {
        switch (meterType) {
            case GAUGE:
                return org.coinen.pacman.metrics.Meter.Type.GAUGE;
            case TIMER:
                return org.coinen.pacman.metrics.Meter.Type.TIMER;
            case COUNTER:
                return org.coinen.pacman.metrics.Meter.Type.COUNTER;
            case LONG_TASK_TIMER:
                return org.coinen.pacman.metrics.Meter.Type.LONG_TASK_TIMER;
            case DISTRIBUTION_SUMMARY:
                return org.coinen.pacman.metrics.Meter.Type.DISTRIBUTION_SUMMARY;
            case OTHER:
                return org.coinen.pacman.metrics.Meter.Type.OTHER;
            default:
                return org.coinen.pacman.metrics.Meter.Type.UNRECOGNIZED;
        }
    }

    public static org.coinen.pacman.metrics.Meter.Statistic mapStatistic(Statistic meterStatistic) {
        switch (meterStatistic) {
            case MAX:
                return org.coinen.pacman.metrics.Meter.Statistic.MAX;
            case COUNT:
                return org.coinen.pacman.metrics.Meter.Statistic.COUNT;
            case TOTAL:
                return org.coinen.pacman.metrics.Meter.Statistic.TOTAL;
            case VALUE:
                return org.coinen.pacman.metrics.Meter.Statistic.VALUE;
            case DURATION:
                return org.coinen.pacman.metrics.Meter.Statistic.DURATION;
            case TOTAL_TIME:
                return org.coinen.pacman.metrics.Meter.Statistic.TOTAL_TIME;
            case ACTIVE_TASKS:
                return org.coinen.pacman.metrics.Meter.Statistic.ACTIVE_TASKS;
            case UNKNOWN:
                return org.coinen.pacman.metrics.Meter.Statistic.UNKNOWN;
            default:
                return org.coinen.pacman.metrics.Meter.Statistic.UNRECOGNIZED;
        }
    }

    public static List<org.coinen.pacman.metrics.Meter.Tag> mapTags(Iterable<Tag> tags) {
        return StreamSupport.stream(tags.spliterator(), false)
                            .map(meterTag -> org.coinen.pacman.metrics.Meter.Tag.newBuilder()
                                                                          .setKey(meterTag.getKey())
                                                                          .setValue(meterTag.getValue())
                                                                          .build())
                            .collect(Collectors.toList());
    }
}
