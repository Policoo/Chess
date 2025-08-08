#include "dialogWidget.h"

#include <QScrollArea>
#include <QScrollBar>
#include <QLabel>
#include <QFontDatabase>
#include <QResizeEvent>
#include <iostream>

DialogWidget::DialogWidget(QWidget* parent) :
    QWidget(parent) {
    setMinimumWidth(240);
    setMaximumWidth(420);
    setStyleSheet("background-color: black;");
    setSizePolicy(QSizePolicy::Preferred, QSizePolicy::Expanding);

    auto* vLayout = new QVBoxLayout(this);
    vLayout->setSpacing(1);
    vLayout->setContentsMargins(0, 0, 0, 0);
    vLayout->setAlignment(Qt::AlignTop);

    auto* desc = new QWidget(this);
    desc->setMinimumHeight(35);
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

    auto* scrollArea = new QScrollArea(this);
    scrollArea->setStyleSheet(
            "border: none;"
            "background-color: #3d3d3d;"
            );
    scrollArea->setWidgetResizable(true);

    scrollWidget = new QWidget(scrollArea);
    layout = new QVBoxLayout(scrollWidget);
    layout->setAlignment(Qt::AlignTop);

    scrollArea->setWidget(scrollWidget);
    vLayout->addWidget(scrollArea);

    // Initial font sizing
    const int w = width();
    // Adjust fonts once constructed
    const auto labels = findChildren<QLabel*>();
    int base = (w >= 420 ? 12 : (w >= 360 ? 11 : 10));
    for (QLabel* lbl : labels) {
        QFont f = lbl->font();
        bool isHeader = (lbl->text() == "Dialog box");
        f.setPointSize(isHeader ? base + 6 : base);
        lbl->setFont(f);
    }

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
    messageLabel->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Minimum);
    messageLabel->setWordWrap(true);
    messageLabel->setTextInteractionFlags(Qt::TextSelectableByMouse | Qt::TextSelectableByKeyboard);
    messageLabel->setStyleSheet(
            "color: white;"
            "padding: 3px;"
            "font-size: 9pt;"
            );

    if (const int fontId = QFontDatabase::addApplicationFont(":/resources/FiraCode-VariableFont_wght.ttf");
        fontId != -1) {
        const QString fontFamily = QFontDatabase::applicationFontFamilies(fontId).at(0);
        const QFont font(fontFamily);
        messageLabel->setFont(font);
    }
    else {
        qWarning() << "Failed to load the font from resources.";
    }

    layout->addWidget(messageLabel);
    // Scale fonts after new content added
    int base = (width() >= 420 ? 12 : (width() >= 360 ? 11 : 10));
    const auto labels2 = findChildren<QLabel*>();
    for (QLabel* lbl : labels2) {
        QFont f = lbl->font();
        bool isHeader = (lbl->text() == "Dialog box");
        f.setPointSize(isHeader ? base + 6 : base);
        lbl->setFont(f);
    }
}

void DialogWidget::displayMessage(const std::string& message) {
    clearDialogBox();

    auto* messageLabel = new QLabel(QString::fromStdString(message));
    messageLabel->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Minimum);
    messageLabel->setWordWrap(true);
    messageLabel->setStyleSheet("color: white;"
            "padding: 3px;"
            "font-size: 10pt;"
            );

    layout->addWidget(messageLabel);
    // Scale fonts after new content added
    int base2 = (width() >= 420 ? 12 : (width() >= 360 ? 11 : 10));
    const auto labels3 = findChildren<QLabel*>();
    for (QLabel* lbl : labels3) {
        QFont f = lbl->font();
        bool isHeader = (lbl->text() == "Dialog box");
        f.setPointSize(isHeader ? base2 + 6 : base2);
        lbl->setFont(f);
    }
}

// (removed stray declaration)


void DialogWidget::displayCountResults(std::vector<std::string> results) {
    clearDialogBox();

    auto* desc = new QLabel(QString::fromStdString(results[0]));
    desc->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Minimum);
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
        moveLabel->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Minimum);
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
        moveLabel->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Minimum);
        moveLabel->setWordWrap(true);
        moveLabel->setStyleSheet("color: white;"
                "padding: 3px;"
                "font-size: 10pt;"
                );

        layout->addWidget(moveLabel);
        // After full content added, recompute font sizes
        int base = (width() >= 420 ? 12 : (width() >= 360 ? 11 : 10));
        const auto labels = findChildren<QLabel*>();
        for (QLabel* lbl : labels) {
            QFont f = lbl->font();
            bool isHeader = (lbl->text() == "Dialog box");
            f.setPointSize(isHeader ? base + 6 : base);
            lbl->setFont(f);
        }
        return;
    }

    for (index = index - 1; index < results.size(); index++) {
        auto* moveLabel = new QLabel(QString::fromStdString(results[index]));
        moveLabel->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Minimum);
        moveLabel->setWordWrap(true);
        moveLabel->setStyleSheet("color: white;"
                "padding: 3px;"
                "font-size: 10pt;"
                );

        layout->addWidget(moveLabel);
    }

    // After full content added, recompute font sizes
    int base = (width() >= 420 ? 12 : (width() >= 360 ? 11 : 10));
    const auto labels = findChildren<QLabel*>();
    for (QLabel* lbl : labels) {
        QFont f = lbl->font();
        bool isHeader = (lbl->text() == "Dialog box");
        f.setPointSize(isHeader ? base + 6 : base);
        lbl->setFont(f);
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

void DialogWidget::resizeEvent(QResizeEvent* event) {
    QWidget::resizeEvent(event);
    updateFontSizes(event->size().width());
}

void DialogWidget::updateFontSizes(int panelWidth) {
    int newBase = 10;
    if (panelWidth >= 420) newBase = 12;
    else if (panelWidth >= 360) newBase = 11;
    if (newBase == currentBaseFontPt) return;
    currentBaseFontPt = newBase;

    const auto labels = findChildren<QLabel*>();
    for (QLabel* lbl : labels) {
        QFont f = lbl->font();
        bool isHeader = (lbl->text() == "Dialog box");
        f.setPointSize(isHeader ? (currentBaseFontPt + 6) : currentBaseFontPt);
        lbl->setFont(f);
    }
}
