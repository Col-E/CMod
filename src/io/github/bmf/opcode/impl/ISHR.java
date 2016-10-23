package io.github.bmf.opcode.impl;

import io.github.bmf.opcode.Opcode;
import io.github.bmf.opcode.OpcodeType;

public class ISHR extends Opcode {
    public ISHR() {
        super(OpcodeType.MATH, Opcode.ISHR, 1);
    }
}
