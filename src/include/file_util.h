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
#include "sys/stat.h"
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
     * list file names
     * @param folderPath
     * @param files  bufferList
     * @param depth
     */
    void listFiles(const string &folderPath, list<string> *files, int depth = 0);

    /**
     * @param filepath
     * @return
     */
    int getFileLength(const char *filepath);

    /**
     * @param filepath
     * @param users
     * @param len
     */
    void saveUsers(const char *filepath, User *users, int len);

    /**
     * @param filepath
     * @param users
     * @return
     */
    int readUsers(const char *filepath, User *users);

    /**
     * @param pszDir
     * @return
     */
    int createDir(const char *pszDir);

}

#endif //FACE_FILE_UTIL_H
