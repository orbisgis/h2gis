// CheckStyle: stop header check
// CheckStyle: stop line length check
// GENERATED CONTENT - DO NOT EDIT
// GENERATOR: org.graalvm.compiler.lir.processor.IntrinsicStubProcessor
package com.oracle.svm.graal.stubs;

import jdk.vm.ci.code.Architecture;
import com.oracle.svm.graal.RuntimeCPUFeatureRegion;
import org.graalvm.compiler.replacements.nodes.AESNode.CryptMode;
import jdk.vm.ci.meta.JavaKind;
import org.graalvm.compiler.replacements.nodes.VectorizedMismatchNode;
import com.oracle.svm.core.cpufeature.Stubs;
import org.graalvm.compiler.replacements.amd64.AMD64CalcStringAttributesNode;
import com.oracle.svm.core.Uninterruptible;
import java.util.EnumSet;
import org.graalvm.compiler.replacements.nodes.GHASHProcessBlocksNode;
import org.graalvm.compiler.replacements.nodes.ArrayCompareToNode;
import org.graalvm.compiler.lir.amd64.AMD64CalcStringAttributesOp.Op;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.compiler.replacements.nodes.ArrayIndexOfNode;
import com.oracle.svm.core.SubstrateTargetDescription;
import org.graalvm.compiler.replacements.nodes.ArrayRegionEqualsNode;
import org.graalvm.compiler.replacements.nodes.AESNode;
import org.graalvm.compiler.replacements.nodes.CounterModeAESNode;
import org.graalvm.compiler.api.replacements.Fold;
import org.graalvm.compiler.replacements.nodes.ArrayEqualsNode;
import com.oracle.svm.core.snippets.SubstrateForeignCallTarget;
import org.graalvm.compiler.replacements.amd64.AMD64ArrayRegionEqualsWithMaskNode;
import org.graalvm.compiler.replacements.nodes.ArrayCopyWithConversionsNode;
import org.graalvm.compiler.core.common.Stride;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.replacements.nodes.ArrayRegionCompareToNode;

public class SVMIntrinsicStubsGen{

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf1S1(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, false, array, arrayOffset, arrayLength, fromIndex, v1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf1S1RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf1S2(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, false, array, arrayOffset, arrayLength, fromIndex, v1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf1S2RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf1S4(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, false, array, arrayOffset, arrayLength, fromIndex, v1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf1S4RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf2S1(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, false, array, arrayOffset, arrayLength, fromIndex, v1, v2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf2S1RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf2S2(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, false, array, arrayOffset, arrayLength, fromIndex, v1, v2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf2S2RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf2S4(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, false, array, arrayOffset, arrayLength, fromIndex, v1, v2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf2S4RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfWithMaskS1(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, true, array, arrayOffset, arrayLength, fromIndex, v1, v2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfWithMaskS1RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, true, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfWithMaskS2(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, true, array, arrayOffset, arrayLength, fromIndex, v1, v2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfWithMaskS2RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, true, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfWithMaskS4(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, true, array, arrayOffset, arrayLength, fromIndex, v1, v2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfWithMaskS4RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, true, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveS1(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, true, false, array, arrayOffset, arrayLength, fromIndex, v1, v2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveS1RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, true, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveS2(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, true, false, array, arrayOffset, arrayLength, fromIndex, v1, v2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveS2RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, true, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveS4(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, true, false, array, arrayOffset, arrayLength, fromIndex, v1, v2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveS4RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, true, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf3S1(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, false, array, arrayOffset, arrayLength, fromIndex, v1, v2, v3);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf3S1RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2, v3);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf3S2(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, false, array, arrayOffset, arrayLength, fromIndex, v1, v2, v3);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf3S2RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2, v3);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf3S4(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, false, array, arrayOffset, arrayLength, fromIndex, v1, v2, v3);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf3S4RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2, v3);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf4S1(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, false, array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf4S1RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf4S2(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, false, array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf4S2RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf4S4(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, false, array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOf4S4RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, false, false, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveWithMaskS1(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, true, true, array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveWithMaskS1RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S1, true, true, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveWithMaskS2(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, true, true, array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveWithMaskS2RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S2, true, true, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveWithMaskS4(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, true, true, array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int indexOfTwoConsecutiveWithMaskS4RTC(java.lang.Object array, long arrayOffset, int arrayLength, int fromIndex, int v1, int v2, int v3, int v4) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class));
        try {
            return ArrayIndexOfNode.optimizedArrayIndexOf(JavaKind.Void, Stride.S4, true, true, Stubs.getRuntimeCheckedCPUFeatures(ArrayIndexOfNode.class), array, arrayOffset, arrayLength, fromIndex, v1, v2, v3, v4);
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean longArraysEquals(org.graalvm.word.Pointer array1, long offset1, org.graalvm.word.Pointer array2, long offset2, int length) {
        return ArrayEqualsNode.equals(array1, offset1, array2, offset2, length, JavaKind.Long);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean longArraysEqualsRTC(org.graalvm.word.Pointer array1, long offset1, org.graalvm.word.Pointer array2, long offset2, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayEqualsNode.class));
        try {
            return ArrayEqualsNode.equals(array1, offset1, array2, offset2, length, JavaKind.Long, Stubs.getRuntimeCheckedCPUFeatures(ArrayEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean floatArraysEquals(org.graalvm.word.Pointer array1, long offset1, org.graalvm.word.Pointer array2, long offset2, int length) {
        return ArrayEqualsNode.equals(array1, offset1, array2, offset2, length, JavaKind.Float);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean floatArraysEqualsRTC(org.graalvm.word.Pointer array1, long offset1, org.graalvm.word.Pointer array2, long offset2, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayEqualsNode.class));
        try {
            return ArrayEqualsNode.equals(array1, offset1, array2, offset2, length, JavaKind.Float, Stubs.getRuntimeCheckedCPUFeatures(ArrayEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean doubleArraysEquals(org.graalvm.word.Pointer array1, long offset1, org.graalvm.word.Pointer array2, long offset2, int length) {
        return ArrayEqualsNode.equals(array1, offset1, array2, offset2, length, JavaKind.Double);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean doubleArraysEqualsRTC(org.graalvm.word.Pointer array1, long offset1, org.graalvm.word.Pointer array2, long offset2, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayEqualsNode.class));
        try {
            return ArrayEqualsNode.equals(array1, offset1, array2, offset2, length, JavaKind.Double, Stubs.getRuntimeCheckedCPUFeatures(ArrayEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS1S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS1S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS1S2(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS1S2RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS1S4(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS1S4RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS2S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS2S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS2S2(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS2S2RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS2S4(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS2S4RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS4S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS4S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS4S2(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS4S2RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS4S4(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsS4S4RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsDynamicStrides(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length, int dynamicStrides) {
        return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, dynamicStrides);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsDynamicStridesRTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length, int dynamicStrides) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        try {
            return ArrayRegionEqualsNode.regionEquals(arrayA, offsetA, arrayB, offsetB, length, dynamicStrides, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionEqualsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int byteArrayCompareToByteArray(org.graalvm.word.Pointer arrayA, int lengthA, org.graalvm.word.Pointer arrayB, int lengthB) {
        return ArrayCompareToNode.compareTo(arrayA, lengthA, arrayB, lengthB, Stride.S1, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int byteArrayCompareToByteArrayRTC(org.graalvm.word.Pointer arrayA, int lengthA, org.graalvm.word.Pointer arrayB, int lengthB) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCompareToNode.class));
        try {
            return ArrayCompareToNode.compareTo(arrayA, lengthA, arrayB, lengthB, Stride.S1, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int byteArrayCompareToCharArray(org.graalvm.word.Pointer arrayA, int lengthA, org.graalvm.word.Pointer arrayB, int lengthB) {
        return ArrayCompareToNode.compareTo(arrayA, lengthA, arrayB, lengthB, Stride.S1, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int byteArrayCompareToCharArrayRTC(org.graalvm.word.Pointer arrayA, int lengthA, org.graalvm.word.Pointer arrayB, int lengthB) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCompareToNode.class));
        try {
            return ArrayCompareToNode.compareTo(arrayA, lengthA, arrayB, lengthB, Stride.S1, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int charArrayCompareToByteArray(org.graalvm.word.Pointer arrayA, int lengthA, org.graalvm.word.Pointer arrayB, int lengthB) {
        return ArrayCompareToNode.compareTo(arrayA, lengthA, arrayB, lengthB, Stride.S2, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int charArrayCompareToByteArrayRTC(org.graalvm.word.Pointer arrayA, int lengthA, org.graalvm.word.Pointer arrayB, int lengthB) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCompareToNode.class));
        try {
            return ArrayCompareToNode.compareTo(arrayA, lengthA, arrayB, lengthB, Stride.S2, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int charArrayCompareToCharArray(org.graalvm.word.Pointer arrayA, int lengthA, org.graalvm.word.Pointer arrayB, int lengthB) {
        return ArrayCompareToNode.compareTo(arrayA, lengthA, arrayB, lengthB, Stride.S2, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int charArrayCompareToCharArrayRTC(org.graalvm.word.Pointer arrayA, int lengthA, org.graalvm.word.Pointer arrayB, int lengthB) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCompareToNode.class));
        try {
            return ArrayCompareToNode.compareTo(arrayA, lengthA, arrayB, lengthB, Stride.S2, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS1S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS1S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS1S2(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS1S2RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS1S4(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS1S4RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S1, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS2S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS2S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS2S2(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS2S2RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS2S4(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS2S4RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S2, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS4S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS4S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS4S2(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS4S2RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS4S4(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToS4S4RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, Stride.S4, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToDynamicStrides(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length, int dynamicStrides) {
        return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, dynamicStrides);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int arrayRegionCompareToDynamicStridesRTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, int length, int dynamicStrides) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        try {
            return ArrayRegionCompareToNode.compare(arrayA, offsetA, arrayB, offsetB, length, dynamicStrides, Stubs.getRuntimeCheckedCPUFeatures(ArrayRegionCompareToNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS1S1(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S1, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS1S1RTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S1, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS1S2(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S1, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS1S2RTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S1, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS1S4(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S1, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS1S4RTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S1, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS2S1(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S2, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS2S1RTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S2, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS2S2(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S2, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS2S2RTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S2, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS2S4(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S2, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS2S4RTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S2, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS4S1(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S4, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS4S1RTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S4, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS4S2(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S4, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS4S2RTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S4, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS4S4(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S4, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsS4S4RTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, Stride.S4, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsDynamicStrides(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length, int stride) {
        ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, stride);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void arrayCopyWithConversionsDynamicStridesRTC(java.lang.Object arraySrc, long offsetSrc, java.lang.Object arrayDst, long offsetDst, int length, int stride) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        try {
            ArrayCopyWithConversionsNode.arrayCopy(arraySrc, offsetSrc, arrayDst, offsetDst, length, stride, Stubs.getRuntimeCheckedCPUFeatures(ArrayCopyWithConversionsNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int vectorizedMismatch(org.graalvm.word.Pointer arrayA, org.graalvm.word.Pointer arrayB, int length, int stride) {
        return VectorizedMismatchNode.vectorizedMismatch(arrayA, arrayB, length, stride);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int vectorizedMismatchRTC(org.graalvm.word.Pointer arrayA, org.graalvm.word.Pointer arrayB, int length, int stride) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(VectorizedMismatchNode.class));
        try {
            return VectorizedMismatchNode.vectorizedMismatch(arrayA, arrayB, length, stride, Stubs.getRuntimeCheckedCPUFeatures(VectorizedMismatchNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS1S2S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S1, Stride.S2, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS1S2S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S1, Stride.S2, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS2S2S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S2, Stride.S2, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS2S2S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S2, Stride.S2, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS1S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S1, Stride.S1, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS1S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S1, Stride.S1, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS1S2(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S1, Stride.S2, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS1S2RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S1, Stride.S2, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS1S4(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S1, Stride.S4, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS1S4RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S1, Stride.S4, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS2S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S2, Stride.S1, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS2S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S2, Stride.S1, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS2S2(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S2, Stride.S2, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS2S2RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S2, Stride.S2, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS2S4(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S2, Stride.S4, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS2S4RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S2, Stride.S4, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS4S1(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S4, Stride.S1, Stride.S1);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS4S1RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S4, Stride.S1, Stride.S1, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS4S2(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S4, Stride.S2, Stride.S2);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS4S2RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S4, Stride.S2, Stride.S2, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS4S4(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S4, Stride.S4, Stride.S4);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskS4S4RTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, Stride.S4, Stride.S4, Stride.S4, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskDynamicStrides(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length, int stride) {
        return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, stride);
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static boolean arrayRegionEqualsWithMaskDynamicStridesRTC(java.lang.Object arrayA, long offsetA, java.lang.Object arrayB, long offsetB, org.graalvm.word.Pointer mask, int length, int stride) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        try {
            return AMD64ArrayRegionEqualsWithMaskNode.regionEquals(arrayA, offsetA, arrayB, offsetB, mask, length, stride, Stubs.getRuntimeCheckedCPUFeatures(AMD64ArrayRegionEqualsWithMaskNode.class));
        } finally {
            region.leave();
        }
    }

    @Fold
    public static EnumSet<?> AMD64CalcStringAttributesNode_getMinimumFeatures() {
        Architecture arch = ImageSingletons.lookup(SubstrateTargetDescription.class).arch;
        if (arch instanceof jdk.vm.ci.amd64.AMD64) {
            return AMD64CalcStringAttributesNode.minFeaturesAMD64();
        }
        if (arch instanceof jdk.vm.ci.aarch64.AArch64) {
            return AMD64CalcStringAttributesNode.minFeaturesAARCH64();
        }
        throw GraalError.shouldNotReachHere();
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int calcStringAttributesLatin1(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(AMD64CalcStringAttributesNode_getMinimumFeatures());
        try {
            return AMD64CalcStringAttributesNode.intReturnValue(array, offset, length, Op.LATIN1, false, AMD64CalcStringAttributesNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int calcStringAttributesLatin1RTC(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        try {
            return AMD64CalcStringAttributesNode.intReturnValue(array, offset, length, Op.LATIN1, false, Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int calcStringAttributesBMP(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(AMD64CalcStringAttributesNode_getMinimumFeatures());
        try {
            return AMD64CalcStringAttributesNode.intReturnValue(array, offset, length, Op.BMP, false, AMD64CalcStringAttributesNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int calcStringAttributesBMPRTC(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        try {
            return AMD64CalcStringAttributesNode.intReturnValue(array, offset, length, Op.BMP, false, Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int calcStringAttributesUTF32(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(AMD64CalcStringAttributesNode_getMinimumFeatures());
        try {
            return AMD64CalcStringAttributesNode.intReturnValue(array, offset, length, Op.UTF_32, false, AMD64CalcStringAttributesNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int calcStringAttributesUTF32RTC(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        try {
            return AMD64CalcStringAttributesNode.intReturnValue(array, offset, length, Op.UTF_32, false, Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static long calcStringAttributesUTF8Valid(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(AMD64CalcStringAttributesNode_getMinimumFeatures());
        try {
            return AMD64CalcStringAttributesNode.longReturnValue(array, offset, length, Op.UTF_8, true, AMD64CalcStringAttributesNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static long calcStringAttributesUTF8ValidRTC(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        try {
            return AMD64CalcStringAttributesNode.longReturnValue(array, offset, length, Op.UTF_8, true, Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static long calcStringAttributesUTF8Unknown(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(AMD64CalcStringAttributesNode_getMinimumFeatures());
        try {
            return AMD64CalcStringAttributesNode.longReturnValue(array, offset, length, Op.UTF_8, false, AMD64CalcStringAttributesNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static long calcStringAttributesUTF8UnknownRTC(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        try {
            return AMD64CalcStringAttributesNode.longReturnValue(array, offset, length, Op.UTF_8, false, Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static long calcStringAttributesUTF16Valid(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(AMD64CalcStringAttributesNode_getMinimumFeatures());
        try {
            return AMD64CalcStringAttributesNode.longReturnValue(array, offset, length, Op.UTF_16, true, AMD64CalcStringAttributesNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static long calcStringAttributesUTF16ValidRTC(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        try {
            return AMD64CalcStringAttributesNode.longReturnValue(array, offset, length, Op.UTF_16, true, Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static long calcStringAttributesUTF16Unknown(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(AMD64CalcStringAttributesNode_getMinimumFeatures());
        try {
            return AMD64CalcStringAttributesNode.longReturnValue(array, offset, length, Op.UTF_16, false, AMD64CalcStringAttributesNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static long calcStringAttributesUTF16UnknownRTC(java.lang.Object array, long offset, int length) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        try {
            return AMD64CalcStringAttributesNode.longReturnValue(array, offset, length, Op.UTF_16, false, Stubs.getRuntimeCheckedCPUFeatures(AMD64CalcStringAttributesNode.class));
        } finally {
            region.leave();
        }
    }

    @Fold
    public static EnumSet<?> AESNode_getMinimumFeatures() {
        Architecture arch = ImageSingletons.lookup(SubstrateTargetDescription.class).arch;
        if (arch instanceof jdk.vm.ci.amd64.AMD64) {
            return AESNode.minFeaturesAMD64();
        }
        if (arch instanceof jdk.vm.ci.aarch64.AArch64) {
            return AESNode.minFeaturesAARCH64();
        }
        throw GraalError.shouldNotReachHere();
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void aesEncrypt(org.graalvm.word.Pointer from, org.graalvm.word.Pointer to, org.graalvm.word.Pointer key) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(AESNode_getMinimumFeatures());
        try {
            AESNode.apply(from, to, key, CryptMode.ENCRYPT, AESNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void aesEncryptRTC(org.graalvm.word.Pointer from, org.graalvm.word.Pointer to, org.graalvm.word.Pointer key) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AESNode.class));
        try {
            AESNode.apply(from, to, key, CryptMode.ENCRYPT, Stubs.getRuntimeCheckedCPUFeatures(AESNode.class));
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void aesDecrypt(org.graalvm.word.Pointer from, org.graalvm.word.Pointer to, org.graalvm.word.Pointer key) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(AESNode_getMinimumFeatures());
        try {
            AESNode.apply(from, to, key, CryptMode.DECRYPT, AESNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void aesDecryptRTC(org.graalvm.word.Pointer from, org.graalvm.word.Pointer to, org.graalvm.word.Pointer key) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(AESNode.class));
        try {
            AESNode.apply(from, to, key, CryptMode.DECRYPT, Stubs.getRuntimeCheckedCPUFeatures(AESNode.class));
        } finally {
            region.leave();
        }
    }

    @Fold
    public static EnumSet<?> CounterModeAESNode_getMinimumFeatures() {
        Architecture arch = ImageSingletons.lookup(SubstrateTargetDescription.class).arch;
        if (arch instanceof jdk.vm.ci.amd64.AMD64) {
            return CounterModeAESNode.minFeaturesAMD64();
        }
        if (arch instanceof jdk.vm.ci.aarch64.AArch64) {
            return CounterModeAESNode.minFeaturesAARCH64();
        }
        throw GraalError.shouldNotReachHere();
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int ctrAESCrypt(org.graalvm.word.Pointer inAddr, org.graalvm.word.Pointer outAddr, org.graalvm.word.Pointer kAddr, org.graalvm.word.Pointer counterAddr, int len, org.graalvm.word.Pointer encryptedCounterAddr, org.graalvm.word.Pointer usedPtr) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(CounterModeAESNode_getMinimumFeatures());
        try {
            return CounterModeAESNode.apply(inAddr, outAddr, kAddr, counterAddr, len, encryptedCounterAddr, usedPtr, CounterModeAESNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static int ctrAESCryptRTC(org.graalvm.word.Pointer inAddr, org.graalvm.word.Pointer outAddr, org.graalvm.word.Pointer kAddr, org.graalvm.word.Pointer counterAddr, int len, org.graalvm.word.Pointer encryptedCounterAddr, org.graalvm.word.Pointer usedPtr) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(CounterModeAESNode.class));
        try {
            return CounterModeAESNode.apply(inAddr, outAddr, kAddr, counterAddr, len, encryptedCounterAddr, usedPtr, Stubs.getRuntimeCheckedCPUFeatures(CounterModeAESNode.class));
        } finally {
            region.leave();
        }
    }

    @Fold
    public static EnumSet<?> GHASHProcessBlocksNode_getMinimumFeatures() {
        Architecture arch = ImageSingletons.lookup(SubstrateTargetDescription.class).arch;
        if (arch instanceof jdk.vm.ci.amd64.AMD64) {
            return GHASHProcessBlocksNode.minFeaturesAMD64();
        }
        if (arch instanceof jdk.vm.ci.aarch64.AArch64) {
            return GHASHProcessBlocksNode.minFeaturesAARCH64();
        }
        throw GraalError.shouldNotReachHere();
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void ghashProcessBlocks(org.graalvm.word.Pointer state, org.graalvm.word.Pointer hashSubkey, org.graalvm.word.Pointer data, int blocks) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(GHASHProcessBlocksNode_getMinimumFeatures());
        try {
            GHASHProcessBlocksNode.apply(state, hashSubkey, data, blocks, GHASHProcessBlocksNode_getMinimumFeatures());
        } finally {
            region.leave();
        }
    }

    @Uninterruptible(reason = "Must not do a safepoint check.")
    @SubstrateForeignCallTarget(stubCallingConvention = false, fullyUninterruptible = true)
    private static void ghashProcessBlocksRTC(org.graalvm.word.Pointer state, org.graalvm.word.Pointer hashSubkey, org.graalvm.word.Pointer data, int blocks) {
        RuntimeCPUFeatureRegion region = RuntimeCPUFeatureRegion.enterSet(Stubs.getRuntimeCheckedCPUFeatures(GHASHProcessBlocksNode.class));
        try {
            GHASHProcessBlocksNode.apply(state, hashSubkey, data, blocks, Stubs.getRuntimeCheckedCPUFeatures(GHASHProcessBlocksNode.class));
        } finally {
            region.leave();
        }
    }

}
