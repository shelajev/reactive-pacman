import { IMeter, Timer, RawMeterTag } from "rsocket-rpc-metrics";
import { Meter } from "metrics-idl";


export function convert(meter: IMeter): Meter[] {
    const meterType = meterTypeLookup(meter.type);
    switch (meterType) {
        case Meter.Type.TIMER:
            return convertTimer((meter as any) as Timer);
        case Meter.Type.COUNTER:
        case Meter.Type.GAUGE:
        case Meter.Type.LONG_TASK_TIMER:
        case Meter.Type.DISTRIBUTION_SUMMARY:
        case Meter.Type.OTHER:
            return basicConverter(meter);
        default:
            throw new Error('unsupported type ' + meterType);
    }
}

function meterTypeLookup(meterType: string): Meter.Type {
    switch (meterType) {
        case 'gauge':
            return Meter.Type.GAUGE;
        case 'timer':
            return Meter.Type.TIMER;
        case 'counter':
            return Meter.Type.COUNTER;
        case 'longTaskTimer':
            return Meter.Type.LONG_TASK_TIMER;
        case 'distributionSummary':
            return Meter.Type.DISTRIBUTION_SUMMARY;
        case 'other':
            return Meter.Type.OTHER;
        default:
            throw new Error('unknown type ' + meterType);
    }
}

function statisticTypeLookup(statistic: string): Meter.Statistic {
    switch (statistic) {
        case 'max':
            return Meter.Statistic.MAX;
        case 'count':
            return Meter.Statistic.COUNT;
        case 'total':
            return Meter.Statistic.TOTAL;
        case 'value':
            return Meter.Statistic.VALUE;
        case 'unknown':
            return Meter.Statistic.UNKNOWN;
        case 'duration':
            return Meter.Statistic.DURATION;
        case 'totalTime':
            return Meter.Statistic.TOTAL_TIME;
        case 'activeTasks':
            return Meter.Statistic.ACTIVE_TASKS;
        default:
            throw new Error('unknown type ' + statistic);
    }
}

function convertTimer(timer: Timer): Meter[] {
    const meters = [];
    const name = timer.name;
    const tags = convertTags(timer.tags);

    //Add meters for percentiles of interest
    const valuesSnapshot = timer.percentiles();
    Object.keys(valuesSnapshot).forEach(percentile => {
        const value = toNanoseconds(valuesSnapshot[percentile]);
        // Make sure we're dealing with a real value before pushing
        if (!isNaN(value)) {
            const meter = new Meter();

            const percentileTag = new Meter.Tag();
            percentileTag.setKey('percentile');
            percentileTag.setValue(percentile);

            const meterId = new Meter.Id();
            meterId.setName(name);
            tags.forEach(tag => meterId.addTag(tag));
            meterId.addTag(percentileTag);
            meterId.setType(Meter.Type.TIMER);
            meterId.setDescription(timer.description);
            meterId.setBaseUnit('nanoseconds');

            meter.setId(meterId);

            const measure = new Meter.Measurement();
            measure.setValue(value);
            measure.setStatistic(statisticTypeLookup(timer.statistic));

            meter.addMeasure(measure);

            meters.push(meter);
        }
    });

    //add a meter for total count and max time
    const histMeter = new Meter();

    const meterId = new Meter.Id();
    meterId.setName(name);
    tags.forEach(tag => meterId.addTag(tag));
    meterId.setType(Meter.Type.TIMER);
    meterId.setDescription(timer.description);
    meterId.setBaseUnit('nanoseconds');

    const totalCount = new Meter.Measurement();
    totalCount.setValue(timer.totalCount());
    totalCount.setStatistic(Meter.Statistic.COUNT);
    histMeter.addMeasure(totalCount);

    const maxTime = new Meter.Measurement();
    maxTime.setValue(timer.max());
    maxTime.setStatistic(Meter.Statistic.MAX);
    histMeter.addMeasure(maxTime);

    histMeter.setId(meterId);

    meters.push(histMeter);

    return meters;
}

function basicConverter(imeter: IMeter): Meter[] {
    const meters: Meter[] = [];
    const name = imeter.name;
    const tags = convertTags(imeter.tags);

    //Add meters for different windowed EWMAs
    const valuesSnapshot = imeter.rates();
    Object.keys(valuesSnapshot).forEach(rate => {
        const meter = new Meter();

        const value = valuesSnapshot[rate];
        const ewmaTag = new Meter.Tag();
        ewmaTag.setKey('moving-average-minutes');
        ewmaTag.setValue(rate);

        const meterId = new Meter.Id();
        meterId.setName(name);
        tags.forEach(tag => meterId.addTag(tag));
        meterId.addTag(ewmaTag);
        meterId.setType(meterTypeLookup(imeter.type));
        meterId.setDescription(imeter.description);
        meterId.setBaseUnit(imeter.units);

        meter.setId(meterId);

        const measure = new Meter.Measurement();
        measure.setValue(value);
        measure.setStatistic(statisticTypeLookup(imeter.statistic));

        meter.addMeasure(measure);

        meters.push(meter);
    });

    return meters;
}

function convertTags(tags: RawMeterTag[]): Meter.Tag[] {
    return (tags || []).map(tag => {
        const finalTag = new Meter.Tag();
        finalTag.setKey(tag.key);
        finalTag.setValue(tag.value);
        return finalTag;
    });
}

//Not safe for timestamps, just time measurements
function toNanoseconds(milliseconds: number) {
    return milliseconds * 1000 * 1000;
}
