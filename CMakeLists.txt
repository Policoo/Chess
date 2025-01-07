cmake_minimum_required(VERSION 3.29)
project(Chess)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_AUTOUIC ON)

if (NOT DEFINED ENV{QT6_MSVC})
    message(FATAL_ERROR "Environment variable QT6_MSVC is not set. Please set it Qt/<version>/<compiler>.")
endif ()

set(Qt6_DIR $ENV{QT6_MSVC})
set(CMAKE_PREFIX_PATH ${Qt6_DIR})

find_package(Qt6 COMPONENTS
        Core
        Gui
        Widgets
        REQUIRED)

qt_add_resources(RESOURCE_FILES
        resources/resources.qrc
)

add_executable(Chess main.cpp
        # GUI
        src/gui/chess.cpp
        src/gui/dialogWidget.cpp
        src/gui/dialogWidget.h
        src/gui/engineWorker.cpp
        src/gui/engineWorker.h
        src/gui/gameWidget.cpp
        src/gui/gameWidget.h
        src/gui/optionsWidget.cpp
        src/gui/optionsWidget.h

        # Game
        src/board.cpp
        src/counter.cpp
        src/counter.h
        src/engine.h
        src/move.cpp
        src/move.h
        src/moveGenerator.cpp
        src/moveGenerator.h
        src/piece.cpp
        src/piece.h
        src/util/precomputedGameData.cpp
        src/util/precomputedGameData.h
        src/util/utils.cpp
        src/util/utils.h
        src/util/zobrist.cpp
        src/util/zobrist.h
)

target_sources(Chess PRIVATE ${RESOURCE_FILES})
target_include_directories(Chess PRIVATE ${Qt6_DIR}/include)
target_link_libraries(Chess PRIVATE Qt6::Core Qt6::Gui Qt6::Widgets)

if (WIN32 AND NOT DEFINED CMAKE_TOOLCHAIN_FILE)
    set(DEBUG_SUFFIX)
    if (MSVC AND CMAKE_BUILD_TYPE MATCHES "Debug")
        set(DEBUG_SUFFIX "d")
    endif ()
    set(QT_INSTALL_PATH "${CMAKE_PREFIX_PATH}")
    if (NOT EXISTS "${QT_INSTALL_PATH}/bin")
        set(QT_INSTALL_PATH "${QT_INSTALL_PATH}/..")
        if (NOT EXISTS "${QT_INSTALL_PATH}/bin")
            set(QT_INSTALL_PATH "${QT_INSTALL_PATH}/..")
        endif ()
    endif ()
    if (EXISTS "${QT_INSTALL_PATH}/plugins/platforms/qwindows${DEBUG_SUFFIX}.dll")
        add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E make_directory
                "$<TARGET_FILE_DIR:${PROJECT_NAME}>/plugins/platforms/")
        add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy
                "${QT_INSTALL_PATH}/plugins/platforms/qwindows${DEBUG_SUFFIX}.dll"
                "$<TARGET_FILE_DIR:${PROJECT_NAME}>/plugins/platforms/")
    endif ()
    foreach (QT_LIB Core Gui Widgets)
        add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy
                "${QT_INSTALL_PATH}/bin/Qt6${QT_LIB}${DEBUG_SUFFIX}.dll"
                "$<TARGET_FILE_DIR:${PROJECT_NAME}>")
    endforeach (QT_LIB)
endif ()

# Default build type
if (NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE "Debug" CACHE STRING "Build type (Debug or Release)" FORCE)
endif ()