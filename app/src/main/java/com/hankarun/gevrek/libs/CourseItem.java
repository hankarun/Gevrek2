package com.hankarun.gevrek.libs;

import android.content.ContentValues;
import android.database.Cursor;

import com.hankarun.gevrek.database.CourseTable;

public class CourseItem {
    public final String shorName;
    public final String longName;
    public final String hmtl;

    public CourseItem(String sname, String lname, String _html){
        shorName = sname;
        longName = lname;
        hmtl = _html;
    }

    public static CourseItem fromCursor(Cursor cursor){
        return new CourseItem(cursor.getString(cursor.getColumnIndex(CourseTable.COURSE_NAME)),
                cursor.getString(cursor.getColumnIndex(CourseTable.COURSE_LONG_NAME)),
                cursor.getString(cursor.getColumnIndex(CourseTable.COURSE_URL)));
    }

    public ContentValues toContentCalues(){
        ContentValues tmp = new ContentValues();
        tmp.put(CourseTable.COURSE_LONG_NAME,longName);
        tmp.put(CourseTable.COURSE_NAME,shorName);
        tmp.put(CourseTable.COURSE_URL,hmtl);
        return tmp;
    }
}
