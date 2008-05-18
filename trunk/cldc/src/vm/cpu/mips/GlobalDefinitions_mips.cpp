// FIXME: these are temporary place holders. They will be moved into
// Interpreter_mips.S pretty soon.

#include "incls/_precompiled.incl"
#include "incls/_GlobalDefinitions_mips.cpp.incl"

/*
 * Note: in cross-generator mode, these symbols are generated using the
 * ARM source assembler. Search for GP_TABLE_OBJ inside build/share/jvm.make.
 */

/// The file defines the "quick access" global variables for C interpreter. 
extern "C" {

unsigned char * _kni_parameter_base;

#if ENABLE_INTERPRETATION_LOG
OopDesc*        _interpretation_log[INTERP_LOG_SIZE];
int             _interpretation_log_idx;
#endif

OopDesc**       _old_generation_end;

address         _current_stack_limit       = NULL;
address         _compiler_stack_limit      = NULL;
int             _rt_timer_ticks            = 0;
address         _primordial_sp             = NULL;
OopDesc*        _interned_string_near_addr = NULL;
OopDesc**       _persistent_handles_addr   = NULL;
int		_jvm_in_quick_native_method= 0;
char*		_jvm_quick_native_exception= NULL;

#if ENABLE_ISOLATES
OopDesc*        _current_task;

// where the address of the being initialized marker is kept for the
// interpreter
OopDesc*         _task_class_init_marker = NULL;
OopDesc*         _task_array_class_init_marker = NULL;
#endif //ENABLE_ISOLATES

#if ENABLE_JAVA_DEBUGGER
int             _debugger_active = 0;
#endif
}

