//
// Created by wangxiaoming on 2019/2/28.
//

#include "file_util.h"
#include <fstream>
#include <iostream>

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


using namespace std;

namespace Face {

    /**
     * @param filepath
     * @return
     */
    int getFileLength(const char *filepath) {
        struct stat info{};
        int result = stat(filepath, &info);
        if (!result) {
            int size = info.st_size;
            return size;
        }
        return -1;
    }

    /**
     * @param filepath
     * @param users
     * @param len
     */
    bool saveUsers(const char *filepath, User *users, int len) {
        ofstream outfile(filepath, ios::binary);
        if (!outfile) {
            cerr << "open error!" << endl;
            return false;
        }

        for (int i = 0; i < len; i++) {
            User user = users[i];
            outfile.write((char *) &user, sizeof(user));
        }

        outfile.close();
        return true;
    }

    /**
     * @param filepath
     * @param users
     * @return
     */
    int readUsers(const char *filepath, User *users) {

        ifstream infile(filepath, ios::binary);
        if (!infile) {
            cerr << "open error!" << endl;
            return -1;
        }

        int size = getFileLength(filepath);
        const int len = size / sizeof(User);

        for (int i = 0; i < len; i++)
            infile.read((char *) &users[i], sizeof(users[i]));

        infile.close();

        return len;
    }

    /**
     * @param folderPath
     * @param files  bufferList
     * @param depth
     */
    void listFiles(const string &folderPath, list<string> *files, int depth) {
#ifdef _WIN32
        _finddata_t FileInfo{};
        string strfind = folderPath + "\\*";
        intptr_t Handle = _findfirst(strfind.c_str(), &FileInfo);

        if (Handle == -1L) {
            exit(-1);
        }
        do {
            string filename = (folderPath + "\\" + FileInfo.name);
            // has children
            if (FileInfo.attrib & _A_SUBDIR) {
                // curr dir & per dir
                if ((depth - 1 > 0) && (strcmp(FileInfo.name, ".") != 0) && (strcmp(FileInfo.name, "..") != 0)) {
                    files->push_back(filename);
                    listFiles(filename, files, depth - 1);
                }
            } else {
                files->push_back(filename);
            }
        } while (_findnext(Handle, &FileInfo) == 0);

        _findclose(Handle);
#else
        DIR *dir;
        struct dirent *entry;
        if ((dir = opendir(folderPath.c_str())) == NULL) {
            fprintf(stderr, "cannot open directory: %s\n", folderPath.c_str());
            return;
        }
        while ((entry = readdir(dir)) != NULL) {
            struct stat s{};
            string filename = (folderPath + "/" + entry->d_name);
            lstat(filename.c_str(), &s);
            cout << filename << endl;
            if (S_ISDIR(s.st_mode)) {

                if (strcmp(".", entry->d_name) == 0 ||
                    strcmp("..", entry->d_name) == 0)
                    continue;

                if (depth - 1 > 0) {
                    files->push_back(filename);
                    listFiles(entry->d_name, files, depth - 1);
                }
            } else {
                files->push_back(filename);
            }
        }
        closedir(dir);
#endif
    }

    /**
     * @param pszDir
     * @return
     */
    int createDir(const char *pszDir) {
        int iRet;
        //not exist
        iRet = ACCESS(pszDir, 0);
        if (iRet != 0) {
            iRet = MKDIR(pszDir);
            if (iRet != 0) {
                return -1;
            }
        }
        return 0;
    }
}