/*
 * MIPS notes 20080513: currently we use the same layout as the
 * ARM platform (and Frame_c.hpp), in the hope that AOT on MIPS can
 * be supported in a similar way.
 *
 * Of course we need to implement JIT first before AOT ... :-)
 */

inline int   JavaFrame__arg_offset_from_sp(int index) {
  return arm_JavaFrame__arg_offset_from_sp(index);
}

inline int   JavaFrame__stack_bottom_pointer_offset() {
  return arm_JavaFrame__stack_bottom_pointer_offset();
}
inline int   JavaFrame__bcp_store_offset() {
  return arm_JavaFrame__bcp_store_offset();
}
inline int   JavaFrame__locals_pointer_offset() {
  return arm_JavaFrame__locals_pointer_offset();
}
inline int   JavaFrame__cpool_offset() {
  return arm_JavaFrame__cpool_offset();
}
inline int   JavaFrame__method_offset() {
  return arm_JavaFrame__method_offset();
}
inline int   JavaFrame__caller_fp_offset() {
  return arm_JavaFrame__caller_fp_offset();
} 
inline int   JavaFrame__return_address_offset() {
  return arm_JavaFrame__return_address_offset();
}

// These two slots are not used in this architecture.
inline int   JavaFrame__stored_int_value1_offset() {
  return arm_JavaFrame__stored_int_value1_offset();
}
inline int   JavaFrame__stored_int_value2_offset() {
  return arm_JavaFrame__stored_int_value2_offset();
}

inline int   JavaFrame__frame_desc_size() {
  return arm_JavaFrame__frame_desc_size();
}  

inline int   JavaFrame__end_of_locals_offset() {
  return arm_JavaFrame__end_of_locals_offset();
}

inline int   JavaFrame__empty_stack_offset() {
  return arm_JavaFrame__empty_stack_offset();
}

inline int   EntryFrame__pending_activation_offset() {
  return arm_EntryFrame__pending_activation_offset();
}
inline int   EntryFrame__pending_exception_offset() {
  return arm_EntryFrame__pending_exception_offset();
}
inline int   EntryFrame__stored_obj_value_offset() {
  return arm_EntryFrame__stored_obj_value_offset();
}
inline int   EntryFrame__stored_int_value2_offset() {
  return arm_EntryFrame__stored_int_value2_offset();
}
inline int   EntryFrame__stored_int_value1_offset() {
  return arm_EntryFrame__stored_int_value1_offset();
}
inline int   EntryFrame__stored_last_sp_offset() {
  return arm_EntryFrame__stored_last_sp_offset();
}
inline int   EntryFrame__stored_last_fp_offset() {
  return arm_EntryFrame__stored_last_fp_offset();
}
inline int   EntryFrame__real_return_address_offset() {
  return arm_EntryFrame__real_return_address_offset();
}
inline int   EntryFrame__fake_return_address_offset() {
  return arm_EntryFrame__fake_return_address_offset();
}

inline int   EntryFrame__frame_desc_size() {
  return arm_EntryFrame__frame_desc_size();
}

// When an EntryFrame is empty, the sp points at the word above
inline int   EntryFrame__empty_stack_offset() {
  return arm_EntryFrame__empty_stack_offset();
}

#if ENABLE_EMBEDDED_CALLINFO
// number of bytes between the return address and the start of the callinfo
inline int   JavaFrame__callinfo_offset_from_return_address() {
  return arm_JavaFrame__callinfo_offset_from_return_address();
}
#endif

// number of bytes between a stack tag value and its tag
inline int   StackValue__stack_tag_offset() {
  return arm_StackValue__stack_tag_offset();
}
