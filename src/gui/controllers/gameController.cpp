#include "gameController.h"

#include <QThread>
#include <QMetaObject>
#include <future>
#include <tuple>
#include <thread>

#include "boardController.h"
#include "../engineWorker.h"
#include "../../board.h"
#include "../../move.h"
#include "../../piece.h"

GameController::GameController(BoardController* boardController, QObject* parent) :
    QObject(parent),
    boardController(boardController),
    opponent(nullptr) {
    // React to board changes so the engine can reply automatically
    connect(boardController, &BoardController::boardChanged,
            this, &GameController::maybeMakeEngineMove);
}

void GameController::goPerft(int depth) {
    if (!boardController || depth <= 0) {
        return;
    }

    auto* engineWorker = new EngineWorker();
    auto* thread = new QThread(this);

    engineWorker->moveToThread(thread);

    connect(thread, &QThread::started, this, [engineWorker, this, depth]() {
        engineWorker->goPerft(boardController->boardRef(), depth);
    });

    connect(engineWorker, &EngineWorker::perftDone, thread, &QThread::quit);
    connect(engineWorker, &EngineWorker::perftDone, engineWorker, &EngineWorker::deleteLater);
    connect(thread, &QThread::finished, thread, &QThread::deleteLater);

    connect(engineWorker, &EngineWorker::perftDone, this,
            [this](const std::vector<std::string>& results) {
                QVariantList list;
                list.reserve(static_cast<int>(results.size()));
                for (const auto& s : results) {
                    list.append(QString::fromStdString(s));
                }
                emit perftResultsReady(list);
            });

    thread->start();
}

void GameController::setOpponent(int index) {
    if (!boardController) {
        return;
    }

    if (index <= 0) {
        opponent = nullptr;
        return;
    }

    const int engineIndex = index - 1;
    if (engineIndex < 0 || engineIndex >= static_cast<int>(engines.size())) {
        opponent = nullptr;
        return;
    }

    opponent = engines[engineIndex].get();

    // In case it's currently the engine's turn, let it move immediately.
    maybeMakeEngineMove();
}

void GameController::undo() {
    if (!boardController) {
        return;
    }

    boardController->undoMove();

    // If an opponent engine is active, undo one more move so that undo
    // returns the position to the player's previous turn.
    if (opponent) {
        boardController->undoMove();
    }
}

void GameController::maybeMakeEngineMove() {
    if (!boardController || !opponent) {
        return;
    }

    Board& board = boardController->boardRef();

    const int perspectiveColor = boardController->whitePerspective()
        ? Piece::WHITE
        : Piece::BLACK;

    // If it's the player's turn, do nothing.
    if (board.getTurn() == perspectiveColor) {
        return;
    }

    Move engineMove = opponent->bestMove(board, 0);
    boardController->applyMove(engineMove);
}

namespace {
static std::tuple<int,int,int>
    playChunk(int start,
            int end,
            Engine* whiteEngine,
            Engine* blackEngine,
            int timePerMove)
    {
        int w = 0, b = 0, d = 0;
        for (int i = start; i < end; ++i) {
            Board board;
            bool whiteToMove = (i % 2 == 0);
            while (!board.isGameOver()) {
                Engine* cur = whiteToMove ? whiteEngine : blackEngine;
                Move m = cur->bestMove(board, timePerMove);
                board.makeMove(m);
                whiteToMove = !whiteToMove;
            }
            if (board.isCheck()) {
                if (std::abs(board.getTurn()) == Piece::WHITE) ++w;
                else                                           ++b;
            } else {
                ++d;
            }
        }
        return { w, b, d };
    }
}

void GameController::startEngineMatch(const QString& e1, const QString& e2) {
    const std::string engine1 = e1.toStdString();
    const std::string engine2 = e2.toStdString();

    std::thread([this, engine1, engine2]() {
        Engine* whiteEngine = nullptr;
        Engine* blackEngine = nullptr;
        for (auto& up : engines) {
            if (up->name() == engine1) whiteEngine = up.get();
            if (up->name() == engine2) blackEngine = up.get();
        }
        if (!whiteEngine || !blackEngine) {
            return;
        }

        const int nrGames     = 1000;
        const int timePerMove = 100;

        unsigned nThreads = std::thread::hardware_concurrency();
        if (nThreads < 1) nThreads = 1;

        int chunkSize = nrGames / static_cast<int>(nThreads);
        int leftover  = nrGames % static_cast<int>(nThreads);

        std::vector<std::future<std::tuple<int,int,int>>> futures;
        int start = 0;
        for (unsigned t = 0; t < nThreads; ++t) {
            int end = start + chunkSize + (t < static_cast<unsigned>(leftover) ? 1 : 0);
            futures.push_back(
                std::async(std::launch::async,
                           playChunk, start, end,
                           whiteEngine, blackEngine,
                           timePerMove));
            start = end;
        }

        int whiteWins = 0, blackWins = 0, draws = 0;
        for (auto& f : futures) {
            auto [w, b, d] = f.get();
            whiteWins += w;
            blackWins += b;
            draws     += d;
        }

        std::string report =
            "Match complete:\n"
            "  " + engine1 + " (as White) wins: " + std::to_string(whiteWins) + "\n"
            "  " + engine2 + " (as Black) wins: " + std::to_string(blackWins) + "\n"
            "  draws: "                         + std::to_string(draws)      + "\n";

        const QString qReport = QString::fromStdString(report);
        QMetaObject::invokeMethod(this, [this, qReport]() {
            emit engineMatchResultsReady(qReport);
        });
    }).detach();
}

