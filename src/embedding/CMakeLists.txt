
#include头文件目录
include_directories(
        ../include
        ../thirdparty/include
        ${OPENCV_INCLUDE}
)
link_directories(../libs)
# 项目cpp 文件
file(GLOB PROJECT_CPP ../detect.cpp ../recognize.cpp ../file_util.cpp embedding.cpp)
# 创建可执行文件
add_executable(embedding ${PROJECT_CPP})

target_link_libraries(
        embedding
        ncnn
        opencv_world
)


if (CMAKE_SYSTEM_NAME MATCHES "Linux")
    add_custom_command(TARGET embedding POST_BUILD        # Adds a post-build event to embedding
            COMMAND ${CMAKE_COMMAND} -E copy_if_different # which executes "cmake - E copy_if_different..."
            "${CMAKE_CURRENT_SOURCE_DIR}/../libs/libopencv_world.so"                 # <--this is in-file
            $<TARGET_FILE_DIR:embedding>)                 # <--this is out-file path
elseif (CMAKE_SYSTEM_NAME MATCHES "Windows")
    add_custom_command(TARGET embedding POST_BUILD        # Adds a post-build event to embedding
            COMMAND ${CMAKE_COMMAND} -E copy_if_different # which executes "cmake - E copy_if_different..."
            "${CMAKE_CURRENT_SOURCE_DIR}/../libs/opencv_world343.dll"                 # <--this is in-file
            $<TARGET_FILE_DIR:embedding>)                 # <--this is out-file path
endif (CMAKE_SYSTEM_NAME MATCHES "Linux")

