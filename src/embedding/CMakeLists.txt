
#include头文件目录
include_directories(
        ../include
        ${NCNN_DIR}/include
        ${OPENCV_INCLUDE}
)
link_directories(../libs ${NCNN_DIR}/lib ${OpenCV_DIR}/lib)
# 项目cpp 文件
file(GLOB PROJECT_CPP ../detect.cpp ../recognize.cpp ../file_util.cpp ../socket.cpp embedding.cpp)
# 创建可执行文件
add_executable(embedding ${PROJECT_CPP})

target_link_libraries(
        embedding
        ncnn
        ${OpenCV_LIBS}
)

install(TARGETS
        embedding DESTINATION bin
        RUNTIME DESTINATION bin
        LIBRARY DESTINATION lib
        ARCHIVE DESTINATION lib)