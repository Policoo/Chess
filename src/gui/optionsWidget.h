#pragma once

#include <QWidget>
#include <QLineEdit>
#include <QComboBox>
#include <QPushButton>

class OptionsWidget : public QWidget {
    Q_OBJECT

public:
    explicit OptionsWidget(QWidget* parent);

signals:
    void resetBoardSignal();

    void debugModeSignal();

    void undoMoveSignal();

    void flipBoardSignal();

    void makeBoardFromFenSignal(std::string fenString);

    void goPerftSignal(std::string depth);

    void setOpponentSignal(std::string opponent);

    void startEngineMatchSignal(std::string engine1, std::string engine2);

private:
    std::vector<QString> availableEngines;

    QLayout* optionsLayout;

    QPushButton* debugButton;

    QLineEdit* fenInput;
    QLineEdit* depthInput;

    QComboBox* opponentChoice;
    QComboBox* engine1;
    QComboBox* engine2;

    void constructTopButtons();

    void constructFenWidget();

    void constructPerftWidget();

    void constructOpponentWidget();

    void constructMatchWidget();

    void constructEvalWidget();

    void debugPressed();

    void makeBoardFromFen();

    void goPerft();

    void setOpponent();

    void startEngineMatch();
};
