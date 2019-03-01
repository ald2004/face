cmake_minimum_required(VERSION 3.6)
project(face)
set(CMAKE_CXX_STANDARD 11)

option(OPEN_OPENMP "OpenMP ON" ON)

option(BUILD_FACE "BUILD_FACE" ON)
option(BUILD_EMBEDDING "BUILD_EMBEDDING" ON)

if (OPEN_OPENMP)
    FIND_PACKAGE(OpenMP REQUIRED)
    if (OPENMP_FOUND)
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


set(OPENCV_INCLUDE "OPENCV_INCLUDE" CACHE PATH "opencv path")

#if (NOT DEFINED CMAKE_INSTALL_PREFIX)
#    set(CMAKE_INSTALL_PREFIX "${CMAKE_BINARY_DIR}/install" CACHE PATH "Installation Directory")
#endif ()

if (BUILD_FACE)
    add_subdirectory(src)
endif ()
