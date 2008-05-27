# This is a handy script for rebuilding only the interpreter loop.
# It avoid rebuilding unnecessary parts of the target VM.


BUILD=$1
if test "$BUILD" = ""; then
    BUILD=product;
fi

if test "$NUM_JOBS" = ""; then
    NUM_JOBS=2
fi

# At this point, there's no point building product VM with optimization.
export PRODUCT_DEBUG=true

make parallel_loopgen NUM_JOBS=$NUM_JOBS || exit 1
touch ${JVMBuildSpace}/linux_mips_qemu/target/${BUILD}/*.o
rm -f ${JVMBuildSpace}/linux_mips_qemu/target/${BUILD}/Interpreter_mips.s
rm -f ${JVMBuildSpace}/linux_mips_qemu/target/${BUILD}/Interpreter_mips.o
time make _${BUILD}

