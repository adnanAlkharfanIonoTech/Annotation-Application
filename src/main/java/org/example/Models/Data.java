package org.example.Models;

import java.util.List;
@lombok.Data
public class Data {
    List<Tree> data;

    public Data(List<Tree> data) {
        this.data = data;
    }
}
