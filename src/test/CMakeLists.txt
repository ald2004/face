
#include头文件目录
include_directories(
        ../include
        include
        ${NCNN_DIR}/include
        ${OpenCV_INCLUDE_DIRS}
)
link_directories(libs ${NCNN_DIR}/lib)

# 项目cpp 文件
file(GLOB PROJECT_CPP test.cpp ../detect.cpp ../recognize.cpp ../file_util.cpp ../socket.cpp)
# 创建可执行文件
add_executable(test ${PROJECT_CPP})

target_link_libraries(test
        ncnn
        ${OpenCV_LIBS}
        )
