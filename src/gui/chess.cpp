#include "chess.h"

#include <QThread>
#include <iostream>
#include <QHBoxLayout>

Chess::Chess(QWidget* parent) :
    QMainWindow(parent),
    debug(false) {
    mainWidget = new QWidget(this);
    mainWidget->setStyleSheet("background-color: black;");
    mainWidget->setFixedSize(1154, 552);

    setCentralWidget(mainWidget);
    layout = new QHBoxLayout(mainWidget);
    layout->setSpacing(1);
    layout->setContentsMargins(0, 0, 0, 0);

    optionsWidget = new OptionsWidget(this);
    connect(optionsWidget, &OptionsWidget::resetBoardSignal, this, &Chess::resetBoard);
    connect(optionsWidget, &OptionsWidget::debugModeSignal, this, &Chess::debugMode);
    connect(optionsWidget, &OptionsWidget::undoMoveSignal, this, &Chess::undoMove);
    connect(optionsWidget, &OptionsWidget::flipBoardSignal, this, &Chess::flipBoard);
    connect(optionsWidget, &OptionsWidget::makeBoardFromFenSignal, this, &Chess::makeBoardFromFen);
    connect(optionsWidget, &OptionsWidget::goPerftSignal, this, &Chess::goPerft);
    connect(optionsWidget, &OptionsWidget::setOpponentSignal, this, &Chess::setOpponent);
    connect(optionsWidget, &OptionsWidget::startEngineMatchSignal, this, &Chess::startEngineMatch);

    boardWidget = new GameWidget(this);
    connect(boardWidget, &GameWidget::moveMadeSignal, this, &Chess::moveMade);
    connect(boardWidget, &GameWidget::perftResultsReady, this, &Chess::perftResultsReady);
    connect(boardWidget, &GameWidget::engineMatchResultsReady, this, &Chess::showEngineMatchResults);

    dialogWidget = new DialogWidget(this);

    layout->addWidget(optionsWidget);
    layout->addWidget(boardWidget);
    layout->addWidget(dialogWidget);
}

void Chess::resetBoard() {
    boardWidget->resetBoard();
    if (debug) {
        const std::string debugString = boardWidget->getDebugString();
        dialogWidget->displayDebugString(debugString);
    }
    else {
        dialogWidget->displayMessage("Board has been reset!");
    }
}

void Chess::debugMode() {
    debug = !debug;
    if (debug) {
        const std::string debugString = boardWidget->getDebugString();
        dialogWidget->displayDebugString(debugString);
    }
    else {
        dialogWidget->displayMessage("Debug mode off!");
    }

}

void Chess::undoMove() {
    boardWidget->undoMove();
    if (debug) {
        std::string debugString = boardWidget->getDebugString();
        dialogWidget->displayDebugString(debugString);
    }
}

void Chess::flipBoard() {
    boardWidget->flipBoard();

    if (!debug) {
        dialogWidget->displayMessage("You are now playing as " + boardWidget->getPerspective() + "!");
    }
}

void Chess::moveMade() {
    if (debug) {
        const std::string debugString = boardWidget->getDebugString();
        dialogWidget->displayDebugString(debugString);
    }
}

void Chess::makeBoardFromFen(const std::string& fenString) {
    if (fenString.size() < 15) {
        dialogWidget->displayMessage("Invalid FEN string!");
        return;
    }

    boardWidget->makeBoardFromFen(fenString);
    if (debug) {
        const std::string debugString = boardWidget->getDebugString();
        dialogWidget->displayDebugString(debugString);
    }
}

void Chess::goPerft(const std::string& depth) {
    try {
        size_t pos;
        const int depthInt = std::stoi(depth, &pos);

        // Check if there are any non-numeric characters after the number
        if (pos != depth.size()) {
            std::cout << pos << " " << depth.size();
            dialogWidget->displayMessage("Depth input is not a number!");
            return;
        }

        boardWidget->goPerft(depthInt);
    } catch (const std::exception& e) {
        std::cerr << "Caught exception: " << e.what() << std::endl;
        dialogWidget->displayMessage("Depth input is not a number!");
    }
}

void Chess::perftResultsReady(const std::vector<std::string> results) const {
    dialogWidget->displayCountResults(results);
}


void Chess::setOpponent(std::string opponentChoice) {
    boardWidget->setOpponent(opponentChoice);
}

void Chess::startEngineMatch(std::string engine1, std::string engine2) {
    boardWidget->startEngineMatch(engine1, engine2);
}

void Chess::showEngineMatchResults(std::string results) const {
    dialogWidget->displayMessage(results);
}
