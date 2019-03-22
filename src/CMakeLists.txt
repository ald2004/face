
#include头文件目录
include_directories(
        include
        ${NCNN_DIR}/include
        ${OpenCV_INCLUDE_DIRS}
)
link_directories(libs ${NCNN_DIR}/lib ${OpenCV_DIR}/lib)
# 项目cpp 文件
file(GLOB PROJECT_CPP multivideoserver.cpp detect.cpp recognize.cpp file_util.cpp socket.cpp)
# 创建可执行文件
add_executable(face ${PROJECT_CPP})

target_link_libraries(face
        ncnn
        ${OpenCV_LIBS}
        )

add_custom_command(TARGET face POST_BUILD        # Adds a post-build event to embedding
        COMMAND ${CMAKE_COMMAND} -E copy_directory         # which executes "cmake - E copy_if_different..."
        "${CMAKE_CURRENT_SOURCE_DIR}/model"                 # <--this is in-file
        ${CMAKE_BINARY_DIR}/src/model)                 # <--this is out-file path

add_custom_command(TARGET face POST_BUILD        # Adds a post-build event to embedding
        COMMAND ${CMAKE_COMMAND} -E copy_directory         # which executes "cmake - E copy_if_different..."
        "${CMAKE_CURRENT_SOURCE_DIR}/../imgs"                 # <--this is in-file
        ${CMAKE_BINARY_DIR}/imgs)                 # <--this is out-file path

#[[
# copy static lib
if (CMAKE_SYSTEM_NAME MATCHES "Linux")
    add_custom_command(TARGET face POST_BUILD        # Adds a post-build event to embedding
            COMMAND ${CMAKE_COMMAND} -E copy_if_different # which executes "cmake - E copy_if_different..."
            "${OpenCV_LIB_PATH}/libopencv_world.so"                 # <--this is in-file
            $<TARGET_FILE_DIR:face>)                 # <--this is out-file path
elseif (CMAKE_SYSTEM_NAME MATCHES "Windows")
    add_custom_command(TARGET face POST_BUILD        # Adds a post-build event to embedding
            COMMAND ${CMAKE_COMMAND} -E copy_if_different # which executes "cmake - E copy_if_different..."
            "${OpenCV_LIB_PATH}/../bin/opencv_world${OpenCV_VERSION_MAJOR}${OpenCV_VERSION_MINOR}${OpenCV_VERSION_PATCH}.dll"                 # <--this is in-file
            $<TARGET_FILE_DIR:face>)                 # <--this is out-file path
    add_custom_command(TARGET face POST_BUILD        # Adds a post-build event to embedding
            COMMAND ${CMAKE_COMMAND} -E copy_if_different # which executes "cmake - E copy_if_different..."
            "${OpenCV_LIB_PATH}/../bin/opencv_ffmpeg${OpenCV_VERSION_MAJOR}${OpenCV_VERSION_MINOR}${OpenCV_VERSION_PATCH}.dll"                 # <--this is in-file
            $<TARGET_FILE_DIR:face>)                 # <--this is out-file path
endif (CMAKE_SYSTEM_NAME MATCHES "Linux")


]]

#install(TARGETS face ARCHIVE DESTINATION "${CMAKE_CURRENT_SOURCE_DIR}/model")

install(TARGETS
        face DESTINATION bin
        RUNTIME DESTINATION bin
        LIBRARY DESTINATION lib
        ARCHIVE DESTINATION lib)

install(DIRECTORY model DESTINATION ''
        PATTERN "*.bin|*.param"
        PERMISSIONS OWNER_READ GROUP_READ
        )

install(FILES
        include/detect.h
        include/file_util.h
        include/recognize.h
        include/User.h
        DESTINATION include)