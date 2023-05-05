package org.evomaster.client.java.instrumentation;

import org.objectweb.asm.Opcodes;

public class Constants {

    public static final String PROP_SKIP_CLASSES = "em.skipClasses";

    public static final int ASM = Opcodes.ASM7;

    public static final String CLASS_INIT_METHOD = "<clinit>";

    public static final String INIT_METHOD = "<init>";


    public static boolean isMethodSyntheticOrBridge(int methodAccess){
        return (methodAccess & Opcodes.ACC_SYNTHETIC) > 0
                || (methodAccess & Opcodes.ACC_BRIDGE) > 0 ;
    }
}
