#include <QHBoxLayout>
#include <QThread>
#include <QVBoxLayout>
#include <QDir>

#include <iostream>
#include <memory>

#include "dialogWidget.h"
#include "gameWidget.h"
#include "engineWorker.h"

#include "../moveGenerator.h"
#include "../piece.h"
#include "../utils.h"

// <--> INITIALIZATION AND COLORING <--> //

GameWidget::GameWidget(QWidget* parent) : QWidget(parent) {
	setFixedSize(552, 552);

	QLayout* hLayout = new QVBoxLayout(this);
	hLayout->setSpacing(0);
	hLayout->setContentsMargins(0, 0, 0, 0);

	auto* hContainer = new QWidget(this);

	QLayout* vLayout = new QHBoxLayout(hContainer);
	vLayout->setSpacing(0);
	vLayout->setContentsMargins(0, 0, 0, 0);

	boardWidget = new QWidget(this);
	boardWidget->setFixedSize(512, 512);

	northWidget = new QWidget(this);
	northWidget->setStyleSheet("background-color: #363636");

	eastWidget = new QWidget(this);
	eastWidget->setStyleSheet("background-color: #363636");

	southWidget = new QWidget(this);
	southWidget->setStyleSheet("background-color: #363636");

	westWidget = new QWidget(this);
	westWidget->setStyleSheet("background-color: #363636");

	hLayout->addWidget(northWidget);
	hLayout->addWidget(hContainer);
	hLayout->addWidget(southWidget);

	vLayout->addWidget(westWidget);
	vLayout->addWidget(boardWidget);
	vLayout->addWidget(eastWidget);

	perspective = Piece::WHITE;
	constructBoard();
	legalMoves = MoveGenerator::generateMoves(*board);
	lastClick = -1;
}

void GameWidget::constructBoard() {
	auto* gridLayout = new QGridLayout(boardWidget);
	gridLayout->setSpacing(0);
	gridLayout->setContentsMargins(0, 0, 0, 0);

	tile.resize(64);
	for (int index = 0; index < 64; index++) {
		tile[index] = new QLabel(boardWidget);

		tile[index]->installEventFilter(this);
		gridLayout->addWidget(tile[index], index / 8, index % 8);
	}

	resetColors();
	addCoordinates();
	initializePieceImages();

	board = new Board();
	updateBoard();
}

void GameWidget::addCoordinates() {
	clearWidgets(westWidget);
	clearWidgets(southWidget);

	std::vector<int> numbers = {8, 7, 6, 5, 4, 3, 2, 1};
	std::vector<char> letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};
	if (perspective == Piece::BLACK) {
		std::reverse(numbers.begin(), numbers.end());
		std::reverse(letters.begin(), letters.end());
	}

	QFont font("Impact", 14);

	QLayout* vLayout = new QVBoxLayout(westWidget);
	vLayout->setSpacing(0);
	vLayout->setContentsMargins(0, 0, 0, 0);

	QLayout* hLayout = new QHBoxLayout(southWidget);
	hLayout->setSpacing(0);
	hLayout->setContentsMargins(0, 0, 0, 0);

	auto* leftSpacer = new QLabel(southWidget);
	leftSpacer->setFixedSize(20, 20);
	hLayout->addWidget(leftSpacer);

	for (int index = 0; index < 8; index++) {
		auto* row = new QLabel(westWidget);
		row->setFixedSize(20, 64);
		row->setStyleSheet("color: white;");
		row->setText(QString::fromStdString(std::to_string(numbers[index])));

		row->setFont(font);
		row->setAlignment(Qt::AlignCenter);
		vLayout->addWidget(row);

		auto* column = new QLabel(southWidget);
		column->setFixedSize(64, 20);
		column->setStyleSheet("color: white;");
		column->setText(QString(letters[index]));

		column->setFont(font);
		column->setAlignment(Qt::AlignCenter);
		hLayout->addWidget(column);
	}

	auto* rightSpacer = new QLabel(southWidget);
	rightSpacer->setFixedSize(20, 20);
	hLayout->addWidget(rightSpacer);
}

void GameWidget::initializePieceImages() {
	const QImage fullImage(":/resources/chess pieces.png");

	// Cut up the image into pieces
	for (int y = 0; y < fullImage.height(); y += 200) {
		for (int x = 0; x < fullImage.width(); x += 200) {
			QImage piece = fullImage.copy(x, y, 200, 200);
			pieceImages.push_back(
				QPixmap::fromImage(piece).scaled(64, 64, Qt::KeepAspectRatio, Qt::SmoothTransformation));
		}
	}
}

void GameWidget::resetColors() {
	for (int index = 0; index < 64; index++) {
		if (((index % 8) + (index / 8)) % 2 == 0) {
			tile[index]->setStyleSheet("background-color: #ebd5b1");
		}
		else {
			tile[index]->setStyleSheet("background-color: #b48662");
		}
	}
}

void GameWidget::highlightClick(int index) {
	int perIndex = (perspective == Piece::WHITE) ? index : 63 - index;
	if (board->isEmpty(perIndex)) {
		return;
	}

	if (((index % 8) + (index / 8)) % 2 == 0) {
		tile[index]->setStyleSheet("background-color: #f4e979;");
	}
	else {
		tile[index]->setStyleSheet("background-color: #d9c252;");
	}
}

void GameWidget::highlightMove(Move move) {
	const int start = (perspective == Piece::WHITE) ? move.start() : 63 - move.start();
	const int end = (perspective == Piece::WHITE) ? move.end() : 63 - move.end();

	if (((start % 8) + (start / 8)) % 2 == 0) {
		tile[start]->setStyleSheet("background-color: #f4e979;");
	}
	else {
		tile[start]->setStyleSheet("background-color: #d9c252;");
	}

	if (((end % 8) + (end / 8)) % 2 == 0) {
		tile[end]->setStyleSheet("background-color: #f4e979;");
	}
	else {
		tile[end]->setStyleSheet("background-color: #d9c252;");
	}
}

void GameWidget::showLegalMoves(int index) {
	for (Move move : legalMoves) {
		if (move.start() != index) {
			continue;
		}

		const int end = (perspective == Piece::WHITE) ? move.end() : 63 - move.end();
		if (((end % 8) + (end / 8)) % 2 == 0) {
			tile[end]->setStyleSheet("background-color: #f86d5b");
		}
		else {
			tile[end]->setStyleSheet("background-color: #da4432");
		}
	}
}

void GameWidget::showPromotionOptions(int index) {
	std::vector<QPixmap> promotionPieces;
	if (index < 8) {
		promotionPieces.push_back(pieceImages[1]);
		promotionPieces.push_back(pieceImages[4]);
		promotionPieces.push_back(pieceImages[2]);
		promotionPieces.push_back(pieceImages[3]);
	}
	else {
		promotionPieces.push_back(pieceImages[7]);
		promotionPieces.push_back(pieceImages[10]);
		promotionPieces.push_back(pieceImages[8]);
		promotionPieces.push_back(pieceImages[9]);
	}

	int direction;
	if (perspective == Piece::WHITE) {
		direction = (index < 8) ? 8 : -8;
	}
	else {
		direction = (index < 8) ? -8 : 8;
		index = 63 - index;
	}

	for (int step = 0; step < 4; step++) {
		int curIndex = index + (direction * step);
		tile[curIndex]->setStyleSheet("background-color: white;");
		tile[curIndex]->setPixmap(promotionPieces[step]);

		promotionTiles.push_back(curIndex);
	}
}

void GameWidget::updateBoard() {
	for (int index = 0; index < 64; index++) {
		const int perIndex = (perspective == Piece::WHITE) ? index : 63 - index;

		if (board->isEmpty(perIndex)) {
			tile[index]->clear();
			continue;
		}

		int pieceIndex = 0;
		switch (board->getPieceType(perIndex)) {
		case Piece::KING:
			pieceIndex = 0;
			break;
		case Piece::QUEEN:
			pieceIndex = 1;
			break;
		case Piece::BISHOP:
			pieceIndex = 2;
			break;
		case Piece::KNIGHT:
			pieceIndex = 3;
			break;
		case Piece::ROOK:
			pieceIndex = 4;
			break;
		case Piece::PAWN:
			pieceIndex = 5;
			break;
		}
		pieceIndex = (board->getPieceColor(perIndex) == Piece::BLACK) ? pieceIndex + 6 : pieceIndex;
		tile[index]->setPixmap(pieceImages[pieceIndex]);
	}
}

// <--> INITIALIZATION AND COLORING <--> //

// <--> CLICKING AND GAME LOGIC <--> //

void GameWidget::mousePressEvent(QMouseEvent* event) {
	// Find the widget that was clicked
	QWidget* clickedWidget = childAt(event->pos());

	for (int index = 0; index < 64; index++) {
		if (tile[index] == clickedWidget) {
			handleClick(index);
		}
	}
}

void GameWidget::handleClick(int index) {
	//special case for promotions
	if (!promotionTiles.empty()) {
		//if we didn't promote, but clicked outside the options
		if (std::find(promotionTiles.begin(), promotionTiles.end(), index) == promotionTiles.end()) {
			promotionTiles.clear();
			resetColors();
			updateBoard();
			return;
		}

		if (index / 8 == 0 || index / 8 == 7) {
			promotionMove.setPromotion(Piece::QUEEN);
		}

		if (index / 8 == 1 || index / 8 == 6) {
			promotionMove.setPromotion(Piece::ROOK);
		}

		if (index / 8 == 2 || index / 8 == 5) {
			promotionMove.setPromotion(Piece::BISHOP);
		}

		if (index / 8 == 3 || index / 8 == 4) {
			promotionMove.setPromotion(Piece::KNIGHT);
		}

		makeMove(promotionMove);
		promotionTiles.clear();
		resetColors();
		highlightMove(promotionMove);
		return;
	}

	//variables that have been edited to reflect the perspective of the board
	int perIndex = (perspective == Piece::WHITE) ? index : 63 - index;
	int perLastClick = (perspective == Piece::WHITE) ? lastClick : 63 - lastClick;

	//if this is a legal move
	if (!pieceMoves.empty() && std::find(pieceMoves.begin(), pieceMoves.end(), perIndex) != pieceMoves.end()) {
		for (Move move : legalMoves) {
			if (move.start() == perLastClick && move.end() == perIndex) {
				if (move.flag() == Flag::PROMOTION) {
					showPromotionOptions(move.end());
					promotionMove = move;
					return;
				}

				makeMove(move);
				resetColors();
				highlightMove(move);
				break;
			}
		}

		lastClick = -1;
		pieceMoves.clear();
		return;
	}

	//if we clicked on an empty tile that is not a legal move
	if (board->isEmpty(perIndex)) {
		resetColors();
		lastClick = -1;
		pieceMoves.clear();
		return;
	}

	//if we click on a piece whose turn it is
	if (board->isColor(perIndex, board->getTurn())) {
		resetColors();
		highlightClick(index);
		showLegalMoves(perIndex);

		//add all possible end squares of piece to vector
		if (!legalMoves.empty()) {
			lastClick = index;
			pieceMoves.clear();
			for (Move move : legalMoves) {
				if (move.start() == perIndex) {
					pieceMoves.push_back(move.end());
				}
			}
		}

		return;
	}
	else {
		//if we get here that means the user clicked on a tile that is not a move, but an opponents tile
		resetColors();
		highlightClick(index);
		pieceMoves.clear();
		lastClick = -1;
	}
}

void GameWidget::makeMove(Move move) {
	board->makeMove(move);
	moveHistory.push_back(move);
	updateBoard();

	legalMoves = MoveGenerator::generateMoves(*board);
	emit moveMadeSignal();
}

void GameWidget::undoMove() {
	if (moveHistory.empty()) {
		return;
	}

	Move move = moveHistory.back();
	moveHistory.pop_back();
	board->undoMove(move);
	updateBoard();
	resetColors();

	legalMoves = MoveGenerator::generateMoves(*board);
	emit moveMadeSignal();
}

// <--> CLICKING AND GAME LOGIC <--> //

// <--> BUTTONS <--> //

void GameWidget::resetBoard() {
	delete board;
	board = new Board();
	updateBoard();
	resetColors();
	legalMoves = MoveGenerator::generateMoves(*board);
}

std::string GameWidget::getDebugString() {
	return board->debugString();
}

void GameWidget::flipBoard() {
	perspective = (perspective == Piece::WHITE) ? Piece::BLACK : Piece::WHITE;

	resetColors();
	updateBoard();
	addCoordinates();
}

void GameWidget::makeBoardFromFen(const std::string& fenString) {
	delete board;
	board = new Board(fenString);
	updateBoard();
	resetColors();
	legalMoves = MoveGenerator::generateMoves(*board);
}

void GameWidget::goPerft(int depth) {
	auto engineWorker = new EngineWorker(this);
	connect(engineWorker, &EngineWorker::perftDone, this, &GameWidget::perftResultsReady);

	const auto thread = new QThread;
	engineWorker->moveToThread(thread);
	connect(thread, &QThread::started, [engineWorker, this, depth]() {
		engineWorker->goPerft(*board, depth);
		});
	connect(engineWorker, &EngineWorker::perftDone, thread, &QThread::quit);
	connect(engineWorker, &EngineWorker::perftDone, engineWorker, &EngineWorker::deleteLater);
	connect(thread, &QThread::finished, thread, &QThread::deleteLater);

	thread->start();
}

void GameWidget::perftDone(std::vector<std::string> results) {
	emit perftResultsReady(results);
}


void GameWidget::setOpponent(std::string opponentChoice) {
	//TODO: Implement this
}

void GameWidget::startEngineMatch(std::string engine1, std::string engine2) {
	//TODO: Implement this
}

// <--> BUTTONS <--> //

// Function to clear widgets and layouts
void GameWidget::clearWidgets(QWidget* widget) {
	QLayout* layout = widget->layout();
	if (layout) {
		while (QLayoutItem* item = layout->takeAt(0)) {
			if (QWidget* widgetItem = item->widget()) {
				delete widgetItem;
			}
			delete item;
		}
		delete layout;
	}
}

std::string GameWidget::getPerspective() {
	if (perspective == Piece::WHITE) {
		return "white";
	}
	else {
		return "black";
	}
}
