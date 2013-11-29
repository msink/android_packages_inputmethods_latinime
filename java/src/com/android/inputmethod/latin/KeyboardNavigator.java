/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.inputmethod.latin;

import android.graphics.Point;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class KeyboardNavigator {

    private static class Row {
        final private ArrayList<Key> mKeys = new ArrayList();
        final private int mY;
        public Row(int y) {
            mY = y;
        }
        public ArrayList<Key> getKeys() {
            return mKeys;
        }
        public void insertKey(Key key) {
            mKeys.add(key);
        }
    }
    ArrayList<Row> mRows = new ArrayList();
    private Key mCurrentKey = null;
    private HashMap<Key,Point> mKeyPositionMap = new HashMap();

    public KeyboardNavigator(Keyboard keyboard) {
        List<Key> keys = keyboard.getKeys();
        ArrayList<Integer> sorted_y_rows = new ArrayList();
        SparseArray<Row> row_map = new SparseArray();
        for (Key key : keys) {
            Row r = row_map.get(key.y);
            if (r == null) {
                r = new Row(key.y);
                row_map.put(key.y, r);
                sorted_y_rows.add(Integer.valueOf(key.y));
            }
            r.insertKey(key);
        }
        Collections.sort(sorted_y_rows);
        int pos_x = 0;
        int pos_y = 0;
        for (Integer i : sorted_y_rows) {
            Row r = row_map.get(i);
            assert(r != null);
            Collections.sort(r.getKeys(), new Comparator<Key>() {
                public int compare(Key key1, Key key2) {
                    return (key1.x - key2.x);
                }
            });
            mRows.add(r);
            pos_x = 0;
            for (Key k : r.getKeys()) {
                Point pos = new Point(pos_x, pos_y);
                mKeyPositionMap.put(k, pos);
                pos_x++;
            }
            pos_y++;
        }
        mCurrentKey = mRows.get(0).getKeys().get(0);
    }

    public Key getCurrentKey() {
        return mCurrentKey;
    }

    public void setCurrentKey(Key key) {
        mCurrentKey = key;
    }

    public Key searchLeft(Key key) {
        Point pos = mKeyPositionMap.get(key);
        if (pos == null) {
            assert false;
            return null;
        }
        if (pos.x <= 0) {
            return null;
        }
        return mRows.get(pos.y).getKeys().get(pos.x - 1);
    }

    public Key searchLeftFarMost(Key key) {
        Point pos = mKeyPositionMap.get(key);
        if (pos == null) {
            assert false;
            return null;
        }
        return mRows.get(pos.y).getKeys().get(0);
    }

    public Key searchRight(Key key) {
        Point pos = mKeyPositionMap.get(key);
        if (pos == null) {
            assert false;
            return null;
        }
        ArrayList<Key> keys = mRows.get(pos.y).getKeys();
        if (pos.x >= keys.size() - 1) {
            return null;
        }
        return mRows.get(pos.y).getKeys().get(pos.x + 1);
    }

    public Key searchRightFarMost(Key key) {
        Point pos = mKeyPositionMap.get(key);
        if (pos == null) {
            assert false;
            return null;
        }
        ArrayList<Key> keys = mRows.get(pos.y).getKeys();
        return keys.get(keys.size() - 1);
    }

    public Key searchUp(Key key) {
        Point pos = mKeyPositionMap.get(key);
        if (pos == null) {
            assert false;
            return null;
        }
        if (pos.y <= 0) {
            return null;
        }
        Row r = mRows.get(pos.y - 1);
        return locateNearestKeyInRow(r, key);
    }

    public Key searchUpFarMost(Key key) {
        Point pos = mKeyPositionMap.get(key);
        if (pos == null) {
            assert false;
            return null;
        }
        Row r = mRows.get(0);
        return locateNearestKeyInRow(r, key);
    }

    public Key searchDown(Key key) {
        Point pos = mKeyPositionMap.get(key);
        if (pos == null) {
            assert false;
            return null;
        }
        if (pos.y >= mRows.size() - 1) {
            return null;
        }
        Row r = mRows.get(pos.y + 1);
        return locateNearestKeyInRow(r, key);
    }

    public Key searchDownFarMost(Key key) {
        Point pos = mKeyPositionMap.get(key);
        if (pos == null) {
            assert false;
            return null;
        }
        Row r = mRows.get(mRows.size() - 1);
        return locateNearestKeyInRow(r, key);
    }

    private Key locateNearestKeyInRow(Row row, Key key) {
        int size = row.getKeys().size();
        assert (size > 0);
        if (size <= 1) {
            return row.getKeys().get(0);
        }
        Key first = row.getKeys().get(0);
        if (key.x <= first.x) {
            return row.getKeys().get(0);
        }
        Key last = row.getKeys().get(size - 1);
        if (key.x > last.x) {
            return row.getKeys().get(size - 1);
        }
        int key_right = key.x + key.width;
        for (int i = 0; i < size - 1; i++) {
            Key left = row.getKeys().get(i);
            Key right = row.getKeys().get(i + 1);
            int x = left.x + left.width;
            if (x < key.x) {
                continue;
            }
            if (key_right < right.x) {
                return left;
            } else if (x - key.x >= key_right - right.x) {
                return left;
            } else {
                return right;
            }
        }
        return row.getKeys().get(size - 1);
    }
}
