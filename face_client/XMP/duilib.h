// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Build:   Oct/21/2013
// Author:  Alberl Lee
// Email:   ItsBird@qq.com
// Website: http://www.cnblogs.com/Alberl/p/3381820.html
//
// 温馨提示：
// 此邮箱每天都收到很多垃圾邮件，邮件名请不要随意乱取，应尽量与垃圾邮件区分开来，幸好QQ的过滤功能还算强大~O(∩_∩)O~
// 此邮箱仅供【错误指正和技术交流】，若是技术问题请直接在文章评论区提问，否则恕不回复，这样做是为了让后面的小伙伴也能看到解决方案~O(∩_∩)O~
//
// 使用协议：WTFPL
// 鉴于在天朝神马协议都被无视，甚至很多大公司都不遵守，故索性采用了【DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE】协议~O(∩_∩)O~
//
// 代码安全性：
// 此项目为示例项目，为了方便大家编译，没用三方库，因此写了一些简单的函数，很多逻辑判断也只是保证正常操作，实际使用请自行保证代码安全~O(∩_∩)O~
// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------


#pragma once
#include <windowsx.h>
#include <algorithm>
#include "..\_include\xmp\AVPlayer.h"
#include "../duilib/UIlib.h"
using namespace DuiLib;

#ifdef _DEBUG
#   ifdef _UNICODE
#       pragma comment(lib, "../_lib/DuiLib_ud.lib")
#   else
#       pragma comment(lib, "../_lib/DuiLib_d.lib")
#   endif
#else
#   ifdef _UNICODE
#       pragma comment(lib, "../_lib/DuiLib_u.lib")
#   else
#       pragma comment(lib, "../_lib/DuiLib.lib")
#   endif
#endif


// 以XML生成界面的窗口基类
class CXMLWnd : public WindowImplBase
{
public:
    explicit CXMLWnd(LPCTSTR pszXMLName) 
        : m_strXMLName(pszXMLName){}

public:
    virtual LPCTSTR GetWindowClassName() const
    {
        return _T("XMLWnd");
    }

    virtual CDuiString GetSkinFile()
    {
        return m_strXMLName;
    }

    virtual CDuiString GetSkinFolder()
    {
        return _T("");
    }

protected:
    CDuiString m_strXMLName;    // XML的名字
};


// 将HWND显示到CControlUI上面
class CWndUI: public CControlUI
{
public:
    CWndUI(): m_hWnd(NULL){}

    virtual void SetVisible(bool bVisible = true)
    {
        __super::SetVisible(bVisible);
        ::ShowWindow(m_hWnd, bVisible);
    }

    virtual void SetInternVisible(bool bVisible = true)
    {
        __super::SetInternVisible(bVisible);
        ::ShowWindow(m_hWnd, bVisible);
    }

    virtual void SetPos(RECT rc)
    {
        __super::SetPos(rc);
        ::SetWindowPos(m_hWnd, NULL, rc.left + 2, rc.top + 2, rc.right - rc.left - 4, rc.bottom - rc.top - 4, SWP_NOZORDER | SWP_NOACTIVATE);
    }

    BOOL Attach(HWND hWndNew)
    { 
        if (! ::IsWindow(hWndNew))
        {
            return FALSE;
        }

        m_hWnd = hWndNew;
        return TRUE;
    }

    HWND Detach()
    {
        HWND hWnd = m_hWnd;
        m_hWnd = NULL;
        return hWnd;
    }

    HWND GetHWND()
    {
        return m_hWnd;
    }

protected:
    HWND m_hWnd;
};


#define WM_USER_PLAYING         WM_USER + 1     // 开始播放文件
#define WM_USER_POS_CHANGED     WM_USER + 2     // 文件播放位置改变
#define WM_USER_END_REACHED     WM_USER + 3     // 播放完毕

static void CallbackPlayer(void *data, UINT uMsg)
{
	CAVPlayer *pAVPlayer = (CAVPlayer *)data;
	if (pAVPlayer)
	{
		HWND hWnd = pAVPlayer->GetHWND();
		if (::IsWindow(hWnd) && ::IsWindow(::GetParent(hWnd)))
		{
			::PostMessage(::GetParent(hWnd), uMsg, (WPARAM)data, 0);
		}
	}
}

static void CallbackPlaying(void *data)
{
	CallbackPlayer(data, WM_USER_PLAYING);
}

static void CallbackPosChanged(void *data)
{
	CallbackPlayer(data, WM_USER_POS_CHANGED);
}

static void CallbackEndReached(void *data)
{
	CallbackPlayer(data, WM_USER_END_REACHED);
}

class CPlayerUI : public CControlUI, public CAVPlayer
{
public:
	CPlayerUI() : m_hWnd(NULL), m_bSelected(false) {}

	virtual void SetVisible(bool bVisible = true)
	{
		__super::SetVisible(bVisible);
		::ShowWindow(m_hWnd, bVisible);
		if (!IsVisible()) m_bSelected = false;
	}

	virtual void SetInternVisible(bool bVisible = true)
	{
		__super::SetInternVisible(bVisible);
		::ShowWindow(m_hWnd, bVisible);
	}

	virtual void SetPos(RECT rc)
	{
		__super::SetPos(rc);
		::SetWindowPos(m_hWnd, NULL, rc.left + 2, rc.top + 2, rc.right - rc.left - 4, rc.bottom - rc.top - 4, SWP_NOZORDER | SWP_NOACTIVATE);
	}

	BOOL Attach(HWND hWndNew)
	{
		if (!::IsWindow(hWndNew)) {
			return FALSE;
		}

		m_hWnd = hWndNew;
		SetHWND(m_hWnd);
		EnableWindow(m_hWnd, FALSE);
		SetCallbackPlaying(CallbackPlaying);
		SetCallbackPosChanged(CallbackPosChanged);
		SetCallbackEndReached(CallbackEndReached);
		return TRUE;
	}

	HWND Detach()
	{
		HWND hWnd = m_hWnd;
		m_hWnd = NULL;
		SetHWND(NULL);
		return hWnd;
	}

	HWND GetHWND()
	{
		return m_hWnd;
	}

	void Select(bool bSelected)
	{
		m_bSelected = bSelected;
		Invalidate();
	}

	bool IsSelected()
	{
		return m_bSelected;
	}
	void DoEvent(TEventUI& event);
	virtual void DoPaint(HDC hDC, const RECT& rcPaint)
	{
		CControlUI::DoPaint(hDC, rcPaint);
		if (m_bSelected) {
			//CRenderEngine::DrawRect(hDC, m_rcItem, 2, GetAdjustColor(0xFFFF0000));
		}
	}

protected:
	HWND m_hWnd;
	bool m_bSelected;
};

class CPlayerLayoutUI : public CContainerUI
{
public:
	CPlayerLayoutUI()
	{
		m_nPL = 2;
		m_bNeedPL = true;
	}

public:
	HWND GetHWND(int idx)
	{
		if (idx >= 0 && idx <  m_vPlayerWnds.size()) {
			CPlayerUI* pWnd = m_vPlayerWnds[idx];
			return pWnd->GetHWND();
		}

		return NULL;
	}

	CPlayerUI* GetPlayer(int idx)
	{
		if (idx >= 0 && idx < m_vPlayerWnds.size()) {
			return m_vPlayerWnds[idx];
		}
		return NULL;
	}

	CPlayerUI* PlayerFromHWND(HWND hWnd)
	{
		for (size_t i = 0; i <m_vPlayerWnds.size(); i++) {
			CPlayerUI* pWnd = m_vPlayerWnds[i];
			if (pWnd->GetHWND() == hWnd) {
				return pWnd;
			}
		}
		return NULL;
	}

	void Select(int idx)
	{
		for (size_t i = 0; i <m_vPlayerWnds.size(); i++) {
			CPlayerUI* pWnd = m_vPlayerWnds[i];
			if (i == idx) {
				pWnd->Select(true);
			}
			else {
				pWnd->Select(false);
			}
		}
	}

	void Select(HWND hWnd)
	{
		int idx = -1;
		for (size_t i = 0; i <m_vPlayerWnds.size(); i++) {
			CPlayerUI* pWnd = m_vPlayerWnds[i];
			if (pWnd->GetHWND() == hWnd) {
				idx = i;
				break;
			}
		}

		return Select(idx);
	}

	void Select(CPlayerUI* pPlayer)
	{
		int idx = -1;
		for (size_t i = 0; i <m_vPlayerWnds.size(); i++) {
			CPlayerUI* pWnd = m_vPlayerWnds[i];
			if (pWnd == pPlayer) {
				idx = i;
				break;
			}
		}

		return Select(idx);
	}
	int GetCurSel()
	{
		int nIdx = -1;
		for (size_t i = 0; i < m_vPlayerWnds.size(); i++) {
			CPlayerUI* pWnd = m_vPlayerWnds[i];
			if (pWnd->IsSelected()) {
				nIdx = i;
				break;
			}
		}
		return nIdx;
	}

	// 播放器操作类
	bool Play(const std::string &strPath, int i = -1)  // 播放路径为strPath的文件
	{
		if(i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return false;

		return GetPlayer(i)->Play(strPath);
	}
	void Play(int i = -1)                          // 播放
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return;

		return GetPlayer(i)->Play();
	}
	void Pause(int i = -1)                           // 暂停
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return;

		return GetPlayer(i)->Pause();
	}
	void Stop(int i = -1)                           // 停止
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return;

		return GetPlayer(i)->Stop();
	}

	void Volume(int iVol, int i = -1)                 // 音量设置为iVol
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return;

		return GetPlayer(i)->Volume(iVol);
	}
	void VolumeIncrease(int i = -1)                  // 音量增大
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return;

		return GetPlayer(i)->VolumeIncrease();
	}
	void VolumeReduce(int i = -1)                    // 音量减小 
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return;

		return GetPlayer(i)->VolumeReduce();
	}

	void SeekTo(int iPos, int i = -1)                  // 跳到指定位置iPos
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return;

		return GetPlayer(i)->SeekTo(iPos);
	}
	void SeekForward(int i = -1)                    // 快进
	{
		if (i < 0 || i >= 16) return;

		return GetPlayer(i)->SeekForward();
	}
	void SeekBackward(int i = -1)                    // 快退
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return;

		return GetPlayer(i)->SeekBackward();
	}

	bool    IsOpen(int i = -1)                      // 文件是否打开
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return false;

		return GetPlayer(i)->IsOpen();
	}
	bool    IsPlaying(int i = -1)                    // 文件是否正在播放
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return false;

		return GetPlayer(i)->IsPlaying();
	}
	int     GetPos(int i = -1)                       // 获取文件当前播放的位置
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return 0;

		return GetPlayer(i)->CAVPlayer::GetPos();
	}
	__int64 GetTotalTime(int i = -1)                 // 获取总时间
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return 0;

		return GetPlayer(i)->GetTotalTime();
	}
	__int64 GetTime(int i = -1)                      // 获取时间
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return 0;

		return GetPlayer(i)->GetTime();
	}
	int     GetVolume(int i = -1)                    // 获取音量
	{
		if (i == -1) i = GetCurSel();
		if (i < 0 || i >= 16) return 0;

		return GetPlayer(i)->GetVolume();
	}

public:
	void SetAttribute(LPCTSTR pstrName, LPCTSTR pstrValue)
	{
		if (_tcsicmp(pstrName, _T("pl")) == 0) SetPL(_ttoi(pstrValue));
		return CContainerUI::SetAttribute(pstrName, pstrValue);
	}

	void SetPL(int nCount)
	{
		if (m_nPL != nCount) {
			m_nPL = nCount;
			NeedParentUpdate();
			m_bNeedPL = true;
		}
	}

public:
	void DoInit()
	{
		for (int i = 0; i < 16; i++) {
			CPlayerUI* pWnd = new CPlayerUI();
			HWND hWnd = CreateWindow(_T("#32770"), _T("WndMediaDisplay"), WS_CHILD, 0, 0, 0, 0, m_pManager->GetPaintWindow(), (HMENU)0, NULL, NULL);
			pWnd->Attach(hWnd);
			if (Add(pWnd)) {
				m_vPlayerWnds.push_back(pWnd);
			}
		}
	}

	void SetPos(RECT rc)
	{
		CControlUI::SetPos(rc);

		rc = m_rcItem;
		rc.left += m_rcInset.left;
		rc.top += m_rcInset.top;
		rc.right -= m_rcInset.right;
		rc.bottom -= m_rcInset.bottom;
		
		int nRowCount = 1;
		int nColCount = 1;
		if(m_nPL == 1) {
			nRowCount = 1;
		}
		else if (m_nPL == 2) {
			nRowCount = 1;
			nColCount = 2;
		}
		else if (m_nPL == 4) {
			nRowCount = 2;
			nColCount = 2;
		}
		else if (m_nPL == 9) {
			nRowCount = 3;
			nColCount = 3;
		}
		else if (m_nPL == 16) {
			nRowCount = 4;
			nColCount = 4;
		}
		int nWidth = (rc.right - rc.left) / (nColCount);
		int nHeight = (rc.bottom - rc.top) / (nRowCount);

		for (size_t i = 0; i < m_nPL; i++) {
			CPlayerUI* pWnd = m_vPlayerWnds[i];
			RECT rcItem = m_rcItem;
			rcItem.left += nWidth * (i%nColCount);
			rcItem.right = rcItem.left + nWidth;
			rcItem.top += nHeight * (i / nColCount);
			rcItem.bottom = (rcItem.top + nHeight);
			pWnd->SetPos(rcItem);
			if(m_bNeedPL) pWnd->SetVisible(true);
		}
		if (m_bNeedPL) {
			for (size_t i = m_nPL; i < m_vPlayerWnds.size(); i++) {
				CPlayerUI* pWnd = m_vPlayerWnds[i];
				pWnd->SetVisible(false);
			}
		}
		m_bNeedPL = false;
	}

public:
	std::vector<CPlayerUI*> m_vPlayerWnds;
	int m_nPL;
	bool m_bNeedPL;
};