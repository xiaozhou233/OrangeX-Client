cmake_minimum_required(VERSION 3.15)

project(OrangeXNative LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_C_STANDARD 11)

# Output directories
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

# Source files
file(GLOB_RECURSE SOURCES CONFIGURE_DEPENDS
    "src/*.cpp"
    "src/*.c"
)

# Build shared library
add_library(OrangeXNative SHARED ${SOURCES})

# Include directories
target_include_directories(OrangeXNative PRIVATE
    includes/
    includes/jvm
    includes/jvm/win32
)

link_directories("${CMAKE_SOURCE_DIR}/lib")

# Windows DLL export
if(WIN32)
    set_target_properties(OrangeXNative PROPERTIES
        WINDOWS_EXPORT_ALL_SYMBOLS ON
    )
endif()