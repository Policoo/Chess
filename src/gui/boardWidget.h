#pragma once

#include <QWidget>
#include <QResizeEvent>

class BoardWidget final : public QWidget {
    Q_OBJECT
public:
    explicit BoardWidget(QWidget* parent = nullptr) : QWidget(parent) {}

protected:
    void resizeEvent(QResizeEvent* event) override {
        const int newSize = qMin(event->size().width(), event->size().height());
        resize(newSize, newSize);
    }
};
