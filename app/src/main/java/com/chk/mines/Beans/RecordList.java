package com.chk.mines.Beans;

import android.util.Log;

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
        if (recordList.size() != 0) {
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
            Log.i(TAG,"CurrentNodeTime:"+currentNode.record.getGameTime());
            currentNode = currentNode.nextNode;
        }
        Log.i(TAG,"longestTime:"+getLongestTime());
    }

    /**
     * 插入节点
     * @param recordNode
     */
    void insert(RecordNode recordNode) {
        if (size == 0) {
            recordNodeHead = recordNode;
        } else {
            boolean inserted = false;
            RecordNode currentNode = recordNodeHead;
            RecordNode previousNode = recordNodeHead;
            do {
                if (recordNode.record.getGameType() < currentNode.record.getGameTime()) {
                    recordNode.nextNode = currentNode;
                    if (previousNode == recordNodeHead) {   //说明比第一个节点还快
                        recordNodeHead = recordNode;
                    } else {
                        previousNode.nextNode = recordNode;
                    }
                    inserted = true;
                } else {
                    previousNode = currentNode;
                    currentNode = currentNode.nextNode;
                    if (currentNode == null) {   //说明目前还不足5个，直接插入到最后一个
                        previousNode.nextNode = recordNode;
                        inserted = true;
                    }
                }
            } while (!inserted);
        }
        size ++;
        removeMoreThan5Node();

        RecordNode currentNode = recordNodeHead;
        while (currentNode != null) {
            Log.i(TAG,"链表内的Node:--Time:"+currentNode.record.getGameTime()+"--Player:"+currentNode.record.getGamePlayer()+"--Type:"+currentNode.record.getGameType());
            currentNode = currentNode.nextNode;
        }
    }

    /**
     * 如果超过5个则移除最后一个，确保链表内最多只有5个节点
     */
    void removeMoreThan5Node() {
        if (size > 5) {
            RecordNode currentNode = recordNodeHead;
            RecordNode previousNode = recordNodeHead;
            while (currentNode.nextNode != null) {
                previousNode = currentNode;
                currentNode = currentNode.nextNode;
            }
            previousNode.nextNode = null;
            size = 5;
        }
    }

    public void printAllNode() {
        RecordNode currentNode = recordNodeHead;
        while(currentNode != null) {
            Log.i(TAG,"Time:"+currentNode.record.getGameTime()+" player:"+currentNode.record.getGamePlayer()+" Type:"+currentNode.record.getGameType());
            currentNode = currentNode.nextNode;
        }
    }

    /**
     * 获取所有的Node
     * @return 如果有Node的话则返回一个Arraylist,否则返回Null
     */
    ArrayList<RecordNode> getAllNode() {
        if (recordNodeHead != null) {
            ArrayList<RecordNode> recordNodes= new ArrayList<>();
            RecordNode currentNode = recordNodeHead;
            do {
                recordNodes.add(currentNode);
                currentNode = currentNode.nextNode;
            } while (currentNode != null);
            return recordNodes;
        }
        return null;
    }

//    /**
//     * 插入节点
//     * @param recordNode
//     */
//    void insert(RecordNode recordNode) {
//        if (size == 0) {
//            recordNodeHead = recordNode;
//        } else {
//            RecordNode currentNode = recordNodeHead;
//            RecordNode previousNode = recordNodeHead;
//            do {
//                if (recordNode.record.getGameTime() < currentNode.record.getGameTime()) {
//                    recordNode.nextNode = currentNode;
//                    if (previousNode == recordNodeHead) {   //说明比第一个节点的时间还短，需要更新头节点
//                        recordNodeHead = recordNode;
//                    } else {    //否则的话
//                        previousNode.nextNode = recordNode;
//                        if (currentNode.nextNode != null)   //确保最后一个的节点的nextNode是Null
//                            recordNode.nextNode = currentNode;
//                    }
//                    break;
//                } else {
//                    previousNode = currentNode;
//                    currentNode = currentNode.nextNode;
//                    if (currentNode == null) {  //判断当前节点是否为null，是的话说明不足5个直接插入即可,其实这里还是有个bug,如果是第五个那么则应该还会插入到最后一个地方
//                                                //其实不用的，因为已经是有判断是否能插入到节点内部
//                        previousNode.nextNode = recordNode;
//                        break;
//                    }
//                }
//            } while (currentNode != null);
//        }
//        if (size < 5)
//            size++;
//    }

    void remove() {

    }

    /**
     * 获取游戏完成的最长时间
     * @return  返回游戏时间，如果没有游戏记录则返回-1
     */
    public int getLongestTime() {
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
