package org.example.Models;

import lombok.Data;

import java.util.List;

@Data
public class Tree {
    int id;
    String name;
    List<Device> devices;
    List<Tree> tree;

    public Tree(int id, String name, List<Device> devices, List<Tree> tree) {
        this.id = id;
        this.name = name;
        this.devices = devices;
        this.tree = tree;
    }

    @Override
    public String toString(){
        return this.name;
    }
}