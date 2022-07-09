package me.danielml.schooltests.objects;

public enum ChangeType {

    ADD("ADD"),REMOVE("REMOVE");

    private final String name;

     ChangeType(String name) {
        this.name = name;
    }

    public static ChangeType from(String s) {
        for(ChangeType type : values()) {
            if(type.name.contains(s.toUpperCase()))
                return type;
        }
        return null;
    }
}
