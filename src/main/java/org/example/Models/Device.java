package org.example.Models;
import lombok.Data;
import java.util.List;
@Data
public class Device {
    int id;
    String name;
    String url;
    List<String> slots;

    public Device(int id, String name, String url, List<String> slots) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.slots = slots;
    }
    @Override
    public String toString(){
        return this.name;
    }
}
