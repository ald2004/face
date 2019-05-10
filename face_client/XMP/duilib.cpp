#include "duilib.h"

void CPlayerUI::DoEvent(TEventUI& event)
{
	CControlUI::DoEvent(event);
	if (event.Type == UIEVENT_BUTTONDOWN) {
		CPlayerLayoutUI* pParent = (CPlayerLayoutUI*)GetParent();
		if (pParent != NULL) {
			pParent->Select(this);
		}
	}
}
