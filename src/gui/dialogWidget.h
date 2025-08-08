#pragma once

#include <QWidget>
#include <QLayout>
#include <QEvent>

class DialogWidget : public QWidget {
    Q_OBJECT

public:
    explicit DialogWidget(QWidget* parent);
    void displayDebugString(const std::string& debugString);
    void displayMessage(const std::string& message);
    void displayCountResults(std::vector<std::string> results);
protected:
    void resizeEvent(QResizeEvent* event) override;

private:
    QLayout* layout;
    QWidget* scrollWidget;
    int currentBaseFontPt = 10;

    void clearDialogBox();

    void updateFontSizes(int panelWidth);
};
