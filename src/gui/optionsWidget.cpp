#include <QVBoxLayout>
#include <QLabel>
#include <iostream>

#include "optionsWidget.h"

#include "../engines/engine.h"
#include "../engines/engineRegistry.h"

// <--> INITIALIZATION <--> //

OptionsWidget::OptionsWidget(QWidget* parent) :
    QWidget(parent) {
    auto* widget = new QWidget(this);
    widget->setStyleSheet("background-color: black;");
    setMinimumWidth(260);
    setMaximumWidth(420);
    setSizePolicy(QSizePolicy::Preferred, QSizePolicy::Expanding);

    optionsLayout = new QVBoxLayout(this);
    optionsLayout->setSpacing(1);
    optionsLayout->setContentsMargins(0, 0, 0, 0);
    optionsLayout->setAlignment(Qt::AlignTop);

    constructTopButtons();
    constructFenWidget();
    constructPerftWidget();
    constructOpponentWidget();
    constructMatchWidget();
    constructEvalWidget();
}

void OptionsWidget::constructTopButtons() {
    auto* widget = new QWidget(this);
    widget->setMinimumHeight(40);
    widget->setStyleSheet("background-color: #3d3d3d;");

    auto* layout = new QHBoxLayout(widget);
    layout->setContentsMargins(5, 0, 5, 0);
    layout->setAlignment(Qt::AlignVCenter);

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

    auto* spacer1 = new QSpacerItem(24, 1, QSizePolicy::Expanding, QSizePolicy::Minimum);
    layout->addItem(spacer1);

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

    auto* spacer2 = new QSpacerItem(24, 1, QSizePolicy::Expanding, QSizePolicy::Minimum);
    layout->addItem(spacer2);

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

    auto* spacer3 = new QSpacerItem(24, 1, QSizePolicy::Expanding, QSizePolicy::Minimum);
    layout->addItem(spacer3);

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
    widget->setMinimumHeight(90);
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
    fenInput->setMinimumHeight(24);
    fenInput->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Fixed);
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
    widget->setMinimumHeight(40);
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
    depthInput->setMinimumSize(35, 24);
    depthInput->setMaximumWidth(60);
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
    goPerftButton->setMinimumHeight(25);
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

    layout->addItem(new QSpacerItem(0, 0, QSizePolicy::Expanding, QSizePolicy::Minimum));

    optionsLayout->addWidget(widget);
}

void OptionsWidget::constructOpponentWidget() {
    auto* widget = new QWidget(this);
    widget->setMinimumHeight(40);
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
    opponentChoice->setSizeAdjustPolicy(QComboBox::AdjustToContents);
    opponentChoice->setMinimumWidth(120);
    opponentChoice->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Fixed);

    opponentChoice->addItem("Yourself :(");
    for (const auto& option: engines) {
        opponentChoice->addItem(option->name().data());
    }
    connect(opponentChoice, &QComboBox::currentIndexChanged, this, &OptionsWidget::setOpponent);
    layout->addWidget(opponentChoice);

    optionsLayout->addWidget(widget);
}

void OptionsWidget::constructMatchWidget() {
    auto* widget = new QWidget(this);
    widget->setMinimumHeight(90);
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
    engine1->setSizeAdjustPolicy(QComboBox::AdjustToContents);
    engine1->setMinimumWidth(100);
    engine1->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Fixed);
    for (const auto& option: engines) {
        engine1->addItem(option->name().data());
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
    engine2->setSizeAdjustPolicy(QComboBox::AdjustToContents);
    engine2->setMinimumWidth(100);
    engine2->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Fixed);
    for (const auto& option: engines) {
        engine2->addItem(option->name().data());
    }
    horizontalLayout->addWidget(engine2);

    layout->addItem(horizontalLayout);

    auto* startButton = new QPushButton("Start", widget);
    startButton->setMinimumHeight(25);
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
    widget->setMinimumHeight(120);
    widget->setSizePolicy(QSizePolicy::Preferred, QSizePolicy::Expanding);
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
    emit setOpponentSignal(opponentChoice->currentText().toStdString());
}

void OptionsWidget::startEngineMatch() {
    QString engine1Choice = engine1->currentText();
    QString engine2Choice = engine2->currentText();

    emit startEngineMatchSignal(engine1Choice.toStdString(),
                                engine2Choice.toStdString());
}

// <--> BUTTON PRESSES AND STUFF <--> //
