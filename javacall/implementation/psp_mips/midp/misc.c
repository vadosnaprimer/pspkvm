#include <javacall_misc.h>

#include "psppower.h"

void javacall_misc_set_cpu_speed(int speed) {
    javacall_printf("javacall_misc_set_cpu_speed(%d)\n", speed);
    scePowerSetClockFrequency(speed, speed, speed/2);
}


