//
// Created by wangxiaoming on 2019/2/28.
//

#ifndef FACE_USER_H
#define FACE_USER_H

#include<string.h>
#include<string>

namespace Face {

    enum Sex {
        MAN = (char) 1,
        WOMAN = (char) 0
    };

    struct User {
        char name[30]{};
        float embedding[128]{};

        explicit User(const char *name, float *embedding) {
            memcpy(this->name, name, sizeof(this->name));
            memcpy(this->embedding, embedding, sizeof(this->embedding));
        }


    };
}

#endif //FACE_USER_H
