//
// Created by wangxiaoming on 2019/2/28.
//

#ifndef FACE_FILE_UTIL_H
#define FACE_FILE_UTIL_H


#ifdef _WIN32

#include <direct.h>
#include <io.h>

#define ACCESS _access
#define MKDIR(a) _mkdir((a))

#else
#include <unistd.h>
#include <dirent.h>
#include <stdarg.h>
#define ACCESS access
#define MKDIR(a) mkdir((a),0755)
#endif


#include <list>
#include <fstream>
#include <iostream>
#include "User.h"


using std::string;

using namespace std;

namespace Face {
    /**
     * 递归列出文件名
     * @param folderPath 搜索目录
     * @param files  bufferList
     * @param depth  搜索深度，默认0
     */
    void listFiles(const string &folderPath, list<string> *files, int depth = 0);

    /**
     * 获取文件大小
     * @param filepath
     * @return
     */
    int getFileLength(const char *filepath);

    /**
     * 保存
     * @param filepath
     * @param users
     * @param len
     */
    void saveUsers(const char *filepath, User *users, int len);

    /**
     * 读取对象
     * @param filepath
     * @param users
     * @return
     */
    int readUsers(const char *filepath, User *users);

    /**
     * 创建 目录
     * @param pszDir
     * @return
     */
    int createDir(const char *pszDir);

}

#endif //FACE_FILE_UTIL_H
