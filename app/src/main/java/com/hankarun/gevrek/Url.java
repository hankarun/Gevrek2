package com.hankarun.gevrek;

import android.content.ContentValues;
import android.database.Cursor;

import com.hankarun.gevrek.database.NewsGroupTable;

public class Url {
    public final String name;
    public final String url;
    public final String count;
    public final String color;
    public final String group;

    @Override
    public String toString(){return name + " <font color=\""+ color +"\">" +count + "</font>";}

    Url(String _name, String _url, String _count, String _color, String _group){
        name = _name;
        count = _count;
        url = _url;
        color = _color;
        group = _group;
    }

    public static Url fromCursor(Cursor cursor){
        return new Url(cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_NAME)),
                cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_URL)),
                cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_COUNT)),
                cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_COLOR)),
                cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_GROUP)));
    }

    public ContentValues toContentValues(){
        ContentValues tmp = new ContentValues();
        tmp.put(NewsGroupTable.NEWSGROUP_COLOR,color);
        tmp.put(NewsGroupTable.NEWSGROUP_COUNT,count);
        tmp.put(NewsGroupTable.NEWSGROUP_GROUP,group);
        tmp.put(NewsGroupTable.NEWSGROUP_NAME,name);
        tmp.put(NewsGroupTable.NEWSGROUP_URL,url);
        return tmp;
    }
}
