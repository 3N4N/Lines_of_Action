import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.ArrayList;

public class CheckersApp extends Application {
    public static final int TILE_SIZE = 80;
    public static final int HEIGHT = 8;
    public static final int WIDTH = 8;

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    Tile[][] board = new Tile[HEIGHT][WIDTH];

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                board[x][y] = new Tile(((x+y) % 2 == 0), x, y);
                tileGroup.getChildren().add(board[x][y]);

                Piece piece = null;
                if ((y == 0 || y == 7) && (x > 0 && x < 7)) {
                    piece = makePiece(PieceType.RED, x, y);
                }
                if ((x == 0 || x == 7) && (y > 0 && y < 7)) {
                    piece = makePiece(PieceType.WHITE, x, y);
                }
                if (piece != null) {
                    board[x][y].setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        return root;
    }

    private MoveResult tryMove(Piece piece, int newX, int newY) {
        int x0 = toBoard(piece.getOldX());
        int y0 = toBoard(piece.getOldY());

        TilePosition newPosition = new TilePosition(newX, newY);
        if (availableMoves(x0, y0).contains(newPosition)) {
            if (board[newX][newY].hasPiece())
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            else
                return new MoveResult(MoveType.NORMAL);
        }

        return new MoveResult(MoveType.NONE);
    }

    private int toBoard(double pixel) {
        return (int) (pixel + TILE_SIZE / 2) / TILE_SIZE;
    }

    private Piece makePiece(PieceType type, int x, int y) {
        Piece piece = new Piece(type, x, y);

        piece.setOnMouseReleased(e -> {
            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());

            MoveResult result;

            if (newX < 0 || newY < 0 || newX >= WIDTH || newY >= HEIGHT) {
                result = new MoveResult(MoveType.NONE);
            } else {
                result = tryMove(piece, newX, newY);
            }

            int x0 = toBoard(piece.getOldX());
            int y0 = toBoard(piece.getOldY());

            switch (result.getType()) {
                case NONE:
                    piece.abortMove();
                    break;
                case NORMAL:
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);
                    break;
                case KILL:
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);

                    Piece otherPiece = result.getPiece();
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOldY())].setPiece(null);
                    pieceGroup.getChildren().remove(otherPiece);
                    break;
            }
        });

        return piece;
    }

    private ArrayList<TilePosition> availableMoves(int x, int y) {
        ArrayList<TilePosition> availableTiles = new ArrayList<>();

        /*
         * Check the availability of horizontal tiles
         */
        int totalPiece = 0;
        for (int i = 0; i < WIDTH; i++) {
            if (board[i][y].hasPiece()) totalPiece++;
        }
        if (x + totalPiece >= 0 && x + totalPiece < WIDTH)
            availableTiles.add(new TilePosition(x + totalPiece, y));
        if (x - totalPiece >= 0 && x - totalPiece < WIDTH)
            availableTiles.add(new TilePosition(x - totalPiece, y));

        /*
         * Check the availability of vertical tiles
         */
        totalPiece = 0;
        for (int i = 0; i < HEIGHT; i++) {
            if (board[x][i].hasPiece()) totalPiece++;
        }
        if (y + totalPiece >= 0 && y + totalPiece <= HEIGHT)
            availableTiles.add(new TilePosition(x, y + totalPiece));
        if (y - totalPiece >= 0 && y - totalPiece <= HEIGHT)
            availableTiles.add(new TilePosition(x, y - totalPiece));

        /*
         * Check the availability of diagonal tiles going from top-left to bottom-right
         */
        totalPiece = 0;
        for (int i = x, j = y; i >= 0 && i < WIDTH && j >= 0 && j < HEIGHT; i--, j--) {
            if (board[i][j].hasPiece()) totalPiece++;
        }
        for (int i = x, j = y; i >= 0 && i < WIDTH && j >= 0 && j < HEIGHT; i++, j++) {
            if (board[i][j].hasPiece()) totalPiece++;
        }
        totalPiece--; // counted the piece in board[x][y] twice
        if (x - totalPiece >= 0 && x - totalPiece < WIDTH && y - totalPiece >= 0 && y - totalPiece < HEIGHT)
            availableTiles.add(new TilePosition(x - totalPiece, y - totalPiece));
        if (x + totalPiece >= 0 && x + totalPiece < WIDTH && y + totalPiece >= 0 && y + totalPiece < HEIGHT)
            availableTiles.add(new TilePosition(x + totalPiece, y + totalPiece));

        /*
         * Check the availability of diagonal tiles going from bottom-left to top-right
         */
        totalPiece = 0;
        for (int i = x, j = y; i >= 0 && i < WIDTH && j >= 0 && j < HEIGHT; i--, j++) {
            if (board[i][j].hasPiece()) totalPiece++;
        }
        for (int i = x, j = y; i >= 0 && i < WIDTH && j >= 0 && j < HEIGHT; i++, j--) {
            if (board[i][j].hasPiece()) totalPiece++;
        }
        totalPiece--; // counted the piece in board[x][y] twice
        if (x - totalPiece >= 0 && x - totalPiece < WIDTH && y + totalPiece >= 0 && y + totalPiece < HEIGHT)
            availableTiles.add(new TilePosition(x - totalPiece, y + totalPiece));
        if (x + totalPiece >= 0 && x + totalPiece < WIDTH && y - totalPiece >= 0 && y - totalPiece < HEIGHT)
            availableTiles.add(new TilePosition(x + totalPiece, y - totalPiece));

        return availableTiles;
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("Lines of Action");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
