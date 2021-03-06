/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.PermCollection;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_FULL_GC
 * </p>
 * 
 * <p>
 * G1 collector Full GC event. A serial (single-threaded) collector, which means it will take a very long time to
 * collect a large heap. If the G1 collector is running optimally, there will not be any G1 Full GC collections. G1 Full
 * GCs happen when the PermGen/Metaspace fills up, when the old space fills up with humongous objects (allocated
 * directly in the old space), or when there are more allocations than the G1 can concurrently collect.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 5060.152: [Full GC (System.gc()) 2270M->2038M(3398M), 5.8360430 secs]
 * </pre>
 * 
 * <p>
 * 2) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [Full GC (System.gc()) 2270M->2038M(3398M), 5.8360430 secs]
 * </pre>
 * 
 * <p>
 * 3) After {@link org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction}:
 * </p>
 * 
 * <pre>
 * 105.151: [Full GC (System.gc()) 5820M->1381M(30G), 5.5390169 secs] 5820M->1382M(30720M) [Times: user=5.76 sys=1.00, real=5.53 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1FullGCEvent implements BlockingEvent, TriggerData, CombinedData, G1Collection, PermCollection {

    /**
     * Regular expression for triggers associated with this logging event.
     */
    public static final String TRIGGER = "(" + JdkRegEx.TRIGGER_SYSTEM_GC + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[Full GC (\\("
            + TRIGGER + "\\) )?" + JdkRegEx.SIZE_G1 + "->" + JdkRegEx.SIZE_G1 + "\\(" + JdkRegEx.SIZE_G1 + "\\), "
            + JdkRegEx.DURATION + "\\]( " + JdkRegEx.SIZE_G1 + "->" + JdkRegEx.SIZE_G1 + "\\(" + JdkRegEx.SIZE_G1
            + "\\))?" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    private static final Pattern pattern = Pattern.compile(REGEX);
    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the GC event in milliseconds (rounded).
     */
    private int duration;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Young generation size (kilobytes) at beginning of GC event.
     */
    private int combined;

    /**
     * Young generation size (kilobytes) at end of GC event.
     */
    private int combinedEnd;

    /**
     * Available space in young generation (kilobytes). Equals young generation allocation minus one survivor space.
     */
    private int combinedAvailable;

    /**
     * Create detail logging event from log entry.
     */
    public G1FullGCEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            trigger = matcher.group(14);
            combined = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(16)), matcher.group(17).charAt(0));
            combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(18)), matcher.group(19).charAt(0));
            combinedAvailable = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(20)), matcher.group(21).charAt(0));
            duration = JdkMath.convertSecsToMillis(matcher.group(22)).intValue();
        }
    }

    /**
     * Alternate constructor. Create detail logging event from values.
     * 
     * @param logEntry
     * @param timestamp
     * @param duration
     */
    public G1FullGCEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public int getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getCombinedOccupancyInit() {
        return combined;
    }

    public int getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public int getCombinedSpace() {
        return combinedAvailable;
    }

    public String getName() {
        return JdkUtil.LogEventType.G1_FULL_GC.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX);
    }
}
