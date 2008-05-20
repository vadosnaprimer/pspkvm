# This is a handy script for rebuilding only the interpreter loop.
# It avoid rebuilding unnecessary parts of the target VM.


BUILD=$1
if test "$BUILD" = ""; then
    BUILD=product;
fi

make loopgen
touch ${JVMBuildSpace}/linux_mips_qemu/target/${BUILD}/*.o
rm -f ${JVMBuildSpace}/linux_mips_qemu/target/${BUILD}/Interpreter_mips.s
rm -f ${JVMBuildSpace}/linux_mips_qemu/target/${BUILD}/Interpreter_mips.o
make _${BUILD}

