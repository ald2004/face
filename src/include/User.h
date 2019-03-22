//
// Created by wangxiaoming on 2019/2/28.
//

#ifndef FACE_USER_H
#define FACE_USER_H

#include "string"
#include "string.h"

namespace Face {

    struct User {
        char id[32]{};
        char name[64]{};
        float embedding[128]{};

        explicit User(const char *id, const char *name, float *embedding) {
            memcpy(this->id, id, sizeof(this->id));
            memcpy(this->name, name, sizeof(this->name));
            memcpy(this->embedding, embedding, sizeof(this->embedding));
        }
    };
}

#endif //FACE_USER_H
