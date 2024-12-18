#pragma once

#include <QObject>
#include <string>
#include <vector>

#include "../counter.h"
#include "../move.h"
#include "../board.h"

class EngineWorker : public QObject {
    Q_OBJECT

public:
    EngineWorker(QObject* = nullptr);

    void bestMove(Board& board);

    void setEngine(std::string engine);

public slots:
    void goPerft(Board& board, int depth);

signals:
    void perftDone(std::vector<std::string> counterResults);

    void moveReady(Move move);
};
