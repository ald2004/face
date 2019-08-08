cmake_minimum_required(VERSION 3.1.3)
project(face)
set(CMAKE_CXX_STANDARD 11)
set(OPENCV_DIR "$ENV{OPENCV_DIR}" CACHE PATH "OpenCV INSTALL PATH")
set(NCNN_DIR "$ENV{NCNN_DIR}" CACHE PATH "NCNN INSTALL PATH")
set(CMAKE_INSTALL_PREFIX "$ENV{PWD}")

MACRO(status ENV)
    message(STATUS "${ENV}")
ENDMACRO()
MACRO(check_env ENV)
    string(TOUPPER ${ENV} name)
    if (${name})
        message(STATUS "[${name}]: ${${name}}")
    else ()
        message(FATAL_ERROR "not defined environment variable:${name} or -D${name}")
    endif ()
ENDMACRO()

check_env(NCNN_DIR)

option(OPEN_OPENMP "OpenMP ON" ON)
option(BUILD_FACE "BUILD_FACE" OFF)
option(BUILD_EMBEDDING "BUILD_EMBEDDING" OFF)
option(BUILD_SDK "BUILD_SDK c/c++ (face_sdk.so | face_sdk.dll) " ON)
option(BUILD_JNI "BUILD_JNI c/c++ (face_sdk_jni.so | face_sdk_jni.dll) " ON)
option(BUILD_TEST "BUILD_TEST" ON)
option(NCNN_VULKAN "NCNN_VULKAN" ON)

if (NCNN_VULKAN)
    find_package(Vulkan REQUIRED)
    include_directories(${Vulkan_INCLUDE_DIR})
    list(APPEND Vulkan_LINK_LIBRARIES ${Vulkan_LIBRARY})
else ()
    set(Vulkan_LINK_LIBRARIES)
endif ()


# Find OpenCV, you may need to set OpenCV_DIR variable
# to the absolute path to the directory containing OpenCVConfig.cmake file
# via the command line or GUI
find_package(OpenCV REQUIRED)

if (OpenCV_FOUND)
    # If the package has been found, several variables will
    # be set, you can find the full list with descriptions
    # in the OpenCVConfig.cmake file.
    # Print some message showing some of them
    status("OpenCV library status:")
    status("    version: ${OpenCV_VERSION}")
    status("    libraries: ${OpenCV_LIBS}")
    status("    arch: ${OpenCV_ARCH}")
    status("    opencv dir: ${OpenCV_DIR}")
    status("    opencv lib path: ${OpenCV_LIB_PATH}")
    status("    opencv install path: ${OpenCV_INSTALL_PATH}")
    status("    include path: ${OpenCV_INCLUDE_DIRS}")
else ()
    check_env(OPENCV_DIR)
endif ()

# 通过 JAVA_HOME环境变量定位 jni.h和jni_md.h
if(NOT DEFINED ENV{JAVA_HOME})
    # 没有找到JAVA_HOME环境变量，输出错误信息退出
    message(FATAL_ERROR "not defined environment variable:JAVA_HOME")  
endif()
set(JNI_INCLUDE "$ENV{JAVA_HOME}/include")
include_directories(${JNI_INCLUDE})
IF(WIN32)
    #win32系统下`jni_md.h`位于`<jni_h_dir>/win32`下
    include_directories("${JNI_INCLUDE}/win32")
elseif(CMAKE_SYSTEM_NAME MATCHES "Linux")
    #linux系统下`jni_md.h`位于`<jni_h_dir>/linux`下
    include_directories("${JNI_INCLUDE}/linux")
else()
    # 不支持的操作系统报错退出
    message(FATAL_ERROR "other unsupported platform: ${CMAKE_SYSTEM_NAME}")  
endif(CMAKE_SYSTEM_NAME MATCHES "Windows")

if (BUILD_JNI)
    find_package(JNI REQUIRED)
    if (JNI_FOUND)
        include_directories(${JAVA_INCLUDE_PATH})
        include_directories(${JAVA_INCLUDE_PATH2})
    else ()
        find_package(JNI REQUIRED)
    endif ()
    if (JNI_FOUND)
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

        status("JNI library status:")
        status("    JNI_LIBRARIES: ${JNI_LIBRARIES}")
        status("    JAVA_AWT_LIBRARY: ${JAVA_AWT_LIBRARY}")
        status("    JAVA_JVM_LIBRARY: ${JAVA_JVM_LIBRARY}")
        status("    JNI_INCLUDE_DIRS: ${JNI_INCLUDE_DIRS}")
        status("    JAVA_INCLUDE_PATH: ${JAVA_INCLUDE_PATH}")
        status("    JAVA_INCLUDE_PATH2: ${JAVA_INCLUDE_PATH2}")
        status("    JAVA_AWT_INCLUDE_PATH: ${JAVA_AWT_INCLUDE_PATH}")
    else ()
        message(FATAL_ERROR "not defined environment variable:[ JAVA_HOME ]")
    endif ()
endif ()

if (OPEN_OPENMP)
    FIND_PACKAGE(OpenMP REQUIRED)
    if (OpenMP_FOUND)
        status("OPENMP FOUND")
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${OpenMP_CXX_FLAGS}")
        set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} ${OpenMP_EXE_LINKER_FLAGS}")
    endif ()
endif ()


status("operation system is ${CMAKE_SYSTEM}")
if (CMAKE_SYSTEM_NAME MATCHES "Linux")
    status("current platform: Linux ")
elseif (CMAKE_SYSTEM_NAME MATCHES "Windows")
    status("current platform: Windows")
elseif (CMAKE_SYSTEM_NAME MATCHES "FreeBSD")
    status("current platform: FreeBSD")
else ()
    status("other platform: ${CMAKE_SYSTEM_NAME}")
endif (CMAKE_SYSTEM_NAME MATCHES "Linux")


# file encoding utf-8
IF (WIN32)
    MESSAGE(STATUS "Now is windows")
    add_compile_options("/source-charset:utf-8")
    add_compile_options("/Ox")
    add_compile_options("/w")
    #    add_compile_options("/std:c++14")
    #    add_compile_options("/cgthreads8")
    add_compile_options("/MP")
    add_compile_options("/nologo")
    add_compile_options("/Gm-")

    add_definitions(-D_SCL_SECURE_NO_WARNINGS -D_CRT_SECURE_NO_DEPRECATE)
    add_definitions(-DNOMINMAX)
ELSE ()
    MESSAGE(STATUS "Now is UNIX-like OS's. Including aPPLE os x  and CygWin")
    #    SET(CMAKE_CXX_FLAGS "-std=c++11 -O3")
    add_compile_options("-finput-charset=utf-8")
    add_compile_options("-fexec-charset=utf-8")
    add_compile_options("-O3")
    #    add_definitions(-Wall -Wextra -Wno-unused-function -Wmissing-field-initializers)
    add_definitions(-fPIC)
    if (CMAKE_BUILD_TYPE MATCHES "(Release|RELEASE|release)")
        if (NOT CMAKE_SYSTEM_NAME STREQUAL "Emscripten")
            add_definitions(-Ofast)
        endif ()

        add_definitions(-ffast-math)
    endif ()

    if (PI3)
        add_definitions(-march=native)
        add_definitions(-mfpu=neon)
        add_definitions(-mfloat-abi=hard)
        add_definitions(-D__ARM_NEON)
        add_definitions(-D__ANDROID__)
    endif ()
    # add_definitions(-march=native)
    add_definitions(-flto)
    # add_definitions(-fvisibility=hidden -fvisibility-inlines-hidden)
ENDIF ()

if (NOT DEFINED CMAKE_INSTALL_PREFIX)
    set(CMAKE_INSTALL_PREFIX "${CMAKE_BINARY_DIR}/install" CACHE PATH "Installation Directory")
endif ()

if (BUILD_FACE)
    add_subdirectory(src)
endif ()
if (BUILD_EMBEDDING)
    add_subdirectory(src/embedding)
endif ()
if (BUILD_SDK OR BUILD_JNI)
    add_subdirectory(src/sdk)
endif ()
if (BUILD_TEST)
    add_subdirectory(src/test)
endif ()

install(FILES "/opt/face/imgs/embedding.dat" "/opt/face/build/src/test/test1"
	DESTINATION bin
	PERMISSIONS WORLD_EXECUTE
	)
