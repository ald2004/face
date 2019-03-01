//
// Created by wangxiaoming on 2019/2/28.
//

#include "file_util.h"

using namespace std;

namespace Face {

    /**
     * 获取文件大小
     * @param filepath
     * @return
     */
    int getFileLength(const char *filepath) {
        struct stat info{};
//    struct stat {
//        _dev_t     st_dev;        //文件所在磁盘驱动器号
//        _ino_t     st_ino;        //inode，FAT、NTFS文件系统无意义
//        unsigned short st_mode;   //文件、文件夹的标志
//        short      st_nlink;      //非NTFS系统上通常为1
//        short      st_uid;        //UNIX系统上为userid，windows上为0
//        short      st_gid;        //UNIX系统上为groupid，windows上为0
//        _dev_t     st_rdev;       //驱动器号，与st_dev相同
//        _off_t     st_size;       //文件字节数
//        time_t st_atime;          //上次访问时间
//        time_t st_mtime;          //上次修改时间
//        time_t st_ctime;          //创建时间
//    };
        int result = stat(filepath, &info);
        if (!result) {
            int size = info.st_size;
            return size;
        }
        return -1;
    }

    /**
     * 保存
     * @param filepath
     * @param users
     * @param len
     */
    void saveUsers(const char *filepath, User *users, int len) {
        ofstream outfile(filepath, ios::binary);
        if (!outfile) {
            cerr << "open error!" << endl;
            abort();//退出程序
        }

        for (int i = 0; i < len; i++) {
            User user = users[i];
            outfile.write((char *) &user, sizeof(user));
        }

        outfile.close();
    }

    /**
     * 读取对象
     * @param filepath
     * @param users
     * @return
     */
    int readUsers(const char *filepath, User *users) {

        ifstream infile(filepath, ios::binary);
        if (!infile) {
            cerr << "open error!" << endl;
            abort();//退出程序
        }

        int size = getFileLength(filepath);
        const int len = size / sizeof(User);

        for (int i = 0; i < len; i++)
            infile.read((char *) &users[i], sizeof(users[i]));

        infile.close();

        return len;
    }

    /**
     * 递归列出文件名
     * @param folderPath 搜索目录
     * @param files  bufferList
     * @param depth  搜索深度，默认0
     */
    void listFiles(const string &folderPath, list<string> *files, int depth) {
#ifdef _WIN32
        _finddata_t FileInfo{};
        string strfind = folderPath + "\\*";
        long Handle = _findfirst(strfind.c_str(), &FileInfo);

        if (Handle == -1L) {
            exit(-1);
        }
        do {
            string filename = (folderPath + "\\" + FileInfo.name);
            //判断是否有子目录
            if (FileInfo.attrib & _A_SUBDIR) {
                //这个语句很重要
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
        DIR *dp;
        struct dirent *entry;
        struct stat statbuf;
        if ((dp = opendir(folderPath.c_str())) == NULL) {
            fprintf(stderr, "cannot open directory: %s\n", folderPath.c_str());
            return;
        }
        chdir(folderPath.c_str());
        while ((entry = readdir(dp)) != NULL) {
            lstat(entry->d_name, &statbuf);
            string filename = (folderPath + "/" + entry->d_name);
            if (S_ISDIR(statbuf.st_mode)) {

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
        chdir("..");
        closedir(dp);
#endif
    }

    /**
     * 创建 目录
     * @param pszDir
     * @return
     */
    int createDir(const char *pszDir) {
        int iRet;
        //如果不存在,创建
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