#include "dialogWidget.h"

#include <QScrollArea>
#include <QScrollBar>
#include <QLabel>
#include <iostream>

DialogWidget::DialogWidget(QWidget* parent) : QWidget(parent) {
    setFixedSize(300, 552);

    auto* widget = new QWidget(this);
    widget->setStyleSheet("background-color: black;");
    widget->setFixedSize(300, 552);

    auto* vLayout = new QVBoxLayout(widget);
    vLayout->setSpacing(1);
    vLayout->setContentsMargins(0, 0, 0, 0);
    vLayout->setAlignment(Qt::AlignTop);

    auto* desc = new QWidget(widget);
    desc->setFixedSize(300, 35);
    desc->setStyleSheet("background-color: #3d3d3d;");

    auto* descLayout = new QVBoxLayout(desc);
    descLayout->setSpacing(0);
    descLayout->setContentsMargins(0, 0, 0, 0);

    auto* descText = new QLabel("Dialog box", desc);
    descText->setAlignment(Qt::AlignCenter);
    descText->setStyleSheet(
        "color: white;"
        "font-size: 18px;"
        "font-weight: bold;"
    );
    descLayout->addWidget(descText);
    vLayout->addWidget(desc);

    auto* scrollArea = new QScrollArea(widget);
    scrollArea->setStyleSheet(
        "border: none;"
        "background-color: #3d3d3d;"
    );
    scrollArea->setWidgetResizable(true);

    scrollWidget = new QWidget(scrollArea);
    layout = new QVBoxLayout(scrollWidget);
    layout->setAlignment(Qt::AlignTop);

    vLayout->addWidget(scrollArea);
    scrollArea->setWidget(scrollWidget);

    QScrollBar* scrollBar = scrollArea->verticalScrollBar();
    scrollBar->setStyleSheet(
        "QScrollBar:vertical {"
        "    border: 1px solid #3d3d3d;"
        "    background: #565656;"
        "    width: 6px;"
        "    margin: 0px 0px 0px 0px;"
        "}"
        "QScrollBar::handle:vertical {"
        "    background: #7f7f7f;"
        "    min-height: 10px;"
        "}"
        "QScrollBar::add-line:vertical, QScrollBar::sub-line:vertical {"
        "    background: none;"
        "}"
        "QScrollBar::add-page:vertical, QScrollBar::sub-page:vertical {"
        "    background: #565656;"
        "}"
    );
}

void DialogWidget::displayDebugString(const std::string& debugString) {
    clearDialogBox();

    auto* messageLabel = new QLabel(QString::fromStdString(debugString));
    messageLabel->setFixedWidth(270);
    messageLabel->setWordWrap(true);
    messageLabel->setStyleSheet("color: white;"
        "padding: 3px;"
        "font-size: 9pt;"
    );

    QFont font("Consolas");
    messageLabel->setFont(font);

    layout->addWidget(messageLabel);
}

void DialogWidget::displayMessage(const std::string& message) {
    clearDialogBox();

    auto* messageLabel = new QLabel(QString::fromStdString(message));
    messageLabel->setSizePolicy(QSizePolicy::Fixed, QSizePolicy::Fixed);
    messageLabel->setMinimumSize(messageLabel->sizeHint());
    messageLabel->setFixedWidth(270);
    messageLabel->setWordWrap(true);
    messageLabel->setStyleSheet("color: white;"
        "padding: 3px;"
        "font-size: 10pt;"
    );

    layout->addWidget(messageLabel);
}

void DialogWidget::displayCountResults(std::vector<std::string> results) {
    clearDialogBox();

    auto* desc = new QLabel(QString::fromStdString(results[0]));
    desc->setSizePolicy(QSizePolicy::Fixed, QSizePolicy::Fixed);
    desc->setMinimumSize(desc->sizeHint());
    desc->setFixedWidth(270);
    desc->setWordWrap(true);
    desc->setStyleSheet("color: white;"
        "padding: 3px;"
        "font-weight: bold;"
        "font-size: 12pt;"
    );

    layout->addWidget(desc);

    bool correct = true;
    int index;
    std::string nodes;
    for (index = 1; index < results.size(); index++) {
        if (results[index].find("Nodes") != std::string::npos) {
            nodes = results[index];
            continue;
        }

        if (results[index].find("found") != std::string::npos) {
            correct = false;
            break;
        }

        if (results[index].find("StockFish") != std::string::npos) {
            results[index] += R"( <font size="+1" color="red">&#10008;</font>)";
        }
        else {
            results[index] += R"( <font size="+1" color="green">&#10004;</font>)";
        }

        auto* moveLabel = new QLabel(QString::fromStdString(results[index]));
        moveLabel->setSizePolicy(QSizePolicy::Fixed, QSizePolicy::Fixed);
        moveLabel->setMinimumSize(moveLabel->sizeHint());
        moveLabel->setFixedWidth(270);
        moveLabel->setWordWrap(true);
        moveLabel->setStyleSheet("color: white;"
            "padding: 3px;"
            "font-size: 10pt;"
        );

        layout->addWidget(moveLabel);
    }

    if (correct) {
        if (nodes.find("StockFish") != std::string::npos) {
            nodes += R"( <font size="+1" color="red">&#10008;</font>)";
        }
        else {
            nodes += R"( <font size="+1" color="green">&#10004;</font>)";
        }

        auto* moveLabel = new QLabel(QString::fromStdString(nodes));
        moveLabel->setSizePolicy(QSizePolicy::Fixed, QSizePolicy::Fixed);
        moveLabel->setMinimumSize(moveLabel->sizeHint());
        moveLabel->setFixedWidth(270);
        moveLabel->setWordWrap(true);
        moveLabel->setStyleSheet("color: white;"
            "padding: 3px;"
            "font-size: 10pt;"
        );

        layout->addWidget(moveLabel);
        return;
    }

    for (index = index - 1; index < results.size(); index++) {
        auto* moveLabel = new QLabel(QString::fromStdString(results[index]));
        moveLabel->setSizePolicy(QSizePolicy::Fixed, QSizePolicy::Fixed);
        moveLabel->setMinimumSize(moveLabel->sizeHint());
        moveLabel->setFixedWidth(270);
        moveLabel->setWordWrap(true);
        moveLabel->setStyleSheet("color: white;"
            "padding: 3px;"
            "font-size: 10pt;"
        );

        layout->addWidget(moveLabel);
    }

}

void DialogWidget::clearDialogBox() {
    QLayoutItem* item;
    while ((item = layout->takeAt(0)) != nullptr) {
        QWidget* widget = item->widget();
        if (widget) {
            widget->hide();
            delete widget;
        }
        delete item;
    }
}