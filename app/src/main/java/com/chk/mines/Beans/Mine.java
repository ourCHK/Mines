package com.chk.mines.Beans;

/**
 * Created by chk on 18-1-31.
 * 方块
 */
public class Mine {

    /**
     * 数字，-1表示雷
     */
    int num;

    /**
     * 是否是雷
     */
    boolean isMine;

    /**
     * 是否被打开了
     */
    boolean isOpen;

    /**
     * 是否被标记了
     */
    boolean isFlaged;

    /**
     * 是否疑惑
     */
    boolean isConfused;

    public Mine() {
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isFlaged() {
        return isFlaged;
    }

    public void setFlaged(boolean flaged) {
        isFlaged = flaged;
    }

    public boolean isConfused() {
        return isConfused;
    }

    public void setConfused(boolean confused) {
        isConfused = confused;
    }
}
