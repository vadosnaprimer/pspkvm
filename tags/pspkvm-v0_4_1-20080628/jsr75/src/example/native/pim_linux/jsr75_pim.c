/*
 *   
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

#include <stdio.h>
#include <stdlib.h>
#include <memory.h>

#include <jsr75_pim.h>

#include <pcsl_file.h>
#include <pcsl_directory.h>

#define PIM_DB_PATH_LEN         128
#define DATA_BUFFER_LENGTH      1024

typedef struct {
    jsr75_pim_type           type;
    pcsl_string*             path;
    void*                    handle;
    struct _pim_opened_item *item_list;
} pim_opened_list;

typedef struct _pim_opened_item {
    char* name;
    struct _pim_opened_item *next;
    pim_opened_list *list;
} pim_opened_item;

typedef struct {
    jsr75_pim_type   type;
    pcsl_string*     path;
    int              num_fields;
    jsr75_pim_field* fields;
    pim_opened_list  default_list;
} pim_list_dscr;


PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(pimDBDir)
    {'/', 'a', 'p', 'p', 'd', 'b', '/', 'p', 'i', 'm', 'd', 'b', '/', 'p', 'i', 'm', '/', '\0'}
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(pimDBDir);
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(pimContactDir)
    {'c', 'o', 'n', 't', 'a', 'c', 't', 's', '\0'}
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(pimContactDir);
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(pimEventsDir)
    {'e', 'v', 'e', 'n', 't', 's', '\0'}
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(pimEventsDir);
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(pimTodoDir)
    {'t', 'o', 'd', 'o', '\0'}
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(pimTodoDir);
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(fileSeparator)
    {'/', '\0'}
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(fileSeparator);
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(currentDir)
    {'.', '\0'}
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(currentDir);
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(topDir)
    {'.', '.', '\0'}
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(topDir);

static char readMode[]       = "r";
static char writeMode[]      = "w";
static char appendMode[]     = "a";
static char contactExt[]     = ".vcf";
static char calendarExt[]    = ".vcs";
static char categoriesFile[] = "categories.txt";
static jchar categoryDelim   = ',';

static jsr75_pim_field pimContactFields[] = {
    // NAME field
    {
        // id
        jsr75_pim_contact_field_name,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string_array,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', 0},
        // attributes
        0,
        // arrayElements
        {
            {
                jsr75_pim_contact_field_array_element_name_family,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '0', 0}
            },
            {
                jsr75_pim_contact_field_array_element_name_given,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '1', 0}
            },
            {
                jsr75_pim_contact_field_array_element_name_other,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '2', 0}
            },
            {
                jsr75_pim_contact_field_array_element_name_prefix,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '3', 0}
            },
            {
                jsr75_pim_contact_field_array_element_name_suffix,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'a', 'm', 'e', '.', '4', 0}
            },
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // ADDR field
    {
        // id
        jsr75_pim_contact_field_addr,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string_array,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', 0},
        // attributes
        0x2a8,
        // arrayElements
        {
            {
                jsr75_pim_contact_field_array_element_addr_pobox,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '0', 0}
            },
            {
                jsr75_pim_contact_field_array_element_addr_extra,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '1', 0}
            },
            {
                jsr75_pim_contact_field_array_element_addr_street,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '2', 0}
            },
            {
                jsr75_pim_contact_field_array_element_addr_locality,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '3', 0}
            },
            {
                jsr75_pim_contact_field_array_element_addr_region,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '4', 0}
            },
            {
                jsr75_pim_contact_field_array_element_addr_postalcode,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '5', 0}
            },
            {
                jsr75_pim_contact_field_array_element_addr_country,
                {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'A', 'd', 'd', 'r', '.', '6', 0}
            }
        }
    },
    // EMAIL field
    {
        // id
        jsr75_pim_contact_field_email,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'E', 'm', 'a', 'i', 'l', 0},
        // attributes
        0x2a8,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // FORMATTED_NAME field
    {
        // id
        jsr75_pim_contact_field_formatted_name,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'F', 'o', 'r', 'm', 'a', 't', 't', 'e', 'd', 'N', 'a', 'm', 'e', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // FORMATTED_ADDR field
    {
        // id
        jsr75_pim_contact_field_formatted_addr,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'F', 'o', 'r', 'm', 'a', 't', 't', 'e', 'd', 'A', 'd', 'd', 'r', 0},
        // attributes
        0x2a8,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // NICKNAME field
    {
        // id
        jsr75_pim_contact_field_nickname,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'i', 'c', 'k', 'n', 'a', 'm', 'e', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // NOTE field
    {
        // id
        jsr75_pim_contact_field_note,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'N', 'o', 't', 'e', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // ORG field
    {
        // id
        jsr75_pim_contact_field_org,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'O', 'r', 'g', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // TEL field
    {
        // id
        jsr75_pim_contact_field_tel,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'T', 'e', 'l', 0},
        // attributes
        0x3ff,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // TITLE field
    {
        // id
        jsr75_pim_contact_field_title,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'T', 'i', 't', 'l', 'e', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // UID field
    {
        // id
        jsr75_pim_contact_field_uid,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'U', 'I', 'D', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // BIRTHDAY field
    {
        // id
        jsr75_pim_contact_field_birthday,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_date,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'B', 'i', 'r', 't', 'h', 'd', 'a', 'y', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // REVISION field
    {
        // id
        jsr75_pim_contact_field_revision,
        // maxValues
        1,
        // type
        jsr75_pim_field_type_date,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'R', 'e', 'v', 'i', 's', 'i', 'o', 'n', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // PHOTO field
    {
        // id
        jsr75_pim_contact_field_photo,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_binary,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'P', 'h', 'o', 't', 'o', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // CLASS field
    {
        // id
        jsr75_pim_contact_field_class,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_int,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'C', 'l', 'a', 's', 's', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // PUBLIC_KEY field
    {
        // id
        jsr75_pim_contact_field_public_key,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_binary,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'P', 'u', 'b', 'l', 'i', 'c', 'K', 'e', 'y', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // PUBLIC_KEY_STRING field
    {
        // id
        jsr75_pim_contact_field_public_key_string,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'P', 'u', 'b', 'l', 'i', 'c', 'K', 'e', 'y', 'S', 't', 'r', 'i', 'n', 'g', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // URL field
    {
        // id
        jsr75_pim_contact_field_url,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'U', 'R', 'L', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // PHOTO_URL field
    {
        // id
        jsr75_pim_contact_field_photo_url,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', 'P', 'h', 'o', 't', 'o', 'U', 'R', 'L', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    }
};

static jsr75_pim_field pimEventFields[] = {
    // LOCATION field
    {
        // id
        jsr75_pim_event_field_location,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'L', 'o', 'c', 'a', 't', 'i', 'o', 'n', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // NOTE field
    {
        // id
        jsr75_pim_event_field_note,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'N', 'o', 't', 'e', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // SUMMARY field
    {
        // id
        jsr75_pim_event_field_summary,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'S', 'u', 'm', 'm', 'a', 'r', 'y', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // UID field
    {
        // id
        jsr75_pim_event_field_uid,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'U', 'I', 'D', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // END field
    {
        // id
        jsr75_pim_event_field_end,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_date,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'E', 'n', 'd', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // REVISION field
    {
        // id
        jsr75_pim_event_field_revision,
        // maxValues
        1,
        // type
        jsr75_pim_field_type_date,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'R', 'e', 'v', 'i', 's', 'i', 'o', 'n', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // START field
    {
        // id
        jsr75_pim_event_field_start,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_date,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'S', 't', 'a', 'r', 't', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // ALARM field
    {
        // id
        jsr75_pim_event_field_alarm,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_int,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'A', 'l', 'a', 'r', 'm', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // CLASS field
    {
        // id
        jsr75_pim_event_field_class,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_int,
        // label
        {'P', 'I', 'M', '.', 'E', 'v', 'e', 'n', 't', 'L', 'i', 's', 't', '.', 'C', 'l', 'a', 's', 's', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    }
};

static jsr75_pim_field pimTodoFields[] = {
    // NOTE field
    {
        // id
        jsr75_pim_todo_field_note,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'N', 'o', 't', 'e', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // SUMMARY field
    {
        // id
        jsr75_pim_todo_field_summary,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'S', 'u', 'm', 'm', 'a', 'r', 'y', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // UID field
    {
        // id
        jsr75_pim_todo_field_uid,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_string,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'U', 'I', 'D', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // CLASS field
    {
        // id
        jsr75_pim_todo_field_class,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_int,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'C', 'l', 'a', 's', 's', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // PRIORITY field
    {
        // id
        jsr75_pim_todo_field_priority,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_int,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'P', 'r', 'i', 'o', 'r', 'i', 't', 'y', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // COMPLETION_DATE field
    {
        // id
        jsr75_pim_todo_field_completion_date,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_date,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'C', 'o', 'm', 'p', 'l', 'e', 't', 'i', 'o', 'n', 'D', 'a', 't', 'e', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // DUE field
    {
        // id
        jsr75_pim_todo_field_due,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_date,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'D', 'u', 'e', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // REVISION field
    {
        // id
        jsr75_pim_todo_field_revision,
        // maxValues
        1,
        // type
        jsr75_pim_field_type_date,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'R', 'e', 'v', 'i', 's', 'i', 'o', 'n', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    },
    // COMPLETED field
    {
        // id
        jsr75_pim_todo_field_completed,
        // maxValues
        -1,
        // type
        jsr75_pim_field_type_boolean,
        // label
        {'P', 'I', 'M', '.', 'T', 'o', 'D', 'o', 'L', 'i', 's', 't', '.', 'C', 'o', 'm', 'p', 'l', 'e', 't', 'e', 'd', 0},
        // attributes
        0,
        // arrayElements
        {
            {JSR75_PIM_INVALID_ID, {0}}
        }
    }
};

static jsr75_pim_field_attribute pimAttributes[] = {
    {
        jsr75_pim_contact_field_attr_asst,
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '0', 0}
    },
    {
        jsr75_pim_contact_field_attr_auto,
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '1', 0}
    },
    {
        jsr75_pim_contact_field_attr_fax,
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '2', 0}
    },
    {
        jsr75_pim_contact_field_attr_home,  
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '3', 0}
    },
    {
        jsr75_pim_contact_field_attr_mobile,
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '4', 0}
    },
    {
        jsr75_pim_contact_field_attr_other, 
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '5', 0}
    },
    {
        jsr75_pim_contact_field_attr_pager,
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '6', 0}
    },
    {
        jsr75_pim_contact_field_attr_preferred,
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '7', 0}
    },
    {
        jsr75_pim_contact_field_attr_sms,
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '8', 0}
    },
    {
        jsr75_pim_contact_field_attr_work,
        {'P', 'I', 'M', '.', 'A', 't', 't', 'r', 'i', 'b', 'u', 't', 'e', 's', '.', 'C', 'o', 'n', 't', 'a', 'c', 't', 'L', 'i', 's', 't', '.', '9', 0}
    }
};


static char pimStringBuffer[PIM_DB_PATH_LEN];
static unsigned char pimDataBuffer[DATA_BUFFER_LENGTH];

static pim_list_dscr list_data[] = {
    {jsr75_pim_type_contact, (pcsl_string *)&pimContactDir, sizeof(pimContactFields) / sizeof(jsr75_pim_field), pimContactFields,
        {
            jsr75_pim_type_contact,
            NULL,
            NULL,
            NULL
        }
    },
    {jsr75_pim_type_event, (pcsl_string *)&pimEventsDir, sizeof(pimEventFields) / sizeof(jsr75_pim_field), pimEventFields,
        {
            jsr75_pim_type_event,
            NULL,
            NULL,
            NULL
        }
    },
    {jsr75_pim_type_todo, (pcsl_string *)&pimTodoDir, sizeof(pimTodoFields) / sizeof(jsr75_pim_field), pimTodoFields,
        {
            jsr75_pim_type_todo,
            NULL,
            NULL,
            NULL
        }
    }
};

extern char* midpFixMidpHome(char *cmd);


static jsize jcslen(jchar* s) {
    jsize i = 0;

    while (s[i]) i++;
    return i;
}

static jchar* jcschr(jchar* jcs, jchar jc) {
    while (*jcs) {
        if (*jcs++ == jc) {
            return jcs - 1;
        }
    }
    return NULL;
}

static jchar* jcsstr(jchar* haystack, jchar* needle) {
    int count = 0;

    if (!*needle) {
        return haystack;
    }

    while (*haystack) {
        if (*haystack++ == needle[count]) {
            if (!needle[++count]) {
                return haystack - count;
            }
        } else {
            count = 0;
        }
    }
    return NULL;
}

static jchar* jcscpy(jchar* dest, jchar* src) {
    jchar* dst = dest;

    do {
        *dst++ = *src;
    } while (*src++);
    return dest;
}

static jsr75_handle get_default_list(jsr75_pim_type type) {
    pim_list_dscr *list, *last;
    
    for (list = list_data, last = &list_data[sizeof(list_data) / sizeof(pim_list_dscr)];
         list < last;
         list++) {
        if (list->type == type) {
            break;
        }    
    }
    if (list == last) {
        return NULL;
    }
    return &list->default_list;
}

/* Returns file system path to PIM list of the given type */
static pcsl_string* get_list_path(jsr75_pim_type type, jchar* name) {
    pcsl_string* path1 = malloc(sizeof(pcsl_string));
    pcsl_string path2 = PCSL_STRING_NULL;
    pcsl_string tmp   = PCSL_STRING_NULL;
    unsigned list_index;
    pcsl_string_status res;
    jbyte* midp_root = (jbyte *)midpFixMidpHome("");

    if (NULL == midp_root) {
        return NULL;
    }

    if (PCSL_STRING_OK != (res = pcsl_string_convert_from_utf8(midp_root, strlen(midp_root), path1))) {
        return NULL;
    }
    for (list_index = 0;
         list_index < sizeof(list_data) / sizeof(pim_list_dscr);
         list_index++) {
         if (list_data[list_index].type == type) {
            break;
         }
    }
    if (list_index == sizeof(list_data) / sizeof(pim_list_dscr)) {
        pcsl_string_free(path1);
        return NULL;
    }

    res = pcsl_string_cat(path1, &pimDBDir, &path2);
    pcsl_string_free(path1);
    if (PCSL_STRING_OK != res) {
        return NULL;
    }

    res = pcsl_string_cat(&path2, list_data[list_index].path, path1);
    pcsl_string_free(&path2);
    if (PCSL_STRING_OK != res) {
        return NULL;
    }

    if (NULL != name) {
        res = pcsl_string_cat(path1, &fileSeparator, &path2);
        pcsl_string_free(path1);
        if (PCSL_STRING_OK != res) {
            return NULL;
        }

        if (PCSL_STRING_OK != pcsl_string_convert_from_utf16(name, jcslen(name), &tmp)) {
            pcsl_string_free(&path2);
            return NULL;
        }
        res = pcsl_string_cat(&path2, &tmp, path1);
        pcsl_string_free(&path2);
        pcsl_string_free(&tmp);
        if (PCSL_STRING_OK != res) {
            return NULL;
        }
    }

    return path1;
}

static char* get_item_path(pim_opened_list *list, char* item_name) {
    char *file_path = NULL;
    pcsl_string list_path = PCSL_STRING_NULL;
    jsize lp_len, fp_len;
    pcsl_string_status res;

    if (PCSL_STRING_OK != pcsl_string_cat(list->path, &fileSeparator, &list_path)) {
        return NULL;
    }
    lp_len = pcsl_string_utf8_length(&list_path);
    file_path = malloc(lp_len + strlen(item_name) + 1);
    if (file_path) {
        res = pcsl_string_convert_to_utf8(&list_path, file_path, lp_len + 1, &fp_len);
        pcsl_string_free(&list_path);
        if (PCSL_STRING_OK != res) {
            free(file_path);
            return NULL;
        }
        strcat(file_path, item_name);
    }
    return file_path;
}

static pim_opened_item* find_item(pim_opened_list *list, char* item_name) {
    pim_opened_item* item;
    
    for (item = list->item_list; item; item = item->next) {
        if (!strcmp(item->name, item_name)) {
            break;
        }
    }
    return item;
}

static void add_new_item(pim_opened_list *list, pim_opened_item *item) {
    pim_opened_item **current;

    for (current = &list->item_list;
         *current;
         current = &(*current)->next) {};
    *current = item;
    item->list = list;
}

static void remove_item(pim_opened_list *list, pim_opened_item *item) {
    pim_opened_item **current;

    for (current = &list->item_list;
         *current;
         current = &(*current)->next) {
         if (*current == item) {
            break;
         }
    }
    if (*current) {
        *current = item->next;
    }
    item->next = NULL;
    item->list = NULL;
}

static FILE* open_list_item(pim_opened_list *list, pim_opened_item *item, int read_only) {
    FILE  *item_stream;
    char **item_name = &item->name;
    char  *file_path;

    if (!*item_name) {
        /* New item, generate file name */
        int index = 0;
        char *ext = list->type == jsr75_pim_type_contact ? contactExt : calendarExt;

        do {
            index++;
            sprintf(pimStringBuffer, "%d%s", index, ext);
        } while (find_item(list, pimStringBuffer));
        *item_name = malloc(strlen(pimStringBuffer) + 1);
        if (!*item_name) {
            return NULL;
        }
        strcpy(*item_name, pimStringBuffer);
    }
    file_path = get_item_path(list, *item_name);
    if (!file_path) {
        return NULL;
    }

    item_stream = fopen(file_path, read_only ? readMode : writeMode);
    free(file_path);
    
    return item_stream;
}

static FILE* open_categories_file(pim_opened_list *list, char *mode) {
    char *cat_path;
    FILE* cat_stream;
    
    cat_path = get_item_path(list, categoriesFile);
    if (!cat_path) {
        return NULL;
    }
    cat_stream = fopen(cat_path, mode);
    free(cat_path);
    return cat_stream;
}

static jchar* get_list_categories(pim_opened_list *list) {
    jchar *categories;
    FILE* cat_stream;
    size_t read_count;
    
    cat_stream = open_categories_file(list, readMode);
    if (!cat_stream) {
        return NULL;
    }
    categories = (jchar *)pimDataBuffer;
    read_count = fread(categories, 1, DATA_BUFFER_LENGTH - 1, cat_stream) / sizeof(jchar);
    categories[read_count++] = 0;
    fclose(cat_stream);
    
    return categories;
}

/**
 * Return a JSR75_PIM_STRING_DELIMITER separated list that contains the names of PIM list
 * the matches the given listType ("Contact" Or "JohnContact(\n)
                                                 SuziContact")
 *
 * @param listType the pim list type the user wishes to obtain
 * @param pimList pointer to where to store the returned list
 *                (the list must be delimited by JSR75_PIM_STRING_DELIMITER,
 *                the default list name should appear in the first place)
 * @param pimListLen the length of the pim list buffer
 *
 * @retval JSR75_STATUSCODE_OK  on success
 * @retval JSR75_STATUSCODE_FAIL  when no list exists or when the buffer size is too small 
 */
JSR75_STATUSCODE 
jsr75_pim_get_lists(
  jsr75_pim_type listType,
  jchar /*OUT*/ *pimList,
  jsize pimListLen
  ) {
    pcsl_string *list_path = get_list_path(listType, NULL);
    pcsl_string fname = PCSL_STRING_NULL;
    pcsl_string tmp = PCSL_STRING_NULL;
    pcsl_string short_name = PCSL_STRING_NULL;
    void* list_dir_handle;
    jsize cur_name_len;
    pcsl_string_status res;
    JSR75_STATUSCODE result = JSR75_STATUSCODE_FAIL;

    if (!list_path) {
        return result;
    }

    if (PCSL_STRING_OK == pcsl_string_cat(list_path, &fileSeparator, &tmp)) {
        jsize prefix_len = pcsl_string_length(&tmp);
        list_dir_handle = pcsl_file_openfilelist(&tmp);
        if (NULL != list_dir_handle) {
            jchar *current = pimList;

            result = JSR75_STATUSCODE_OK;
            while (0 == pcsl_file_getnextentry(list_dir_handle, &tmp, &fname)) {
                if (1 == pcsl_file_is_directory(&fname)) {
                    if (PCSL_STRING_OK != pcsl_string_substring(&fname, prefix_len, pcsl_string_length(&fname), &short_name)) {
                        pcsl_string_free(&fname);
                        result = JSR75_STATUSCODE_FAIL;
                        break;
                    }
                    res = pcsl_string_convert_to_utf16(&short_name, current, pimListLen - 1, &cur_name_len);
                    pcsl_string_free(&short_name);
                    if (PCSL_STRING_OK != res) {
                        pcsl_string_free(&fname);
                        result = JSR75_STATUSCODE_FAIL;
                        break;
                    }
                    pimListLen -= cur_name_len + 1;
                    current += cur_name_len + 1;
                    *(current - 1) = JSR75_PIM_STRING_DELIMITER;
                }
                pcsl_string_free(&fname);
            }
            if (current != pimList) {
                *(current - 1) = 0;
            }
            pcsl_file_closefilelist(list_dir_handle);
        }
        pcsl_string_free(&tmp);
    }

    pcsl_string_free(list_path);
    free(list_path);
    return result;
}


/**
 * Open the requested pim list in the given mode.
 *
 * @param listType the pim list type to open
 * @param pimList the name of the list to open,
 *        if pimList is null the handle of default dummy
 *        list will be returned; this kind of list may
 *        be used only for getting default list structure
 * @param mode the open mode for the list
 * @param listHandle pointer to where to store the list handle
 *
 * @retval JSR75_STATUSCODE_OK  on success
 * @retval JSR75_STATUSCODE_INVALID_ARGUMENT  If an invalid mode is provided as a parameter or 
                                      if pimListType is not a valid PIM list type
 * @retval JSR75_STATUSCODE_FAIL  on other error
 */
JSR75_STATUSCODE 
jsr75_pim_list_open(
  jsr75_pim_type listType,
  jchar *pimList,
  jsr75_pim_open_mode mode,
  jsr75_handle /* OUT */ *listHandle
  ) {   
    pcsl_string *listPath;
    pcsl_string full_path = PCSL_STRING_NULL;
    pim_opened_list* list;
    void* dir_handle;

    *listHandle = NULL;
    if (mode != jsr75_pim_open_mode_read_only  &&
        mode != jsr75_pim_open_mode_write_only &&
        mode != jsr75_pim_open_mode_read_write) {
        return JSR75_STATUSCODE_INVALID_ARGUMENT;
    }
    if (pimList) {
        listPath = get_list_path(listType, pimList);
    }
    else {
        *listHandle = get_default_list(listType);
        return *listHandle ? JSR75_STATUSCODE_OK : JSR75_STATUSCODE_FAIL;
    }
    if (!listPath) {
        return JSR75_STATUSCODE_FAIL;
    }
    if (PCSL_STRING_OK != pcsl_string_cat(listPath, &fileSeparator, &full_path)) {
        pcsl_string_free(listPath);
        free(listPath);
        return JSR75_STATUSCODE_FAIL;
    }

    dir_handle = pcsl_file_openfilelist(&full_path);
    pcsl_string_free(&full_path);
    if (!dir_handle) {
        pcsl_string_free(listPath);
        free(listPath);
        return JSR75_STATUSCODE_FAIL;
    }
    list = malloc(sizeof(pim_opened_list));
    if (!list) {
        pcsl_file_closefilelist(dir_handle);
        pcsl_string_free(listPath);
        free(listPath);
        return JSR75_STATUSCODE_FAIL;
    }
    list->type       = listType;
    list->path       = listPath;
    list->handle     = dir_handle;
    list->item_list  = NULL;

    *listHandle = (jsr75_handle)list;
    return JSR75_STATUSCODE_OK;
}

/**
 * Close the opened pim list
 *
 * @param listHandle handle of the list to close
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case the list is no longer accessible
 */
JSR75_STATUSCODE 
jsr75_pim_list_close(
  jsr75_handle listHandle
  ) {
    pim_opened_item *current_item, *next_item;
    pim_opened_list *list = (pim_opened_list *)listHandle;
  
    if (listHandle == NULL) {
        return JSR75_STATUSCODE_FAIL;
    }
    
    for (current_item = list->item_list;
         current_item;) {
        next_item = current_item->next;
        free(current_item->name);
        free(current_item);
        current_item = next_item;
    }
    pcsl_file_closefilelist(list->handle);
    pcsl_string_free(list->path);
    free(list);
    return JSR75_STATUSCODE_OK;
}


/**
 * Returns the next item in the given pim list 
 * For Contact item the item will be in vCard 2.1 / 3.0 format
 * For Event Todo item the item will be in vCalendar 1.0 format
 *
 * @param listHandle handle of the list the get the item from
 * @param item a pointer to where to store the item, NULL otherwise
 * @param maxItemLen the maximum size of the item
 * @param categories pointer to where to store the item's categories
 *                   separated by JSR75_PIM_STRING_DELIMITER, NULL otherwise
 * @param maxCategoriesLen the maximum size of the categories buffer
 * @param itemHandle pointer to where to store a unique identifier
 *                   for the returned item
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_INVALID_ARGUMENT  maxItemLen is too small 
 * @retval JSR75_STATUSCODE_FAIL  in case of reaching the last item in the list
 */
JSR75_STATUSCODE 
jsr75_pim_list_get_next_item(
  jsr75_handle listHandle,
  unsigned char /* OUT */ *item,
  jsize maxItemLen,
  jchar /* OUT */ *categories,
  jsize maxCategoriesLen,
  jsr75_handle /* OUT */ *itemHandle
  ) {
    pim_opened_list *list = (pim_opened_list *)listHandle;
    char *item_name;
    pim_opened_item *opened_item;
    pcsl_string fname = PCSL_STRING_NULL;
    pcsl_string tmp = PCSL_STRING_NULL;
    int fres;
    pcsl_string_status res;
    jsize item_name_len, len;
    
    *itemHandle = NULL;
    if (listHandle == NULL) {
        return JSR75_STATUSCODE_FAIL;
    }

    if (categories && maxCategoriesLen > 0) {
        *categories = 0;
    }
    if (PCSL_STRING_OK != pcsl_string_cat(list->path, &fileSeparator, &tmp)) {
        return JSR75_STATUSCODE_FAIL;
    }
    for (;;) {
        if (0 != pcsl_file_getnextentry(list->handle, &tmp, &fname)) {
            pcsl_string_free(&tmp);
            return JSR75_STATUSCODE_FAIL;
        }
        fres = pcsl_file_is_directory(&fname);
        if (0 == fres) {
            pcsl_string_free(&tmp);
            break;
        }
        pcsl_string_free(&fname);
    }
    item_name_len = pcsl_string_utf8_length(&fname);
    item_name = malloc(item_name_len + 1);
    if (!item_name) {
        pcsl_string_free(&fname);
        return JSR75_STATUSCODE_FAIL;
    }
    opened_item = malloc(sizeof(pim_opened_item));
    if (!opened_item) {
        pcsl_string_free(&fname);
        free(item_name);
        return JSR75_STATUSCODE_FAIL;
    }
    res = pcsl_string_convert_to_utf8(&fname, item_name, item_name_len, &len);
    pcsl_string_free(&fname);
    if (PCSL_STRING_OK != res) {
        free(item_name);
        return JSR75_STATUSCODE_FAIL;
    }
    item_name[len] = 0;
    opened_item->name = item_name;
    opened_item->next = NULL;
    add_new_item(list, opened_item);
    *itemHandle = (jsr75_handle)opened_item;
    if (item && maxItemLen > 0) {
        FILE  *item_stream = open_list_item(list, opened_item, 1);
        size_t read_count;

        if (item_stream == NULL) {
            *item = 0;
            return JSR75_STATUSCODE_FAIL;
        }
        read_count = fread(item, 1, maxItemLen - 1, item_stream);
        item[read_count] = 0;
        fclose(item_stream);
    }
   
    return JSR75_STATUSCODE_OK;  
}

/**
 * Modify an item.
 * For Contact item the item will be in vCard 2.1 / 3.0 format
 * For Event Todo item the item will be in vCalendar 1.0 format
 *
 * @param listHandle handle of the list where the item is located
 * @param itemHandle handle of the item to modify
 * @param item pointer to the item data
 * @param categories pointer to the item's categories separated by a comma
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of an error
 */
JSR75_STATUSCODE 
jsr75_pim_list_modify_item(
  jsr75_handle listHandle,
  jsr75_handle itemHandle,
  const unsigned char *item,
  const jchar *categories
  ) {
    pim_opened_item *modifing_item = (pim_opened_item *)itemHandle;
    pim_opened_list *list = (pim_opened_list *)listHandle;
    FILE* file_handle;
    int data_length;
    
    (void)categories;

    if (!modifing_item) {
        return JSR75_STATUSCODE_FAIL;
    }
    
    file_handle = open_list_item(list, modifing_item, 0);
    if (!file_handle) {
        return JSR75_STATUSCODE_FAIL;
    }
    
    data_length = strlen(item);
    if (data_length) {
        fwrite(item, 1, data_length, file_handle);
    }
    
    fclose(file_handle);
    return JSR75_STATUSCODE_OK;
}

/**
 * Add a new item to the given item list
 * For Contact item the item will be in vCard 2.1 / 3.0 format
 * For Event Todo item the item will be in vCalendar 1.0 format
 *
 * @param listHandle handle of the list to add the new item to
 * @param item pointer to the item to add to the list
 * @param categories pointer to the item's categories separate by a comma
 * @param itemHandle pointer to where to store a unique identifier 
 *                   for the new item
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of an error
 */
JSR75_STATUSCODE 
jsr75_pim_list_add_item(
  jsr75_handle listHandle,
  const unsigned char *item,
  const jchar *categories,
  jsr75_handle *itemHandle
  ) {
    pim_opened_item *new_item;
    pim_opened_list *list = (pim_opened_list *)listHandle;
    FILE* file_handle;
    int data_length;

    (void)categories;

    new_item = malloc(sizeof(pim_opened_item));
    if (!new_item) {
        return JSR75_STATUSCODE_FAIL;
    }
    memset(new_item, 0, sizeof(pim_opened_item));
    file_handle = open_list_item(list, new_item, 0);
    if (!file_handle) {
        return JSR75_STATUSCODE_FAIL;
    }
    
    data_length = strlen(item);
    if (data_length) {
        fwrite(item, 1, data_length, file_handle);
    }
    
    fclose(file_handle);
    *itemHandle = (jsr75_handle)new_item;
    add_new_item(list, new_item);
    return JSR75_STATUSCODE_OK;
}

/**
 * Removes an item from the list
 *
 * @param listHandle handle of the list to delete the item from.
 * @param itemHandle handle of the item
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of reaching the last item in the list
 */
JSR75_STATUSCODE 
jsr75_pim_list_remove_item(
  jsr75_handle listHandle,
  jsr75_handle itemHandle
  ) {
    pim_opened_item *item = (pim_opened_item *)itemHandle;
    pim_opened_list *list = (pim_opened_list *)listHandle;
    char *path;
    int result;
    
    if (!item) {
        return JSR75_STATUSCODE_FAIL;
    }
    path = get_item_path(list, item->name);
    if (!path) {
        return JSR75_STATUSCODE_FAIL;
    }
    remove_item(list, item);
    result = remove(path);
    
    free(path);
    free(item);    
    return result ? JSR75_STATUSCODE_FAIL : JSR75_STATUSCODE_OK;
}

/**
 * Adds the provided category to the PIM list. If the given category already exists 
 * for the list, the method does not add another category and considers that this 
 * method call is successful and returns.
 *
 * The category names are case sensitive in this API, but not necessarily in the 
 * underlying implementation. For example, "Work" and "WORK" map to the same 
 * underlying category if the platform's implementation of categories is case-insensitive; 
 * adding both separately would result in only one category being created in this case.
 *
 * A string with no characters ("") may or may not be a valid category on a particular platform. 
 * If the string is not a valid category as defined by the platform, JSR75_STATUSCODE_FAIL is returned
 * when trying to add it. 
 *
 * @param listHandle handle of the list to add the new category to
 * @param categoryName the name of the category to be added
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL   If categories are unsupported, an error occurs, 
 *                         or the list is no longer accessible or closed.
 */
JSR75_STATUSCODE 
jsr75_pim_list_add_category(
  jsr75_handle listHandle,
  jchar *categoryName
  ) {
    FILE* cat_file;
    
    cat_file = open_categories_file((pim_opened_list *)listHandle, appendMode);
    if (!cat_file) {
        return JSR75_STATUSCODE_FAIL;
    }
    if (*(get_list_categories((pim_opened_list *)listHandle))) {
        fwrite(&categoryDelim, sizeof(jchar), 1, cat_file);
    }
    fwrite(categoryName,  sizeof(jchar), jcslen(categoryName), cat_file);
    fclose(cat_file);
    return JSR75_STATUSCODE_OK;
}

/**
 * Removes the indicated category from the PIM list. If the indicated category is 
 * not in the PIM list, this method is treated as successfully completing.
 * The category names are case sensitive in this API, but not necessarily in the 
 * underlying implementation. For example, "Work" and "WORK" map to the same underlying 
 * category if the platform's implementation of categories is case-insensitive; 
 * removing both separately would result in only one category being removed in this case. 
 *
 * @param listHandle handle of the list to remove the new category from
 * @param categoryName the name of the category to be removed
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL   if categories are unsupported, an error occurs, 
 *                         or the list is no longer accessible or closed.
 */
JSR75_STATUSCODE 
jsr75_pim_list_remove_category(
  jsr75_handle listHandle,
  jchar *categoryName
  ) {
    jchar* categories;
    FILE*  cat_file;
    jchar* del_cat;
    jchar* delimeter;
    int    del_size;
    jchar* search_start;

    categories = get_list_categories((pim_opened_list *)listHandle);
    if (!categories) {
        return JSR75_STATUSCODE_FAIL;
    }
    del_size = jcslen(categoryName);
    search_start = categories;
    do {
        del_cat = jcsstr(search_start, categoryName);
        search_start = del_cat + 1;
    } while (del_cat && del_cat[del_size] != 0 && del_cat[del_size] != categoryDelim);
    if (!del_cat) {
        return JSR75_STATUSCODE_FAIL;
    }
    delimeter = jcschr(del_cat, categoryDelim);
    if (!delimeter) {
        if (del_cat == categories) {
            cat_file = open_categories_file((pim_opened_list *)listHandle, writeMode);
            if (!cat_file) {
                return JSR75_STATUSCODE_FAIL;
            }
            fclose(cat_file);
            return JSR75_STATUSCODE_OK;
        }
        del_cat--;
    }

    if (delimeter) {
        delimeter ++;
        memmove(del_cat, delimeter, (jcslen(delimeter) + 1) * sizeof(jchar));
    }
    else {
        *del_cat = 0;
    }
    cat_file = open_categories_file((pim_opened_list *)listHandle, writeMode);
    if (!cat_file) {
        return JSR75_STATUSCODE_FAIL;
    }
    fwrite(categories,  sizeof(jchar), jcslen(categories), cat_file);
    fclose(cat_file);
    return JSR75_STATUSCODE_OK;
}


/**
 * Renames a category from an old name to a new name. All items associated with 
 * the old category name are changed to reference the new category name after 
 * this method is invoked. If the new category name is already an existing category, 
 * then the items associated with the old category name are associated with the existing category.
 * A string with no characters ("") may or may not be a valid category on a particular platform. 
 * If the string is not a category on a platform, a JSR75_STATUSCODE_FAIL should returned when trying 
 * to rename a category to it. 
 *
 * @param listHandle handle of the list to remove the new category from
 * @param oldCategoryName the old category name
 * @param newCategoryName the new category name
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL   in case of an error
 */
JSR75_STATUSCODE 
jsr75_pim_list_rename_category(
  jsr75_handle listHandle,
  jchar *oldCategoryName,
  jchar *newCategoryName
  ) {
    jchar* categories;
    FILE*  cat_file;
    jchar* del_cat;
    int    del_size;
    int    new_name_size;
    jchar* search_start;
    jchar* renamed_tail;
    
    categories = get_list_categories((pim_opened_list *)listHandle);
    if (!categories) {
        return JSR75_STATUSCODE_FAIL;
    }
    del_size = jcslen(oldCategoryName);
    search_start = categories;
    do {
        del_cat = jcsstr(search_start, oldCategoryName);
        renamed_tail = del_cat + del_size;
        search_start = del_cat + 1;
    } while (del_cat && *renamed_tail != 0 && *renamed_tail != categoryDelim);
    if (!del_cat) {
        return JSR75_STATUSCODE_FAIL;
    }
    del_size = renamed_tail - del_cat;
    new_name_size = jcslen(newCategoryName);
    
    if (jcslen(categories) - del_size + new_name_size + 1 > DATA_BUFFER_LENGTH) {
        return JSR75_STATUSCODE_FAIL;
    }
    
    memmove(renamed_tail - del_size + new_name_size, renamed_tail, (jcslen(renamed_tail) + 1) * sizeof(jchar));
    memcpy(del_cat, newCategoryName, new_name_size * sizeof(jchar));
    cat_file = open_categories_file((pim_opened_list *)listHandle, writeMode);
    if (!cat_file) {
        return JSR75_STATUSCODE_FAIL;
    }
    fwrite(categories, sizeof(jchar), jcslen(categories), cat_file);
    fclose(cat_file);
    return JSR75_STATUSCODE_OK;
}

/**
 *  Returns the maximum number of categories that this list can have.
 *
 * @param listHandle handle of the list the get the number from
 *
 * @retval -1 - indicates there is no limit the the number of categories that 
 *              this list can have
 * @retval 0  - indicates no category support
 * @retval 0 > - in case of a limitation
 */
jint 
jsr75_pim_list_max_categories(
  jsr75_handle listHandle
  ) {
    (void)listHandle;

    return -1;
}

/**
 * Returns the categories defined for the PIM list in JSR75_PIM_STRING_DELIMITER separated
 * format ("Work,HOME,Friends"). 
 * If there are no categories defined for the PIM list or categories are 
 * unsupported for the list, then JSR75_STATUSCODE_FAIL should be returned
 *
 * @param listHandle handle of the list the get the item from
 * @param categoriesName pointer to where to store the categories
 * @param maxCategoriesLen the maximum size of the categoriesName
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case no categories found or incase of an error.
 */
JSR75_STATUSCODE 
jsr75_pim_list_get_categories(
  jsr75_handle listHandle,
  jchar /* OUT */ *categoriesName,
  jsize maxCategoriesLen
  ) {
    jchar *categories;
    
    categories = get_list_categories((pim_opened_list *)listHandle);
    if (!categories || jcslen(categories) >= maxCategoriesLen) {
        return JSR75_STATUSCODE_FAIL;
    }
    jcscpy(categoriesName, categories);
    
    return JSR75_STATUSCODE_OK;    
}

/**
 * Gets all fields that are supported in this list. 
 * All fields supported by this list, including both standard 
 * and extended, are returned in this array.
 *
 * in order to identify field, field attributes , field array element
 * that aren't in use the JSR75_PIM_INVALID_ID should be set for the
 * member that aren't in use.
 *
 * @param listHandle handle of the list to get the fields from
 * @param fields pointer to where to store the fields to
 * @param maxFields the maximum fields the field buffer can hold
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of reaching the last item in the list
 */
JSR75_STATUSCODE 
jsr75_pim_list_get_fields(
  jsr75_handle listHandle,
  jsr75_pim_field /* OUT */ *fields,
  jint maxFields
  ) {
    pim_opened_list *list = (pim_opened_list *)listHandle;
    unsigned list_index;
    int i;
    
    if (listHandle == NULL) {
        return JSR75_STATUSCODE_FAIL;
    }

    for (list_index = 0;
         list_index < sizeof(list_data) / sizeof(pim_list_dscr);
         list_index++) {
         if (list_data[list_index].type == list->type) {
            break;
         }
    }
    if (list_index == sizeof(list_data) / sizeof(pim_list_dscr)) {
        return JSR75_STATUSCODE_FAIL;
    }

    if (maxFields < list_data[list_index].num_fields) {
        return JSR75_STATUSCODE_FAIL;
    }

    for (i = 0; i < list_data[list_index].num_fields; i++) {
        fields[i] = list_data[list_index].fields[i];
    }
    if (i < maxFields) {
        fields[i].id = JSR75_PIM_INVALID_ID;
    }
    return JSR75_STATUSCODE_OK;
}

/**
 * Gets all attributes supported by the list.
 *
 * @param listHandle handle of the list to get the attributes from
 * @param attributes pointer to buffer to store the attributes in
 * @param maxAttributes maximum number of attributes the buffer can hold
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of reaching the last item in the list
 */
JSR75_STATUSCODE 
jsr75_pim_list_get_attributes(
  jsr75_handle listHandle,
  jsr75_pim_field_attribute /* OUT */ *attributes,
  jint maxAttributes
  ) {
    pim_opened_list *list = (pim_opened_list *)listHandle;
    int i;
    
    if (listHandle == NULL) {
        return JSR75_STATUSCODE_FAIL;
    }

    if (maxAttributes < JSR75_PIM_MAX_ATTRIBUTES) {
        return JSR75_STATUSCODE_FAIL;
    }

    if (jsr75_pim_type_contact == list->type) {
        for (i = 0; i < JSR75_PIM_MAX_ATTRIBUTES; i++) {
            attributes[i] = pimAttributes[i];
        }
        if (i < maxAttributes) {
            attributes[i].id = JSR75_PIM_INVALID_ID;
        }
    } else {
        attributes[0].id = JSR75_PIM_INVALID_ID;
    }
    return JSR75_STATUSCODE_OK;
}
