package io.github.phantamanta44.libnine.util.math;

public class RunningAverage {

    private final int[] samples;
    private int pointer;

    public RunningAverage(int sampleSize) {
        this.samples = new int[sampleSize];
        this.pointer = 0;
    }

    public int calculateAndCycle(int newSample) {
        int result = 0;
        for (int sample : samples)
            result += sample;
        samples[pointer = ((pointer + 1) % samples.length)] = newSample;
        return Math.round((float)result / samples.length);
    }

}
