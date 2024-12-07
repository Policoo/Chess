#include <QApplication>
#include <Windows.h>
#include <iostream>

#include "src/gui/chess.h"

int main(int argc, char* argv[]) {

#ifdef _DEBUG
    AllocConsole();
    FILE* pConsole;
    freopen_s(&pConsole, "CONOUT$", "w", stdout);
    *stdout = *pConsole;
    setvbuf(stdout, nullptr, _IONBF, 0);
#endif

    QApplication a(argc, argv);
    Chess w;
    w.show();
    return QApplication::exec();
}