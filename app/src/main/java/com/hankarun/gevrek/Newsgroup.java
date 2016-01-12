package com.hankarun.gevrek;

import java.util.ArrayList;
import java.util.List;


public class Newsgroup {
    public String name;
    public final List<Url> groups = new ArrayList<Url>();

    @Override
    public String toString(){
        return name;
    }

    public int getSize() { return groups.size();}

    public void addUrl(String _name, String _url, String _count, String _color){
        groups.add(new Url(_name,_url, _count, _color,name));
    }

    public Url getUrl(int i) { return groups.get(i);}

    public void addUrl(Url url){
        groups.add(url);
    }
}