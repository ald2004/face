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
#include "duilib.h"
#include <deque>
#include "../_include/xmp/AVPlayer.h"

class CDuiFrameWnd: public CXMLWnd
{
public:
    explicit CDuiFrameWnd(LPCTSTR pszXMLName);
    ~CDuiFrameWnd();

	void EnumVLCWnd(HWND hWnd);
    virtual void InitWindow();
    virtual CControlUI* CreateControl(LPCTSTR pstrClassName);
    virtual void Notify(TNotifyUI& msg);
    virtual LRESULT HandleMessage(UINT uMsg, WPARAM wParam, LPARAM lParam);
    virtual void OnClick(TNotifyUI& msg);
    virtual LRESULT OnNcHitTest(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
    virtual LRESULT ResponseDefaultKeyEvent(WPARAM wParam); 
	virtual LRESULT MessageHandler(UINT uMsg, WPARAM wParam, LPARAM /*lParam*/, bool& /*bHandled*/);

	DUI_DECLARE_MESSAGE_MAP()
    void OnDropFiles(HWND hwnd, HDROP hDropInfo);
    void OnDisplayChange(HWND hwnd, UINT bitsPerPixel, UINT cxScreen, UINT cyScreen);
    void OnGetMinMaxInfo(HWND hwnd, LPMINMAXINFO lpMinMaxInfo);
    LRESULT OnPlaying(HWND hwnd, WPARAM wParam, LPARAM lParam);     // 文件头读取完毕，开始播放
    LRESULT OnPosChanged(HWND hwnd, WPARAM wParam, LPARAM lParam);  // 进度改变，播放器传回来的进度
    LRESULT OnEndReached(HWND hwnd, WPARAM wParam, LPARAM lParam);  // 文件播放完毕
    bool    OnPosChanged(void* param);                              // 进度改变，用户主动改变进度
    bool    OnVolumeChanged(void* param);                           // 音量改变

    void Play(LPCTSTR pszPath);                              // 播放路径为pszPath的文件
    void Play(bool bPlay);                                   // 播放或暂停
    void Stop();                                               // 停止
    void OpenFileDialog();                                          // 打开文件窗口
	virtual LRESULT OnTimer(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
	virtual LRESULT HandleCustomMessage(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled);
private:
	CPlayerLayoutUI* m_pPlayerMgr;
    CDuiString      m_strPath;          // 当前文件的路径
    CSliderUI       *m_pSliderPlay;     // 文件播放进度
    CLabelUI        *m_pLabelTime;      // 文件播放时间
    WINDOWPLACEMENT m_OldWndPlacement;  // 保存窗口原来的位置
    bool            m_bFullScreenMode;  // 是否在全屏模式

    void ReadConfig(LPCTSTR pszPath);
    void WriteConfig(LPCTSTR pszPath);

    void ShowPlayButton(bool bShow);                                // 显示播放按钮
    void ShowPlayWnd(bool bShow);                                   // 显示播放窗口
    void ShowControlsForPlay(bool bShow);                           // 当开始播放或停止时，显示或隐藏一些控件

    void AdaptWindowSize(UINT cxScreen);                            // 根据屏幕分辨率自动调整窗口大小
    void FullScreen(bool bFull);                                    // 全屏
	
	
	/*{
		::MessageBox(NULL, _T("AC"), _T("随便啥"), NULL);
		bHandled = TRUE;
		return 0;
	}*/
};