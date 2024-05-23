/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Ericsson
 *  Modifications Copyright (C) 2024 OpenInfra Foundation Europe
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.oran.smo.yangtools.parser.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import org.oran.smo.yangtools.parser.ParserExecutionContext;
import org.oran.smo.yangtools.parser.YangDeviceModel;
import org.oran.smo.yangtools.parser.findings.Finding;
import org.oran.smo.yangtools.parser.findings.FindingsManager;
import org.oran.smo.yangtools.parser.findings.ModifyableFindingSeverityCalculator;
import org.oran.smo.yangtools.parser.findings.ParserFindingType;
import org.oran.smo.yangtools.parser.input.FileBasedYangInput;
import org.oran.smo.yangtools.parser.model.ConformanceType;
import org.oran.smo.yangtools.parser.model.YangModel;
import org.oran.smo.yangtools.parser.model.statements.threegpp.ThreeGppExtensionsClassSupplier;

public class ParseIetfModulesTest {

    private static final String ORIG_IANA_CRYPT_HASH = "src/test/resources/_orig-modules/iana-crypt-hash-2014-08-06.yang";

    private static final String ORIG_IETF_YANG_TYPES = "src/test/resources/_orig-modules/ietf-yang-types-2019-11-04.yang";
    private static final String ORIG_IETF_INET_TYPES = "src/test/resources/_orig-modules/ietf-inet-types-2019-11-04.yang";

    private static final String ORIG_IETF_CRYPTO_TYPES = "src/test/resources/_orig-modules/ietf-crypto-types-2019-11-20.yang";
    private static final String ORIG_IETF_DATASTORES = "src/test/resources/_orig-modules/ietf-datastores-2018-02-14.yang";
    private static final String ORIG_IETF_INTERFACES = "src/test/resources/_orig-modules/ietf-interfaces-2018-02-20.yang";
    private static final String ORIG_IETF_IP = "src/test/resources/_orig-modules/ietf-ip-2018-02-22.yang";
    private static final String ORIG_IETF_KEYSTORE = "src/test/resources/_orig-modules/ietf-keystore-2019-11-20.yang";
    private static final String ORIG_IETF_NETCONF = "src/test/resources/_orig-modules/ietf-netconf-2011-06-01.yang";
    private static final String ORIG_IETF_NETCONF_ACM = "src/test/resources/_orig-modules/ietf-netconf-acm-2018-02-14.yang";
    private static final String ORIG_IETF_NETCONF_CLIENT = "src/test/resources/_orig-modules/ietf-netconf-client-2019-11-20.yang";
    private static final String ORIG_IETF_NETCONF_MONITORING = "src/test/resources/_orig-modules/ietf-netconf-monitoring-2010-10-04.yang";
    private static final String ORIG_IETF_NETCONF_NOTIFICATIONS = "src/test/resources/_orig-modules/ietf-netconf-notifications-2012-02-06.yang";
    private static final String ORIG_IETF_NETCONF_SERVER = "src/test/resources/_orig-modules/ietf-netconf-server-2018-09-20.yang";
    private static final String ORIG_IETF_NETCONF_WITH_DEFAULTS = "src/test/resources/_orig-modules/ietf-netconf-with-defaults-2011-06-01.yang";
    private static final String ORIG_IETF_NETWORK_INSTANCE = "src/test/resources/_orig-modules/ietf-network-instance-2019-01-21.yang";
    private static final String ORIG_IETF_RESTCONF = "src/test/resources/_orig-modules/ietf-restconf-2017-01-26.yang";
    private static final String ORIG_IETF_RESTCONF_MONITORING = "src/test/resources/_orig-modules/ietf-restconf-monitoring-2017-01-26.yang";
    private static final String ORIG_IETF_SSH_CLIENT = "src/test/resources/_orig-modules/ietf-ssh-client-2019-11-20.yang";
    private static final String ORIG_IETF_SSH_COMMON = "src/test/resources/_orig-modules/ietf-ssh-common-2019-11-20.yang";
    private static final String ORIG_IETF_SSH_SERVER = "src/test/resources/_orig-modules/ietf-ssh-server-2019-11-20-yang";
    private static final String ORIG_IETF_SUBSCRIBED_NOTIFICATIONS = "src/test/resources/_orig-modules/ietf-subscribed-notifications-2019-05-06.yang";
    private static final String ORIG_IETF_SYSTEM = "src/test/resources/_orig-modules/ietf-system-2014-08-06.yang";
    private static final String ORIG_IETF_TCP_CLIENT = "src/test/resources/_orig-modules/ietf-tcp-client-2019-10-18.yang";
    private static final String ORIG_IETF_TCP_COMMON = "src/test/resources/_orig-modules/ietf-tcp-common-2019-10-18.yang";
    private static final String ORIG_IETF_TCP_SERVER = "src/test/resources/_orig-modules/ietf-tcp-server-2019-10-18.yang";
    private static final String ORIG_IETF_TLS_CLIENT = "src/test/resources/_orig-modules/ietf-tls-client-2019-11-20.yang";
    private static final String ORIG_IETF_TLS_COMMON = "src/test/resources/_orig-modules/ietf-tls-common-2019-11-20.yang";
    private static final String ORIG_IETF_TLS_SERVER = "src/test/resources/_orig-modules/ietf-tls-server-2019-11-20.yang";

    private static final String ORIG_IETF_TRUST_ANCHORS = "src/test/resources/_orig-modules/ietf-trust-anchors-2019-04-29.yang";
    private static final String ORIG_IETF_TRUSTSTORE = "src/test/resources/_orig-modules/ietf-truststore-2019-11-20.yang";
    private static final String ORIG_IETF_X509_CERT_TO_NAME = "src/test/resources/_orig-modules/ietf-x509-cert-to-name-2014-12-10.yang";

    private static final String ORIG_IETF_YANG_LIBRARY = "src/test/resources/_orig-modules/ietf-yang-library-2019-01-04.yang";
    private static final String ORIG_IETF_YANG_METADATA = "src/test/resources/_orig-modules/ietf-yang-metadata-2016-08-05.yang";
    private static final String ORIG_IETF_YANG_PATCH = "src/test/resources/_orig-modules/ietf-yang-patch-2017-02-22.yang";
    private static final String ORIG_IETF_YANG_PUSH = "src/test/resources/_orig-modules/ietf-yang-push-2019-05-21.yang";
    private static final String ORIG_IETF_YANG_SCHEMA_MOUNT = "src/test/resources/_orig-modules/ietf-yang-schema-mount-2019-01-14.yang";

    @Test
    public void test_all_ietf() {

        YangDeviceModel yangDeviceModel;
        ModifyableFindingSeverityCalculator severityCalculator;
        FindingsManager findingsManager;
        ParserExecutionContext context;

        yangDeviceModel = new YangDeviceModel("Yang Parser JAR Test Device Model");
        severityCalculator = new ModifyableFindingSeverityCalculator();
        findingsManager = new FindingsManager(severityCalculator);

        final ThreeGppExtensionsClassSupplier otherStatementFactory = new ThreeGppExtensionsClassSupplier();

        context = new ParserExecutionContext(findingsManager, Arrays.asList(otherStatementFactory));
        context.setFailFast(false);
        context.setIgnoreImportedProtocolAccessibleObjects(true);

        severityCalculator.suppressFinding(ParserFindingType.P114_TYPEDEF_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P115_TYPEDEF_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P132_GROUPING_NOT_USED.toString());
        severityCalculator.suppressFinding(ParserFindingType.P133_GROUPING_USED_ONCE_ONLY.toString());
        severityCalculator.suppressFinding(ParserFindingType.P143_ENUM_WITHOUT_VALUE.toString());
        severityCalculator.suppressFinding(ParserFindingType.P144_BIT_WITHOUT_POSITION.toString());

        final List<String> ietfModules = Arrays.asList(ORIG_IETF_YANG_TYPES, ORIG_IETF_INET_TYPES,

                ORIG_IETF_DATASTORES, ORIG_IETF_INTERFACES, ORIG_IETF_IP, ORIG_IETF_NETCONF, ORIG_IETF_NETCONF_ACM,
                ORIG_IETF_NETCONF_MONITORING, ORIG_IETF_NETCONF_NOTIFICATIONS, ORIG_IETF_NETCONF_WITH_DEFAULTS,
                ORIG_IETF_NETWORK_INSTANCE, ORIG_IETF_RESTCONF, ORIG_IETF_RESTCONF_MONITORING,
                ORIG_IETF_SUBSCRIBED_NOTIFICATIONS, ORIG_IETF_SYSTEM, ORIG_IETF_TCP_CLIENT, ORIG_IETF_TCP_COMMON,
                ORIG_IETF_TCP_SERVER, ORIG_IETF_X509_CERT_TO_NAME, ORIG_IETF_YANG_LIBRARY, ORIG_IETF_YANG_METADATA,
                ORIG_IETF_YANG_PATCH, ORIG_IETF_YANG_PUSH, ORIG_IETF_YANG_SCHEMA_MOUNT,

                ORIG_IANA_CRYPT_HASH

        /*
         * All of the below are not stable, i.e. not released as RFCs yet.
         *
         * They use import-by-revision. We will not process these for now.
         */

        //				ORIG_IETF_CRYPTO_TYPES,
        //				ORIG_IETF_KEYSTORE,
        //				ORIG_IETF_NETCONF_CLIENT,
        //				ORIG_IETF_NETCONF_SERVER,
        //				ORIG_IETF_SSH_CLIENT,
        //				ORIG_IETF_SSH_COMMON,
        //				ORIG_IETF_SSH_SERVER,
        //				ORIG_IETF_TLS_CLIENT,
        //				ORIG_IETF_TLS_COMMON,
        //				ORIG_IETF_TLS_SERVER,
        //				ORIG_IETF_TRUST_ANCHORS,
        //				ORIG_IETF_TRUSTSTORE,
        );

        final List<YangModel> yangFiles = new ArrayList<>();
        for (final String absoluteImplementsFilePath : ietfModules) {
            yangFiles.add(new YangModel(new FileBasedYangInput(new File(absoluteImplementsFilePath)),
                    ConformanceType.IMPLEMENT));
        }

        yangDeviceModel.parseIntoYangModels(context, yangFiles);

        assertTrue(findingsManager.getAllFindings().size() == 0);

        printFindings(findingsManager.getAllFindings());
    }

    private void printFindings(final Set<Finding> findings) {

        final List<String> collect = findings.stream().map(Finding::toString).collect(Collectors.toList());
        Collections.sort(collect);
        collect.forEach(System.err::println);
    }
}
