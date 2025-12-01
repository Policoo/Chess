#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QIcon>

#include "src/gui/controllers/boardController.h"
#include "src/gui/controllers/gameController.h"

int main(int argc, char* argv[]) {
    QGuiApplication app(argc, argv);
    QGuiApplication::setWindowIcon(QIcon(":/resources/icon.png"));

    QQmlApplicationEngine engine;

    // Expose controllers
    BoardController boardController;
    GameController gameController(&boardController);
    engine.rootContext()->setContextProperty(QStringLiteral("boardController"), &boardController);
    engine.rootContext()->setContextProperty(QStringLiteral("gameController"), &gameController);

    const QUrl url(QStringLiteral("qrc:/gui/MainWindow.qml"));

    QObject::connect(
        &engine,
        &QQmlApplicationEngine::objectCreated,
        &app,
        [url](QObject* obj, const QUrl& objUrl) {
            if (!obj && url == objUrl) {
                QCoreApplication::exit(-1);
            }
        },
        Qt::QueuedConnection);

    engine.load(url);

    return app.exec();
}
