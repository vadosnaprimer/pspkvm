#ifndef MP3_ME_H_INCLUDED
#define MP3_ME_H_INCLUDED

#include "psp_mmapi.h"

#ifdef __cplusplus
extern "C" {
#endif
void StopMp3(mp3_player_handle* mp);

int LoadMp3 (mp3_player_handle * mp);

void PlayMp3 (mp3_player_handle * mp);

void PauseMp3 (mp3_player_handle * mp);

long SeekMp3(mp3_player_handle* mp, long ms);

long GetTimeMp3(mp3_player_handle* mp);

void FinalizeMp3 ();

#ifdef __cplusplus
}
#endif

#endif
