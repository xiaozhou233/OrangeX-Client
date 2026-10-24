package cn.xiaozhou233.orangex;

import lombok.Getter;

public class OrangeX {
    @Getter
    private static final OrangeX instance = new OrangeX();

    public OrangeX() {
        System.out.println("OrangeX <init> start!");
    }
}