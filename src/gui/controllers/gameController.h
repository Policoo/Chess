#pragma once

#include <QObject>
#include <QVariantList>
#include <QString>

#include "../../engines/engine.h"
#include "../../engines/engineRegistry.h"

class BoardController;

class GameController : public QObject {
    Q_OBJECT

public:
    explicit GameController(BoardController* boardController, QObject* parent = nullptr);

public slots:
    Q_INVOKABLE void goPerft(int depth);

    Q_INVOKABLE void setOpponent(int index);

    Q_INVOKABLE void startEngineMatch(const QString& engine1, const QString& engine2);

    Q_INVOKABLE void undo();

signals:
    void perftResultsReady(QVariantList results);

    void engineMatchResultsReady(const QString& results);

private slots:
    void maybeMakeEngineMove();

private:
    BoardController* boardController;
    Engine* opponent;
};

