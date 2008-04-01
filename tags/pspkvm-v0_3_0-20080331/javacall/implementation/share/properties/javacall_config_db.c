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

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "javacall_defs.h"
#include "javacall_memory.h"
#include "javacall_logging.h"
#include "javacall_file.h"

#include "javacall_db.h"
#include "javautil_string.h"
#include "javacall_config_db.h"

/* Maximal expected line length from properties file */
#define MAX_LINE_LENGTH	    1023

/* Maximal expected single text word length from properties file*/
#define MAX_STR_LENGTH      1023

/* Invalid key */
#define INI_INVALID_KEY    ((char*)-1)


/*---------------------------------------------------------------------------
                        Internal Functions
 ---------------------------------------------------------------------------*/

static char *configdb_fgets(char *line, int length, javacall_handle file_handle) {
    long read_length = 0;
    long actual_read = 1;

    while ((read_length < length-1) && (actual_read>0)) {
        actual_read = javacall_file_read(file_handle, (unsigned char *)&line[read_length], 1); 
        if (0 < actual_read) {
            if (read_length==0 && ((line[0]=='\n') || (line[0]=='\r'))) {
                continue;
            }

            if (line[read_length] == '\n' || line[read_length] == '\r') {
                break;
            }

            read_length += actual_read;
        }
    }

    line[read_length] = 0;

    if (read_length>0) {
        return line;
    }

    return NULL;
}


/* convert an INI section/key to a database key and add it to the database */
static void configdb_add_entry(
                              string_db * d,
                              char * sec,
                              char * key,
                              char * val) {
    char longkey[2*MAX_STR_LENGTH+1];

    if (NULL == d) {
        return;
    }

/* Make a key as section:keyword */
    if (key != NULL) {
        sprintf(longkey, "%s:%s", sec, key);
    } else {
        strcpy(longkey, sec);
    }

/* Add (key,val) to string_db */
    javacall_string_db_set(d, longkey, val);
}

/*---------------------------------------------------------------------------
                        Public Functions
 ---------------------------------------------------------------------------*/

/**
 * Get the number of sections in the database
 * 
 * @param config_handle database object created by calling javacall_configdb_load
 * @return number of sections in the database or -1 in case of error
 */
int javacall_configdb_get_num_of_sections(javacall_handle config_handle) {
    int i;
    int nsec;
    string_db *d = (string_db *)config_handle;

    if (d == NULL) {
        return -1;
    }

    nsec = 0;

    for (i = 0 ; i < d->size; i++) {
        if (d->key[i] == NULL) {
            continue;
        }
            
        if (strchr(d->key[i], ':')==NULL) {
            nsec++;
        }
    }

    return nsec;
}


/**
 * Get the name of the n'th section
 * 
 * @param config_handle database object created by calling javacall_configdb_load
 * @param n section number
 * @return the name of the n'th section or NULL in case of error
 *          the returned string was STATICALLY ALLOCATED. DO NOT FREE IT!!
 */
char* javacall_configdb_get_section_name(javacall_handle config_handle, int n) {
    int i;
    int foundsec;
    string_db *d = (string_db *)config_handle;

    if (d == NULL || n < 0) {
        return NULL;
    }

    foundsec = 0 ;

    for (i = 0; i < d->size; i++) {
        if (d->key[i] == NULL) {
            continue;
        }
        if (strchr(d->key[i], ':') == NULL) {
            foundsec++;
            if (foundsec > n) {
                return d->key[i];
            }
        }
    }

    return NULL;    
}


/**
 * Dump the content of the parameter database to an open file pointer
 * The output format is pairs of [Key]=[Value]
 * 
 * @param config_handle    database object created by calling javacall_configdb_load
 * @param unicodeFileName  output file name
 * @param fileNameLen      file name length
 */
void javacall_configdb_dump(javacall_handle config_handle, 
                            javacall_utf16* unicodeFileName, 
                            int fileNameLen) {

    int     i;
    string_db *d = (string_db *)config_handle;
    javacall_handle file_handle;
    char            l[MAX_STR_LENGTH];
    javacall_result res;

    if (d == NULL || unicodeFileName == NULL || fileNameLen <= 0) {
        return; 
    }

    res = javacall_file_open(unicodeFileName,
                             fileNameLen,
                             JAVACALL_FILE_O_RDWR | JAVACALL_FILE_O_CREAT,
                             &file_handle);
    if (res != JAVACALL_OK) {
        javacall_print("javacall_configdb_dump(): ERROR - Can't open the dump file!\n");
        return;
    }

    for (i = 0; i < d->size; i++) {
        if (d->key[i] == NULL) {
            continue;
        }

        if (d->val[i] != NULL) {
            sprintf(l, "[%s]=[%s]\n", d->key[i], d->val[i]);
        } else {
            sprintf(l, "[%s]=UNDEF\n", d->key[i]);
        }
        javacall_file_write(file_handle, (unsigned char*)l, strlen(l)); 
    }
    javacall_file_close(file_handle);
}

/**
 * Dump the content of the parameter database to an open file pointer
 * The output format is as a standard INI file
 * 
 * @param config_handle    database object created by calling javacall_configdb_load
 * @param unicodeFileName  output file name
 * @param fileNameLen      file name length
 */
void javacall_configdb_dump_ini(javacall_handle config_handle, 
                                javacall_utf16* unicodeFileName, 
                                int fileNameLen) {
    int     i, j;
    char    keym[MAX_STR_LENGTH+1];
    int     nsec;
    char*   secname;
    int     seclen;
    string_db* d = (string_db *)config_handle;
    javacall_handle file_handle;
    char    l[MAX_STR_LENGTH];
    javacall_result res;

    if (d == NULL || unicodeFileName == NULL || fileNameLen <= 0) {
        return; 
    }


    res = javacall_file_open(unicodeFileName,
                             fileNameLen,
                             JAVACALL_FILE_O_RDWR | JAVACALL_FILE_O_CREAT,
                             &file_handle);
    if (res != JAVACALL_OK) {
        javacall_print("javacall_configdb_dump_ini(): ERROR - Can't open the dump file!\n");
        return;
    }

    nsec = javacall_configdb_get_num_of_sections(d);
    if (nsec < 1) {
        /* No section in file: dump all keys as they are */
        for (i = 0; i < d->size; i++) {
            if (d->key[i] == NULL) {
                continue;
            }

            if (d->val[i] != NULL) {
                sprintf(l, "[%s]=[%s]\n", d->key[i], d->val[i]);
            } else {
                sprintf(l, "[%s]=UNDEF\n", d->key[i]);
            }

            javacall_file_write(file_handle, (unsigned char*)l, strlen(l)); 
        }
        javacall_file_close(file_handle);
        return ;
    }
    for (i = 0; i < nsec; i++) {
        secname = javacall_configdb_get_section_name(d, i) ;
        seclen  = (int)strlen(secname);
        sprintf(l, "\n[%s]\n", secname);
        javacall_file_write(file_handle, (unsigned char*)l, strlen(l)); 
        sprintf(keym, "%s:", secname);
        for (j = 0; j < d->size; j++) {
            if (d->key[j] == NULL) {
                continue;
            }                
            if (!strncmp(d->key[j], keym, seclen+1)) {
                sprintf(l,
                        "%-30s = %s\n",
                        d->key[j]+seclen+1,
                        d->val[j] ? d->val[j] : "");
                javacall_file_write(file_handle, (unsigned char*)l, strlen(l)); 
            }
        }
    }
    sprintf(l, "\n");
    javacall_file_write(file_handle, (unsigned char*)l, strlen(l)); 

    javacall_file_close(file_handle);
}


/**
 * Get the value corresponding to the key as a string
 * 
 * @param config_handle   database object created by calling javacall_configdb_load
 * @param key             The key to get the corresponding value of
 * @param def             default parameter to return if the value has not been found
 * @param result          where to store the result string
 * @return  JAVACALL_FAIL   bad arguments are supplied
 *          JAVACALL_OK     otherwise
 */
javacall_result javacall_configdb_getstring(javacall_handle config_handle, char* key, 
                                            char* def, char** result) {
    char* lc_key;
    string_db *d = (string_db *)config_handle;

    if (d == NULL || key == NULL) {
        *result = def;
        return JAVACALL_FAIL;
    }

    lc_key = key;
    *result = javacall_string_db_getstr(d, lc_key, def);    
    return JAVACALL_OK;
}



/**
 * Find a key in the database
 * 
 * @param config_handle database object created by calling javacall_configdb_load
 * @param key   the key to find
 * @return      JAVACALL_OK if the key exists 
 *              JAVACALL_KEY_NOT_FOUND otherwise
 */
javacall_result javacall_configdb_find_key(javacall_handle config_handle, char* key) {
    char* str;
    string_db *d = (string_db *)config_handle;

    if (JAVACALL_OK == javacall_configdb_getstring(d, key, INI_INVALID_KEY, &str)) {
        return JAVACALL_OK;
    } else {
        return JAVACALL_KEY_NOT_FOUND;
    }
}



/**
 * Set new value for key. If key is not in the database, it will be added.
 * 
 * @param config_handle  database object created by calling javacall_configdb_load
 * @param key   the key value corresponding to which to modify. the key is given as INI "section:key"
 * @param val   the new value for key
 * @return      JAVACALL_OK in case of success
 *              JAVACALL_FAIL otherwise
 */
javacall_result javacall_configdb_setstr(javacall_handle config_handle, char* key, char* val) {
    string_db* d = (string_db *)config_handle;

    if (key == NULL || val == NULL) {
        return JAVACALL_FAIL;
    }

    javacall_string_db_set(d, key, val);
    return JAVACALL_OK;
}

/**
 * Delete a key from the database
 * 
 * @param config_handle     database object created by calling javacall_configdb_load
 * @param key   the key to delete
 */
void javacall_configdb_unset(javacall_handle config_handle, char* key) {
    string_db* d = (string_db *)config_handle;
    javacall_string_db_unset(d, key);
}


#ifndef USE_PROPERTIES_FROM_FS
#include "javacall_static_properties.h"

javacall_handle configdb_load_no_fs () {
    string_db* db;
    int i, j;

    //allocate memory for db
    db = javacall_string_db_new(0);
    if (NULL == db) {
        return NULL;
    }

    for (i = 0; javacall_static_properties_sections[i] != NULL; i++) {        
        //add section to db
        configdb_add_entry(db, javacall_static_properties_sections[i], NULL, NULL);
        //add keys and values
        for (j = 0; javacall_static_properties_keys[i][j] != NULL; j++) {
            configdb_add_entry(db, javacall_static_properties_sections[i], javacall_static_properties_keys[i][j], 
                               javacall_static_properties_values[i][j]);
        }
    }
    return(javacall_handle)db;
}
#endif	//USE_PROPERTIES_FROM_FS

javacall_handle configdb_load_from_fs(javacall_utf16* unicodeFileName, int fileNameLen) {
    string_db*   d ;
    char    line[MAX_LINE_LENGTH+1];
    char    sec[MAX_STR_LENGTH+1];
    char    key[MAX_STR_LENGTH+1];
    char    val[MAX_STR_LENGTH+1];
    char*   where;     
    int lineno;
    javacall_handle file_handle;
    javacall_result res;


    res = javacall_file_open(unicodeFileName,
                             fileNameLen,
                             JAVACALL_FILE_O_RDWR | JAVACALL_FILE_O_CREAT,
                             &file_handle);
    if (res != JAVACALL_OK) {
        javacall_print("javacall_configdb_load(): ERROR - Can't open the dump file!\n");
        return NULL;
    }

    sec[0] = 0;

/*
 * Initialize a new string_db entry
 */
    d = javacall_string_db_new(0);
    if (NULL == d) {
        return NULL;
    }        
    lineno = 0;
    while (configdb_fgets(line, MAX_LINE_LENGTH, file_handle) != NULL) {
        lineno++;
        where = javautil_string_skip_leading_blanks(line); /* Skip leading spaces */
        if (*where==';' || *where=='#' || *where == 0){
            continue; /* Comment lines */
        }
        else {
            if (sscanf(where, "[%[^]]", sec) == 1) {
                /* Valid section name */
                strcpy(sec, sec);
                configdb_add_entry(d, sec, NULL, NULL);
            } 
            else if (sscanf (where, "%[^=] = \"%[^\"]\"", key, val) == 2
                       ||  sscanf (where, "%[^=] = '%[^\']'",   key, val) == 2
                       ||  sscanf (where, "%[^=] = %[^;#]",     key, val) == 2) {
                javautil_string_skip_trailing_blanks(key);
                /*
                 * sscanf cannot handle "" or '' as empty value,
                 * this is done here
                 */
                if (!strcmp(val, "\"\"") || !strcmp(val, "''")) {
                    val[0] = (char)0;
                } else {
                    javautil_string_skip_trailing_blanks(val);
                }
                configdb_add_entry(d, sec, key, val);
            }
        }
    }
    javacall_file_close(file_handle);
    return(javacall_handle)d ;
}

/**
 * Loads a database from an INI file
 * 
 * @param unicodeFileName  input INI file name
 * @param fileNameLen      file name length
 * @return  newly created database object
 */
javacall_handle javacall_configdb_load(javacall_utf16* unicodeFileName, int fileNameLen) {
#ifdef USE_PROPERTIES_FROM_FS
    return configdb_load_from_fs(unicodeFileName, fileNameLen);
#else
    return configdb_load_no_fs();
#endif	//USE_PROPERTIES_FROM_FS
}


/**
 * Free a database
 * 
 * @param   config_handle database object created in a call to javacall_configdb_load
 */
void javacall_configdb_free(javacall_handle config_handle) {
    string_db* d = (string_db *)config_handle;
    javacall_string_db_del(d);
}

