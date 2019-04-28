

#include头文件目录
include_directories(
        ../include
        ${NCNN_DIR}/include
        ${OPENCV_INCLUDE}
)

link_directories(libs ${NCNN_DIR}/lib ${OpenCV_DIR}/lib)

if (BUILD_SDK)
    #编译为动态库
    add_library(face_sdk SHARED face_sdk.cpp
            ../detect.cpp
            ../recognize.cpp
            ../file_util.cpp
            ../socket.cpp)

    target_link_libraries(
            face_sdk
            ncnn
            ${Vulkan_LINK_LIBRARIES}
            ${OpenCV_LIBS}
    )
endif ()
if (BUILD_JNI)

    #[[ FindJNI
        Find JNI java libraries.

        This module finds if Java is installed and determines where the include files and libraries are. It also determines what the name of the library is. The caller may set variable JAVA_HOME to specify a Java installation prefix explicitly.

        This module sets the following result variables:

        JNI_INCLUDE_DIRS      = the include dirs to use
        JNI_LIBRARIES         = the libraries to use
        JNI_FOUND             = TRUE if JNI headers and libraries were found.
        JAVA_AWT_LIBRARY      = the path to the jawt library
        JAVA_JVM_LIBRARY      = the path to the jvm library
        JAVA_INCLUDE_PATH     = the include path to jni.h
        JAVA_INCLUDE_PATH2    = the include path to jni_md.h
        JAVA_AWT_INCLUDE_PATH = the include path to jawt.h ]]

    # 加入jni支持
    include_directories(${JNI_INCLUDE_DIRS})

    #编译为动态库
    add_library(face_sdk_jni SHARED face_sdk_jni.cpp
            ../detect.cpp
            ../recognize.cpp
            ../file_util.cpp
            ../socket.cpp)

    target_link_libraries(
            face_sdk_jni
            ncnn
            ${Vulkan_LINK_LIBRARIES}
            ${OpenCV_LIBS}
    )

endif ()