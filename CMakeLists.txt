cmake_minimum_required(VERSION 3.29)
project(Chess LANGUAGES CXX)

# Toolchain / Standards

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)
set(CMAKE_AUTOUIC ON)

# Locate Qt 6

if (WIN32)
    if (NOT DEFINED ENV{QT6_MSVC})
        message(FATAL_ERROR "Environment variable QT6_MSVC is not set. Please set it to the root of your Qt installation, e.g. C:/Qt/6.7.0/msvc2019_64.")
    endif ()
    set(Qt6_DIR $ENV{QT6_MSVC})
    list(PREPEND CMAKE_PREFIX_PATH "${Qt6_DIR}")
elseif (APPLE)
    # Try the usual Homebrew install locations (Apple‑Silicon & Intel)
    foreach(qt_prefix IN ITEMS "/opt/homebrew/opt/qt@6" "/opt/homebrew/opt/qt" "/usr/local/opt/qt@6" "/usr/local/opt/qt")
        if (EXISTS "${qt_prefix}/lib/cmake")
            list(APPEND CMAKE_PREFIX_PATH "${qt_prefix}/lib/cmake")
        endif()
    endforeach()
endif()

find_package(Qt6 COMPONENTS Core Gui Widgets REQUIRED)

qt_add_resources(RESOURCE_FILES
    resources/resources.qrc
)

# ----------------------------------------------------------------------------
# Sources & Target
# ----------------------------------------------------------------------------
add_executable(Chess
    main.cpp

    # GUI
    src/gui/chess.cpp
    src/gui/dialogWidget.cpp src/gui/dialogWidget.h
    src/gui/engineWorker.cpp src/gui/engineWorker.h
    src/gui/gameWidget.cpp   src/gui/gameWidget.h
    src/gui/optionsWidget.cpp src/gui/optionsWidget.h
    src/gui/squareGridLayout.h

    # Game‑logic
    src/board.cpp
    src/counter.cpp          src/counter.h
    src/engine.h
    src/move.cpp             src/move.h
    src/moveGenerator.cpp    src/moveGenerator.h
    src/piece.cpp            src/piece.h
    src/util/precomputedGameData.cpp src/util/precomputedGameData.h
    src/util/utils.cpp       src/util/utils.h
    src/util/zobrist.cpp     src/util/zobrist.h

    ${RESOURCE_FILES}
)

target_link_libraries(Chess PRIVATE Qt6::Core Qt6::Gui Qt6::Widgets)

# Windows‑only: copy the DLLs + platform plugin next to the executable so the
# app starts without windeployqt.
if (WIN32 AND NOT DEFINED CMAKE_TOOLCHAIN_FILE)
    set(DEBUG_SUFFIX "")
    if (MSVC AND CMAKE_BUILD_TYPE MATCHES "Debug")
        set(DEBUG_SUFFIX "d")
    endif ()

    # Resolve Qt install path (handles the case where CMAKE_PREFIX_PATH points
    # directly at the "lib/cmake" folder).
    set(QT_INSTALL_PATH "${CMAKE_PREFIX_PATH}")
    if (NOT EXISTS "${QT_INSTALL_PATH}/bin")
        set(QT_INSTALL_PATH "${QT_INSTALL_PATH}/..")
        if (NOT EXISTS "${QT_INSTALL_PATH}/bin")
            set(QT_INSTALL_PATH "${QT_INSTALL_PATH}/..")
        endif ()
    endif ()

    # Platform plugin
    if (EXISTS "${QT_INSTALL_PATH}/plugins/platforms/qwindows${DEBUG_SUFFIX}.dll")
        add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E make_directory "$<TARGET_FILE_DIR:${PROJECT_NAME}>/plugins/platforms/")
        add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E copy
            "${QT_INSTALL_PATH}/plugins/platforms/qwindows${DEBUG_SUFFIX}.dll"
            "$<TARGET_FILE_DIR:${PROJECT_NAME}>/plugins/platforms/")
    endif ()

    # Core Qt runtime libraries we need at runtime
    foreach(QT_LIB Core Gui Widgets)
        add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
            COMMAND ${CMAKE_COMMAND} -E copy
            "${QT_INSTALL_PATH}/bin/Qt6${QT_LIB}${DEBUG_SUFFIX}.dll"
            "$<TARGET_FILE_DIR:${PROJECT_NAME}>")
    endforeach()
endif()
