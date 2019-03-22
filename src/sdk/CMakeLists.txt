
# 项目cpp 文件
file(GLOB PROJECT_CPP
        face_sdk.cpp
        ../detect.cpp
        ../recognize.cpp
        ../file_util.cpp
        ../socket.cpp
        )
#include头文件目录
include_directories(
        ../include
        ${NCNN_DIR}/include
        ${OPENCV_INCLUDE}
)

link_directories(libs ${NCNN_DIR}/lib ${OpenCV_DIR}/lib)


#编译为动态库
add_library(face_sdk SHARED ${PROJECT_CPP})

target_link_libraries(
        face_sdk
        ncnn
        ${OpenCV_LIBS}
)
