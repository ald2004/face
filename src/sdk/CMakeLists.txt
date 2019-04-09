
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
if (NCNN_VULKAN)
    list(APPEND NCNN_EXAMPLE_LINK_LIBRARIES ${Vulkan_LIBRARY})
else ()
    set(NCNN_EXAMPLE_LINK_LIBRARIES  )
endif ()

#编译为动态库
add_library(face_sdk SHARED ${PROJECT_CPP})

target_link_libraries(
        face_sdk
        ncnn
        ${NCNN_EXAMPLE_LINK_LIBRARIES}
        ${OpenCV_LIBS}
)
