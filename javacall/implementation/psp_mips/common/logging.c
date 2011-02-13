/*
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

#include "javacall_logging.h"
#include <stdio.h>
#include <string.h>
#include <stdarg.h>

#ifdef __cplusplus
extern "C" {
#endif

static int log_channel_consle = 0;
static int log_channel_startup = 0;
static int log_channel_stdout = 0;
static int log_channel_file = 0;
    
/**
 * Prints out a string to a system specific output strream
 *
 * @param s a NULL terminated character buffer to be printed
*/
void javacall_print(const char *s) {
    
    if (log_channel_consle) pspDebugScreenPrintf(s);
    if (log_channel_startup) pspkvm_screen_log(s);
    if (log_channel_stdout) printf(s);
    //if (log_channel_file) log_2_file(s);

}

void javacall_printf (const char* format, ...) {
#define log_buf_size 65536
  static char logs[log_buf_size]={0};

  va_list ap;
  va_start(ap, format);
  vsnprintf(logs, log_buf_size, format, ap);
  va_end(ap);

  javacall_print(logs);
}

void javacall_logging_channel(int channel) {
  if (channel & JAVACALL_LOG_CHANNEL_CONSOLE) log_channel_consle = 1;
  else log_channel_consle = 0;
  if (channel & JAVACALL_LOG_CHANNEL_STARTUP) log_channel_startup = 1;
  else log_channel_startup = 0;
  if (channel & JAVACALL_LOG_CHANNEL_STDOUT) log_channel_stdout = 1;
  else log_channel_stdout = 0;
  if (channel & JAVACALL_LOG_CHANNEL_FILE) log_channel_file = 1;
  else log_channel_file = 0;
}

#ifdef __cplusplus
}
#endif
