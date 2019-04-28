
#include头文件目录
include_directories(
        .
        ../include
        include
        ../sdk
        ${NCNN_DIR}/include
        ${OpenCV_INCLUDE_DIRS}
)
link_directories(../libs . ${NCNN_DIR}/lib ${OpenCV_DIR}/lib)

# 创建可执行文件
#add_executable(test1 test.cpp ../detect.cpp ../recognize.cpp)
add_executable(test1 main.cpp)

target_link_libraries(test1
        face_sdk
        ncnn
        ${OpenCV_LIBS}
        )


add_custom_command(TARGET test1 POST_BUILD        # Adds a post-build event to embedding
        COMMAND ${CMAKE_COMMAND} -E copy_directory         # which executes "cmake - E copy_if_different..."
        $<TARGET_FILE_DIR:face_sdk>                 # <--this is in-file
        $<TARGET_FILE_DIR:test1>)                 # <--this is out-file path