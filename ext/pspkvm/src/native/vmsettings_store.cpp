/**
 * @file vmsettings_store.cpp
 *
 * Contains the real storage for the vmsettings
 */

#include <string>
#include <map>
#include <iostream>
#include <fstream>
#include "vmsettings_store.h"

using namespace std;

static const char* fname = "vmsettings.cfg";
static bool initialized = false;
static bool modified = false;

// A single Unicode string
typedef basic_string<jchar> ustring;

// An associative map of Unicode strings
typedef map<ustring, ustring> ustringmap;
static ustringmap properties;

// Read a ustring
static void write(ostream& o, const ustring& u) {
	unsigned int l = u.length();
	o.write((char*)&l, sizeof(l));
	o.write((char*)u.data(), l*sizeof(jchar)); }
	
// Write a ustring
static void read(istream& i, ustring& u) {
	u.clear();
	unsigned int l = 0;
	i.read((char*)&l, sizeof(l));
	while(l>0) {
		jchar t;
		i.read((char*)&t, sizeof(t));
		u.push_back(t);
		--l; } } 

// Write a ustringmap
static void write(ostream& o, const ustringmap& m) {
	unsigned int l = m.size();
	o.write((char*)&l, sizeof(l));
	ustringmap::iterator ix = properties.begin();
	while (ix != properties.end()) {
		write(o, ix->first);
		write(o, ix->second);
		++ix; } }
		
// Read a ustringmap
static void read(istream& i, ustringmap& m) {
	m.clear();
	unsigned int l = 0;
	i.read((char*)&l, sizeof(l));
	while(l>0) {
		ustring k, v;
		read(i, k);
		read(i, v);
		m[k]=v;
		--l; } }
		
// Init--makes sure read gets done
static void vmsettings_init() {
	if (initialized) {
		return; }
	ifstream i(fname);
	if (!i.is_open()) {
		// Valid on first run
		initialized=true;
		return; }
	read(i, properties);
	initialized=true; }

// Internal (C++) set
static void _vmsettings_set(const jchar* k, unsigned int kl,
	const jchar* v, unsigned int vl) {
		vmsettings_init();
		ustring ks(k, kl);
		ustring vs(v, vl);
		properties[ks]=vs;
		modified = true; }
		
// Internal (C++) get
static void _vmsettings_get(const jchar* k, unsigned int kl,
	const jchar** v, unsigned int* vl) {
		vmsettings_init();
		ustring ks(k, kl);
		ustringmap::iterator i = properties.find(ks);
		if (i==properties.end()) {
			// No such value. Return empty string
			*vl = 0;
			return; }
		*v = i->second.data();
		*vl = i->second.length(); }

// External (C) set
extern "C" void vmsettings_set(const jchar* k, unsigned int kl,
	const jchar* v, unsigned int vl) {
		_vmsettings_set(k, kl, v, vl); }
		
// External (C) get
extern "C" void vmsettings_get(const jchar* k, unsigned int kl,
	const jchar** v, unsigned int* vl) {
		_vmsettings_get(k, kl, v, vl); }

// External (C) commit
extern "C" void vmsettings_commit() {
	if (!initialized) {
		// No one's even read this
		return; }
	if (!modified) {
		// Not modified
		return; }
	ofstream o(fname);
	write(o, properties);
	modified = false; }
