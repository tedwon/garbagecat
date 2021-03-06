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
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermCollection;
import org.eclipselabs.garbagecat.domain.PermData;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * PAR_NEW_CMS_SERIAL_OLD
 * </p>
 * 
 * <p>
 * Combined {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} and
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent}. It looks like this happens when the young generation
 * fills up.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 42782.086: [GC 42782.086: [ParNew: 254464K->7680K(254464K), 0.2853553 secs]42782.371: [Tenured: 1082057K->934941K(1082084K), 6.2719770 secs] 1310721K->934941K(1336548K), 6.5587770 secs]
 * </pre>
 * 
 * <p>
 * 2) JDK 1.7 with perm data
 * </p>
 * 
 * <pre>
 * 6.102: [GC6.102: [ParNew: 19648K->2176K(19648K), 0.0184470 secs]6.121: [Tenured: 44849K->25946K(44864K), 0.2586250 secs] 60100K->25946K(64512K), [Perm : 43759K->43759K(262144K)], 0.2773070 secs] [Times: user=0.16 sys=0.01, real=0.28 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class ParNewCmsSerialOldEvent
        implements BlockingEvent, OldCollection, YoungData, OldData, CmsCollection, PermCollection, PermData {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC( )?" + JdkRegEx.TIMESTAMP + ": \\[ParNew: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]"
            + JdkRegEx.TIMESTAMP + ": \\[Tenured: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)(, \\[Perm : " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\])?, "
            + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";
    private static Pattern pattern = Pattern.compile(ParNewCmsSerialOldEvent.REGEX);

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
     * Young generation size (kilobytes) at beginning of GC event.
     */
    private int young;

    /**
     * Young generation size (kilobytes) at end of GC event.
     */
    private int youngEnd;

    /**
     * Available space in young generation (kilobytes). Equals young generation allocation minus one survivor space.
     */
    private int youngAvailable;

    /**
     * Old generation size (kilobytes) at beginning of GC event.
     */
    private int old;

    /**
     * Old generation size (kilobytes) at end of GC event.
     */
    private int oldEnd;

    /**
     * Space allocated to old generation (kilobytes).
     */
    private int oldAllocation;

    /**
     * Permanent generation size (kilobytes) at beginning of GC event.
     */
    private int permGen;

    /**
     * Permanent generation size (kilobytes) at end of GC event.
     */
    private int permGenEnd;

    /**
     * Space allocated to permanent generation (kilobytes).
     */
    private int permGenAllocation;

    /**
     * Create ParNew detail logging event from log entry.
     */
    public ParNewCmsSerialOldEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            young = Integer.parseInt(matcher.group(4));
            old = Integer.parseInt(matcher.group(9));
            oldEnd = Integer.parseInt(matcher.group(10));
            oldAllocation = Integer.parseInt(matcher.group(11));
            // Compute young end and young allocation after full GC
            int totalEnd = Integer.parseInt(matcher.group(14));
            youngEnd = totalEnd - oldEnd;
            int totalAllocation = Integer.parseInt(matcher.group(15));
            youngAvailable = totalAllocation - oldAllocation;
            if (matcher.group(16) != null) {
                permGen = Integer.parseInt(matcher.group(17));
                permGenEnd = Integer.parseInt(matcher.group(18));
                permGenAllocation = Integer.parseInt(matcher.group(19));

            }
            duration = JdkMath.convertSecsToMillis(matcher.group(20)).intValue();
        }
    }

    /**
     * Alternate constructor. Create ParNew detail logging event from values.
     * 
     * @param logEntry
     * @param timestamp
     * @param duration
     */
    public ParNewCmsSerialOldEvent(String logEntry, long timestamp, int duration) {
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

    public int getYoungOccupancyInit() {
        return young;
    }

    public int getYoungOccupancyEnd() {
        return youngEnd;
    }

    public int getYoungSpace() {
        return youngAvailable;
    }

    public int getOldOccupancyInit() {
        return old;
    }

    public int getOldOccupancyEnd() {
        return oldEnd;
    }

    public int getOldSpace() {
        return oldAllocation;
    }

    public int getPermOccupancyInit() {
        return permGen;
    }

    public int getPermOccupancyEnd() {
        return permGenEnd;
    }

    public int getPermSpace() {
        return permGenAllocation;
    }

    public String getName() {
        return JdkUtil.LogEventType.PAR_NEW_CMS_SERIAL_OLD.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return pattern.matcher(logLine).matches();
    }
}
