// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// Build:   Oct/21/2013
// Author:  Alberl Lee
// Email:   ItsBird@qq.com
// Website: http://www.cnblogs.com/Alberl/p/3381820.html
//
// ��ܰ��ʾ��
// ������ÿ�춼�յ��ܶ������ʼ����ʼ����벻Ҫ������ȡ��Ӧ�����������ʼ����ֿ������Һ�QQ�Ĺ��˹��ܻ���ǿ��~O(��_��)O~
// ���������������ָ���ͼ��������������Ǽ���������ֱ�����������������ʣ�����ˡ���ظ�����������Ϊ���ú����С���Ҳ�ܿ����������~O(��_��)O~
//
// ʹ��Э�飺WTFPL
// �������쳯����Э�鶼�����ӣ������ܶ��˾�������أ������Բ����ˡ�DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE��Э��~O(��_��)O~
//
// ���밲ȫ�ԣ�
// ����ĿΪʾ����Ŀ��Ϊ�˷����ұ��룬û�������⣬���д��һЩ�򵥵ĺ������ܶ��߼��ж�Ҳֻ�Ǳ�֤����������ʵ��ʹ�������б�֤���밲ȫ~O(��_��)O~
// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------


#include "DuiFrameWnd.h"
#include "KDetectMemoryLeak.h"

int APIENTRY _tWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPTSTR lpCmdLine, int nCmdShow)
{
    atexit(CheckOnExit);
    ::CoInitialize(NULL);
    CPaintManagerUI::SetInstance(hInstance);
    CPaintManagerUI::SetResourcePath(CPaintManagerUI::GetInstancePath() + _T("skin"));

    CDuiFrameWnd *pFrame = new CDuiFrameWnd(_T("XMP.xml"));
    pFrame->Create(NULL, _T("װ��Ժ�ͻ���"), UI_WNDSTYLE_FRAME, WS_EX_WINDOWEDGE | WS_EX_ACCEPTFILES);
    pFrame->ShowModal();
    delete pFrame;
    ::CoUninitialize();
    return 0;
}
