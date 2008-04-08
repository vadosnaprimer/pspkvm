#include <string.h>
#include "sjis2uni.tab"

int jis2unicode(unsigned char *jis, unsigned short *uni)
{
	unsigned short jc;
	unsigned char* tmp = (unsigned char*)&jc;
	int len = strlen(jis);
	int i, j = 0;
	for(i = 0; i < len;)
	{
		if(jis[i]<0x81)
		{
			uni[j++]=jis[i++];
		}		
		else 
		{
			tmp[1] = jis[i++];
			tmp[0] = jis[i++];									
			uni[j++] = JIS_UNICODE[jc];
		}
	}
	uni[j] = 0;
	return j;
}

