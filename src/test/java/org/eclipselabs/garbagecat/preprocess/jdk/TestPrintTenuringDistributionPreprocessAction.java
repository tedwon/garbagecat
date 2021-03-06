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
package org.eclipselabs.garbagecat.preprocess.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestPrintTenuringDistributionPreprocessAction extends TestCase {

    public void testDefNewLine() {
        String priorLogLine = null;
        String logLine = "10.204: [GC 10.204: [DefNew";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }

    public void testDesiredSurvivorSizeLine() {
        String priorLogLine = null;
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }

    public void testAgeLine() {
        String priorLogLine = null;
        String logLine = "- age 1: 3177664 bytes, 3177664 total";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }

    public void testLastLine() {
        String priorLogLine = "- age 1: 3177664 bytes, 3177664 total";
        String logLine = ": 36825K->4352K(39424K), 0.0224830 secs] 44983K->14441K(126848K), 0.0225800 secs]";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }

    public void testParNewLine() {
        String priorLogLine = null;
        String logLine = "50.594: [GC 50.594: [ParNew";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }

    public void testParNewPromotionFailedLine() {
        String priorLogLine = null;
        String logLine = "877369.458: [GC 877369.459: [ParNew (promotion failed)";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }

    public void testLineBeforeConcurrentModeFailure() {
        String priorLogLine = null;
        String logLine = ": 917504K->917504K(917504K), 5.5887120 secs]877375.047: [CMS877378.691: "
                + "[CMS-concurrent-mark: 5.714/11.380 secs] [Times: user=14.72 sys=4.81, real=11.38 secs]";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }

    public void testAbortPrecleanLineBeforeConcurrentModeFailure() {
        String priorLogLine = null;
        String logLine = ": 910978K->910978K(917504K), 1.7096800 secs]891199.155: [CMS CMS: abort preclean "
                + "due to time 891199.474: [CMS-concurrent-abortable-preclean: 4.227/6.007 secs] "
                + "[Times: user=9.55 sys=1.32, real=6.01 secs]";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }

    public void testParallelScavengeFirstLine() {
        String priorLogLine = null;
        String logLine = "10.392: [GC";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }

    public void testParallelScavengeLastLine() {
        String priorLogLine = "Desired survivor size 497025024 bytes, new threshold 7 (max 15)";
        String logLine = " [PSYoungGen: 970752K->104301K(1456128K)] 970752K->104301K(3708928K), 0.1992940 secs] "
                + "[Times: user=0.68 sys=0.05, real=0.20 secs]";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString() + ".",
                PrintTenuringDistributionPreprocessAction.match(logLine, priorLogLine));
    }
}
