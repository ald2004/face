# README 
## 依赖
* OPENCV
* NCNN
## NCNN BUILD Windows
```shell
cmake -G"NMake Makefiles" -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=%cd%/install ^
    -DProtobuf_INCLUDE_DIR=D:/ncnn/protobuf/build-vs2015/install/include ^
    -DProtobuf_LIBRARIES=D:/ncnn/protobuf/build-vs2015/install/lib/libprotobuf.lib ^
    -DProtobuf_PROTOC_EXECUTABLE=D:/ncnn/protobuf/build-vs2015/install/bin/protoc.exe ..
```

## BUILD Windows
```shell
mkdir build
cd build
cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=Release ^
    -DCMAKE_INSTALL_PREFIX=./install ^
    -DOPENCV_DIR=D:/projects/cpp/opencv-3.4.3/build_nmake/install ^
    -DNCNN_DIR=D:/ncnn/build-win32/install  ..
```