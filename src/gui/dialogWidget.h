#pragma once

#include <QWidget>
#include <QLayout>

class DialogWidget : public QWidget {
    Q_OBJECT

public:
    explicit DialogWidget(QWidget* parent);

    void displayDebugString(const std::string& debugString);

    void displayMessage(const std::string& message);

    void displayCountResults(std::vector<std::string> results);

private:
    QLayout* layout;
    QWidget* scrollWidget;

    void clearDialogBox();
};
