#include <stdio.h>

#define JPEG_IMAGE 1
#define PNG_IMAGE 2

struct ID3Tag {
    char   ID3Title[100];
    char   ID3Artist[100];
    char   ID3Album[100];
	char   ID3Year[12];
	char   ID3Comment[200];
	char   ID3GenreCode[12];
	char   ID3GenreText[100];
    char   versionfound[12];
    int    ID3Track;
    char   ID3TrackText[8];
    int    ID3EncapsulatedPictureType;
    int    ID3EncapsulatedPictureOffset; /* Offset address of an attached picture, NULL if no attached picture exists */
    int    ID3EncapsulatedPictureLength;
    int    ID3Length;
};

int ID3v2TagSize(const char *mp3path);
struct ID3Tag ParseID3(char *mp3path);

//Helper functions (used also by aa3mplayerME to read tags):
void readTagData(FILE *fp, int tagLength, char *tagValue);
int swapInt32BigToHost(int arg);
//short int swapInt16BigToHost(short int arg);
