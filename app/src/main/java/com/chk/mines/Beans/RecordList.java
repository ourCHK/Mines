package com.chk.mines.Beans;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by chk on 18-3-26.
 * Record的链表
 */
public class RecordList {
    private final static String TAG  = RecordList.class.getSimpleName();
    int size;   //链表的长度
    RecordNode recordNodeHead;

    public RecordList(ArrayList<Record> recordList) {
        if (size != 0) {
            for (Record record:recordList) {
                RecordNode recordNode = new RecordNode();
                recordNode.record = record;
                insert(recordNode);
                if (record == recordList.get(0)) {
                    recordNodeHead = recordNode;
                }
            }
        }

        RecordNode currentNode = recordNodeHead;
        while (currentNode != null) {
            Log.i(TAG,currentNode.record.getGameTime()+"");
        }
        Log.i(TAG,getLongestTime()+"");
    }

    /**
     * 插入节点
     * @param recordNode
     */
    void insert(RecordNode recordNode) {
        if (size == 0) {
            recordNodeHead = recordNode;
        } else {
            RecordNode currentNode = recordNodeHead;
            RecordNode previousNode = recordNodeHead;
            do {
                if (recordNode.record.getGameTime() < currentNode.record.getGameTime()) {
                    recordNode.nextNode = currentNode;
                    if (previousNode == recordNodeHead) {   //说明比第一个节点的时间还短，需要更新头节点
                        recordNodeHead = recordNode;
                    } else {    //否则的话
                        previousNode.nextNode = recordNode;
                        recordNode.nextNode = currentNode;
                    }
                } else {
                    previousNode = currentNode;
                    currentNode = currentNode.nextNode;
                    if (currentNode == null) {  //判断当前节点是否为null，是的话说明不足5个直接插入即可
                        previousNode.nextNode = recordNode;
                    }
                }
            } while (currentNode != null);
        }
        if (size < 5)
            size++;
    }

    void remove() {

    }

    /**
     * 获取游戏完成的最长时间
     * @return  返回游戏时间，如果没有游戏记录则返回-1
     */
    int getLongestTime() {
        int currentTime = -1;
        if (size > 0) {
            RecordNode currentNode = recordNodeHead;
            do {
                if (currentNode.nextNode != null)
                    currentNode = currentNode.nextNode;
                else
                    break;
            } while (true);
            currentTime = currentNode.record.getGameTime();
        }
        return currentTime;
    }
}
