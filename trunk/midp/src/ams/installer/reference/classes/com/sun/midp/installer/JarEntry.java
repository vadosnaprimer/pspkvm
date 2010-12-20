package com.sun.midp.installer;

/**
 *
 * @author Max
 */
class JarEntry {
    private final int entryID;
    private final String entryName;
    private final String entryTitle;
    private final String entrySmalldesc;
    private final String entryFilesize;

    public JarEntry(int id, String name, String title, String smalldesc, String filesize) {
        entryID = id;
        entryName = name;
        entryTitle = title;
        entrySmalldesc = smalldesc;
        entryFilesize = filesize;
    }

  int getID() {
      return entryID;
  }

  String getName() {
    return entryName;
  }

  String getTitle() {
      return entryTitle;
  }

  String getSmallDesc() {
      return entrySmalldesc;
  }

  String getFilesize() {
      return entryFilesize;
  }
}
