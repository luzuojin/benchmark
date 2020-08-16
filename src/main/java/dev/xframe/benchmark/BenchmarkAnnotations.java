package dev.xframe.benchmark;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

public class BenchmarkAnnotations {
    
    @SuppressWarnings("unchecked")
    public static void makeComplete(Class<?> claz) {
        try {
            Method mt = Class.class.getDeclaredMethod("annotationData");
            mt.setAccessible(true);
            
            Object annotationData = mt.invoke(claz);
            
            Field ft = annotationData.getClass().getDeclaredField("annotations");
            ft.setAccessible(true);
            
            Map<Class<?>, Annotation> map = (Map<Class<?>, Annotation>) ft.get(annotationData);
            if(map == Collections.EMPTY_MAP) {
                map = new LinkedHashMap<>();
                ft.set(annotationData, map);
            }
            
            if(!map.containsKey(BenchmarkMode.class)) map.put(BenchmarkMode.class, benchmarkMode(Mode.AverageTime));
            if(!map.containsKey(Warmup.class)) map.put(Warmup.class, warmup(5, 100000, 1));
            if(!map.containsKey(Measurement.class)) map.put(Measurement.class, measurement(5, 100000, 2));
            if(!map.containsKey(OutputTimeUnit.class)) map.put(OutputTimeUnit.class, outputTimeUnit(TimeUnit.MICROSECONDS));
            if(!map.containsKey(Fork.class)) map.put(Fork.class, fork(1));
            
        } catch (Exception e) {
            e.printStackTrace();
            //ignore
        }
    }
    
    public static BenchmarkMode benchmarkMode(Mode... mode) {
        return new BenchmarkMode() {
            public Class<? extends Annotation> annotationType() {
                return BenchmarkMode.class;
            }
            public Mode[] value() {
                return mode;
            }
        };
    }
    
    public static Warmup warmup(int iterations, int batchSize, int perSeconds) {
        return new Warmup() {
            public Class<? extends Annotation> annotationType() {
                return Warmup.class;
            }
            public TimeUnit timeUnit() {
                return TimeUnit.SECONDS;
            }
            public int time() {
                return perSeconds;
            }
            public int iterations() {
                return iterations;
            }
            public int batchSize() {
                return batchSize;
            }
        };
    }
    
    public static Measurement measurement(int iterations, int batchSize, int perSeconds) {
        return new Measurement() {
            public Class<? extends Annotation> annotationType() {
                return Measurement.class;
            }
            public TimeUnit timeUnit() {
                return TimeUnit.SECONDS;
            }
            public int time() {
                return perSeconds;
            }
            public int iterations() {
                return iterations;
            }
            public int batchSize() {
                return batchSize;
            }
        };
    }
    
    public static OutputTimeUnit outputTimeUnit(TimeUnit unit) {
        return new OutputTimeUnit() {
            public Class<? extends Annotation> annotationType() {
                return OutputTimeUnit.class;
            }
            public TimeUnit value() {
                return unit;
            }
        };
    }
    
    public static Fork fork(int fork) {
        String emptyArg = "blank_blank_blank_2014";
        String[] emptyArgs = new String[] {emptyArg};
        return new Fork() {
            public Class<? extends Annotation> annotationType() {
                return Fork.class;
            }
            public int value() {
                return fork;
            }
            public int warmups() {
                return -1;
            }
            public String[] jvmArgsPrepend() {
                return emptyArgs;
            }
            public String[] jvmArgsAppend() {
                return emptyArgs;
            }
            public String[] jvmArgs() {
                return emptyArgs;
            }
            public String jvm() {
                return emptyArg;
            }
        };
    }
    

}
