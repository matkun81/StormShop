package sample;

public class Good {
    public String name;
    public int price;
    public int count;

    public Good(String name, int price, int count){
        this.name = name;
        this.price = price;
        this.count = count;
    }

    public Good(){

    }
    @Override
    public String toString(){
        return name + " " + price + " " + count;
    }

}
