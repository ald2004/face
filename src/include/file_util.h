//
// Created by wangxiaoming on 2019/2/28.
//

#ifndef FACE_FILE_UTIL_H
#define FACE_FILE_UTIL_H



#include <list>
#include "User.h"


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
