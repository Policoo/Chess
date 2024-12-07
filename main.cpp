#include <QApplication>
#include "src/gui/chess.h"

int main(int argc, char* argv[]) {
    QApplication a(argc, argv);
    QApplication::setWindowIcon(QIcon(":/resources/icon.png"));
    Chess w;
    w.show();
    return QApplication::exec();
}