#pragma once

#include <QtWidgets/QMainWindow>
#include <vector>
#include <string>

#include "gameWidget.h"
#include "optionsWidget.h"
#include "dialogWidget.h"

class Chess : public QMainWindow {
    Q_OBJECT

public:
    explicit Chess(QWidget *parent = nullptr);

public slots:
    void resetBoard();

    void debugMode();

    void undoMove();

    void flipBoard();

    void moveMade();

    void makeBoardFromFen(const std::string &fenString);

    void goPerft(const std::string &depth);

    void perftResultsReady(const std::vector<std::string> results) const;

    void setOpponent(std::string opponentChoice);

    void startEngineMatch(std::string engine1, std::string engine2);

    void showEngineMatchResults(std::string results) const;

private:
    QWidget *mainWidget;
    QLayout *layout;

    OptionsWidget *optionsWidget;
    GameWidget *boardWidget;
    DialogWidget *dialogWidget;

    bool debug;
};
