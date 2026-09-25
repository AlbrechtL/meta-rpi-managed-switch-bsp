# LLVM 22's optimized single precision assembly for 32-bit Arm
# (arm/divsf3.S, arm/mulsf3.S) uses mls, which needs ARMv6T2. The Pi Zero's
# ARM1176 is plain ARMv6, and the build fails:
#   divsf3.S:206:3: error: invalid instruction ... requires: armv6t2
# Without it compiler-rt uses its C implementations.
EXTRA_OECMAKE:append:armv6 = " -DCOMPILER_RT_ARM_OPTIMIZED_FP=OFF"
