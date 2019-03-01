//
// Created by wangxiaoming on 2019/2/28.
//

#ifndef FACE_FILE_UTIL_H
#define FACE_FILE_UTIL_H


#ifdef _WIN32

#include <io.h>

#else
#include <unistd.h>
#include <dirent.h>
#endif

#include <list>
#include <fstream>
#include <iostream>
#include "User.h"
#include <sys/stat.h>
#include <string.h>
#include <string>

using std::string;

using namespace std;

namespace Face {
    void listFiles(const string &folderPath, list<string> *files, int depth = 0);

    int get_file_length(const char *filepath);

    void save_user(const char *filepath, User *users, int len);

    int read_users(const char *filepath, User *users);
}

#endif //FACE_FILE_UTIL_H
