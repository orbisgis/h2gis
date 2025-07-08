/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.identityhashcode;

import static org.graalvm.compiler.nodes.extended.BranchProbabilityNode.FAST_PATH_PROBABILITY;
import static org.graalvm.compiler.nodes.extended.BranchProbabilityNode.probability;

import org.graalvm.compiler.core.common.spi.ForeignCallDescriptor;
import org.graalvm.compiler.graph.Node.ConstantNodeParameter;
import org.graalvm.compiler.graph.Node.NodeIntrinsic;
import org.graalvm.compiler.nodes.extended.ForeignCallNode;
import org.graalvm.compiler.replacements.IdentityHashCodeSnippets;
import org.graalvm.compiler.word.ObjectAccess;

import com.oracle.svm.core.config.ConfigurationValues;
import com.oracle.svm.core.snippets.SnippetRuntime;
import com.oracle.svm.core.snippets.SnippetRuntime.SubstrateForeignCallDescriptor;

final class SubstrateIdentityHashCodeSnippets extends IdentityHashCodeSnippets {

    static final SubstrateForeignCallDescriptor GENERATE_IDENTITY_HASH_CODE = SnippetRuntime.findForeignCall(IdentityHashCodeSupport.class, "generateIdentityHashCode", true,
                    IdentityHashCodeSupport.IDENTITY_HASHCODE_LOCATION);

    @Override
    protected int computeIdentityHashCode(Object obj) {
        int identityHashCode = ObjectAccess.readInt(obj, ConfigurationValues.getObjectLayout().getIdentityHashCodeOffset(), IdentityHashCodeSupport.IDENTITY_HASHCODE_LOCATION);
        if (probability(FAST_PATH_PROBABILITY, identityHashCode != 0)) {
            return identityHashCode;
        } else {
            return generateIdentityHashCode(GENERATE_IDENTITY_HASH_CODE, obj);
        }
    }

    @NodeIntrinsic(ForeignCallNode.class)
    private static native int generateIdentityHashCode(@ConstantNodeParameter ForeignCallDescriptor descriptor, Object obj);
}
