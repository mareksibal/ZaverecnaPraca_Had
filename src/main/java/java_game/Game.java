package java_game;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.ScreenWriter;
import com.googlecode.lanterna.terminal.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {

    private final int WORLD_WIDTH = 80;
    private final int WORLD_HEIGHT = 24;
    private final int GAME_SPEED = 150;

    private Terminal terminal;
    private Screen screen;
    private ScreenWriter writer;
    private final Key[] CONTROL_KEYS = {
            new Key(Key.Kind.ArrowUp),
            new Key(Key.Kind.ArrowDown),
            new Key(Key.Kind.ArrowLeft),
            new Key(Key.Kind.ArrowRight)
    };
    private final Key TERMINATE_KEY = new Key(Key.Kind.Escape);

    private Snake snake;
    private List<Direction> directionsQueue;
    private State state;

    private Pair apple;

    public Game() {
        terminal =  TerminalFacade.createTerminal();

        screen = new Screen(terminal);

        screen.setCursorPosition(null); // Hack to hide command line cursor
        screen.startScreen();

        screen.refresh();

        writer = new ScreenWriter(screen);

        snake = new Snake(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 3, Direction.UP);
        directionsQueue = new ArrayList<>();

        generateApple();
    }

    public void start() {

        drawScreen();

        long stepStartMillis = 0;

        state = State.RUNNING;

        while (true) {

            handleInput();

            long currentMillis = System.currentTimeMillis();
            if (currentMillis - stepStartMillis > GAME_SPEED) {
                stepStartMillis = currentMillis;

                if(directionsQueue.size() > 0) {
                    snake.makeStep(directionsQueue.remove(0));
                } else {
                    snake.makeStep();
                }

                checkWin();

                checkCrash();

                if(state != State.RUNNING) {
                    break;
                }

                if(snake.getHead().equals(apple)) {
                    snake.setEat(true);
                    generateApple();
                }

                drawScreen();
            }
        }

        switch (state){
            case EXIT:
                screen.stopScreen();
                return;
            case CRASH:
                writer.drawString(24, 12, "####     NABURAL SI :(     ####");
                break;
            case WIN:
                writer.drawString(24, 12, "####      VYHRAL SI :)     ####");
                break;
        }

        writer.drawString(24, 11, "###############################");
        writer.drawString(24, 13, "####         [ESC]         ####");
        writer.drawString(24, 14, "###############################");
        screen.refresh();

        while (true) {
            Key key = terminal.readInput();
            if (key != null) {
                if (key.equals(TERMINATE_KEY)) {
                    break;
                }
            }
        }

        screen.stopScreen();
    }

    private Direction getLastDirection() {
        return directionsQueue.get(directionsQueue.size() - 1);
    }

    private void handleInput() {
        Key key = terminal.readInput();

        if (key != null) {
            Direction direction = getDirectionFromKey(key);

            if (direction != null) {
                if (directionsQueue.size() == 0) {
                    if (snake.getDirection() != getOppositeDirection(direction)) {
                        directionsQueue.add(direction);
                    }
                } else if (getLastDirection() != getOppositeDirection(direction)) {
                    directionsQueue.add(direction);
                }
            } else if (key.equals(TERMINATE_KEY)) {
                state = State.EXIT;
            }
        }
    }

    private void checkCrash() {
        Pair snakeHead = snake.getHead();

        if(snakeHead.getX() == 0 || snakeHead.getY() == 0 || snakeHead.getX() == WORLD_WIDTH - 1 || snakeHead.getY() == WORLD_HEIGHT - 1) {
            state = State.CRASH;
        }

        for(int i = 1; i < snake.getBody().size(); i++) {
            if(snakeHead.equals(snake.getBody().get(i))) {
                state = State.CRASH;
                break;
            }
        }
    }

    private void checkWin() {
        if(snake.getBody().size() == ((WORLD_WIDTH - 2) * (WORLD_HEIGHT - 2))) {
            state = State.WIN;
        }
    }

    private void drawScreen() {
        screen.clear();

        StringBuilder wall = new StringBuilder();
        for(int i = 0; i < WORLD_WIDTH; i ++) {
            wall.append("■");
        }

        writer.drawString(0, 0, wall.toString());
        for (int i = 1; i < WORLD_HEIGHT - 1; i++) {
            writer.drawString(0, i, "■");
            writer.drawString(WORLD_WIDTH - 1, i, "■");
        }
        writer.drawString(0, WORLD_HEIGHT - 1, wall.toString());

        for(int i = 1; i < snake.getBody().size(); i++) {
            Pair bodyPart = snake.getBody().get(i);
            writer.drawString(bodyPart.getX(), bodyPart.getY(), "*");
        }

        Pair headPart = snake.getHead();
        writer.drawString(headPart.getX(), headPart.getY(), "O");

        writer.drawString(apple.getX(), apple.getY(), "ò");

        screen.refresh();
    }

    private Direction getDirectionFromKey(Key key) {
        for(int i = 0; i < CONTROL_KEYS.length; i++) {
            if(CONTROL_KEYS[i].equals(key)) {
                return Direction.values()[i];
            }
        }

        return null;
    }

    private Direction getOppositeDirection(Direction direction) {
        if (direction == Direction.LEFT) return Direction.RIGHT;
        if (direction == Direction.RIGHT) return Direction.LEFT;
        if (direction == Direction.UP) return Direction.DOWN;
        if (direction == Direction.DOWN) return Direction.UP;

        throw new IllegalArgumentException("Something went terribly wrong in getOppositeDirection function. Passed direction: " + direction);
    }

    private void generateApple() {
        Random random = new Random();
        Pair newApple;

        do {
            newApple = new Pair(random.nextInt(WORLD_WIDTH - 2) + 1, random.nextInt(WORLD_HEIGHT - 2) + 1);
        } while (isOccupied(newApple));

        apple = newApple;
    }

    private boolean isOccupied(Pair pair) {
        return snake.getBody().contains(pair);
    }

    private enum State {
        RUNNING,
        EXIT,
        CRASH,
        WIN
    }

}