#pragma once

#include <QWidget>
#include <QRect>
#include <QLabel>
#include <QGridLayout>
#include <QResizeEvent>

class SquareLabel : public QLabel {
    Q_OBJECT
public:
    explicit SquareLabel(QWidget* parent = nullptr)
      : QLabel(parent)
    {
        // let them grow if there’s extra space
        QSizePolicy sp(QSizePolicy::Expanding,
                       QSizePolicy::Expanding);
        sp.setHeightForWidth(true);
        setSizePolicy(sp);
        // optional: a small minimum so the board never collapses
        setMinimumSize(20,20);
    }

    // Qt only calls heightForWidth if this is true
    bool hasHeightForWidth() const override { return true; }
    int  heightForWidth(int w) const override { return w; }

    QSize sizeHint() const override {
        int w = width();
        // if we haven’t been shown yet, fall back to minimum
        if (w < minimumWidth()) w = minimumWidth();
        return QSize(w, heightForWidth(w));
    }
protected:
    void resizeEvent(QResizeEvent* ev) override {
        QLabel::resizeEvent(ev);
        // nothing else needed here
    }
};

class SquareGridLayout : public QGridLayout
{
public:
    explicit SquareGridLayout(QWidget *parent = nullptr)
      : QGridLayout(parent)
    {
        setSpacing(0);
        setContentsMargins(0,0,0,0);
    }

    // Tell Qt we have a height-for-width constraint
    bool hasHeightForWidth() const override       { return true; }
    int  heightForWidth(int w) const override     { return w; }

    // Make sizeHint square too
    QSize sizeHint() const override
    {
        QSize s = QGridLayout::sizeHint();
        int side = qMin(s.width(), s.height());
        return { side, side };
    }

    // Carve out a centered square in the available rect
    void setGeometry(const QRect &rect) override
    {
        int side = qMin(rect.width(), rect.height());
        int x = rect.x() + (rect.width()  - side) / 2;
        int y = rect.y() + (rect.height() - side) / 2;
        QGridLayout::setGeometry({ x, y, side, side });
    }
};
