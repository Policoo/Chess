#include <QVBoxLayout>
#include <QLabel>
#include <iostream>

#include "optionsWidget.h"

// <--> INITIALIZATION <--> //

OptionsWidget::OptionsWidget(QWidget* parent) :
    QWidget(parent) {
    auto* widget = new QWidget(this);
    widget->resize(300, 552);
    widget->setStyleSheet("background-color: black;");
    resize(300, 552);

    optionsLayout = new QVBoxLayout(this);
    optionsLayout->setSpacing(1);
    optionsLayout->setContentsMargins(0, 0, 0, 0);
    optionsLayout->setAlignment(Qt::AlignTop);

    availableEngines = { "Random", "Greedy" };

    constructTopButtons();
    constructFenWidget();
    constructPerftWidget();
    constructOpponentWidget();
    constructMatchWidget();
    constructEvalWidget();
}

void OptionsWidget::constructTopButtons() {
    auto* widget = new QWidget(this);
    widget->setFixedSize(300, 40);
    widget->setStyleSheet("background-color: #3d3d3d;");

    QLayout* layout = new QHBoxLayout(widget);
    layout->setContentsMargins(5, 0, 5, 0);
    layout->setAlignment(Qt::AlignCenter);

    auto* reset = new QPushButton(widget);
    reset->setFixedSize(50, 27);
    reset->setText("Reset");
    reset->setStyleSheet(
            "QPushButton {"
            "   background-color: #6495ED;"
            "   color: black;"
            "   border: 2px solid black;"
            "   border-radius: 5px;"
            "   font-family: 'Arial', sans - serif;"
            "   font-size: 13px;"
            "   font-weight: bold;"
            "}"
            "QPushButton:hover {"
            "   background-color: #4169E1;"
            "}"
            "QPushButton:pressed {"
            "   background-color: #1E90FF;"
            "}");
    connect(reset, &QPushButton::clicked, this, &OptionsWidget::resetBoardSignal);
    layout->addWidget(reset);

    layout->addItem(new QSpacerItem(24, 20));

    debugButton = new QPushButton(widget);
    debugButton->setFixedSize(50, 27);
    debugButton->setText("Debug");
    debugButton->setStyleSheet(
            "QPushButton {"
            "   background-color: #6495ED;"
            "   color: black;"
            "   border: 2px solid black;"
            "   border-radius: 5px;"
            "   font-family: 'Arial', sans - serif;"
            "   font-size: 13px;"
            "   font-weight: bold;"
            "}"
            "QPushButton:hover {"
            "   background-color: #4169E1;"
            "}"
            "QPushButton:pressed {"
            "   background-color: #1E90FF;"
            "}");
    connect(debugButton, &QPushButton::clicked, this, &OptionsWidget::debugPressed);
    layout->addWidget(debugButton);

    layout->addItem(new QSpacerItem(24, 20));

    auto* undo = new QPushButton(widget);
    undo->setFixedSize(50, 27);
    undo->setText("Undo");
    undo->setStyleSheet(
            "QPushButton {"
            "   background-color: #6495ED;"
            "   color: black;"
            "   border: 2px solid black;"
            "   border-radius: 5px;"
            "   font-family: 'Arial', sans - serif;"
            "   font-size: 13px;"
            "   font-weight: bold;"
            "}"
            "QPushButton:hover {"
            "   background-color: #4169E1;"
            "}"
            "QPushButton:pressed {"
            "   background-color: #1E90FF;"
            "}");
    connect(undo, &QPushButton::clicked, this, &OptionsWidget::undoMoveSignal);
    layout->addWidget(undo);

    layout->addItem(new QSpacerItem(24, 20));

    auto* imageButton = new QPushButton(widget);
    imageButton->setFixedSize(50, 27);
    imageButton->setIcon(QIcon(":/resources/flip board.png"));
    imageButton->setIconSize(QSize(20, 23));
    imageButton->setStyleSheet(
            "QPushButton {"
            "   background-color: #6495ED;"
            "   border: 2px solid black;"
            "   border-radius: 5px;"
            "}"
            "QPushButton:hover {"
            "   background-color: #4169E1;"
            "}"
            "QPushButton:pressed {"
            "   background-color: #1E90FF;"
            "}");
    connect(imageButton, &QPushButton::clicked, this, &OptionsWidget::flipBoardSignal);
    layout->addWidget(imageButton);

    optionsLayout->addWidget(widget);
}

void OptionsWidget::constructFenWidget() {
    auto* widget = new QWidget(this);
    widget->setFixedSize(300, 90);
    widget->setStyleSheet("background-color: #3d3d3d;");

    QLayout* layout = new QVBoxLayout(widget);

    auto* label = new QLabel(widget);
    label->setText("Make board from FEN string");
    label->setStyleSheet(
            "color: white;"
            "font-weight: bold;"
            "font-family: 'Arial', sans - serif;"
            "font-size: 13px;"
            );
    label->setAlignment(Qt::AlignCenter);
    layout->addWidget(label);

    fenInput = new QLineEdit(widget);
    fenInput->setFixedSize(280, 24);
    fenInput->setStyleSheet(
            "QLineEdit {"
            "   background-color: #565656;"
            "   border: 2px solid #1f1f1f;"
            "   border-radius: 3px;"
            "   color: white;"
            "   padding: 1px;"
            "}"
            "QLineEdit:focus {"
            "   border: 2px solid #00A2E8;"
            "}"
            );
    layout->addWidget(fenInput);

    auto* confirmButton = new QPushButton(widget);
    confirmButton->setFixedHeight(25);
    confirmButton->setText("Confirm");
    confirmButton->setStyleSheet(
            "QPushButton {"
            "   background-color: #6495ED;"
            "   color: black;"
            "   border: 2px solid black;"
            "   border-radius: 5px;"
            "   font-family: 'Arial', sans - serif;"
            "   font-size: 13px;"
            "   font-weight: bold;"
            "}"
            "QPushButton:hover {"
            "   background-color: #4169E1;"
            "}"
            "QPushButton:pressed {"
            "   background-color: #1E90FF;"
            "}");
    connect(confirmButton, &QPushButton::clicked, this, &OptionsWidget::makeBoardFromFen);
    layout->addWidget(confirmButton);

    optionsLayout->addWidget(widget);
}

void OptionsWidget::constructPerftWidget() {
    auto* widget = new QWidget(this);
    widget->setFixedSize(299, 40);
    widget->setStyleSheet("background-color: #3d3d3d;");

    QLayout* layout = new QHBoxLayout(widget);

    auto* label = new QLabel(widget);
    label->setText("Depth:");
    label->setStyleSheet(
            "color: white;"
            "font-weight: bold;"
            "font-family: 'Arial', sans - serif;"
            "font-size: 13px;"
            );
    layout->addWidget(label);

    depthInput = new QLineEdit(widget);
    depthInput->setFixedSize(25, 24);
    depthInput->setStyleSheet(
            "QLineEdit {"
            "   background-color: #565656;"
            "   border: 2px solid #1f1f1f;"
            "   border-radius: 3px;"
            "   color: white;"
            "   padding: 1px;"
            "}"
            "QLineEdit:focus {"
            "   border: 2px solid #00A2E8;"
            "}"
            );
    layout->addWidget(depthInput);

    auto* goPerftButton = new QPushButton("Go Perft", widget);
    goPerftButton->setFixedSize(70, 25);
    goPerftButton->setStyleSheet(
            "QPushButton {"
            "   background-color: #6495ED;"
            "   color: black;"
            "   border: 2px solid #1f1f1f;"
            "   border-radius: 5px;"
            "   font-family: 'Arial', sans - serif;"
            "   font-size: 13px;"
            "   font-weight: bold;"
            "}"
            "QPushButton:hover {"
            "   background-color: #4169E1;"
            "}"
            "QPushButton:pressed {"
            "   background-color: #1E90FF;"
            "}");
    connect(goPerftButton, &QPushButton::clicked, this, &OptionsWidget::goPerft);
    layout->addWidget(goPerftButton);

    layout->addItem(new QSpacerItem(130, 40));

    optionsLayout->addWidget(widget);
}

void OptionsWidget::constructOpponentWidget() {
    auto* widget = new QWidget(this);
    widget->setFixedSize(300, 40);
    widget->setStyleSheet("background-color: #3d3d3d;");

    QLayout* layout = new QHBoxLayout(widget);

    auto* label = new QLabel("Opponent: ", widget);
    label->setStyleSheet(
            "color: white;"
            "font-weight: bold;"
            "font-family: 'Arial', sans - serif;"
            "font-size: 13px;"
            );
    layout->addWidget(label);

    opponentChoice = new QComboBox(widget);
    opponentChoice->setStyleSheet(
            "QComboBox {"
            "   background-color: #565656;"
            "   border: 2px solid #1f1f1f;"
            "   border-radius: 3px;"
            "   color: white;"
            "   padding: 3px;"
            "}"
            "QComboBox:focus {"
            "   border: 2px solid #00A2E8;"
            "}"
            "QComboBox::drop-down {"
            "   subcontrol-origin: padding;"
            "   subcontrol-position: top right;"
            "   width: 20px;"
            "   border-left-width: 1px;"
            "   border-left-color: darkgray;"
            "   border-left-style: solid;"
            "}"
            );

    opponentChoice->addItem("Yourself :(");
    for (const auto& option: availableEngines) {
        opponentChoice->addItem(option);
    }
    connect(opponentChoice, &QComboBox::currentIndexChanged, this, &OptionsWidget::setOpponent);
    layout->addWidget(opponentChoice);

    optionsLayout->addWidget(widget);
}

void OptionsWidget::constructMatchWidget() {
    auto* widget = new QWidget(this);
    widget->setFixedSize(300, 90);
    widget->setStyleSheet("background-color: #3d3d3d;");

    QLayout* layout = new QVBoxLayout(widget);

    auto* titleLabel = new QLabel("Set up engine battle", widget);
    titleLabel->setStyleSheet(
            "color: white;"
            "font-weight: bold;"
            "font-size: 14px;"
            );
    titleLabel->setAlignment(Qt::AlignHCenter);
    layout->addWidget(titleLabel);

    auto* horizontalLayout = new QHBoxLayout;

    engine1 = new QComboBox(widget);
    engine1->setStyleSheet(
            "QComboBox {"
            "   background-color: #565656;"
            "   border: 2px solid #1f1f1f;"
            "   border-radius: 3px;"
            "   color: white;"
            "   padding: 3px;"
            "}"
            "QComboBox:focus {"
            "   border: 2px solid #00A2E8;"
            "}"
            "QComboBox::drop-down {"
            "   subcontrol-origin: padding;"
            "   subcontrol-position: top right;"
            "   width: 20px;"
            "   border-left-width: 1px;"
            "   border-left-color: darkgray;"
            "   border-left-style: solid;"
            "}"
            );
    for (const auto& option: availableEngines) {
        engine1->addItem(option);
    }
    horizontalLayout->addWidget(engine1);

    auto* vsLabel = new QLabel("vs.", widget);
    vsLabel->setAlignment(Qt::AlignCenter);
    vsLabel->setStyleSheet(
            "color: white;"
            "font-weight: bold;"
            "font-size: 14px;"
            );
    horizontalLayout->addWidget(vsLabel);

    // Combo box for the second opponent
    engine2 = new QComboBox(widget);
    engine2->setStyleSheet(
            "QComboBox {"
            "   background-color: #565656;"
            "   border: 2px solid #1f1f1f;"
            "   border-radius: 3px;"
            "   color: white;"
            "   padding: 3px;"
            "}"
            "QComboBox:focus {"
            "   border: 2px solid #00A2E8;"
            "}"
            "QComboBox::drop-down {"
            "   subcontrol-origin: padding;"
            "   subcontrol-position: top right;"
            "   width: 20px;"
            "   border-left-width: 1px;"
            "   border-left-color: darkgray;"
            "   border-left-style: solid;"
            "}"
            );
    for (const auto& option: availableEngines) {
        engine2->addItem(option);
    }
    horizontalLayout->addWidget(engine2);

    layout->addItem(horizontalLayout);

    auto* startButton = new QPushButton("Start", widget);
    startButton->setFixedHeight(25);
    startButton->setStyleSheet(
            "QPushButton {"
            "   background-color: #6495ED;"
            "   color: black;"
            "   border: 2px solid black;"
            "   border-radius: 5px;"
            "   font-family: 'Arial', sans - serif;"
            "   font-size: 13px;"
            "   font-weight: bold;"
            "}"
            "QPushButton:hover {"
            "   background-color: #4169E1;"
            "}"
            "QPushButton:pressed {"
            "   background-color: #1E90FF;"
            "}");
    startButton->setText("Start ⚔");
    connect(startButton, &QPushButton::clicked, this, &OptionsWidget::startEngineMatch);
    layout->addWidget(startButton);

    optionsLayout->addWidget(widget);
}

void OptionsWidget::constructEvalWidget() {
    auto* widget = new QWidget(this);
    widget->setFixedSize(300, 247);
    widget->setStyleSheet("background-color: #3d3d3d;");

    QLayout* layout = new QVBoxLayout(widget);

    //TODO: DESIGN AND IMPLEMENT THIS

    optionsLayout->addWidget(widget);
}

// <--> INITIALIZATION <--> //

// <--> BUTTON PRESSES AND STUFF <--> //

void OptionsWidget::debugPressed() {
    QString buttonStyleSheet = debugButton->styleSheet();
    if (buttonStyleSheet.contains("background-color: #6495ED;")) {
        debugButton->setStyleSheet(
                "QPushButton {"
                "   background-color: #eb4242;"
                "   color: black;"
                "   border: 2px solid black;"
                "   border-radius: 5px;"
                "   font-family: 'Arial', sans - serif;"
                "   font-size: 13px;"
                "   font-weight: bold;"
                "}"
                "QPushButton:hover {"
                "   background-color: #c73434;"
                "}"
                "QPushButton:pressed {"
                "   background-color: #f54040;"
                "}");
    }
    else {
        debugButton->setStyleSheet(
                "QPushButton {"
                "   background-color: #6495ED;"
                "   color: black;"
                "   border: 2px solid black;"
                "   border-radius: 5px;"
                "   font-family: 'Arial', sans - serif;"
                "   font-size: 13px;"
                "   font-weight: bold;"
                "}"
                "QPushButton:hover {"
                "   background-color: #4169E1;"
                "}"
                "QPushButton:pressed {"
                "   background-color: #1E90FF;"
                "}");
    }
    emit debugModeSignal();
}

void OptionsWidget::makeBoardFromFen() {
    std::string fenString = fenInput->text().toStdString();
    fenInput->setText("");
    emit makeBoardFromFenSignal(fenString);
}

void OptionsWidget::goPerft() {
    std::string depth = depthInput->text().toStdString();
    depthInput->setText("");
    emit goPerftSignal(depth);
}

void OptionsWidget::setOpponent() {
    int selectedIndex = opponentChoice->currentIndex();
    std::string choice = (selectedIndex == 0) ? "" : availableEngines[selectedIndex - 1].toStdString();
    emit setOpponentSignal(choice);
}

void OptionsWidget::startEngineMatch() {
    int engine1Choice = engine1->currentIndex();
    int engine2Choice = engine2->currentIndex();

    emit startEngineMatchSignal(availableEngines[engine1Choice].toStdString(),
                                availableEngines[engine2Choice].toStdString());
}

// <--> BUTTON PRESSES AND STUFF <--> //
