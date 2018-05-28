package java_game;

import java.util.ArrayList;
import java.util.List;

public class Snake {

    private List<Pair> body = new ArrayList<>();
    private Direction direction;
    private boolean eat = false;

    public Snake(int x, int y, int length, Direction direction) {
        this.direction = direction;

        for (int i = 0; i < length; i++) {
            int bodyX = x;
            int bodyY = y;

            switch (direction){
                case DOWN:
                    bodyY = bodyY - i;
                    break;
                case UP:
                    bodyY = bodyY + i;
                    break;
                case RIGHT:
                    bodyX = bodyX - i;
                    break;
                case LEFT:
                    bodyX = bodyX + i;
                    break;
            }

            body.add(new Pair(bodyX, bodyY));
        }
    }

    public List<Pair> getBody() {
        return body;
    }

    public Direction getDirection() {
        return direction;
    }

    public void makeStep(Direction direction) {
        this.direction = direction;

        Pair newBodyPart = null;

        if (eat) {
            newBodyPart = new Pair(getTail().getX(), getTail().getY());
        }

        for (int i = body.size() - 1; i > 0; i--) {
            body.get(i).setX(body.get(i - 1).getX());
            body.get(i).setY(body.get(i - 1).getY());
        }

        if (direction == Direction.LEFT) {
            getHead().setX(getHead().getX() - 1);
        }
        if (direction == Direction.RIGHT) {
            getHead().setX(getHead().getX() + 1);
        }
        if (direction == Direction.UP) {
            getHead().setY(getHead().getY() - 1);
        }
        if (direction == Direction.DOWN) {
            getHead().setY(getHead().getY() + 1);
        }

        if (eat) {
            body.add(newBodyPart);
            eat = false;
        }
    }

    public void makeStep() {
        makeStep(direction);
    }

    public Pair getHead(){
        return body.get(0);
    }

    public Pair getTail(){
        return body.get(body.size() - 1);
    }

    public void setEat(boolean eat) {
        this.eat = eat;
    }
}