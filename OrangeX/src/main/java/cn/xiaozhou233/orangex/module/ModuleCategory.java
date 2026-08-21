package cn.xiaozhou233.orangex.module;

import lombok.Getter;

@Getter
public enum ModuleCategory {
    RENDER("Render"),
    COMBAT("Combat"),
    WORLD("World"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    MISC("Misc"),
    FUN("Fun"),
    MINIGAME("Minigame");


    private final String name;


    ModuleCategory(String name){
        this.name = name;
    }


}