cmake_minimum_required(VERSION 3.6)
project(face)
set(CMAKE_CXX_STANDARD 11)
set(OPENCV_DIR "$ENV{OPENCV_DIR}" CACHE PATH "OpenCV INSTALL PATH")
set(NCNN_DIR "$ENV{NCNN_DIR}" CACHE PATH "NCNN INSTALL PATH")

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
option(BUILD_FACE "BUILD_FACE" ON)
option(BUILD_EMBEDDING "BUILD_EMBEDDING" ON)
option(BUILD_TEST "BUILD_TEST" ON)

# Find OpenCV, you may need to set OpenCV_DIR variable
# to the absolute path to the directory containing OpenCVConfig.cmake file
# via the command line or GUI
find_package(OpenCV REQUIRED)

if (OpenCV_FOUND)
    # If the package has been found, several variables will
    # be set, you can find the full list with descriptions
    # in the OpenCVConfig.cmake file.
    # Print some message showing some of them
    message(STATUS "OpenCV library status:")
    message(STATUS "    version: ${OpenCV_VERSION}")
    message(STATUS "    libraries: ${OpenCV_LIBS}")
    message(STATUS "    arch: ${OpenCV_ARCH}")
    message(STATUS "    opencv dir: ${OpenCV_DIR}")
    message(STATUS "    opencv lib path: ${OpenCV_LIB_PATH}")
    message(STATUS "    opencv install path: ${OpenCV_INSTALL_PATH}")
    message(STATUS "    include path: ${OpenCV_INCLUDE_DIRS}")
else ()
    check_env(OPENCV_DIR)
endif ()

if (OPEN_OPENMP)
    FIND_PACKAGE(OpenMP REQUIRED)
    if (OpenMP_FOUND)
        message(STATUS "OPENMP FOUND")
        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${OpenMP_CXX_FLAGS}")
        set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} ${OpenMP_EXE_LINKER_FLAGS}")
    endif ()
endif ()


MESSAGE(STATUS "operation system is ${CMAKE_SYSTEM}")
if (CMAKE_SYSTEM_NAME MATCHES "Linux")
    MESSAGE(STATUS "current platform: Linux ")
elseif (CMAKE_SYSTEM_NAME MATCHES "Windows")
    MESSAGE(STATUS "current platform: Windows")
elseif (CMAKE_SYSTEM_NAME MATCHES "FreeBSD")
    MESSAGE(STATUS "current platform: FreeBSD")
else ()
    MESSAGE(STATUS "other platform: ${CMAKE_SYSTEM_NAME}")
endif (CMAKE_SYSTEM_NAME MATCHES "Linux")

# file encoding utf-8
IF (WIN32)
    MESSAGE(STATUS "Now is windows")
    add_compile_options("/source-charset:utf-8")
ELSEIF (APPLE)
    MESSAGE(STATUS "Now is Apple systens.")
#    add_compile_options(-std=c++11)
    add_compile_options("-finput-charset=utf-8")
    add_compile_options("-fexec-charset=utf-8")
ELSEIF (UNIX)
    MESSAGE(STATUS "Now is UNIX-like OS's. Including aPPLE os x  and CygWin")
#    SET(CMAKE_CXX_FLAGS "-std=c++11 -O3")
    add_compile_options("-finput-charset=utf-8")
    add_compile_options("-fexec-charset=utf-8")
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
if (BUILD_TEST)
    add_subdirectory(src/test)
endif ()

