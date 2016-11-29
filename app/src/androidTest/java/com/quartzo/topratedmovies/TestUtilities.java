/*
 * MIT License
 *
 * Copyright (c) 2016 Victor Hugo Montes Neves
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.quartzo.topratedmovies;

/**
 * Created by victoraldir on 20/11/2016.
 */


public class TestUtilities {

//    public static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
//        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
//        validateCurrentRecord(error, valueCursor, expectedValues);
//        valueCursor.close();
//    }

//    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
//        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
//        for (Map.Entry<String, Object> entry : valueSet) {
//            String columnName = entry.getKey();
//            int idx = valueCursor.getColumnIndex(columnName);
//            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
//            String expectedValue = entry.getValue().toString();
//            assertEquals("Value '" + entry.getValue().toString() +
//                    "' did not match the expected value '" +
//                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
//        }
//    }

}
