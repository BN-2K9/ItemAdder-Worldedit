package dev.lone.iaedit.hook.util;

import dev.lone.itemsadder.api.CustomFurniture;

public class Data {

    CustomFurniture furniture;
    Boolean Done = false;
    public Boolean getDone() {
        return Done;
    }

    public void setDone(Boolean done) {
        Done = done;
    }



    public void setFurniture(CustomFurniture f) {
        furniture = f;
    }

    public CustomFurniture getFurniture() {
        return furniture;
    }
}
